import { LanguageCode } from "./i18n";

/** Words for the redesigned home page: the promise, header links, build box, preview and links strip. */
export interface HomeStrings {
  promise: [string, string];
  templates: string;
  features: string;
  contact: string;
  myDashboard: string;
  importChats: string;
  speak: string;
  tryLabel: string;
  examples: { label: string; text: string }[];
  coachAsk: string;
  coachLink: string;
  whatYouGet: string;
  /** "{passed} of {total} …" */
  badge: string;
  openSite: string;
  include: string;
  design: string;
  removeDesign: string;
  strip: { big: string; small: string; link: string }[];
}

export const HOME: Record<LanguageCode, HomeStrings> = {
  en: {
    promise: ["Describe your business.", "We build it, test it, and keep it safe."],
    templates: "Templates",
    features: "Features",
    contact: "Contact",
    myDashboard: "My dashboard",
    speak: "Speak",
    importChats: "Import WhatsApp or Instagram chats",
    tryLabel: "Try:",
    examples: [
      { label: "Home bakery in Pune", text: "I bake cakes at home in Pune. Chocolate cake ₹900 per kg, cupcakes ₹60 each. I deliver in Kothrud on weekends." },
      { label: "Tuition classes", text: "I teach maths and science to classes 6 to 10 in Indore. ₹1500 a month per subject, batches of 8, evenings 5 to 8 pm." },
      { label: "Photo studio", text: "Photo studio in Lucknow. Passport photos ₹100, birthday shoots ₹4000, wedding packages from ₹35000. Open 10 am to 8 pm." },
      { label: "Handmade pottery", text: "I make handmade pottery in Jaipur. Mugs ₹450, serving bowls ₹900, planters ₹650. I ship across India." },
    ],
    coachAsk: "Not sure about your idea yet?",
    coachLink: "Talk it through with the Idea coach →",
    whatYouGet: "This is what you get ↓",
    badge: "{passed} of {total} AI customers got what they came for",
    openSite: "Open this website →",
    include: "What to include",
    design: "Design: {name}",
    removeDesign: "Let the agent choose",
    strip: [
      { big: "7 designs", small: "picked for your business", link: "See templates" },
      { big: "10 AI customers", small: "test it before launch", link: "How it works" },
      { big: "Scam check", small: "for every collab offer", link: "Features" },
      { big: "8 languages", small: "type or speak", link: "Features" },
    ],
  },
  hi: {
    promise: ["अपने काम के बारे में बताइए।", "हम वेबसाइट बनाते हैं, जाँचते हैं और सुरक्षित रखते हैं।"],
    templates: "डिज़ाइन",
    features: "खूबियाँ",
    contact: "संपर्क",
    myDashboard: "मेरा डैशबोर्ड",
    speak: "बोलिए",
    importChats: "WhatsApp या Instagram चैट लाएँ",
    tryLabel: "आज़माएँ:",
    examples: [
      { label: "पुणे में घर की बेकरी", text: "मैं पुणे में घर पर केक बनाती हूँ। चॉकलेट केक ₹900 प्रति किलो, कपकेक ₹60 का एक। कोथरूड में शनिवार-रविवार डिलीवरी।" },
      { label: "ट्यूशन क्लास", text: "मैं इंदौर में कक्षा 6 से 10 तक गणित और विज्ञान पढ़ाती हूँ। एक विषय ₹1500 महीना, 8 बच्चों का बैच, शाम 5 से 8 बजे।" },
      { label: "फोटो स्टूडियो", text: "लखनऊ में फोटो स्टूडियो। पासपोर्ट फोटो ₹100, जन्मदिन शूट ₹4000, शादी पैकेज ₹35000 से। सुबह 10 से रात 8 बजे तक।" },
      { label: "हाथ से बने मिट्टी के बर्तन", text: "मैं जयपुर में हाथ से मिट्टी के बर्तन बनाती हूँ। मग ₹450, सर्विंग बाउल ₹900, गमले ₹650। पूरे भारत में भेजती हूँ।" },
    ],
    coachAsk: "अपने आइडिया को लेकर पक्का नहीं?",
    coachLink: "आइडिया कोच से बात कीजिए →",
    whatYouGet: "आपको ऐसी वेबसाइट मिलेगी ↓",
    badge: "{total} में से {passed} AI ग्राहकों को जो चाहिए था, मिल गया",
    openSite: "यह वेबसाइट खोलें →",
    include: "क्या-क्या लिखें",
    design: "डिज़ाइन: {name}",
    removeDesign: "एजेंट को चुनने दें",
    strip: [
      { big: "7 डिज़ाइन", small: "आपके काम के हिसाब से", link: "डिज़ाइन देखें" },
      { big: "10 AI ग्राहक", small: "लॉन्च से पहले जाँच", link: "यह कैसे काम करता है" },
      { big: "धोखे की जाँच", small: "हर कोलैब ऑफ़र के लिए", link: "खूबियाँ" },
      { big: "8 भाषाएँ", small: "लिखें या बोलें", link: "खूबियाँ" },
    ],
  },
  kn: {
    promise: ["ನಿಮ್ಮ ವ್ಯಾಪಾರದ ಬಗ್ಗೆ ಹೇಳಿ.", "ನಾವು ಅದನ್ನು ನಿರ್ಮಿಸಿ, ಪರೀಕ್ಷಿಸಿ, ಸುರಕ್ಷಿತವಾಗಿಡುತ್ತೇವೆ."],
    templates: "ವಿನ್ಯಾಸಗಳು",
    features: "ವೈಶಿಷ್ಟ್ಯಗಳು",
    contact: "ಸಂಪರ್ಕ",
    myDashboard: "ನನ್ನ ಡ್ಯಾಶ್‌ಬೋರ್ಡ್",
    speak: "ಮಾತನಾಡಿ",
    importChats: "WhatsApp ಅಥವಾ Instagram ಚಾಟ್ ತನ್ನಿ",
    tryLabel: "ಪ್ರಯತ್ನಿಸಿ:",
    examples: [
      { label: "ಪುಣೆಯಲ್ಲಿ ಮನೆ ಬೇಕರಿ", text: "ನಾನು ಪುಣೆಯಲ್ಲಿ ಮನೆಯಲ್ಲೇ ಕೇಕ್ ಮಾಡುತ್ತೇನೆ. ಚಾಕೊಲೇಟ್ ಕೇಕ್ ಕೆಜಿಗೆ ₹900, ಕಪ್‌ಕೇಕ್ ಒಂದಕ್ಕೆ ₹60. ವಾರಾಂತ್ಯದಲ್ಲಿ ಕೋಥ್ರೂಡ್‌ಗೆ ಡೆಲಿವರಿ." },
      { label: "ಟ್ಯೂಷನ್ ತರಗತಿಗಳು", text: "ನಾನು ಇಂದೋರ್‌ನಲ್ಲಿ 6 ರಿಂದ 10ನೇ ತರಗತಿಗೆ ಗಣಿತ ಮತ್ತು ವಿಜ್ಞಾನ ಕಲಿಸುತ್ತೇನೆ. ಒಂದು ವಿಷಯಕ್ಕೆ ತಿಂಗಳಿಗೆ ₹1500, 8 ಮಕ್ಕಳ ಬ್ಯಾಚ್, ಸಂಜೆ 5 ರಿಂದ 8." },
      { label: "ಫೋಟೋ ಸ್ಟುಡಿಯೋ", text: "ಲಕ್ನೋದಲ್ಲಿ ಫೋಟೋ ಸ್ಟುಡಿಯೋ. ಪಾಸ್‌ಪೋರ್ಟ್ ಫೋಟೋ ₹100, ಹುಟ್ಟುಹಬ್ಬದ ಶೂಟ್ ₹4000, ಮದುವೆ ಪ್ಯಾಕೇಜ್ ₹35000 ರಿಂದ. ಬೆಳಿಗ್ಗೆ 10 ರಿಂದ ರಾತ್ರಿ 8." },
      { label: "ಕೈಯಿಂದ ಮಾಡಿದ ಮಣ್ಣಿನ ಪಾತ್ರೆ", text: "ನಾನು ಜೈಪುರದಲ್ಲಿ ಕೈಯಿಂದ ಮಣ್ಣಿನ ಪಾತ್ರೆ ಮಾಡುತ್ತೇನೆ. ಮಗ್ ₹450, ಬಟ್ಟಲು ₹900, ಹೂಕುಂಡ ₹650. ಭಾರತದಾದ್ಯಂತ ಕಳುಹಿಸುತ್ತೇನೆ." },
    ],
    coachAsk: "ನಿಮ್ಮ ಆಲೋಚನೆಯ ಬಗ್ಗೆ ಖಚಿತವಿಲ್ಲವೇ?",
    coachLink: "ಐಡಿಯಾ ಕೋಚ್ ಜೊತೆ ಮಾತನಾಡಿ →",
    whatYouGet: "ನಿಮಗೆ ಸಿಗುವುದು ಇದು ↓",
    badge: "{total} AI ಗ್ರಾಹಕರಲ್ಲಿ {passed} ಜನರಿಗೆ ಬೇಕಾದ್ದು ಸಿಕ್ಕಿತು",
    openSite: "ಈ ವೆಬ್‌ಸೈಟ್ ತೆರೆಯಿರಿ →",
    include: "ಏನೇನು ಬರೆಯಬೇಕು",
    design: "ವಿನ್ಯಾಸ: {name}",
    removeDesign: "ಏಜೆಂಟ್ ಆಯ್ಕೆ ಮಾಡಲಿ",
    strip: [
      { big: "7 ವಿನ್ಯಾಸಗಳು", small: "ನಿಮ್ಮ ವ್ಯಾಪಾರಕ್ಕೆ ತಕ್ಕಂತೆ", link: "ವಿನ್ಯಾಸಗಳನ್ನು ನೋಡಿ" },
      { big: "10 AI ಗ್ರಾಹಕರು", small: "ಆರಂಭಕ್ಕೂ ಮುನ್ನ ಪರೀಕ್ಷೆ", link: "ಇದು ಹೇಗೆ ಕೆಲಸ ಮಾಡುತ್ತದೆ" },
      { big: "ಮೋಸ ಪರಿಶೀಲನೆ", small: "ಪ್ರತಿ ಕೊಲ್ಯಾಬ್ ಆಫರ್‌ಗೆ", link: "ವೈಶಿಷ್ಟ್ಯಗಳು" },
      { big: "8 ಭಾಷೆಗಳು", small: "ಬರೆಯಿರಿ ಅಥವಾ ಮಾತನಾಡಿ", link: "ವೈಶಿಷ್ಟ್ಯಗಳು" },
    ],
  },
  ta: {
    promise: ["உங்கள் தொழிலைப் பற்றி சொல்லுங்கள்.", "நாங்கள் உருவாக்கி, சோதித்து, பாதுகாப்பாக வைக்கிறோம்."],
    templates: "வடிவமைப்புகள்",
    features: "அம்சங்கள்",
    contact: "தொடர்பு",
    myDashboard: "என் டாஷ்போர்டு",
    speak: "பேசுங்கள்",
    importChats: "WhatsApp அல்லது Instagram உரையாடல்களைக் கொண்டு வா",
    tryLabel: "முயற்சிக்கவும்:",
    examples: [
      { label: "புனேயில் வீட்டு பேக்கரி", text: "நான் புனேயில் வீட்டிலேயே கேக் செய்கிறேன். சாக்லேட் கேக் கிலோ ₹900, கப்கேக் ஒன்று ₹60. வார இறுதியில் கோத்ருடில் டெலிவரி." },
      { label: "டியூஷன் வகுப்புகள்", text: "இந்தூரில் 6 முதல் 10 ஆம் வகுப்பு வரை கணிதம், அறிவியல் கற்பிக்கிறேன். ஒரு பாடத்துக்கு மாதம் ₹1500, 8 பேர் கொண்ட பேட்ச், மாலை 5 முதல் 8 வரை." },
      { label: "ஃபோட்டோ ஸ்டுடியோ", text: "லக்னோவில் ஃபோட்டோ ஸ்டுடியோ. பாஸ்போர்ட் புகைப்படம் ₹100, பிறந்தநாள் ஷூட் ₹4000, திருமண பேக்கேஜ் ₹35000 முதல். காலை 10 முதல் இரவு 8 வரை." },
      { label: "கையால் செய்த மண்பாண்டம்", text: "ஜெய்ப்பூரில் கையால் மண்பாண்டங்கள் செய்கிறேன். மக் ₹450, பரிமாறும் கிண்ணம் ₹900, பூந்தொட்டி ₹650. இந்தியா முழுவதும் அனுப்புகிறேன்." },
    ],
    coachAsk: "உங்கள் யோசனை பற்றி உறுதியில்லையா?",
    coachLink: "ஐடியா கோச்சுடன் பேசுங்கள் →",
    whatYouGet: "உங்களுக்குக் கிடைப்பது இது ↓",
    badge: "{total} AI வாடிக்கையாளர்களில் {passed} பேருக்கு தேவையானது கிடைத்தது",
    openSite: "இந்த இணையதளத்தைத் திறக்கவும் →",
    include: "என்ன எழுத வேண்டும்",
    design: "வடிவமைப்பு: {name}",
    removeDesign: "ஏஜென்ட் தேர்வு செய்யட்டும்",
    strip: [
      { big: "7 வடிவமைப்புகள்", small: "உங்கள் தொழிலுக்கு ஏற்றவை", link: "வடிவமைப்புகளைப் பார்" },
      { big: "10 AI வாடிக்கையாளர்கள்", small: "தொடங்கும் முன் சோதனை", link: "இது எப்படி வேலை செய்கிறது" },
      { big: "மோசடி சோதனை", small: "ஒவ்வொரு கொலாப் சலுகைக்கும்", link: "அம்சங்கள்" },
      { big: "8 மொழிகள்", small: "எழுதுங்கள் அல்லது பேசுங்கள்", link: "அம்சங்கள்" },
    ],
  },
  te: {
    promise: ["మీ వ్యాపారం గురించి చెప్పండి.", "మేము దాన్ని తయారు చేసి, పరీక్షించి, సురక్షితంగా ఉంచుతాం."],
    templates: "డిజైన్లు",
    features: "ప్రత్యేకతలు",
    contact: "సంప్రదించండి",
    myDashboard: "నా డాష్‌బోర్డ్",
    speak: "మాట్లాడండి",
    importChats: "WhatsApp లేదా Instagram చాట్‌లు తీసుకురండి",
    tryLabel: "ప్రయత్నించండి:",
    examples: [
      { label: "పుణెలో ఇంటి బేకరీ", text: "నేను పుణెలో ఇంట్లోనే కేకులు చేస్తాను. చాక్లెట్ కేక్ కిలో ₹900, కప్‌కేక్ ఒక్కటి ₹60. వారాంతాల్లో కోత్రుడ్‌లో డెలివరీ." },
      { label: "ట్యూషన్ క్లాసులు", text: "ఇండోర్‌లో 6 నుండి 10వ తరగతి వరకు లెక్కలు, సైన్స్ చెబుతాను. ఒక సబ్జెక్టుకు నెలకు ₹1500, 8 మంది బ్యాచ్, సాయంత్రం 5 నుండి 8." },
      { label: "ఫోటో స్టూడియో", text: "లక్నోలో ఫోటో స్టూడియో. పాస్‌పోర్ట్ ఫోటో ₹100, పుట్టినరోజు షూట్ ₹4000, పెళ్లి ప్యాకేజీ ₹35000 నుండి. ఉదయం 10 నుండి రాత్రి 8." },
      { label: "చేతితో చేసిన మట్టి పాత్రలు", text: "జైపూర్‌లో చేతితో మట్టి పాత్రలు చేస్తాను. మగ్ ₹450, సర్వింగ్ బౌల్ ₹900, పూలకుండీ ₹650. భారతదేశమంతా పంపిస్తాను." },
    ],
    coachAsk: "మీ ఆలోచన గురించి ఖచ్చితంగా తెలియదా?",
    coachLink: "ఐడియా కోచ్‌తో మాట్లాడండి →",
    whatYouGet: "మీకు దొరికేది ఇది ↓",
    badge: "{total} AI కస్టమర్లలో {passed} మందికి కావలసింది దొరికింది",
    openSite: "ఈ వెబ్‌సైట్ తెరవండి →",
    include: "ఏమేమి రాయాలి",
    design: "డిజైన్: {name}",
    removeDesign: "ఏజెంట్ ఎంచుకోనివ్వండి",
    strip: [
      { big: "7 డిజైన్లు", small: "మీ వ్యాపారానికి తగినవి", link: "డిజైన్లు చూడండి" },
      { big: "10 AI కస్టమర్లు", small: "ప్రారంభానికి ముందే పరీక్ష", link: "ఇది ఎలా పనిచేస్తుంది" },
      { big: "మోసం తనిఖీ", small: "ప్రతి కొలాబ్ ఆఫర్‌కు", link: "ప్రత్యేకతలు" },
      { big: "8 భాషలు", small: "రాయండి లేదా మాట్లాడండి", link: "ప్రత్యేకతలు" },
    ],
  },
  mr: {
    promise: ["तुमच्या व्यवसायाबद्दल सांगा.", "आम्ही वेबसाइट बनवतो, तपासतो आणि सुरक्षित ठेवतो."],
    templates: "डिझाइन",
    features: "वैशिष्ट्ये",
    contact: "संपर्क",
    myDashboard: "माझा डॅशबोर्ड",
    speak: "बोला",
    importChats: "WhatsApp किंवा Instagram चॅट आणा",
    tryLabel: "करून पहा:",
    examples: [
      { label: "पुण्यात घरची बेकरी", text: "मी पुण्यात घरी केक बनवते. चॉकलेट केक ₹900 किलो, कपकेक ₹60 एक. शनिवार-रविवार कोथरूडमध्ये डिलिव्हरी." },
      { label: "ट्यूशन क्लास", text: "मी इंदूरमध्ये 6वी ते 10वीला गणित आणि विज्ञान शिकवते. एका विषयाला ₹1500 महिना, 8 मुलांची बॅच, संध्याकाळी 5 ते 8." },
      { label: "फोटो स्टुडिओ", text: "लखनऊमध्ये फोटो स्टुडिओ. पासपोर्ट फोटो ₹100, वाढदिवस शूट ₹4000, लग्न पॅकेज ₹35000 पासून. सकाळी 10 ते रात्री 8." },
      { label: "हाताने बनवलेली मातीची भांडी", text: "मी जयपूरमध्ये हाताने मातीची भांडी बनवते. मग ₹450, वाडगा ₹900, कुंडी ₹650. संपूर्ण भारतात पाठवते." },
    ],
    coachAsk: "तुमच्या कल्पनेबद्दल खात्री नाही?",
    coachLink: "आयडिया कोचशी बोला →",
    whatYouGet: "तुम्हाला असं मिळेल ↓",
    badge: "{total} पैकी {passed} AI ग्राहकांना हवं ते मिळालं",
    openSite: "ही वेबसाइट उघडा →",
    include: "काय काय लिहावं",
    design: "डिझाइन: {name}",
    removeDesign: "एजंटला निवडू द्या",
    strip: [
      { big: "7 डिझाइन", small: "तुमच्या व्यवसायानुसार", link: "डिझाइन पहा" },
      { big: "10 AI ग्राहक", small: "सुरू करण्याआधी तपासणी", link: "हे कसं काम करतं" },
      { big: "फसवणूक तपासणी", small: "प्रत्येक कोलॅब ऑफरसाठी", link: "वैशिष्ट्ये" },
      { big: "8 भाषा", small: "लिहा किंवा बोला", link: "वैशिष्ट्ये" },
    ],
  },
  bn: {
    promise: ["আপনার ব্যবসার কথা বলুন।", "আমরা বানাই, পরীক্ষা করি আর সুরক্ষিত রাখি।"],
    templates: "ডিজাইন",
    features: "বৈশিষ্ট্য",
    contact: "যোগাযোগ",
    myDashboard: "আমার ড্যাশবোর্ড",
    speak: "বলুন",
    importChats: "WhatsApp বা Instagram চ্যাট আনুন",
    tryLabel: "চেষ্টা করুন:",
    examples: [
      { label: "পুনেতে ঘরোয়া বেকারি", text: "আমি পুনেতে বাড়িতে কেক বানাই। চকলেট কেক কেজি ₹900, কাপকেক একটা ₹60। সপ্তাহান্তে কোথরুডে ডেলিভারি।" },
      { label: "টিউশন ক্লাস", text: "আমি ইন্দোরে ক্লাস 6 থেকে 10 পর্যন্ত অঙ্ক আর বিজ্ঞান পড়াই। এক বিষয় মাসে ₹1500, 8 জনের ব্যাচ, সন্ধ্যা 5টা থেকে 8টা।" },
      { label: "ফটো স্টুডিও", text: "লখনউতে ফটো স্টুডিও। পাসপোর্ট ছবি ₹100, জন্মদিনের শুট ₹4000, বিয়ের প্যাকেজ ₹35000 থেকে। সকাল 10টা থেকে রাত 8টা।" },
      { label: "হাতে তৈরি মাটির জিনিস", text: "আমি জয়পুরে হাতে মাটির জিনিস বানাই। মগ ₹450, সার্ভিং বাটি ₹900, টব ₹650। সারা ভারতে পাঠাই।" },
    ],
    coachAsk: "আপনার আইডিয়া নিয়ে নিশ্চিত নন?",
    coachLink: "আইডিয়া কোচের সঙ্গে কথা বলুন →",
    whatYouGet: "আপনি এমনটা পাবেন ↓",
    badge: "{total} জন AI ক্রেতার মধ্যে {passed} জন যা চেয়েছিলেন পেয়েছেন",
    openSite: "এই ওয়েবসাইট খুলুন →",
    include: "কী কী লিখবেন",
    design: "ডিজাইন: {name}",
    removeDesign: "এজেন্টকে বেছে নিতে দিন",
    strip: [
      { big: "7টি ডিজাইন", small: "আপনার ব্যবসা অনুযায়ী", link: "ডিজাইন দেখুন" },
      { big: "10 জন AI ক্রেতা", small: "চালুর আগেই পরীক্ষা", link: "কীভাবে কাজ করে" },
      { big: "প্রতারণা যাচাই", small: "প্রতিটি কোলাব অফারের জন্য", link: "বৈশিষ্ট্য" },
      { big: "8টি ভাষা", small: "লিখুন বা বলুন", link: "বৈশিষ্ট্য" },
    ],
  },
  gu: {
    promise: ["તમારા વેપાર વિશે કહો.", "અમે બનાવીએ, ચકાસીએ અને સુરક્ષિત રાખીએ."],
    templates: "ડિઝાઇન",
    features: "ખાસિયતો",
    contact: "સંપર્ક",
    myDashboard: "મારું ડેશબોર્ડ",
    speak: "બોલો",
    importChats: "WhatsApp અથવા Instagram ચેટ લાવો",
    tryLabel: "અજમાવો:",
    examples: [
      { label: "પુણેમાં ઘરની બેકરી", text: "હું પુણેમાં ઘરે કેક બનાવું છું. ચોકલેટ કેક ₹900 કિલો, કપકેક ₹60 એક. શનિ-રવિ કોથરૂડમાં ડિલિવરી." },
      { label: "ટ્યુશન ક્લાસ", text: "હું ઇન્દોરમાં ધોરણ 6 થી 10 સુધી ગણિત અને વિજ્ઞાન ભણાવું છું. એક વિષયના ₹1500 મહિને, 8 બાળકોની બેચ, સાંજે 5 થી 8." },
      { label: "ફોટો સ્ટુડિયો", text: "લખનઉમાં ફોટો સ્ટુડિયો. પાસપોર્ટ ફોટો ₹100, જન્મદિવસ શૂટ ₹4000, લગ્ન પેકેજ ₹35000 થી. સવારે 10 થી રાત્રે 8." },
      { label: "હાથે બનાવેલાં માટીનાં વાસણ", text: "હું જયપુરમાં હાથે માટીનાં વાસણ બનાવું છું. મગ ₹450, વાટકો ₹900, કૂંડું ₹650. આખા ભારતમાં મોકલું છું." },
    ],
    coachAsk: "તમારા આઇડિયા વિશે ખાતરી નથી?",
    coachLink: "આઇડિયા કોચ સાથે વાત કરો →",
    whatYouGet: "તમને આવું મળશે ↓",
    badge: "{total} માંથી {passed} AI ગ્રાહકોને જોઈતું મળ્યું",
    openSite: "આ વેબસાઇટ ખોલો →",
    include: "શું શું લખવું",
    design: "ડિઝાઇન: {name}",
    removeDesign: "એજન્ટને પસંદ કરવા દો",
    strip: [
      { big: "7 ડિઝાઇન", small: "તમારા વેપાર મુજબ", link: "ડિઝાઇન જુઓ" },
      { big: "10 AI ગ્રાહકો", small: "શરૂ કરતાં પહેલાં ચકાસણી", link: "આ કેવી રીતે કામ કરે છે" },
      { big: "છેતરપિંડી તપાસ", small: "દરેક કોલૅબ ઑફર માટે", link: "ખાસિયતો" },
      { big: "8 ભાષાઓ", small: "લખો અથવા બોલો", link: "ખાસિયતો" },
    ],
  },
};

/** Where each card in the links strip goes. */
export const STRIP_LINKS = ["/templates", "/guide", "/features#collab", "/features#languages"];

/** The seven site designs, as the agent names them. */
export const DESIGNS = ["classic", "boutique", "mart", "split", "bento", "magazine", "noir"] as const;
