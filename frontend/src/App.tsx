import { FormEvent, useEffect, useMemo, useState } from "react";
import { ActionView, SessionView, sendApproval, sendReply, startSession } from "./api";
import { Bunting, OpenSign, Petals, Shutters } from "./Decor";
import { countries, defaultCountry } from "./countries";
import { applyFonts } from "./fonts";
import { LanguageCode, STRINGS, Strings, initialLanguage, rememberLanguage } from "./i18n";
import { EXTRA, HEAR_QUESTION, ORDERS_LINK } from "./i18nExtra";
import { canSpeakAloud, speakAloud, useSpeech } from "./useSpeech";

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
  const extra = EXTRA[language];

  const [description, setDescription] = useState("");
  const [session, setSession] = useState<SessionView | null>(null);
  const [answer, setAnswer] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [celebrations, setCelebrations] = useState(0);
  const [country, setCountry] = useState(defaultCountry);
  const countryList = useMemo(() => countries(language), [language]);

  const speech = useSpeech((text) => setDescription((current) => (current ? current + " " + text : text)));
  const answerSpeech = useSpeech((text) => setAnswer((current) => (current ? current + " " + text : text)));
  const live = session?.status === "DONE" && session.published;

  useEffect(() => {
    document.documentElement.lang = language;
    applyFonts(language);
  }, [language]);

  function chooseLanguage(code: LanguageCode) {
    if (speech.listening) speech.stop();
    if (answerSpeech.listening) answerSpeech.stop();
    setLanguage(code);
    rememberLanguage(code);
  }

  async function run(work: () => Promise<SessionView>) {
    setBusy(true);
    setError(null);
    try {
      const next = await work();
      if (next.status === "DONE" && next.published && !(session?.status === "DONE")) {
        setCelebrations((n) => n + 1);
      }
      setSession(next);
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
    if (answerSpeech.listening) answerSpeech.stop();
    const dial = countryList.find((c) => c.iso === country)?.dial ?? "";
    const text = session.questionType === "phone" ? `+${dial} ${answer.replace(/[^0-9]/g, "")}` : answer;
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
    <>
      <Petals trigger={celebrations} />

      <header className="signboard">
        <Bunting />
        <div className="wrap hero">
          <div className="hero-text">
            <p className="kicker">{t.eyebrow}</p>
            <h1>
              Bazaar<span>Ready</span>
            </h1>
            <p className="headline">{extra.headline}</p>
          </div>
          <OpenSign />
        </div>

        <div className="wrap">
          <div className="chips" role="group" aria-label={t.language}>
            {(Object.keys(STRINGS) as LanguageCode[]).map((code) => (
              <button
                key={code}
                type="button"
                lang={code}
                className={`chip ${code === language ? "is-on" : ""}`}
                aria-pressed={code === language}
                onClick={() => chooseLanguage(code)}
              >
                {STRINGS[code].name}
              </button>
            ))}
          </div>
        </div>

        <div className="wrap">
          <Shutters labels={extra.shutters} hint={extra.shutterHint} allOpen={!!live} />
        </div>
      </header>

      <main className="wrap workspace">
        {!session && (
          <form className="ledger" onSubmit={onStart}>
            <label htmlFor="description" className="label">
              {t.tellUs}
            </label>
            <p className="hint">{t.hint}</p>
            <textarea
              id="description"
              rows={6}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder={t.example}
              maxLength={4000}
              required
            />
            <div className="row">
              {speech.supported ? (
                <button
                  type="button"
                  className={`button mic ${speech.listening ? "is-listening" : ""}`}
                  onClick={() => (speech.listening ? speech.stop() : speech.start(t.speech))}
                >
                  <span className="bars" aria-hidden="true">
                    <i />
                    <i />
                    <i />
                    <i />
                  </span>
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
            <section className="panel" aria-labelledby="activity">
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
              <form className="panel ask" onSubmit={onReply}>
                <label htmlFor="answer" className="label">
                  {session.question}
                </label>
                {session.questionType === "phone" ? (
                  <div className="phone">
                    <label htmlFor="country" className="visually-hidden">
                      Country code
                    </label>
                    <select id="country" value={country} onChange={(e) => setCountry(e.target.value)}>
                      {countryList.map((c) => (
                        <option key={c.iso} value={c.iso}>
                          {c.flag} {c.name} (+{c.dial})
                        </option>
                      ))}
                    </select>
                    <input
                      id="answer"
                      type="tel"
                      inputMode="numeric"
                      autoComplete="tel-national"
                      pattern="[0-9 ]{4,17}"
                      placeholder="98765 43210"
                      value={answer}
                      onChange={(e) => setAnswer(e.target.value.replace(/[^0-9 ]/g, ""))}
                      required
                      autoFocus
                    />
                  </div>
                ) : (
                  <input id="answer" value={answer} onChange={(e) => setAnswer(e.target.value)} maxLength={2000} required autoFocus />
                )}
                <div className="row">
                  {answerSpeech.supported && session.questionType !== "phone" && (
                    <button
                      type="button"
                      className={`button mic ${answerSpeech.listening ? "is-listening" : ""}`}
                      onClick={() => (answerSpeech.listening ? answerSpeech.stop() : answerSpeech.start(t.speech))}
                    >
                      <span className="bars" aria-hidden="true">
                        <i />
                        <i />
                        <i />
                        <i />
                      </span>
                      {answerSpeech.listening ? t.stopListening : t.speak}
                    </button>
                  )}
                  {canSpeakAloud() && session.question && (
                    <button type="button" className="button" onClick={() => speakAloud(session.question ?? "", t.speech)}>
                      {HEAR_QUESTION[language]}
                    </button>
                  )}
                  <button type="submit" className="button primary">
                    {t.sendAnswer}
                  </button>
                </div>
                {answerSpeech.listening && (
                  <p className="hint" role="status">
                    {t.listening}
                  </p>
                )}
              </form>
            )}

            {session.status === "WAITING_APPROVAL" && !busy && (
              <section className="panel ask" aria-labelledby="approve">
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
              <section className={`panel result ${session.status === "FAILED" ? "failed" : "done"}`}>
                {live && (
                  <span className="stamp" aria-hidden="true">
                    Ready
                  </span>
                )}
                {session.finalMessage && <p className="final">{session.finalMessage}</p>}
                <div className="row">
                  {session.shopPath && (
                    <a className="button primary" href={session.shopPath} target="_blank" rel="noreferrer">
                      {session.published ? t.openSite : t.openPreview}
                    </a>
                  )}
                  {session.ownerPath && session.published && (
                    <a className="button" href={session.ownerPath} target="_blank" rel="noreferrer">
                      {ORDERS_LINK[language]}
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

      <footer className="foot">BazaarReady</footer>
    </>
  );
}
