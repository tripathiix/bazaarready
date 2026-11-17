import { useEffect, useState } from "react";
import { LanguageCode } from "./i18n";

export interface Photo {
  url: string;
  credit: string | null;
  creditUrl: string | null;
  licence: string | null;
  source: string | null;
}

export interface RecentSite {
  name: string;
  tagline: string | null;
  path: string;
  photo: Photo;
  location: string | null;
  publishedAt: string | null;
}

export interface FeaturedSite {
  name: string;
  tagline: string | null;
  path: string;
  photo: Photo;
  items: { name: string; price: string | null; photo: Photo | null }[];
  /** Result of the site's latest AI customer test; null when it hasn't been tested. */
  passed: number | null;
  total: number | null;
}

export interface Showcase {
  examples: (Photo | null)[];
  recent: RecentSite[];
  featured: FeaturedSite | null;
}

/** Photos for the home page. The page works without them, so a failed request is ignored. */
export function useShowcase(refreshKey: number) {
  const [showcase, setShowcase] = useState<Showcase | null>(null);
  useEffect(() => {
    let cancelled = false;
    fetch("/api/showcase")
      .then((r) => (r.ok ? r.json() : null))
      .then((data: Showcase | null) => {
        if (!cancelled && data) setShowcase(data);
      })
      .catch(() => undefined);
    return () => {
      cancelled = true;
    };
  }, [refreshKey]);
  return showcase;
}

/** Handwritten notes and gallery wording in each language. */
export const NOTES: Record<LanguageCode, { talk: string; gallery: string; galleryHint: string; photos: string }> = {
  en: { talk: "Just talk. We'll do the typing.", gallery: "Fresh from the bazaar", galleryHint: "Sites people built here. Open one.", photos: "Photos" },
  hi: { talk: "बस बोलिए, लिखना हम कर लेंगे।", gallery: "बाज़ार से ताज़ा", galleryHint: "यहाँ बनी वेबसाइटें। कोई भी खोलकर देखिए।", photos: "फ़ोटो" },
  kn: { talk: "ಸುಮ್ಮನೆ ಮಾತಾಡಿ, ಬರೆಯುವುದು ನಮ್ಮ ಕೆಲಸ.", gallery: "ಬಜಾರಿನಿಂದ ಹೊಸದು", galleryHint: "ಇಲ್ಲಿ ಮಾಡಿದ ವೆಬ್‌ಸೈಟ್‌ಗಳು. ಒಂದನ್ನು ತೆರೆಯಿರಿ.", photos: "ಫೋಟೋಗಳು" },
  ta: { talk: "பேசுங்கள் போதும், எழுதுவது எங்கள் வேலை.", gallery: "சந்தையிலிருந்து புதியவை", galleryHint: "இங்கே உருவான இணையதளங்கள். ஒன்றைத் திறந்து பாருங்கள்.", photos: "புகைப்படங்கள்" },
  te: { talk: "మాట్లాడండి చాలు, రాయడం మా పని.", gallery: "బజారు నుండి తాజాగా", galleryHint: "ఇక్కడ తయారైన వెబ్‌సైట్లు. ఒకటి తెరిచి చూడండి.", photos: "ఫోటోలు" },
  mr: { talk: "फक्त बोला, लिहायचं काम आमचं.", gallery: "बाजारातून ताजं", galleryHint: "इथे बनलेल्या वेबसाइट्स. एखादी उघडून पहा.", photos: "फोटो" },
  bn: { talk: "শুধু বলুন, লেখার কাজ আমাদের।", gallery: "বাজার থেকে টাটকা", galleryHint: "এখানে তৈরি ওয়েবসাইট। একটা খুলে দেখুন।", photos: "ছবি" },
  gu: { talk: "બસ બોલો, લખવાનું કામ અમારું.", gallery: "બજારમાંથી તાજું", galleryHint: "અહીં બનેલી વેબસાઇટ્સ. એક ખોલીને જુઓ.", photos: "ફોટા" },
};
