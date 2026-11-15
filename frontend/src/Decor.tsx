import { useEffect, useRef, useState } from "react";
import { OPEN_SIGN } from "./i18nExtra";
import { Photo, RecentSite } from "./showcase";

const reducedMotion = () => window.matchMedia("(prefers-reduced-motion: reduce)").matches;

/** A string of festive flags across the top of the page, as hung for a shop's opening day. */
export function Bunting() {
  const colors = ["var(--rani)", "var(--leaf)", "var(--ink)", "var(--paper)", "var(--marigold)"];
  const swags = 4;
  const perSwag = 7;
  const flags = [];
  for (let s = 0; s < swags; s++) {
    for (let i = 0; i < perSwag; i++) {
      const t = (i + 0.5) / perSwag;
      const x = ((s + t) / swags) * 1000;
      const y = 6 + 22 * Math.sin(Math.PI * t);
      flags.push({ x, y, n: s * perSwag + i });
    }
  }
  const rope = Array.from({ length: swags }, (_, s) => {
    const x0 = (s / swags) * 1000;
    const x1 = ((s + 1) / swags) * 1000;
    return `M${x0} 6 Q${(x0 + x1) / 2} 50 ${x1} 6`;
  }).join(" ");
  return (
    <svg className="bunting" viewBox="0 0 1000 70" preserveAspectRatio="none" aria-hidden="true">
      <path d={rope} fill="none" stroke="var(--ink)" strokeWidth={2} />
      {flags.map((f) => (
        <path
          key={f.n}
          d={`M${f.x - 13} ${f.y} L${f.x + 13} ${f.y} L${f.x} ${f.y + 32} Z`}
          fill={colors[f.n % colors.length]}
          stroke="var(--ink)"
          strokeWidth={1.5}
        />
      ))}
    </svg>
  );
}

/** A hanging shop sign that flips through "We're open" in every supported language. */
export function OpenSign() {
  const [index, setIndex] = useState(0);
  useEffect(() => {
    if (reducedMotion()) return;
    const timer = window.setInterval(() => setIndex((i) => (i + 1) % OPEN_SIGN.length), 2200);
    return () => window.clearInterval(timer);
  }, []);
  const phrase = OPEN_SIGN[index];
  return (
    <div className="open-sign" aria-hidden="true">
      <svg className="strings" viewBox="0 0 100 44" aria-hidden="true">
        <path d="M50 4 L14 44 M50 4 L86 44" stroke="var(--ink)" strokeWidth={2.5} fill="none" />
        <circle cx={50} cy={5} r={4.5} fill="var(--ink)" />
      </svg>
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
  const toggle = (i: number) => setOpen((current) => current.map((value, k) => (k === i ? !value : value)));
  return (
    <section className="shutters" aria-label={hint}>
      <div className="shutter-row">
        {labels.map((label, i) => {
          const isOpen = allOpen || open[i];
          return (
            <button
              key={i}
              type="button"
              className={`shopfront front-${SHUTTER_THEMES[i]} ${isOpen ? "is-open" : ""}`}
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
          <li key={site.path} className="polaroid" style={{ ["--tilt" as string]: `${[-3, 2, -1.5, 3, -2.5, 1.5][i % 6]}deg` }}>
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
    const colors = ["#f28c0f", "#ffc531", "#d81b60", "#ffdf7e"];
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
