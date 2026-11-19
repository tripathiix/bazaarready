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

/** Words for importing WhatsApp chats. */
export const CHATS: Record<LanguageCode, { or: string; button: string; how: string; owner: string; read: string; igButton: string; igHow: string; received: string; opening: string; found: string; igNone: string; igHtml: string; }> = {
  en: { or: "Already selling on WhatsApp?", button: "Upload WhatsApp chats", how: "In WhatsApp, open a chat with a customer, tap ⋮ or the name, choose Export chat, then Without media. Upload one or more files. Names and numbers are removed before anything is saved.", owner: "Your name as it appears in the chats (optional)", read: "Read {messages} messages from {customers} customers. Names and numbers removed.", igButton: "Upload Instagram download", igHow: "Instagram: Settings → Accounts Center → Your information and permissions → Download your information → choose Messages and the JSON format. Instagram emails you a zip; upload it here.", received: "Got {name}. Reading your messages… this takes about half a minute.", opening: "Opening {name}…", found: "Found {n} conversations. Sending only the messages, not photos.", igNone: "No Instagram messages found in this zip. When downloading, choose Messages and the JSON format.", igHtml: "This download is in HTML format. Download it again and choose JSON." },
  hi: { or: "पहले से WhatsApp पर बेचते हैं?", button: "WhatsApp चैट अपलोड करें", how: "WhatsApp में ग्राहक की चैट खोलें, ⋮ या नाम पर टैप करें, Export chat चुनें, फिर Without media। एक या ज़्यादा फ़ाइलें अपलोड करें। सेव करने से पहले नाम और नंबर हटा दिए जाते हैं।", owner: "चैट में दिखने वाला आपका नाम (ज़रूरी नहीं)", read: "{customers} ग्राहकों के {messages} संदेश पढ़े। नाम और नंबर हटा दिए।", igButton: "Instagram डाउनलोड अपलोड करें", igHow: "Instagram: Settings → Accounts Center → Your information and permissions → Download your information → Messages और JSON फ़ॉर्मैट चुनें। Instagram आपको एक zip भेजेगा; उसे यहाँ अपलोड करें।", received: "{name} मिल गई। आपके मैसेज पढ़ रहे हैं… लगभग आधा मिनट लगेगा।", opening: "{name} खोल रहे हैं…", found: "{n} बातचीत मिलीं। सिर्फ़ मैसेज भेज रहे हैं, फ़ोटो नहीं।", igNone: "इस zip में Instagram के मैसेज नहीं मिले। डाउनलोड करते समय Messages और JSON फ़ॉर्मैट चुनें।", igHtml: "यह डाउनलोड HTML फ़ॉर्मैट में है। दोबारा डाउनलोड करें और JSON चुनें।" },
  kn: { or: "ಈಗಾಗಲೇ WhatsApp ನಲ್ಲಿ ಮಾರುತ್ತೀರಾ?", button: "WhatsApp ಚಾಟ್ ಅಪ್‌ಲೋಡ್ ಮಾಡಿ", how: "WhatsApp ನಲ್ಲಿ ಗ್ರಾಹಕರ ಚಾಟ್ ತೆರೆಯಿರಿ, ⋮ ಅಥವಾ ಹೆಸರನ್ನು ಒತ್ತಿ, Export chat ಆಯ್ಕೆಮಾಡಿ, ನಂತರ Without media. ಒಂದು ಅಥವಾ ಹೆಚ್ಚು ಫೈಲ್ ಅಪ್‌ಲೋಡ್ ಮಾಡಿ. ಉಳಿಸುವ ಮೊದಲು ಹೆಸರು ಮತ್ತು ಸಂಖ್ಯೆಗಳನ್ನು ತೆಗೆಯಲಾಗುತ್ತದೆ.", owner: "ಚಾಟ್‌ನಲ್ಲಿ ಕಾಣುವ ನಿಮ್ಮ ಹೆಸರು (ಐಚ್ಛಿಕ)", read: "{customers} ಗ್ರಾಹಕರ {messages} ಸಂದೇಶಗಳನ್ನು ಓದಲಾಗಿದೆ. ಹೆಸರು ಮತ್ತು ಸಂಖ್ಯೆಗಳನ್ನು ತೆಗೆಯಲಾಗಿದೆ.", igButton: "Instagram ಡೌನ್‌ಲೋಡ್ ಅಪ್‌ಲೋಡ್ ಮಾಡಿ", igHow: "Instagram: Settings → Accounts Center → Your information and permissions → Download your information → Messages ಮತ್ತು JSON ಆಯ್ಕೆಮಾಡಿ. Instagram ಕಳುಹಿಸುವ zip ಅನ್ನು ಇಲ್ಲಿ ಅಪ್‌ಲೋಡ್ ಮಾಡಿ.", received: "{name} ಸಿಕ್ಕಿತು. ನಿಮ್ಮ ಸಂದೇಶಗಳನ್ನು ಓದುತ್ತಿದ್ದೇವೆ… ಸುಮಾರು ಅರ್ಧ ನಿಮಿಷ.", opening: "{name} ತೆರೆಯುತ್ತಿದ್ದೇವೆ…", found: "{n} ಸಂಭಾಷಣೆಗಳು ಸಿಕ್ಕಿವೆ. ಸಂದೇಶಗಳನ್ನು ಮಾತ್ರ ಕಳುಹಿಸುತ್ತಿದ್ದೇವೆ, ಫೋಟೋಗಳನ್ನಲ್ಲ.", igNone: "ಈ zip ನಲ್ಲಿ Instagram ಸಂದೇಶಗಳು ಸಿಗಲಿಲ್ಲ. ಡೌನ್‌ಲೋಡ್ ಮಾಡುವಾಗ Messages ಮತ್ತು JSON ಆಯ್ಕೆಮಾಡಿ.", igHtml: "ಈ ಡೌನ್‌ಲೋಡ್ HTML ರೂಪದಲ್ಲಿದೆ. ಮತ್ತೆ ಡೌನ್‌ಲೋಡ್ ಮಾಡಿ JSON ಆಯ್ಕೆಮಾಡಿ." },
  ta: { or: "ஏற்கனவே WhatsApp-இல் விற்கிறீர்களா?", button: "WhatsApp உரையாடல்களைப் பதிவேற்று", how: "WhatsApp-இல் வாடிக்கையாளர் உரையாடலைத் திறந்து, ⋮ அல்லது பெயரைத் தட்டி, Export chat, பிறகு Without media தேர்ந்தெடுக்கவும். ஒன்று அல்லது அதற்கு மேற்பட்ட கோப்புகளைப் பதிவேற்றவும். சேமிக்கும் முன் பெயர்களும் எண்களும் நீக்கப்படும்.", owner: "உரையாடலில் தெரியும் உங்கள் பெயர் (விருப்பம்)", read: "{customers} வாடிக்கையாளர்களின் {messages} செய்திகள் படிக்கப்பட்டன. பெயர்களும் எண்களும் நீக்கப்பட்டன.", igButton: "Instagram பதிவிறக்கத்தைப் பதிவேற்று", igHow: "Instagram: Settings → Accounts Center → Your information and permissions → Download your information → Messages, JSON தேர்ந்தெடுக்கவும். Instagram அனுப்பும் zip-ஐ இங்கே பதிவேற்றவும்.", received: "{name} கிடைத்தது. உங்கள் செய்திகளைப் படிக்கிறோம்… சுமார் அரை நிமிடம் ஆகும்.", opening: "{name} திறக்கிறோம்…", found: "{n} உரையாடல்கள் கிடைத்தன. செய்திகளை மட்டும் அனுப்புகிறோம், படங்களை அல்ல.", igNone: "இந்த zip-இல் Instagram செய்திகள் இல்லை. பதிவிறக்கும்போது Messages, JSON தேர்ந்தெடுக்கவும்.", igHtml: "இந்தப் பதிவிறக்கம் HTML வடிவில் உள்ளது. மீண்டும் பதிவிறக்கி JSON தேர்ந்தெடுக்கவும்." },
  te: { or: "ఇప్పటికే WhatsApp లో అమ్ముతున్నారా?", button: "WhatsApp చాట్‌లు అప్‌లోడ్ చేయండి", how: "WhatsApp లో కస్టమర్ చాట్ తెరిచి, ⋮ లేదా పేరుపై నొక్కి, Export chat ఎంచుకోండి, తర్వాత Without media. ఒకటి లేదా ఎక్కువ ఫైళ్లు అప్‌లోడ్ చేయండి. సేవ్ చేసే ముందు పేర్లు, నంబర్లు తీసివేయబడతాయి.", owner: "చాట్‌లో కనిపించే మీ పేరు (ఐచ్ఛికం)", read: "{customers} కస్టమర్ల {messages} సందేశాలు చదివాము. పేర్లు, నంబర్లు తీసివేశాము.", igButton: "Instagram డౌన్‌లోడ్ అప్‌లోడ్ చేయండి", igHow: "Instagram: Settings → Accounts Center → Your information and permissions → Download your information → Messages, JSON ఎంచుకోండి. Instagram పంపే zip ని ఇక్కడ అప్‌లోడ్ చేయండి.", received: "{name} అందింది. మీ సందేశాలు చదువుతున్నాం… సుమారు అర నిమిషం పడుతుంది.", opening: "{name} తెరుస్తున్నాం…", found: "{n} సంభాషణలు దొరికాయి. సందేశాలు మాత్రమే పంపుతున్నాం, ఫోటోలు కాదు.", igNone: "ఈ zip లో Instagram సందేశాలు లేవు. డౌన్‌లోడ్ చేసేటప్పుడు Messages, JSON ఎంచుకోండి.", igHtml: "ఈ డౌన్‌లోడ్ HTML ఫార్మాట్‌లో ఉంది. మళ్ళీ డౌన్‌లోడ్ చేసి JSON ఎంచుకోండి." },
  mr: { or: "आधीच WhatsApp वर विकता?", button: "WhatsApp चॅट अपलोड करा", how: "WhatsApp मध्ये ग्राहकाची चॅट उघडा, ⋮ किंवा नावावर टॅप करा, Export chat निवडा, मग Without media. एक किंवा अधिक फाइल अपलोड करा. सेव्ह करण्यापूर्वी नावे आणि नंबर काढले जातात.", owner: "चॅटमध्ये दिसणारे तुमचे नाव (ऐच्छिक)", read: "{customers} ग्राहकांचे {messages} संदेश वाचले. नावे आणि नंबर काढले.", igButton: "Instagram डाउनलोड अपलोड करा", igHow: "Instagram: Settings → Accounts Center → Your information and permissions → Download your information → Messages आणि JSON निवडा. Instagram पाठवलेली zip इथे अपलोड करा.", received: "{name} मिळाली. तुमचे मेसेज वाचत आहोत… साधारण अर्धा मिनिट लागेल.", opening: "{name} उघडत आहोत…", found: "{n} संभाषणे सापडली. फक्त मेसेज पाठवत आहोत, फोटो नाही.", igNone: "या zip मध्ये Instagram मेसेज सापडले नाहीत. डाउनलोड करताना Messages आणि JSON निवडा.", igHtml: "हे डाउनलोड HTML स्वरूपात आहे. पुन्हा डाउनलोड करा आणि JSON निवडा." },
  bn: { or: "ইতিমধ্যে WhatsApp-এ বিক্রি করেন?", button: "WhatsApp চ্যাট আপলোড করুন", how: "WhatsApp-এ গ্রাহকের চ্যাট খুলুন, ⋮ বা নামে ট্যাপ করুন, Export chat বেছে নিন, তারপর Without media। এক বা একাধিক ফাইল আপলোড করুন। সেভ করার আগে নাম ও নম্বর সরিয়ে ফেলা হয়।", owner: "চ্যাটে আপনার যে নাম দেখা যায় (ঐচ্ছিক)", read: "{customers} জন গ্রাহকের {messages}টি বার্তা পড়া হয়েছে। নাম ও নম্বর সরানো হয়েছে।", igButton: "Instagram ডাউনলোড আপলোড করুন", igHow: "Instagram: Settings → Accounts Center → Your information and permissions → Download your information → Messages আর JSON বেছে নিন। Instagram যে zip পাঠাবে সেটি এখানে আপলোড করুন।", received: "{name} পেয়েছি। আপনার মেসেজ পড়ছি… প্রায় আধ মিনিট লাগবে।", opening: "{name} খুলছি…", found: "{n}টি কথোপকথন পাওয়া গেছে। শুধু মেসেজ পাঠাচ্ছি, ছবি নয়।", igNone: "এই zip-এ Instagram মেসেজ পাওয়া যায়নি। ডাউনলোডের সময় Messages আর JSON বেছে নিন।", igHtml: "এই ডাউনলোড HTML ফরম্যাটে। আবার ডাউনলোড করে JSON বেছে নিন।" },
  gu: { or: "પહેલેથી WhatsApp પર વેચો છો?", button: "WhatsApp ચેટ અપલોડ કરો", how: "WhatsApp માં ગ્રાહકની ચેટ ખોલો, ⋮ અથવા નામ પર ટેપ કરો, Export chat પસંદ કરો, પછી Without media. એક કે વધુ ફાઇલ અપલોડ કરો. સેવ કરતાં પહેલાં નામ અને નંબર દૂર કરાય છે.", owner: "ચેટમાં દેખાતું તમારું નામ (વૈકલ્પિક)", read: "{customers} ગ્રાહકોના {messages} સંદેશ વાંચ્યા. નામ અને નંબર દૂર કર્યા.", igButton: "Instagram ડાઉનલોડ અપલોડ કરો", igHow: "Instagram: Settings → Accounts Center → Your information and permissions → Download your information → Messages અને JSON પસંદ કરો. Instagram મોકલેલી zip અહીં અપલોડ કરો.", received: "{name} મળી ગઈ. તમારા મેસેજ વાંચી રહ્યા છીએ… લગભગ અડધી મિનિટ લાગશે.", opening: "{name} ખોલી રહ્યા છીએ…", found: "{n} વાતચીત મળી. ફક્ત મેસેજ મોકલીએ છીએ, ફોટા નહીં.", igNone: "આ zip માં Instagram મેસેજ મળ્યા નહીં. ડાઉનલોડ કરતી વખતે Messages અને JSON પસંદ કરો.", igHtml: "આ ડાઉનલોડ HTML ફોર્મેટમાં છે. ફરી ડાઉનલોડ કરો અને JSON પસંદ કરો." },
};
