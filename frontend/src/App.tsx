import { FormEvent, useEffect, useMemo, useRef, useState } from "react";
import { ActionView, Me, SessionView, csrfToken, getMe, getSession, sendApproval, sendReply, startFromChats, startSession, startUpdate } from "./api";
import { Coach, OPEN_COACH } from "./Coach";
import { DESIGNS, HOME } from "./home";
import { FeaturedPreview, LinksStrip } from "./HomeParts";
import { SideDecor } from "./SideDecor";
import { UX } from "./ux";
import { Gallery, OpenSign, Petals, PhotoCredits, Shutters, StringLights } from "./Decor";
import { countries, defaultCountry } from "./countries";
import { applyFonts } from "./fonts";
import { LanguageCode, STRINGS, Strings, initialLanguage, rememberLanguage } from "./i18n";
import { CHATS, EXTRA, HEAR_QUESTION, ORDERS_LINK } from "./i18nExtra";
import { canSpeakAloud, speakAloud, useSpeech } from "./useSpeech";
import { InstagramZipError, canOpenZips, instagramFiles } from "./igZip";
import { NOTES, useShowcase } from "./showcase";
import { BUILT_IN, CHIPS, MORE, RTL, loadLanguage, nativeName, pickerName, registerTable, savedExtraLanguage } from "./globalLang";

/** One activity-log line in the owner's language, built from what the agent did. */
function actionText(action: ActionView, t: Strings, readChats: string): string {
  switch (action.tool) {
    case "import_chats":
      return readChats.replace("{messages}", String(action.data?.messages ?? 0)).replace("{customers}", String(action.data?.customers ?? 0));
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

const OTHER = "__other__";
const OTHER_LABEL: Record<LanguageCode, string> = {
  en: "Other…",
  hi: "अन्य…",
  kn: "ಇತರೆ…",
  ta: "மற்றவை…",
  te: "ఇతర…",
  mr: "इतर…",
  bn: "অন্য…",
  gu: "અન્ય…",
};

registerTable("OTHER_LABEL", OTHER_LABEL);

export default function App() {
  const [language, setLanguage] = useState<LanguageCode>(initialLanguage);
  const [translating, setTranslating] = useState<string | null>(null);
  const [languageError, setLanguageError] = useState<string | null>(null);
  const t = STRINGS[language];
  const extra = EXTRA[language];
  const notes = NOTES[language];
  const chatWords = CHATS[language];
  const [ownerName, setOwnerName] = useState("");
  const u = UX[language];
  const h = HOME[language];
  const params = new URLSearchParams(window.location.search);
  const [changeKeyFromUrl] = useState(() => {
    const key = params.get("change");
    return key && /^[0-9a-f-]{36}$/.test(key) ? key : null;
  });
  const [changeText, setChangeText] = useState("");
  // A design picked on the Templates page ("Use this"), passed on to the agent as a wish.
  const [design, setDesign] = useState<string | null>(() => {
    const d = params.get("design");
    return d && (DESIGNS as readonly string[]).includes(d) ? d : null;
  });
  const [showChats, setShowChats] = useState(false);
  const [me, setMe] = useState<Me | null>(null);
  useEffect(() => {
    void getMe().then(setMe);
  }, []);
  const [formAnswers, setFormAnswers] = useState<Record<number, string>>({});
  const [formOther, setFormOther] = useState<Record<number, string>>({});

  const [description, setDescription] = useState("");
  const [session, setSession] = useState<SessionView | null>(null);
  const [answer, setAnswer] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [celebrations, setCelebrations] = useState(0);
  const showcase = useShowcase(celebrations);

  // Fade sections and gallery cards in as they scroll into view.
  useEffect(() => {
    const items = Array.from(document.querySelectorAll<HTMLElement>("[data-reveal]:not(.in)"));
    const still = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    if (still || !("IntersectionObserver" in window)) {
      items.forEach((el) => el.classList.add("in"));
      return;
    }
    const io = new IntersectionObserver(
      (entries) =>
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add("in");
            io.unobserve(entry.target);
          }
        }),
      { threshold: 0.12 }
    );
    items.forEach((el, i) => {
      el.style.setProperty("--d", `${Math.min(i, 6) * 80}ms`);
      io.observe(el);
    });
    return () => io.disconnect();
  }, [showcase, session]);
  const [country, setCountry] = useState(defaultCountry);
  const countryList = useMemo(() => countries(language), [language]);

  const speech = useSpeech((text) => setDescription((current) => (current ? current + " " + text : text)));
  const answerSpeech = useSpeech((text) => setAnswer((current) => (current ? current + " " + text : text)));
  const live = session?.status === "DONE" && session.published;
  // Voice for the question form: one microphone, pointed at whichever field's mic was tapped.
  const [micField, setMicField] = useState<number | null>(null);
  const micFieldRef = useRef<number | null>(null);
  const fieldSpeech = useSpeech((text) => {
    const i = micFieldRef.current;
    if (i === null) return;
    const numeric = session?.fields?.[i]?.kind === "number";
    setFormAnswers((current) => {
      if (numeric) {
        const digits = text.replace(/[^0-9.]/g, "");
        return digits ? { ...current, [i]: digits } : current;
      }
      const before = current[i] ?? "";
      return { ...current, [i]: before ? before + " " + text : text };
    });
  });
  function toggleFieldMic(i: number) {
    const same = micFieldRef.current === i && fieldSpeech.listening;
    if (fieldSpeech.listening) fieldSpeech.stop();
    if (same) {
      setMicField(null);
      micFieldRef.current = null;
      return;
    }
    micFieldRef.current = i;
    setMicField(i);
    // Starting straight after stopping can fail in some browsers; give the old session a moment to close.
    window.setTimeout(() => fieldSpeech.start(t.speech), fieldSpeech.listening ? 250 : 0);
  }
  const changeSpeech = useSpeech((text) => setChangeText((current) => (current ? current + " " + text : text)));
  const changeKey = changeKeyFromUrl ?? (session?.ownerPath && session.published ? session.ownerPath.split("/").pop() ?? null : null);
  const TEST_LINE = `${u.testFirst}. ${u.testHint}`;

  useEffect(() => {
    const query = new URLSearchParams(window.location.search);
    if (changeKeyFromUrl && query.get("fix") === "tests") {
      fetch(`/api/owner/${changeKeyFromUrl}/test-fix`)
        .then((r) => (r.ok ? r.json() : null))
        .then((body: { text?: string } | null) => {
          if (body?.text) {
            setChangeText(body.text);
            window.setTimeout(() => document.getElementById("change-text")?.scrollIntoView({ behavior: "smooth", block: "center" }), 300);
          }
        })
        .catch(() => undefined);
    }
  }, [changeKeyFromUrl]);

  useEffect(() => {
    const id = new URLSearchParams(window.location.search).get("session");
    if (id && /^[0-9a-f-]{36}$/.test(id)) {
      getSession(id).then(setSession).catch(() => undefined);
    }
  }, []);

  function onChange(event: FormEvent) {
    event.preventDefault();
    if (!changeKey || !changeText.trim()) return;
    if (changeSpeech.listening) changeSpeech.stop();
    const text = changeText;
    setChangeText("");
    void run(() => startUpdate(changeKey, text, language));
  }

  useEffect(() => {
    document.documentElement.lang = language;
    document.documentElement.dir = RTL.includes(language) ? "rtl" : "ltr";
    applyFonts(language);
  }, [language]);

  // A language beyond the built-in eight, chosen on an earlier visit: load its words again.
  useEffect(() => {
    const saved = savedExtraLanguage();
    if (saved) void pickMore(saved);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function pickMore(code: string) {
    if (!code) return;
    setLanguageError(null);
    setTranslating(code);
    try {
      await loadLanguage(code);
      chooseLanguage(code);
    } catch (e) {
      setLanguageError(e instanceof Error ? e.message : "This language isn't available right now.");
    } finally {
      setTranslating(null);
    }
  }

  function chooseLanguage(code: LanguageCode) {
    if (speech.listening) speech.stop();
    if (answerSpeech.listening) answerSpeech.stop();
    setLanguage(code);
    rememberLanguage(code);
  }

  // What happened to the chats just chosen, shown right under the upload buttons.
  const [importNote, setImportNote] = useState<{ kind: "working" | "error"; text: string } | null>(null);

  async function importChats(chosen: File[], instagram: boolean) {
    if (!chosen.length) return;
    const first = chosen.length === 1 ? chosen[0].name : `${chosen.length} files`;
    let files = chosen;
    if (instagram && canOpenZips() && chosen.some((f) => f.name.toLowerCase().endsWith(".zip"))) {
      setImportNote({ kind: "working", text: chatWords.opening.replace("{name}", first) });
      try {
        const opened = await instagramFiles(chosen);
        files = opened.files;
        setImportNote({ kind: "working", text: chatWords.found.replace("{n}", String(opened.conversations)) });
      } catch (e) {
        if (e instanceof InstagramZipError) {
          setImportNote({ kind: "error", text: e.code === "html" ? chatWords.igHtml : chatWords.igNone });
          return;
        }
        files = chosen; // couldn't open it here; let the server try the whole zip
      }
    }
    setImportNote({ kind: "working", text: chatWords.received.replace("{name}", first) });
    let failed: string | null = null;
    await run(async () => {
      try {
        return await startFromChats(files, language, ownerName);
      } catch (e) {
        failed = e instanceof Error ? e.message : "Something went wrong. Please try again.";
        throw e;
      }
    });
    setImportNote(failed ? { kind: "error", text: failed } : null);
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
    if (me && me.authForBuilding && !me.signedIn) {
      window.location.href = "/login";
      return;
    }
    if (speech.listening) speech.stop();
    const wish = design ? `\n\nPlease use the ${design} design.` : "";
    void run(() => startSession(description + wish, language));
  }

  function onReply(event: FormEvent) {
    event.preventDefault();
    if (!session) return;
    if (session.fields?.length) {
      const lines = session.fields
        .map((f, i) => {
          const raw = formAnswers[i] ?? "";
          const value = raw === OTHER ? (formOther[i] ?? "").trim() : raw.trim();
          return value ? `${f.label}: ${value}` : null;
        })
        .filter((l): l is string => l !== null);
      const text = lines.length ? lines.join("\n") : "(left these empty)";
      if (fieldSpeech.listening) fieldSpeech.stop();
      setMicField(null);
      micFieldRef.current = null;
      setFormAnswers({});
      setFormOther({});
      void run(() => sendReply(session.id, text));
      return;
    }
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
      <SideDecor sites={showcase?.recent ?? []} language={language} />

      <header className="signboard">
        <StringLights />
        <div className="wrap hero">
          <div className="hero-text">
            <p className="kicker">{t.eyebrow}</p>
            <h1>
              Bazaar<span>Ready</span>
            </h1>
            <p className="headline promise" key={language}>
              <span className="promise-a">{h.promise[0]}</span>
              <span className="promise-b">{h.promise[1]}</span>
            </p>
            <svg className="scribble" viewBox="0 0 300 20" preserveAspectRatio="none" aria-hidden="true">
              <defs>
                <linearGradient id="ink-glow" x1="0" x2="1">
                  <stop offset="0" stopColor="#f4a83a" />
                  <stop offset="1" stopColor="#ec7a8c" />
                </linearGradient>
              </defs>
              <path d="M4 14 C 60 4, 120 18, 180 9 S 260 6, 296 12" fill="none" stroke="url(#ink-glow)" strokeWidth="3" strokeLinecap="round" />
            </svg>
          </div>
          <div className="hero-side">
            <nav className="top-nav" aria-label="Main">
              <a href="/templates">{h.templates}</a>
              <a href="/guide">{u.howItWorks}</a>
              <a href="/features">{h.features}</a>
              <a href="/contact">{h.contact}</a>
              <a className="nav-pill" href="/account">
                {h.myDashboard}
              </a>
              {me?.signedIn ? (
                <form method="post" action="/logout">
                  <input type="hidden" name="_csrf" value={csrfToken()} />
                  <button type="submit" className="nav-link-button">
                    {u.signOut}
                  </button>
                </form>
              ) : (
                me?.authForBuilding && <a href="/login">{u.signIn}</a>
              )}
            </nav>
            <OpenSign />
          </div>
        </div>

        <div className="wrap">
          <div className="chips" role="group" aria-label={t.language}>
            {CHIPS.map((code) => (
              <button
                key={code}
                type="button"
                lang={code}
                className={`chip ${code === language ? "is-on" : ""}`}
                aria-pressed={code === language}
                disabled={!!translating}
                onClick={() => void pickMore(code)}
              >
                {STRINGS[code]?.name ?? nativeName(code)}
              </button>
            ))}
            <label className={`chip more-languages ${CHIPS.includes(language) ? "" : "is-on"}`}>
              <span aria-hidden="true">🌐</span>
              <select
                aria-label="More languages"
                value={CHIPS.includes(language) ? "" : language}
                disabled={!!translating}
                onChange={(e) => void pickMore(e.target.value)}
              >
                <option value="">{translating ? `${pickerName(translating)}…` : "More languages"}</option>
                {MORE.map((code) => (
                  <option key={code} value={code} lang={code}>
                    {pickerName(code)}
                  </option>
                ))}
              </select>
            </label>
            {translating && <span className="lang-note" role="status">Translating the page into {pickerName(translating)}… (only the first time)</span>}
            {!translating && !BUILT_IN.includes(language) && <span className="lang-note">Auto-translated by AI</span>}
            {languageError && <span className="lang-note" role="alert">{languageError}</span>}
          </div>
        </div>

        <div className="wrap">
          <Shutters labels={extra.shutters} hint={extra.shutterHint} allOpen={!!live} photos={showcase?.examples ?? [null, null, null]} />
        </div>
      </header>

      <main className="wrap workspace">
        {!session && (
          <>
          <form className="ledger" data-reveal onSubmit={onStart}>
            <label htmlFor="description" className="label box-title">
              {extra.headline}
            </label>
            <details className="guide">
              <summary className="guide-title">{h.include}</summary>
              <ol>
                {u.guide.map((item) => (
                  <li key={item}>{item}</li>
                ))}
              </ol>
            </details>
            {me && me.authForBuilding && !me.signedIn && (
              <p className="signin-note">
                {u.signInToBuild} <a href="/login">{u.signIn} →</a>
              </p>
            )}
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
                  className={`button pill mic ${speech.listening ? "is-listening" : ""}`}
                  onClick={() => (speech.listening ? speech.stop() : speech.start(t.speech))}
                >
                  {speech.listening ? (
                    <span className="bars" aria-hidden="true">
                      <i />
                      <i />
                      <i />
                      <i />
                    </span>
                  ) : (
                    <svg aria-hidden="true" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
                      <rect x="9" y="3" width="6" height="11" rx="3" />
                      <path d="M5 11a7 7 0 0 0 14 0M12 18v3" />
                    </svg>
                  )}
                  {speech.listening ? t.stopListening : h.speak}
                </button>
              ) : (
                <span className="hint">{t.speechUnsupported}</span>
              )}
              <button
                type="button"
                className={`button pill ${showChats ? "is-on" : ""}`}
                aria-expanded={showChats}
                aria-controls="chats"
                onClick={() => setShowChats((v) => !v)}
              >
                <svg aria-hidden="true" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
                  <path d="M12 3v12M7 10l5 5 5-5M5 21h14" />
                </svg>
                {h.importChats}
              </button>
              <button type="submit" className="button primary push-right" disabled={busy || description.trim().length === 0}>
                {busy ? t.working : t.build}
              </button>
            </div>
            {speech.listening && (
              <p className="hint" role="status">
                {t.listening}
              </p>
            )}
            {design && (
              <p className="design-wish">
                {h.design.replace("{name}", design.charAt(0).toUpperCase() + design.slice(1))}
                <button type="button" className="nav-link-button" onClick={() => setDesign(null)}>
                  {h.removeDesign}
                </button>
              </p>
            )}
            <div className="chats" id="chats" hidden={!showChats}>
              <p className="hint">{chatWords.how}</p>
              <div className="row">
                <label htmlFor="chat-owner" className="visually-hidden">
                  {chatWords.owner}
                </label>
                <input
                  id="chat-owner"
                  className="chat-owner"
                  value={ownerName}
                  onChange={(e) => setOwnerName(e.target.value)}
                  placeholder={chatWords.owner}
                  maxLength={60}
                />
                <label className="button" htmlFor="chat-files">
                  {chatWords.button}
                </label>
                <input
                  id="chat-files"
                  className="visually-hidden"
                  type="file"
                  accept=".txt,.zip,text/plain,application/zip"
                  multiple
                  disabled={busy}
                  onChange={(e) => {
                    const files = Array.from(e.target.files ?? []);
                    e.target.value = "";
                    void importChats(files, false);
                  }}
                />
                <label className="button" htmlFor="ig-files">
                  {chatWords.igButton}
                </label>
                <input
                  id="ig-files"
                  className="visually-hidden"
                  type="file"
                  accept=".zip,.json,.html,application/zip,application/json,text/html"
                  multiple
                  disabled={busy}
                  onChange={(e) => {
                    const files = Array.from(e.target.files ?? []);
                    e.target.value = "";
                    void importChats(files, true);
                  }}
                />
              </div>
              {importNote && (
                <p className={`import-note ${importNote.kind}`} role={importNote.kind === "error" ? "alert" : "status"}>
                  {importNote.kind === "working" && <span className="spinner" aria-hidden="true" />}
                  {importNote.text}
                </p>
              )}
              <p className="hint">{chatWords.igHow}</p>
            </div>
          </form>
          <div className="try-row" data-reveal>
            <span className="hint">{h.tryLabel}</span>
            {h.examples.map((ex) => (
              <button
                key={ex.label}
                type="button"
                className="try-chip"
                onClick={() => {
                  setDescription(ex.text);
                  document.getElementById("description")?.focus();
                }}
              >
                {ex.label}
              </button>
            ))}
          </div>
          <p className="hint coach-ask">
            {h.coachAsk}{" "}
            <button type="button" className="link-button" onClick={() => window.dispatchEvent(new Event(OPEN_COACH))}>
              {h.coachLink}
            </button>
          </p>
          <FeaturedPreview site={showcase?.featured ?? null} h={h} />
          <LinksStrip h={h} />
          </>
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
                    <span>{actionText(action, t, chatWords.read)}</span>
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
                {session.fields?.length ? (
                  <h2 className="label">{session.question}</h2>
                ) : (
                  <label htmlFor="answer" className="label">
                    {session.question}
                  </label>
                )}
                {session.fields?.length ? (
                  <div className="qform">
                    {session.fields.map((f, i) => (
                      <div key={`${f.label}-${i}`} className={`qfield ${f.kind === "long" ? "wide" : ""}`}>
                        <label htmlFor={`qf-${i}`}>{f.label}</label>
                        {f.kind === "choice" ? (
                          <>
                            <select id={`qf-${i}`} value={formAnswers[i] ?? ""} onChange={(e) => setFormAnswers({ ...formAnswers, [i]: e.target.value })}>
                              <option value="">—</option>
                              {f.options.map((o) => (
                                <option key={o} value={o}>
                                  {o}
                                </option>
                              ))}
                              <option value={OTHER}>{OTHER_LABEL[language]}</option>
                            </select>
                            {formAnswers[i] === OTHER && (
                              <input
                                aria-label={`${f.label}: ${OTHER_LABEL[language]}`}
                                value={formOther[i] ?? ""}
                                onChange={(e) => setFormOther({ ...formOther, [i]: e.target.value })}
                                maxLength={300}
                              />
                            )}
                          </>
                        ) : f.kind === "long" ? (
                          <textarea
                            id={`qf-${i}`}
                            rows={3}
                            value={formAnswers[i] ?? ""}
                            placeholder={f.placeholder}
                            onChange={(e) => setFormAnswers({ ...formAnswers, [i]: e.target.value })}
                            maxLength={600}
                          />
                        ) : (
                          <input
                            id={`qf-${i}`}
                            type={f.kind === "email" || f.kind === "date" || f.kind === "time" || f.kind === "number" ? f.kind : "text"}
                            min={f.kind === "number" ? 0 : f.kind === "date" ? new Date().toISOString().slice(0, 10) : undefined}
                            step={f.kind === "number" ? "any" : undefined}
                            inputMode={f.kind === "number" ? "decimal" : undefined}
                            value={formAnswers[i] ?? ""}
                            placeholder={f.placeholder}
                            onChange={(e) => setFormAnswers({ ...formAnswers, [i]: e.target.value })}
                            maxLength={300}
                          />
                        )}
                        {fieldSpeech.supported && (f.kind === "long" || f.kind === "text" || f.kind === "number") && (
                          <button
                            type="button"
                            className={`field-mic ${micField === i && fieldSpeech.listening ? "is-listening" : ""}`}
                            aria-label={`${micField === i && fieldSpeech.listening ? t.stopListening : t.speak}: ${f.label}`}
                            aria-pressed={micField === i && fieldSpeech.listening}
                            onClick={() => toggleFieldMic(i)}
                          >
                            <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
                              <path fill="currentColor" d="M12 14a3 3 0 0 0 3-3V5a3 3 0 1 0-6 0v6a3 3 0 0 0 3 3Zm5-3a5 5 0 0 1-10 0H5a7 7 0 0 0 6 6.92V21h2v-3.08A7 7 0 0 0 19 11h-2Z" />
                            </svg>
                          </button>
                        )}
                      </div>
                    ))}
                  </div>
                ) : session.questionType === "phone" ? (
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
                  {answerSpeech.supported && session.questionType !== "phone" && !session.fields?.length && (
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
                {session.ownerPath && (
                  <p className="dashboard-note">
                    <a href={session.ownerPath} target="_blank" rel="noreferrer">
                      {u.dashboard} →
                    </a>
                    <br />
                    <span className="hint">{u.dashboardHint}</span>
                  </p>
                )}
                {session.ownerPath && (
                  <form className="test-first" method="post" action={`${session.ownerPath}/test`} target="_blank">
                    <input type="hidden" name="_csrf" value={csrfToken()} />
                    <p className="hint">{TEST_LINE}</p>
                    <button type="submit" className="button">
                      {u.testNow}
                    </button>
                  </form>
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
                  {session.ownerPath && (
                    <button type="submit" form="test-after" className="button">
                      {u.testNow}
                    </button>
                  )}
                  {session.ownerPath && session.published && (
                    <a className="button" href={`${session.ownerPath}#collabs`} target="_blank" rel="noreferrer">
                      {u.collabCheck}
                    </a>
                  )}
                  <button type="button" className="button" onClick={startOver}>
                    {t.startAnother}
                  </button>
                </div>
                {session.ownerPath && session.published && <p className="hint dashboard-note">{u.dashboardHint}</p>}
              </section>
            )}
          </>
        )}
        <Gallery sites={showcase?.recent ?? []} title={notes.gallery} hint={notes.galleryHint} />
        <PhotoCredits
          label={notes.photos}
          photos={[...(showcase?.examples ?? []).filter((p): p is NonNullable<typeof p> => !!p), ...(showcase?.recent ?? []).map((r) => r.photo)]}
        />

            {changeKey && (!session || session.status === "DONE") && !busy && (
              <form className="panel" onSubmit={onChange} aria-labelledby="change-title">
                <h2 id="change-title">{u.changeTitle}</h2>
                <p className="hint">{u.changeHint}</p>
                <label htmlFor="change-text" className="visually-hidden">
                  {u.changeTitle}
                </label>
                <textarea
                  id="change-text"
                  className="change-text"
                  rows={3}
                  value={changeText}
                  onChange={(e) => setChangeText(e.target.value)}
                  placeholder={u.changePlaceholder}
                  maxLength={3000}
                />
                <div className="row">
                  {changeSpeech.supported && (
                    <button
                      type="button"
                      className={`button mic ${changeSpeech.listening ? "is-listening" : ""}`}
                      onClick={() => (changeSpeech.listening ? changeSpeech.stop() : changeSpeech.start(t.speech))}
                    >
                      <span className="bars" aria-hidden="true">
                        <i />
                        <i />
                        <i />
                        <i />
                      </span>
                      {changeSpeech.listening ? t.stopListening : t.speak}
                    </button>
                  )}
                  <button type="submit" className="button primary" disabled={!changeText.trim()}>
                    {u.changeButton}
                  </button>
                </div>
              </form>
            )}
        {session?.ownerPath && (
          <form id="test-after" method="post" action={`${session.ownerPath}/test`} target="_blank" hidden>
            <input type="hidden" name="_csrf" value={csrfToken()} />
          </form>
        )}
      </main>

      <Coach
        u={u}
        language={language}
        onUse={(text) => {
          setSession(null);
          setDescription(text);
          document.getElementById("description")?.scrollIntoView({ behavior: "smooth", block: "center" });
        }}
      />

      <footer className="foot">
        <span>BazaarReady</span>
        <span className="holo">
          Crafted with <span aria-label="love">♥</span> by Yashi Tripathi
        </span>
      </footer>
    </>
  );
}
