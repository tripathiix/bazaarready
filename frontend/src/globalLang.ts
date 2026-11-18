import { STRINGS } from "./i18n";
import { CHATS, EXTRA, HEAR_QUESTION, ORDERS_LINK } from "./i18nExtra";
import { HOME } from "./home";
import { UX } from "./ux";
import { NOTES } from "./showcase";
import { csrfToken } from "./api";

// Any language, not just the eight hand-checked ones. Every table of words keyed by language is listed
// here; for a new language the English of all of them is sent to the server once, translated by Claude,
// and added to each table, so the rest of the page works unchanged.

type Table = Record<string, unknown>;

const TABLES: Record<string, Table> = {
  STRINGS: STRINGS as Table,
  EXTRA: EXTRA as Table,
  HEAR_QUESTION: HEAR_QUESTION as Table,
  ORDERS_LINK: ORDERS_LINK as Table,
  CHATS: CHATS as Table,
  HOME: HOME as Table,
  UX: UX as Table,
  NOTES: NOTES as Table,
};

/** Other modules add their own tables of words (for example the side decorations). */
export function registerTable(name: string, table: Table) {
  TABLES[name] = table;
}

/** The hand-checked languages, which ship with the page. */
export const BUILT_IN: string[] = Object.keys(STRINGS);

/** The world's main languages, shown as chips. Any not built in are translated the first time. */
export const CHIPS: string[] = ["en", "es", "fr", "ar", "zh", "pt", "hi", "id"];

/** Every other language, under "More languages", roughly by how many people use them online. */
export const MORE: string[] = [
  "ja", "de", "ru", "bn", "ur", "sw", "ko", "it", "tr", "vi", "fa", "th", "pl", "nl", "uk", "ms", "fil",
  "ta", "te", "mr", "gu", "kn", "ml", "pa", "ro", "el", "he", "cs", "hu", "sv", "ha", "yo", "am", "zu",
  "ig", "so", "ne", "si", "my", "km", "az", "kk", "uz",
];

/** Languages written right to left. */
export const RTL = ["ar", "ur", "fa", "he", "ps", "sd", "yi"];

/** The language's name in itself: "Español", "العربية", "日本語". */
export function nativeName(code: string): string {
  try {
    const name = new Intl.DisplayNames([code], { type: "language" }).of(code);
    if (name && name !== code) return name.charAt(0).toLocaleUpperCase(code) + name.slice(1);
  } catch {
    // fall through
  }
  return code;
}

/** Its name in English as well, so people can find it in the list: "Kiswahili (Swahili)". */
export function pickerName(code: string): string {
  const own = nativeName(code);
  let english = code;
  try {
    english = new Intl.DisplayNames(["en"], { type: "language" }).of(code) ?? code;
  } catch {
    // keep the code
  }
  return own.toLowerCase() === english.toLowerCase() ? own : `${own} (${english})`;
}

const STORAGE_KEY = "bazaarready.language";

/** A language chosen on an earlier visit that isn't built in, to load again. */
export function savedExtraLanguage(): string | null {
  // A shared link can pick the language: bazaarready.com/?lang=es
  const fromLink = new URLSearchParams(window.location.search).get("lang");
  if (fromLink && /^[a-z]{2,3}$/.test(fromLink) && !BUILT_IN.includes(fromLink)) return fromLink;
  let saved: string | null = null;
  try {
    saved = window.localStorage.getItem(STORAGE_KEY);
  } catch {
    // storage blocked
  }
  if (saved) return !BUILT_IN.includes(saved) && /^[a-z]{2,3}$/.test(saved) ? saved : null;
  // First visit: open in the browser's language when we offer it (a visitor in Madrid gets Spanish).
  for (const preferred of navigator.languages ?? [navigator.language]) {
    const code = preferred.split("-")[0].toLowerCase();
    if (BUILT_IN.includes(code)) return null;
    if (CHIPS.includes(code) || MORE.includes(code)) return code;
  }
  return null;
}

function englishBundle(): Record<string, unknown> {
  const bundle: Record<string, unknown> = {};
  for (const [name, table] of Object.entries(TABLES)) {
    if (name === "STRINGS") {
      // The language's own name and speech code are set below, not translated.
      const { name: _n, speech: _s, ...rest } = STRINGS.en;
      bundle[name] = rest;
    } else if (name === "SIDE") {
      const { locale: _l, ...rest } = table.en as Record<string, unknown>;
      bundle[name] = rest;
    } else {
      bundle[name] = table.en;
    }
  }
  return bundle;
}

function install(code: string, data: Record<string, unknown>) {
  for (const [name, table] of Object.entries(TABLES)) {
    const english = table.en;
    const translated = data[name] ?? english;
    if (name === "STRINGS") {
      table[code] = { ...(english as object), ...(translated as object), name: nativeName(code), speech: code };
    } else if (name === "SIDE") {
      table[code] = { ...(english as object), ...(translated as object), locale: code };
    } else {
      table[code] = translated;
    }
  }
}

function hash(text: string): string {
  let h = 5381;
  for (let i = 0; i < text.length; i++) h = ((h << 5) + h + text.charCodeAt(i)) | 0;
  return (h >>> 0).toString(36);
}

/** Makes a language ready to show. Built-in languages are ready at once; others are fetched and kept. */
export async function loadLanguage(code: string): Promise<void> {
  if (code in STRINGS) return;
  const body = JSON.stringify(englishBundle());
  const key = `bazaarready.ui.${code}.${hash(body)}`;
  try {
    const kept = window.localStorage.getItem(key);
    if (kept) {
      install(code, JSON.parse(kept));
      return;
    }
  } catch {
    // no storage; fetch it
  }
  const response = await fetch(`/api/ui-strings/${code}`, {
    method: "POST",
    headers: { "Content-Type": "application/json", "X-XSRF-TOKEN": csrfToken() },
    body,
  });
  if (!response.ok) throw new Error("This language isn't available right now. Please try again in a minute.");
  const data = (await response.json()) as Record<string, unknown>;
  install(code, data);
  try {
    window.localStorage.setItem(key, JSON.stringify(data));
  } catch {
    // fine; it is stored on the server too
  }
}
