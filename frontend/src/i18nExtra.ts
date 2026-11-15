import { LanguageCode } from "./i18n";

// Words for the home page's signboard, shutters and stamp.
export interface ExtraStrings {
  headline: string;
  shutterHint: string;
  shutters: [string, string, string];
}

export const EXTRA: Record<LanguageCode, ExtraStrings> = {
  en: {
    headline: "Just one chat is all it takes to launch your business online.",
    shutterHint: "Tap a shutter to peek inside",
    shutters: ["Home bakery", "Tuition classes", "Photo studio"],
  },
  hi: {
    headline: "अपना कारोबार ऑनलाइन लाने के लिए बस एक बातचीत काफ़ी है।",
    shutterHint: "शटर पर टैप करके अंदर झाँकिए",
    shutters: ["घर की बेकरी", "ट्यूशन क्लास", "फोटो स्टूडियो"],
  },
  kn: {
    headline: "ನಿಮ್ಮ ವ್ಯಾಪಾರವನ್ನು ಆನ್‌ಲೈನ್‌ಗೆ ತರಲು ಒಂದೇ ಒಂದು ಮಾತುಕತೆ ಸಾಕು.",
    shutterHint: "ಒಳಗೆ ನೋಡಲು ಶಟರ್ ಒತ್ತಿ",
    shutters: ["ಮನೆ ಬೇಕರಿ", "ಟ್ಯೂಷನ್ ತರಗತಿಗಳು", "ಫೋಟೋ ಸ್ಟುಡಿಯೋ"],
  },
  ta: {
    headline: "உங்கள் தொழிலை ஆன்லைனில் தொடங்க ஒரே ஒரு உரையாடல் போதும்.",
    shutterHint: "உள்ளே பார்க்க ஷட்டரைத் தட்டுங்கள்",
    shutters: ["வீட்டு பேக்கரி", "டியூஷன் வகுப்புகள்", "ஃபோட்டோ ஸ்டுடியோ"],
  },
  te: {
    headline: "మీ వ్యాపారాన్ని ఆన్‌లైన్‌లోకి తీసుకురావడానికి ఒక్క సంభాషణ చాలు.",
    shutterHint: "లోపల చూడటానికి షట్టర్ నొక్కండి",
    shutters: ["ఇంటి బేకరీ", "ట్యూషన్ క్లాసులు", "ఫోటో స్టూడియో"],
  },
  mr: {
    headline: "तुमचा व्यवसाय ऑनलाइन आणण्यासाठी फक्त एक संवाद पुरेसा आहे.",
    shutterHint: "आत पाहण्यासाठी शटरवर टॅप करा",
    shutters: ["घरची बेकरी", "ट्यूशन क्लास", "फोटो स्टुडिओ"],
  },
  bn: {
    headline: "আপনার ব্যবসা অনলাইনে আনতে একটিমাত্র কথোপকথনই যথেষ্ট।",
    shutterHint: "ভেতরে দেখতে শাটারে টোকা দিন",
    shutters: ["ঘরের বেকারি", "টিউশন ক্লাস", "ফটো স্টুডিও"],
  },
  gu: {
    headline: "તમારો વેપાર ઓનલાઇન લાવવા માટે ફક્ત એક વાતચીત પૂરતી છે.",
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
