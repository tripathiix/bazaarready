import { useEffect, useState } from "react";
import { LanguageCode } from "./i18n";
import { RecentSite } from "./showcase";

/** Icons for every kind of business, drifting up the empty sides of wide screens. */
const ICONS = ["🧁", "📚", "📷", "🪔", "🧵", "☕", "🎨", "🌿", "✂️", "🎧", "🧶", "🍜", "💐", "🏺", "🚲", "🎸"];

const LABELS: Record<LanguageCode, { live: string; justLaunched: string; locale: string }> = {
  en: { live: "Live", justLaunched: "Just launched", locale: "en-IN" },
  hi: { live: "लाइव", justLaunched: "अभी-अभी लॉन्च", locale: "hi-IN" },
  kn: { live: "ಲೈವ್", justLaunched: "ಈಗಷ್ಟೇ ಆರಂಭ", locale: "kn-IN" },
  ta: { live: "நேரலை", justLaunched: "இப்போதுதான் தொடங்கியது", locale: "ta-IN" },
  te: { live: "లైవ్", justLaunched: "ఇప్పుడే ప్రారంభం", locale: "te-IN" },
  mr: { live: "लाइव्ह", justLaunched: "नुकतेच सुरू", locale: "mr-IN" },
  bn: { live: "লাইভ", justLaunched: "এইমাত্র চালু", locale: "bn-IN" },
  gu: { live: "લાઇવ", justLaunched: "હમણાં જ શરૂ", locale: "gu-IN" },
};

function stillMotion() {
  return typeof window !== "undefined" && window.matchMedia?.("(prefers-reduced-motion: reduce)").matches;
}

function ago(iso: string | null, locale: string) {
  if (!iso) return "";
  const minutes = Math.round((Date.now() - new Date(iso).getTime()) / 60000);
  const fmt = new Intl.RelativeTimeFormat(locale, { numeric: "auto" });
  if (minutes < 60) return fmt.format(-Math.max(1, minutes), "minute");
  if (minutes < 60 * 24) return fmt.format(-Math.round(minutes / 60), "hour");
  return fmt.format(-Math.round(minutes / 1440), "day");
}

function Drift({ side }: { side: "left" | "right" }) {
  // A fixed layout per side, so it doesn't jump on re-render.
  const icons = ICONS.filter((_, i) => (side === "left" ? i % 2 === 0 : i % 2 === 1));
  return (
    <div className={`drift drift-${side}`} aria-hidden="true">
      {icons.map((icon, i) => (
        <span
          key={icon}
          style={{
            left: `${10 + ((i * 37) % 70)}%`,
            animationDelay: `${-i * 3.1}s`,
            animationDuration: `${22 + (i % 4) * 4}s`,
            fontSize: `${1.4 + (i % 3) * 0.5}rem`,
          }}
        >
          {icon}
        </span>
      ))}
    </div>
  );
}

/** Small cards announcing recently launched sites, one sliding in after another. */
function Ticker({ sites, side, language }: { sites: RecentSite[]; side: "left" | "right"; language: LanguageCode }) {
  const own = sites.filter((_, i) => (side === "left" ? i % 2 === 0 : i % 2 === 1));
  const list = own.length ? own : sites;
  const [index, setIndex] = useState(0);
  useEffect(() => {
    if (list.length < 2 || stillMotion()) return;
    const timer = window.setInterval(() => setIndex((i) => (i + 1) % list.length), side === "left" ? 5200 : 6100);
    return () => window.clearInterval(timer);
  }, [list.length, side]);
  if (!list.length) return null;
  const label = LABELS[language];
  const shown = [0, 1].map((k) => list[(index + k) % list.length]).filter((s, k, all) => all.indexOf(s) === k);
  return (
    <div className={`ticker ticker-${side}`} aria-label={label.justLaunched}>
      <p className="ticker-title">{label.justLaunched}</p>
      {shown.map((site, k) => (
        <a key={`${site.path}-${index}-${k}`} className="ticker-card" href={site.path} target="_blank" rel="noreferrer" style={{ animationDelay: `${k * 0.15}s` }}>
          <img src={site.photo.url} alt="" loading="lazy" />
          <span className="ticker-text">
            <span className="ticker-live">● {label.live}</span>
            <strong>{site.name}</strong>
            <span className="ticker-meta">{[site.location, ago(site.publishedAt, label.locale)].filter(Boolean).join(" · ")}</span>
          </span>
        </a>
      ))}
    </div>
  );
}

/** Fills the empty sides of wide screens. Hidden on narrower screens, still for people who ask for less motion. */
export function SideDecor({ sites, language }: { sites: RecentSite[]; language: LanguageCode }) {
  return (
    <div className="side-decor">
      <Drift side="left" />
      <Drift side="right" />
      <Ticker sites={sites} side="left" language={language} />
      <Ticker sites={sites} side="right" language={language} />
    </div>
  );
}
