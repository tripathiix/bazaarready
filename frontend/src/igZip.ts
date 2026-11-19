// Opens Instagram's "Download your information" zip in the browser and keeps only the message files.
// A full download with photos and videos can be hundreds of MB; the messages are usually a few hundred KB.
// Only those are sent to the server, one merged JSON file per conversation, so big downloads work and
// photos never leave the device.

const EOCD = 0x06054b50;
const CENTRAL = 0x02014b50;
const LOCAL = 0x04034b50;
const MESSAGE_JSON = /(^|\/)message_\d+\.json$/i;
const MESSAGE_HTML = /(^|\/)message_\d+\.html$/i;

export class InstagramZipError extends Error {
  constructor(public code: "none" | "html") {
    super(code);
  }
}

type Entry = { name: string; method: number; compressed: number; offset: number };

async function bytes(file: Blob, start: number, end: number): Promise<DataView> {
  return new DataView(await file.slice(start, end).arrayBuffer());
}

async function entries(zip: File): Promise<Entry[]> {
  // The end-of-central-directory record sits in the last 64 KB (plus its own 22 bytes).
  const tailStart = Math.max(0, zip.size - 65_557);
  const tail = await bytes(zip, tailStart, zip.size);
  let at = -1;
  for (let i = tail.byteLength - 22; i >= 0; i--) {
    if (tail.getUint32(i, true) === EOCD) {
      at = i;
      break;
    }
  }
  if (at < 0) throw new Error("not a zip");
  const count = tail.getUint16(at + 10, true);
  const size = tail.getUint32(at + 12, true);
  const offset = tail.getUint32(at + 16, true);
  const dir = await bytes(zip, offset, offset + size);
  const decoder = new TextDecoder();
  const out: Entry[] = [];
  let p = 0;
  for (let n = 0; n < count && p + 46 <= dir.byteLength; n++) {
    if (dir.getUint32(p, true) !== CENTRAL) break;
    const method = dir.getUint16(p + 10, true);
    const compressed = dir.getUint32(p + 20, true);
    const nameLength = dir.getUint16(p + 28, true);
    const extraLength = dir.getUint16(p + 30, true);
    const commentLength = dir.getUint16(p + 32, true);
    const local = dir.getUint32(p + 42, true);
    const name = decoder.decode(new Uint8Array(dir.buffer, dir.byteOffset + p + 46, nameLength));
    out.push({ name, method, compressed, offset: local });
    p += 46 + nameLength + extraLength + commentLength;
  }
  return out;
}

async function read(zip: File, e: Entry): Promise<string> {
  const header = await bytes(zip, e.offset, e.offset + 30);
  if (header.getUint32(0, true) !== LOCAL) throw new Error("bad entry");
  const start = e.offset + 30 + header.getUint16(26, true) + header.getUint16(28, true);
  const data = zip.slice(start, start + e.compressed);
  if (e.method === 0) return data.text();
  if (e.method !== 8) throw new Error("unsupported compression");
  const stream = data.stream().pipeThrough(new DecompressionStream("deflate-raw"));
  return new Response(stream).text();
}

/** Whether this browser can open zips itself; if not, the zip is uploaded whole as before. */
export const canOpenZips = () => typeof DecompressionStream !== "undefined";

/**
 * Turns the chosen files into small JSON files, one per conversation. Loose message_N.json files are
 * passed through. Throws InstagramZipError when a zip holds no messages, or only the HTML format.
 */
export async function instagramFiles(chosen: File[]): Promise<{ files: File[]; conversations: number }> {
  const files: File[] = [];
  let conversations = 0;
  for (const file of chosen) {
    if (/\.html?$/i.test(file.name)) {
      // A loose message_1.html taken out of the zip
      const list = messagesFromHtml(await file.text());
      if (!list.length) throw new InstagramZipError("html");
      files.push(new File([JSON.stringify({ messages: list })], `conversation_h${conversations + 1}.json`, { type: "application/json" }));
      conversations++;
      continue;
    }
    if (!file.name.toLowerCase().endsWith(".zip")) {
      files.push(file);
      conversations++;
      continue;
    }
    const all = await entries(file);
    // JSON is preferred; the HTML version of the download is read too.
    let messages = all.filter((e) => MESSAGE_JSON.test(e.name));
    const html = !messages.length;
    if (html) messages = all.filter((e) => MESSAGE_HTML.test(e.name));
    if (!messages.length) throw new InstagramZipError("none");
    const byConversation = new Map<string, unknown[]>();
    for (const e of messages) {
      const folder = e.name.slice(0, e.name.lastIndexOf("/") + 1) || e.name;
      try {
        const text = await read(file, e);
        const list = html ? messagesFromHtml(text) : (JSON.parse(text) as { messages?: unknown[] }).messages;
        if (!Array.isArray(list) || !list.length) continue;
        byConversation.set(folder, [...(byConversation.get(folder) ?? []), ...list]);
      } catch {
        // skip a file that can't be read; the rest still count
      }
    }
    if (!byConversation.size) throw new InstagramZipError(html ? "html" : "none");
    let n = 0;
    for (const list of byConversation.values()) {
      n++;
      files.push(new File([JSON.stringify({ messages: list })], `conversation_${n}.json`, { type: "application/json" }));
    }
    conversations += byConversation.size;
  }
  if (!files.length) throw new InstagramZipError("none");
  return { files, conversations };
}

/**
 * Reads one message_N.html page from the HTML version of the download. Each message is a white box
 * holding the sender's name first, the text in the middle and the date last; like the JSON, the
 * page lists the newest message first. Returns messages in the same shape as the JSON export.
 */
export function messagesFromHtml(html: string): { sender_name: string; timestamp_ms: number; content: string }[] {
  const doc = new DOMParser().parseFromString(html, "text/html");
  const boxes = Array.from(doc.querySelectorAll("div.uiBoxWhite, div.pam")).filter(
    (box) => !box.parentElement?.closest("div.uiBoxWhite, div.pam"),
  );
  const out: { sender_name: string; timestamp_ms: number; content: string }[] = [];
  boxes.forEach((box, i) => {
    const parts = Array.from(box.children);
    if (parts.length < 3) return;
    const sender = (parts[0].textContent ?? "").trim();
    const when = (parts[parts.length - 1].textContent ?? "").trim();
    const content = parts
      .slice(1, -1)
      .map((p) => (p.textContent ?? "").trim())
      .filter(Boolean)
      .join(" ")
      .trim();
    if (!sender || !content || sender.length > 80) return;
    const parsed = Date.parse(when.replace(/(\d)(am|pm)/i, "$1 $2"));
    // Unknown dates keep page order (newest first), which the server turns around like the JSON.
    out.push({ sender_name: sender, timestamp_ms: Number.isNaN(parsed) ? boxes.length - i : parsed, content });
  });
  return out;
}
