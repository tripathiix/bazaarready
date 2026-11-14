import { LanguageCode } from "./i18n";

// Two type families that exist for every script we support, so the page keeps one personality
// in any language: Baloo for signboard-style headings, Anek for reading text.
const FONTS: Record<LanguageCode, { display: string; body: string }> = {
  en: { display: "Baloo 2", body: "Anek Latin" },
  hi: { display: "Baloo 2", body: "Anek Devanagari" },
  mr: { display: "Baloo 2", body: "Anek Devanagari" },
  kn: { display: "Baloo Tamma 2", body: "Anek Kannada" },
  ta: { display: "Baloo Thambi 2", body: "Anek Tamil" },
  te: { display: "Baloo Tammudu 2", body: "Anek Telugu" },
  bn: { display: "Baloo Da 2", body: "Anek Bangla" },
  gu: { display: "Baloo Bhai 2", body: "Anek Gujarati" },
};

const FALLBACK = '"Segoe UI", "Nirmala UI", "Noto Sans", system-ui, sans-serif';

/** Loads the fonts for a language and points the page's font variables at them. */
export function applyFonts(language: LanguageCode) {
  const { display, body } = FONTS[language];
  // The brand name is always in Latin letters, so Baloo 2 and Anek Latin are always loaded.
  const families = Array.from(new Set(["Baloo 2", display])).map((f) => `family=${f.replace(/ /g, "+")}:wght@600;800`);
  const bodies = Array.from(new Set(["Anek Latin", body])).map((f) => `family=${f.replace(/ /g, "+")}:wght@400;600`);
  const href = `https://fonts.googleapis.com/css2?${[...families, ...bodies].join("&")}&display=swap`;

  let link = document.getElementById("language-fonts") as HTMLLinkElement | null;
  if (!link) {
    link = document.createElement("link");
    link.id = "language-fonts";
    link.rel = "stylesheet";
    document.head.appendChild(link);
  }
  if (link.href !== href) link.href = href;

  const root = document.documentElement.style;
  root.setProperty("--display", `"${display}", "Baloo 2", ${FALLBACK}`);
  root.setProperty("--body", `"${body}", "Anek Latin", ${FALLBACK}`);
}
