import { FormEvent, useState } from "react";
import { SessionView, sendApproval, sendReply, startSession } from "./api";
import { useSpeech } from "./useSpeech";

const EXAMPLE =
  "Main ghar se cake banati hoon, Bengaluru mein. Chocolate cake 850 ka 1 kg, eggless 900. " +
  "Cupcakes 40 rupaye each, minimum 12. Indiranagar aur Koramangala free delivery, Whitefield 60 extra. " +
  "2 din pehle order chahiye. WhatsApp 9876543210.";

export default function App() {
  const [description, setDescription] = useState("");
  const [speechLang, setSpeechLang] = useState("hi-IN");
  const [session, setSession] = useState<SessionView | null>(null);
  const [answer, setAnswer] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const speech = useSpeech((text) => setDescription((current) => (current ? current + " " + text : text)));

  async function run(work: () => Promise<SessionView>) {
    setBusy(true);
    setError(null);
    try {
      setSession(await work());
    } catch (e) {
      setError(e instanceof Error ? e.message : "Something went wrong. Please try again.");
    } finally {
      setBusy(false);
    }
  }

  function onStart(event: FormEvent) {
    event.preventDefault();
    if (speech.listening) speech.stop();
    void run(() => startSession(description));
  }

  function onReply(event: FormEvent) {
    event.preventDefault();
    if (!session) return;
    const text = answer;
    setAnswer("");
    void run(() => sendReply(session.id, text));
  }

  function startOver() {
    setSession(null);
    setDescription("");
    setAnswer("");
    setError(null);
  }

  return (
    <main className="page">
      <header>
        <p className="eyebrow">Launch agent for small businesses</p>
        <h1>BazaarReady</h1>
      </header>

      {!session && (
        <form className="card" onSubmit={onStart}>
          <label htmlFor="description" className="label">
            Tell us about your business
          </label>
          <p className="hint">
            Type or speak, in Hindi, Hinglish or English. Say what you sell, your prices, where you deliver and your
            WhatsApp number.
          </p>
          <textarea
            id="description"
            rows={7}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder={EXAMPLE}
            maxLength={4000}
            required
          />
          <div className="row">
            {speech.supported ? (
              <>
                <label htmlFor="speechLang" className="visually-hidden">
                  Language for speaking
                </label>
                <select id="speechLang" value={speechLang} onChange={(e) => setSpeechLang(e.target.value)} disabled={speech.listening}>
                  <option value="hi-IN">Hindi</option>
                  <option value="en-IN">English</option>
                </select>
                <button type="button" className="button" onClick={() => (speech.listening ? speech.stop() : speech.start(speechLang))}>
                  {speech.listening ? "Stop listening" : "Speak instead"}
                </button>
              </>
            ) : (
              <span className="hint">Speaking works in Chrome and Edge.</span>
            )}
            <button type="button" className="button" onClick={() => setDescription(EXAMPLE)}>
              Use the example
            </button>
            <button type="submit" className="button primary" disabled={busy || description.trim().length === 0}>
              {busy ? "Working…" : "Build my shop"}
            </button>
          </div>
          {speech.listening && (
            <p className="hint" role="status">
              Listening. Your words will appear in the box above.
            </p>
          )}
        </form>
      )}

      {error && (
        <p className="error" role="alert">
          {error}
        </p>
      )}

      {session && (
        <>
          <section className="card" aria-labelledby="activity">
            <h2 id="activity">What the agent has done</h2>
            <ol className="log">
              {session.actions.map((action) => (
                <li key={action.stepNo} className={`log-${action.status.toLowerCase()}`}>
                  <span className="mark" aria-hidden="true" />
                  <span>{action.text}</span>
                </li>
              ))}
              {busy && (
                <li className="log-working" role="status">
                  <span className="mark" aria-hidden="true" />
                  <span>Working…</span>
                </li>
              )}
            </ol>
          </section>

          {session.status === "WAITING_OWNER" && !busy && (
            <form className="card ask" onSubmit={onReply}>
              <label htmlFor="answer" className="label">
                {session.question}
              </label>
              <input id="answer" value={answer} onChange={(e) => setAnswer(e.target.value)} maxLength={2000} required autoFocus />
              <div className="row">
                <button type="submit" className="button primary">
                  Send answer
                </button>
              </div>
            </form>
          )}

          {session.status === "WAITING_APPROVAL" && !busy && (
            <section className="card ask" aria-labelledby="approve">
              <h2 id="approve">{session.approvalRequest}</h2>
              {session.shopPath && (
                <p>
                  <a href={session.shopPath} target="_blank" rel="noreferrer">
                    Look at the preview first
                  </a>
                </p>
              )}
              <div className="row">
                <button type="button" className="button primary" onClick={() => void run(() => sendApproval(session.id, true))}>
                  Publish
                </button>
                <button type="button" className="button" onClick={() => void run(() => sendApproval(session.id, false))}>
                  Not yet
                </button>
              </div>
            </section>
          )}

          {(session.status === "DONE" || session.status === "FAILED") && (
            <section className={`card ${session.status === "FAILED" ? "failed" : "done"}`}>
              {session.finalMessage && <p>{session.finalMessage}</p>}
              {session.shopPath && (
                <p>
                  <a className="button primary" href={session.shopPath} target="_blank" rel="noreferrer">
                    {session.published ? "Open your shop" : "Open the preview"}
                  </a>
                </p>
              )}
              <button type="button" className="button" onClick={startOver}>
                Start another business
              </button>
            </section>
          )}
        </>
      )}
    </main>
  );
}
