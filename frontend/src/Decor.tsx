import { useEffect, useRef, useState } from "react";
import { OPEN_SIGN } from "./i18nExtra";
import { Photo, RecentSite } from "./showcase";

const reducedMotion = () => window.matchMedia("(prefers-reduced-motion: reduce)").matches;

/** A string of warm lights across the top of the page, like a market street after dark. */
export function StringLights() {
  const swags = 3;
  const perSwag = 13;
  const bulbs = [];
  for (let s = 0; s < swags; s++) {
    for (let i = 1; i < perSwag; i++) {
      const t = i / perSwag;
      bulbs.push({ x: ((s + t) / swags) * 1000, y: 4 + 26 * Math.sin(Math.PI * t), n: s * perSwag + i });
    }
  }
  const wire = Array.from({ length: swags }, (_, s) => {
    const x0 = (s / swags) * 1000;
    const x1 = ((s + 1) / swags) * 1000;
    return `M${x0} 4 Q${(x0 + x1) / 2} 56 ${x1} 4`;
  }).join(" ");
  return (
    <svg className="lights" viewBox="0 0 1000 48" preserveAspectRatio="none" aria-hidden="true">
      <path d={wire} fill="none" stroke="rgba(255,255,255,0.18)" strokeWidth={1.2} />
      {bulbs.map((b) => (
        <circle key={b.n} className="bulb" cx={b.x} cy={b.y + 4} r={3.4} style={{ animationDelay: `${(b.n % 7) * 0.45}s` }} />
      ))}
    </svg>
  );
}

/** A neon sign that flips through "We're open" in languages from around the world. */
export function OpenSign() {
  const [index, setIndex] = useState(0);
  useEffect(() => {
    if (reducedMotion()) return;
    const timer = window.setInterval(() => setIndex((i) => (i + 1) % OPEN_SIGN.length), 2400);
    return () => window.clearInterval(timer);
  }, []);
  const phrase = OPEN_SIGN[index];
  return (
    <div className="open-sign" aria-hidden="true">
      <span className="neon-dot" />
      <span className="board" key={index} lang={phrase.lang}>
        {phrase.text}
      </span>
    </div>
  );
}

const SHUTTER_THEMES = ["rani", "indigo", "leaf"] as const;

/** Three shopfronts. Each shutter rolls up when pressed to show the kind of site inside. */
export function Shutters({
  labels,
  hint,
  allOpen,
  photos,
}: {
  labels: [string, string, string];
  hint: string;
  allOpen: boolean;
  photos: (Photo | null)[];
}) {
  const [open, setOpen] = useState<boolean[]>([false, false, false]);
  // On first visit the shutters lift a little, one after another, to show they can be opened.
  const [peek, setPeek] = useState(-1);
  const [touched, setTouched] = useState(false);
  useEffect(() => {
    if (reducedMotion() || touched) return;
    const timers = [0, 1, 2].flatMap((i) => [
      window.setTimeout(() => setPeek(i), 1400 + i * 650),
      window.setTimeout(() => setPeek((current) => (current === i ? -1 : current)), 2000 + i * 650),
    ]);
    return () => timers.forEach((t) => window.clearTimeout(t));
  }, [touched]);
  const toggle = (i: number) => {
    setTouched(true);
    setPeek(-1);
    setOpen((current) => current.map((value, k) => (k === i ? !value : value)));
  };
  return (
    <section className="shutters" aria-label={hint}>
      <div className="shutter-row">
        {labels.map((label, i) => {
          const isOpen = allOpen || open[i];
          return (
            <button
              key={i}
              type="button"
              className={`shopfront front-${SHUTTER_THEMES[i]} ${isOpen ? "is-open" : ""} ${peek === i && !isOpen ? "peek" : ""}`}
              aria-pressed={isOpen}
              onClick={() => toggle(i)}
            >
              <span className="awning" />
              {photos[i] && <img className="inside-photo" src={photos[i]!.url} alt="" loading="lazy" />}
              <span className={`inside ${photos[i] ? "has-photo" : ""}`}>
                <span className="mini-head">{label}</span>
                <span className="mini-line w80" />
                <span className="mini-line w60" />
                <span className="mini-cards">
                  <span />
                  <span />
                  <span />
                </span>
                <span className="mini-button" />
              </span>
              <span className="shutter">
                <span className="shutter-name">{label}</span>
                <span className="handle" />
              </span>
            </button>
          );
        })}
      </div>
      <p className="shutter-hint hand">
        <svg className="arrow" viewBox="0 0 60 40" aria-hidden="true">
          <path d="M4 34 C 18 30, 34 22, 46 8 M46 8 L 36 10 M46 8 L 46 19" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" />
        </svg>
        {hint}
      </p>
    </section>
  );
}

/** Sites people have built, as a row of cards with photos. */
export function Gallery({ sites, title, hint }: { sites: RecentSite[]; title: string; hint: string }) {
  if (sites.length === 0) return null;
  return (
    <section className="gallery" aria-labelledby="gallery-title">
      <h2 id="gallery-title" className="hand gallery-title">{title}</h2>
      <p className="hint">{hint}</p>
      <ul className="gallery-row">
        {sites.map((site, i) => (
          <li key={site.path} className="polaroid" data-reveal style={{ ["--tilt" as string]: `${[-3, 2, -1.5, 3, -2.5, 1.5][i % 6]}deg` }}>
            <a href={site.path} target="_blank" rel="noreferrer">
              <img src={site.photo.url} alt="" loading="lazy" />
              <span className="polaroid-name hand">{site.name}</span>
              {site.tagline && <span className="polaroid-tag">{site.tagline}</span>}
            </a>
          </li>
        ))}
      </ul>
    </section>
  );
}

/** Credits for stock photos shown on the home page. */
export function PhotoCredits({ photos, label }: { photos: Photo[]; label: string }) {
  const seen = new Set<string>();
  const unique = photos.filter((p) => p.credit && !seen.has(p.credit) && seen.add(p.credit));
  if (unique.length === 0) return null;
  return (
    <p className="photo-credits">
      {label}:{" "}
      {unique.map((p, i) => (
        <span key={p.credit}>
          <a href={p.creditUrl ?? undefined} target="_blank" rel="noreferrer">{p.credit}</a>
          {p.licence ? ` (${p.licence})` : ""} via {p.source}
          {i < unique.length - 1 ? "; " : ""}
        </span>
      ))}
    </p>
  );
}

/** A short shower of confetti, shown once when a site is published. */
export function Petals({ trigger }: { trigger: number }) {
  const canvas = useRef<HTMLCanvasElement | null>(null);

  useEffect(() => {
    const el = canvas.current;
    if (!el || trigger === 0 || reducedMotion()) return;
    const ctx = el.getContext("2d");
    if (!ctx) return;

    const width = (el.width = window.innerWidth);
    const height = (el.height = window.innerHeight);
    const colors = ["#f4a83a", "#ec7a8c", "#f6efe4", "#ffd27a"];
    const petals = Array.from({ length: 90 }, () => ({
      x: Math.random() * width,
      y: -20 - Math.random() * height * 0.5,
      size: 6 + Math.random() * 8,
      speed: 2 + Math.random() * 3,
      drift: -1 + Math.random() * 2,
      spin: Math.random() * Math.PI,
      color: colors[Math.floor(Math.random() * colors.length)],
    }));

    const started = performance.now();
    let frame = 0;
    const draw = (now: number) => {
      const age = now - started;
      ctx.clearRect(0, 0, width, height);
      ctx.globalAlpha = Math.max(0, 1 - Math.max(0, age - 2600) / 900);
      for (const p of petals) {
        p.y += p.speed;
        p.x += p.drift + Math.sin((age + p.spin * 500) / 400);
        p.spin += 0.05;
        ctx.save();
        ctx.translate(p.x, p.y);
        ctx.rotate(p.spin);
        ctx.fillStyle = p.color;
        ctx.beginPath();
        ctx.ellipse(0, 0, p.size, p.size * 0.5, 0, 0, Math.PI * 2);
        ctx.fill();
        ctx.restore();
      }
      if (age < 3500) frame = requestAnimationFrame(draw);
      else ctx.clearRect(0, 0, width, height);
    };
    frame = requestAnimationFrame(draw);
    return () => cancelAnimationFrame(frame);
  }, [trigger]);

  return <canvas ref={canvas} className="petals" aria-hidden="true" />;
}
