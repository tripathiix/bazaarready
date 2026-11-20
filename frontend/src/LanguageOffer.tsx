import { useEffect, useState } from "react";
import { BUILT_IN, CHIPS, MORE, nativeName } from "./globalLang";

// Offers the languages of the visitor's country when nothing else picked one. The country comes from our
// own server (/api/geo/languages, looked up from the address and not stored). It never switches by
// itself: the visitor taps a language or closes the bar, and that is remembered.

const DISMISSED = "bazaarready.langOffer";
const CHOSEN = "bazaarready.language";

const offered = (code: string) => BUILT_IN.includes(code) || CHIPS.includes(code) || MORE.includes(code);

export function LanguageOffer({ current, onPick }: { current: string; onPick: (code: string) => void }) {
  const [list, setList] = useState<string[]>([]);
  const [closed, setClosed] = useState(false);

  useEffect(() => {
    try {
      if (window.localStorage.getItem(DISMISSED) === "1" || window.localStorage.getItem(CHOSEN)) return;
    } catch {
      // storage blocked: still offer, just not remembered
    }
    if (new URLSearchParams(window.location.search).get("lang")) return;
    // A browser set to a language we offer already opens the page in it: location only fills the gap
    // when the browser says English (or a language we don't have).
    const browser = (navigator.language || "en").split("-")[0].toLowerCase();
    if (browser !== "en" && offered(browser)) return;
    let cancelled = false;
    fetch("/api/geo/languages", { credentials: "same-origin" })
      .then((r) => (r.ok ? r.json() : { languages: [] }))
      .then((geo: { languages?: string[] }) => {
        if (cancelled) return;
        const seen = new Set<string>();
        setList((geo.languages ?? []).filter((c) => c !== "en" && offered(c) && !seen.has(c) && seen.add(c)).slice(0, 6));
      })
      .catch(() => undefined);
    return () => {
      cancelled = true;
    };
  }, []);

  if (closed || current !== "en" || !list.length) return null;

  function remember() {
    try {
      window.localStorage.setItem(DISMISSED, "1");
    } catch {
      // fine
    }
    setClosed(true);
  }

  return (
    <div className="lang-offer" role="region" aria-label="Read this page in another language" translate="no">
      <span aria-hidden="true">🌐</span>
      {list.map((code) => (
        <button
          key={code}
          type="button"
          lang={code}
          onClick={() => {
            remember();
            onPick(code);
          }}
        >
          {nativeName(code)}
        </button>
      ))}
      <button type="button" className="lang-offer-x" aria-label="Close" onClick={remember}>
        ✕
      </button>
    </div>
  );
}
