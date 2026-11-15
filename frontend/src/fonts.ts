import { LanguageCode } from "./i18n";

// Two type families that exist for every script we support, so the page keeps one personality
// in any language: Baloo for signboard-style headings, Anek for reading text.
const FONTS: Record<LanguageCode, { display: string; body: string }> = {
  en: { display: "Bricolage Grotesque", body: "Anek Latin" },
  hi: { display: "Anek Devanagari", body: "Anek Devanagari" },
  mr: { display: "Anek Devanagari", body: "Anek Devanagari" },
  kn: { display: "Anek Kannada", body: "Anek Kannada" },
  ta: { display: "Anek Tamil", body: "Anek Tamil" },
  te: { display: "Anek Telugu", body: "Anek Telugu" },
  bn: { display: "Anek Bangla", body: "Anek Bangla" },
  gu: { display: "Anek Gujarati", body: "Anek Gujarati" },
};

const FALLBACK = '"Segoe UI", "Nirmala UI", "Noto Sans", system-ui, sans-serif';

/** Loads the fonts for a language and points the page's font variables at them. */
export function applyFonts(language: LanguageCode) {
  const { display, body } = FONTS[language];
  // The wordmark is always Bricolage Grotesque, so it and Anek Latin are always loaded.
  const families = Array.from(new Set(["Bricolage Grotesque", "Anek Latin", display, body])).map(
    (f) => `family=${f.replace(/ /g, "+")}:wght@400;600;800`
  );
  const bodies: string[] = [];
  const handwriting = ["en", "hi", "mr"].includes(language) ? ["family=Kalam:wght@400;700"] : [];
  const href = `https://fonts.googleapis.com/css2?${[...families, ...bodies, ...handwriting].join("&")}&display=swap`;

  let link = document.getElementById("language-fonts") as HTMLLinkElement | null;
  if (!link) {
    link = document.createElement("link");
    link.id = "language-fonts";
    link.rel = "stylesheet";
    document.head.appendChild(link);
  }
  if (link.href !== href) link.href = href;

  const root = document.documentElement.style;
  root.setProperty("--display", `"${display}", "Bricolage Grotesque", ${FALLBACK}`);
  root.setProperty("--body", `"${body}", "Anek Latin", ${FALLBACK}`);
  root.setProperty("--hand", handwriting.length ? `"Kalam", "${display}", ${FALLBACK}` : `"${display}", ${FALLBACK}`);
}
