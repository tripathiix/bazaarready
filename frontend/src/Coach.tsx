import { FormEvent, useEffect, useRef, useState } from "react";
import { UxStrings } from "./ux";
import { csrfToken, signInIfNeeded } from "./api";

interface Turn {
  role: "user" | "assistant";
  content: string;
}

async function post(path: string, body: unknown): Promise<string> {
  const response = await fetch(path, {
    method: "POST",
    headers: { "Content-Type": "application/json", "X-XSRF-TOKEN": csrfToken() },
    body: JSON.stringify(body),
  });
  signInIfNeeded(response);
  if (!response.ok) {
    let detail = `The server answered with an error (${response.status}).`;
    try {
      const problem = await response.json();
      if (typeof problem.detail === "string") detail = problem.detail;
    } catch {
      // keep the generic message
    }
    throw new Error(detail);
  }
  return ((await response.json()) as { reply: string }).reply;
}

/** Event that opens the coach from anywhere on the page. */
export const OPEN_COACH = "bazaarready:open-coach";

/** A floating chat that helps someone think through their idea, then hands a description to the builder. */
export function Coach({ u, language, onUse }: { u: UxStrings; language: string; onUse: (description: string) => void }) {
  const [open, setOpen] = useState(false);
  const [turns, setTurns] = useState<Turn[]>([]);
  const [draft, setDraft] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const end = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    end.current?.scrollIntoView({ block: "end" });
  }, [turns, busy]);

  // Other parts of the page (like the link under the build box) can open the coach.
  useEffect(() => {
    const show = () => setOpen(true);
    window.addEventListener(OPEN_COACH, show);
    return () => window.removeEventListener(OPEN_COACH, show);
  }, []);

  async function send(event: FormEvent) {
    event.preventDefault();
    const text = draft.trim();
    if (!text || busy) return;
    const next: Turn[] = [...turns, { role: "user" as const, content: text }].slice(-20);
    setTurns(next);
    setDraft("");
    setBusy(true);
    setError(null);
    try {
      const reply = await post("/api/coach", { messages: next, language });
      setTurns([...next, { role: "assistant", content: reply }]);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Something went wrong.");
    } finally {
      setBusy(false);
    }
  }

  async function use() {
    if (!turns.some((t) => t.role === "user") || busy) return;
    setBusy(true);
    setError(null);
    try {
      const ask: Turn[] = [...turns, { role: "user", content: "Please write the description now." }];
      onUse(await post("/api/coach/brief", { messages: ask, language }));
      setOpen(false);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Something went wrong.");
    } finally {
      setBusy(false);
    }
  }

  if (!open) {
    return (
      <button type="button" className="coach-fab" onClick={() => setOpen(true)}>
        <span aria-hidden="true">✦</span> {u.coachOpen}
      </button>
    );
  }

  return (
    <aside className="coach" aria-labelledby="coach-title">
      <header className="coach-head">
        <h2 id="coach-title">{u.coachTitle}</h2>
        <button type="button" className="button" onClick={() => setOpen(false)}>
          {u.coachClose}
        </button>
      </header>
      <div className="coach-log" aria-live="polite">
        <p className="coach-msg from-coach">{u.coachIntro}</p>
        {turns.map((turn, i) => (
          <p key={i} className={`coach-msg ${turn.role === "user" ? "from-you" : "from-coach"}`}>
            {turn.content}
          </p>
        ))}
        {busy && <p className="coach-msg from-coach thinking">{u.coachThinking}</p>}
        {error && (
          <p className="error" role="alert">
            {error}
          </p>
        )}
        <div ref={end} />
      </div>
      <form className="coach-form" onSubmit={send}>
        <label htmlFor="coach-input" className="visually-hidden">
          {u.coachPlaceholder}
        </label>
        <textarea
          id="coach-input"
          rows={2}
          value={draft}
          onChange={(e) => setDraft(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter" && !e.shiftKey) {
              e.preventDefault();
              void send(e as unknown as FormEvent);
            }
          }}
          placeholder={u.coachPlaceholder}
          maxLength={2000}
        />
        <div className="row">
          {turns.length >= 2 && (
            <button type="button" className="button" onClick={() => void use()} disabled={busy}>
              {u.coachUse}
            </button>
          )}
          <button type="submit" className="button primary" disabled={busy || !draft.trim()}>
            {u.coachSend}
          </button>
        </div>
      </form>
    </aside>
  );
}
