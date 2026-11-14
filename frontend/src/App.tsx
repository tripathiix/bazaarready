import { FormEvent, useEffect, useState } from "react";
import { ActionView, SessionView, sendApproval, sendReply, startSession } from "./api";
import { LanguageCode, STRINGS, Strings, initialLanguage, rememberLanguage } from "./i18n";
import { useSpeech } from "./useSpeech";

/** One activity-log line in the owner's language, built from what the agent did. */
function actionText(action: ActionView, t: Strings): string {
  switch (action.tool) {
    case "start":
      return t.logStart;
    case "ask_owner": {
      const question = action.data?.question ?? "";
      const answer = action.data?.answer;
      return answer ? `${t.logAsked}: ${question} ${t.logAnswered}: ${answer}` : `${t.logAsked}: ${question}`;
    }
    case "save_profile":
      if (action.status === "FAILED") return t.logRetry;
      return t.logSaved
        .replace("{items}", String(action.data?.items ?? 0))
        .replace("{faqs}", String(action.data?.faqs ?? 0));
    case "publish_shop":
      if (action.status === "APPROVED") return t.logPublished;
      if (action.status === "DECLINED") return t.logDeclined;
      if (action.status === "FAILED") return t.logRetry;
      return t.logReady;
    case "error":
      return t.logStopped;
    default:
      return action.text;
  }
}

export default function App() {
  const [language, setLanguage] = useState<LanguageCode>(initialLanguage);
  const t = STRINGS[language];

  const [description, setDescription] = useState("");
  const [session, setSession] = useState<SessionView | null>(null);
  const [answer, setAnswer] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const speech = useSpeech((text) => setDescription((current) => (current ? current + " " + text : text)));

  useEffect(() => {
    document.documentElement.lang = language;
  }, [language]);

  function chooseLanguage(code: LanguageCode) {
    if (speech.listening) speech.stop();
    setLanguage(code);
    rememberLanguage(code);
  }

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
    void run(() => startSession(description, language));
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
      <header className="top">
        <div>
          <p className="eyebrow">{t.eyebrow}</p>
          <h1>BazaarReady</h1>
        </div>
        <div className="picker">
          <label htmlFor="language">{t.language}</label>
          <select id="language" value={language} onChange={(e) => chooseLanguage(e.target.value as LanguageCode)}>
            {(Object.keys(STRINGS) as LanguageCode[]).map((code) => (
              <option key={code} value={code} lang={code}>
                {STRINGS[code].name}
              </option>
            ))}
          </select>
        </div>
      </header>

      {!session && (
        <form className="card" onSubmit={onStart}>
          <label htmlFor="description" className="label">
            {t.tellUs}
          </label>
          <p className="hint">{t.hint}</p>
          <textarea
            id="description"
            rows={7}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder={t.example}
            maxLength={4000}
            required
          />
          <div className="row">
            {speech.supported ? (
              <button type="button" className="button" onClick={() => (speech.listening ? speech.stop() : speech.start(t.speech))}>
                {speech.listening ? t.stopListening : t.speak}
              </button>
            ) : (
              <span className="hint">{t.speechUnsupported}</span>
            )}
            <button type="button" className="button" onClick={() => setDescription(t.example)}>
              {t.useExample}
            </button>
            <button type="submit" className="button primary" disabled={busy || description.trim().length === 0}>
              {busy ? t.working : t.build}
            </button>
          </div>
          {speech.listening && (
            <p className="hint" role="status">
              {t.listening}
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
            <h2 id="activity">{t.activity}</h2>
            <ol className="log">
              {session.actions.map((action) => (
                <li key={action.stepNo} className={`log-${action.status.toLowerCase()}`}>
                  <span className="mark" aria-hidden="true" />
                  <span>{actionText(action, t)}</span>
                </li>
              ))}
              {busy && (
                <li className="log-working" role="status">
                  <span className="mark" aria-hidden="true" />
                  <span>{t.working}</span>
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
                  {t.sendAnswer}
                </button>
              </div>
            </form>
          )}

          {session.status === "WAITING_APPROVAL" && !busy && (
            <section className="card ask" aria-labelledby="approve">
              <h2 id="approve">{t.approveQuestion}</h2>
              {session.shopPath && (
                <p>
                  <a href={session.shopPath} target="_blank" rel="noreferrer">
                    {t.lookPreview}
                  </a>
                </p>
              )}
              <div className="row">
                <button type="button" className="button primary" onClick={() => void run(() => sendApproval(session.id, true))}>
                  {t.publish}
                </button>
                <button type="button" className="button" onClick={() => void run(() => sendApproval(session.id, false))}>
                  {t.notYet}
                </button>
              </div>
            </section>
          )}

          {(session.status === "DONE" || session.status === "FAILED") && (
            <section className={`card ${session.status === "FAILED" ? "failed" : "done"}`}>
              {session.finalMessage && <p>{session.finalMessage}</p>}
              <div className="row">
                {session.shopPath && (
                  <a className="button primary" href={session.shopPath} target="_blank" rel="noreferrer">
                    {session.published ? t.openSite : t.openPreview}
                  </a>
                )}
                <button type="button" className="button" onClick={startOver}>
                  {t.startAnother}
                </button>
              </div>
            </section>
          )}
        </>
      )}
    </main>
  );
}
