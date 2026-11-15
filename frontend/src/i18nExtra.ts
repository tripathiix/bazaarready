import { LanguageCode } from "./i18n";

// Words for the home page's signboard, shutters and stamp.
export interface ExtraStrings {
  headline: string;
  shutterHint: string;
  shutters: [string, string, string];
}

export const EXTRA: Record<LanguageCode, ExtraStrings> = {
  en: {
    headline: "Tell us once. Your business goes online.",
    shutterHint: "Tap a shutter to peek inside",
    shutters: ["Home bakery", "Tuition classes", "Photo studio"],
  },
  hi: {
    headline: "एक बार बताइए। आपका काम ऑनलाइन।",
    shutterHint: "शटर पर टैप करके अंदर झाँकिए",
    shutters: ["घर की बेकरी", "ट्यूशन क्लास", "फोटो स्टूडियो"],
  },
  kn: {
    headline: "ಒಮ್ಮೆ ಹೇಳಿ. ನಿಮ್ಮ ವ್ಯಾಪಾರ ಆನ್‌ಲೈನ್‌ನಲ್ಲಿ.",
    shutterHint: "ಒಳಗೆ ನೋಡಲು ಶಟರ್ ಒತ್ತಿ",
    shutters: ["ಮನೆ ಬೇಕರಿ", "ಟ್ಯೂಷನ್ ತರಗತಿಗಳು", "ಫೋಟೋ ಸ್ಟುಡಿಯೋ"],
  },
  ta: {
    headline: "ஒரு முறை சொல்லுங்கள். உங்கள் தொழில் ஆன்லைனில்.",
    shutterHint: "உள்ளே பார்க்க ஷட்டரைத் தட்டுங்கள்",
    shutters: ["வீட்டு பேக்கரி", "டியூஷன் வகுப்புகள்", "ஃபோட்டோ ஸ்டுடியோ"],
  },
  te: {
    headline: "ఒక్కసారి చెప్పండి. మీ వ్యాపారం ఆన్‌లైన్‌లో.",
    shutterHint: "లోపల చూడటానికి షట్టర్ నొక్కండి",
    shutters: ["ఇంటి బేకరీ", "ట్యూషన్ క్లాసులు", "ఫోటో స్టూడియో"],
  },
  mr: {
    headline: "एकदा सांगा. तुमचा व्यवसाय ऑनलाइन.",
    shutterHint: "आत पाहण्यासाठी शटरवर टॅप करा",
    shutters: ["घरची बेकरी", "ट्यूशन क्लास", "फोटो स्टुडिओ"],
  },
  bn: {
    headline: "একবার বলুন। আপনার ব্যবসা অনলাইনে।",
    shutterHint: "ভেতরে দেখতে শাটারে টোকা দিন",
    shutters: ["ঘরের বেকারি", "টিউশন ক্লাস", "ফটো স্টুডিও"],
  },
  gu: {
    headline: "એક વાર કહો. તમારો વેપાર ઓનલાઇન.",
    shutterHint: "અંદર જોવા શટર પર ટેપ કરો",
    shutters: ["ઘરની બેકરી", "ટ્યુશન ક્લાસ", "ફોટો સ્ટુડિયો"],
  },
};

/** "We're open" in languages from around the world, for the hanging sign that flips between them. */
export const OPEN_SIGN: { lang: string; text: string }[] = [
  { lang: "en", text: "We're open" },
  { lang: "hi", text: "दुकान खुली है" },
  { lang: "es", text: "Abierto" },
  { lang: "ta", text: "கடை திறந்திருக்கு" },
  { lang: "fr", text: "Ouvert" },
  { lang: "ar", text: "مفتوح" },
  { lang: "bn", text: "দোকান খোলা আছে" },
  { lang: "pt", text: "Aberto" },
  { lang: "zh", text: "营业中" },
  { lang: "kn", text: "ಅಂಗಡಿ ತೆರೆದಿದೆ" },
  { lang: "id", text: "Buka" },
];

/** Label for the button that reads the agent's question aloud. */
export const HEAR_QUESTION: Record<LanguageCode, string> = {
  en: "Hear the question",
  hi: "सवाल सुनें",
  kn: "ಪ್ರಶ್ನೆ ಕೇಳಿ",
  ta: "கேள்வியைக் கேளுங்கள்",
  te: "ప్రశ్న వినండి",
  mr: "प्रश्न ऐका",
  bn: "প্রশ্ন শুনুন",
  gu: "પ્રશ્ન સાંભળો",
};

/** Label for the owner's link to orders, bookings and messages. */
export const ORDERS_LINK: Record<LanguageCode, string> = {
  en: "Orders and messages",
  hi: "ऑर्डर और संदेश",
  kn: "ಆರ್ಡರ್‌ಗಳು ಮತ್ತು ಸಂದೇಶಗಳು",
  ta: "ஆர்டர்கள் மற்றும் செய்திகள்",
  te: "ఆర్డర్లు మరియు సందేశాలు",
  mr: "ऑर्डर आणि संदेश",
  bn: "অর্ডার ও বার্তা",
  gu: "ઓર્ડર અને સંદેશા",
};
