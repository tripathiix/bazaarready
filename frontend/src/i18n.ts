// Every word on the owner's screen, in each supported language and its own script.
// To add a language: add its code here, in Languages.java and in ShopLabels.java.

export type LanguageCode = "en" | "hi" | "kn" | "ta" | "te" | "mr" | "bn" | "gu";

export interface Strings {
  name: string; // the language's own name, shown in the picker
  speech: string; // code for the browser's speech recognition
  eyebrow: string;
  language: string;
  tellUs: string;
  hint: string;
  example: string;
  speak: string;
  stopListening: string;
  listening: string;
  speechUnsupported: string;
  useExample: string;
  build: string;
  working: string;
  activity: string;
  sendAnswer: string;
  approveQuestion: string;
  lookPreview: string;
  publish: string;
  notYet: string;
  openSite: string;
  openPreview: string;
  startAnother: string;
  logStart: string;
  logAsked: string;
  logAnswered: string;
  logSaved: string; // {items} and {faqs} are replaced with numbers
  logReady: string;
  logPublished: string;
  logDeclined: string;
  logRetry: string;
  logStopped: string;
}

export const STRINGS: Record<LanguageCode, Strings> = {
  en: {
    name: "English",
    speech: "en-IN",
    eyebrow: "Launch agent for small businesses and creators",
    language: "Language",
    tellUs: "Tell us about your business",
    hint: "Type or speak. Say what you do or sell, your prices if you have them, where you work, and your WhatsApp number.",
    example:
      "I teach maths at home for classes 8 to 10 in Indiranagar, Bengaluru. Group class is 2000 rupees a month, one-to-one is 500 an hour. Evenings 5 to 8, Monday to Saturday. WhatsApp 9876543210.",
    speak: "Speak instead",
    stopListening: "Stop listening",
    listening: "Listening. Your words will appear in the box above.",
    speechUnsupported: "Speaking works in Chrome and Edge.",
    useExample: "Use the example",
    build: "Build my website",
    working: "Working…",
    activity: "What the agent has done",
    sendAnswer: "Send answer",
    approveQuestion: "Publish your website so people can open it?",
    lookPreview: "Look at the preview first",
    publish: "Publish",
    notYet: "Not yet",
    openSite: "Open your website",
    openPreview: "Open the preview",
    startAnother: "Start another business",
    logStart: "Read your description",
    logAsked: "Asked you",
    logAnswered: "You answered",
    logSaved: "Saved your details: {items} offerings, {faqs} questions",
    logReady: "Website is ready. Waiting for your approval to publish",
    logPublished: "Published your website",
    logDeclined: "You chose not to publish yet",
    logRetry: "Some details needed correcting; fixing them",
    logStopped: "Stopped because of a problem",
  },
  hi: {
    name: "हिन्दी",
    speech: "hi-IN",
    eyebrow: "छोटे कारोबार और क्रिएटर्स के लिए लॉन्च एजेंट",
    language: "भाषा",
    tellUs: "अपने काम के बारे में बताइए",
    hint: "लिखिए या बोलिए। बताइए आप क्या करते या बेचते हैं, दाम अगर हों, कहाँ काम करते हैं, और अपना WhatsApp नंबर।",
    example:
      "मैं घर से केक बनाती हूँ, बेंगलुरु में। चॉकलेट केक 850 रुपये का 1 किलो, एगलेस 900। कपकेक 40 रुपये का एक, कम से कम 12। इंदिरानगर और कोरमंगला में फ्री डिलीवरी, व्हाइटफील्ड 60 रुपये अलग। 2 दिन पहले ऑर्डर चाहिए। WhatsApp 9876543210।",
    speak: "बोलकर बताइए",
    stopListening: "सुनना बंद करें",
    listening: "सुन रहे हैं। आपके शब्द ऊपर बॉक्स में दिखेंगे।",
    speechUnsupported: "बोलने की सुविधा Chrome और Edge में चलती है।",
    useExample: "उदाहरण भरें",
    build: "मेरी वेबसाइट बनाइए",
    working: "काम चल रहा है…",
    activity: "एजेंट ने अब तक क्या किया",
    sendAnswer: "जवाब भेजें",
    approveQuestion: "क्या वेबसाइट पब्लिश कर दें ताकि लोग इसे खोल सकें?",
    lookPreview: "पहले प्रीव्यू देखें",
    publish: "पब्लिश करें",
    notYet: "अभी नहीं",
    openSite: "अपनी वेबसाइट खोलें",
    openPreview: "प्रीव्यू खोलें",
    startAnother: "दूसरा कारोबार शुरू करें",
    logStart: "आपका विवरण पढ़ा",
    logAsked: "आपसे पूछा",
    logAnswered: "आपने बताया",
    logSaved: "आपकी जानकारी सेव की: {items} चीज़ें, {faqs} सवाल",
    logReady: "वेबसाइट तैयार है। पब्लिश करने के लिए आपकी मंज़ूरी का इंतज़ार",
    logPublished: "आपकी वेबसाइट पब्लिश कर दी",
    logDeclined: "आपने अभी पब्लिश नहीं करना चुना",
    logRetry: "कुछ जानकारी ठीक करनी पड़ी; ठीक कर रहे हैं",
    logStopped: "एक दिक्कत की वजह से रुक गया",
  },
  kn: {
    name: "ಕನ್ನಡ",
    speech: "kn-IN",
    eyebrow: "ಸಣ್ಣ ವ್ಯಾಪಾರ ಮತ್ತು ಕ್ರಿಯೇಟರ್‌ಗಳಿಗಾಗಿ ಲಾಂಚ್ ಏಜೆಂಟ್",
    language: "ಭಾಷೆ",
    tellUs: "ನಿಮ್ಮ ವ್ಯಾಪಾರದ ಬಗ್ಗೆ ಹೇಳಿ",
    hint: "ಟೈಪ್ ಮಾಡಿ ಅಥವಾ ಮಾತನಾಡಿ. ನೀವು ಏನು ಮಾಡುತ್ತೀರಿ ಅಥವಾ ಮಾರುತ್ತೀರಿ, ಬೆಲೆಗಳು ಇದ್ದರೆ, ಎಲ್ಲಿ ಕೆಲಸ ಮಾಡುತ್ತೀರಿ ಮತ್ತು ನಿಮ್ಮ WhatsApp ಸಂಖ್ಯೆ ತಿಳಿಸಿ.",
    example:
      "ನಾನು ಮನೆಯಿಂದ ಕೇಕ್ ಮಾಡುತ್ತೇನೆ, ಬೆಂಗಳೂರಿನಲ್ಲಿ. ಚಾಕೊಲೇಟ್ ಕೇಕ್ 1 ಕೆಜಿಗೆ 850 ರೂಪಾಯಿ, ಎಗ್‌ಲೆಸ್ 900. ಇಂದಿರಾನಗರ ಮತ್ತು ಕೋರಮಂಗಲಕ್ಕೆ ಉಚಿತ ಡೆಲಿವರಿ. 2 ದಿನ ಮುಂಚಿತವಾಗಿ ಆರ್ಡರ್ ಬೇಕು. WhatsApp 9876543210.",
    speak: "ಮಾತನಾಡಿ ಹೇಳಿ",
    stopListening: "ಕೇಳುವುದನ್ನು ನಿಲ್ಲಿಸಿ",
    listening: "ಕೇಳುತ್ತಿದ್ದೇವೆ. ನಿಮ್ಮ ಮಾತು ಮೇಲಿನ ಬಾಕ್ಸ್‌ನಲ್ಲಿ ಕಾಣಿಸುತ್ತದೆ.",
    speechUnsupported: "ಮಾತನಾಡುವ ಸೌಲಭ್ಯ Chrome ಮತ್ತು Edge ನಲ್ಲಿ ಕೆಲಸ ಮಾಡುತ್ತದೆ.",
    useExample: "ಉದಾಹರಣೆ ಬಳಸಿ",
    build: "ನನ್ನ ವೆಬ್‌ಸೈಟ್ ಮಾಡಿ",
    working: "ಕೆಲಸ ನಡೆಯುತ್ತಿದೆ…",
    activity: "ಏಜೆಂಟ್ ಇದುವರೆಗೆ ಮಾಡಿದ್ದು",
    sendAnswer: "ಉತ್ತರ ಕಳುಹಿಸಿ",
    approveQuestion: "ಜನರು ತೆರೆಯಲು ನಿಮ್ಮ ವೆಬ್‌ಸೈಟ್ ಪ್ರಕಟಿಸಬೇಕೇ?",
    lookPreview: "ಮೊದಲು ಪ್ರಿವ್ಯೂ ನೋಡಿ",
    publish: "ಪ್ರಕಟಿಸಿ",
    notYet: "ಈಗ ಬೇಡ",
    openSite: "ನಿಮ್ಮ ವೆಬ್‌ಸೈಟ್ ತೆರೆಯಿರಿ",
    openPreview: "ಪ್ರಿವ್ಯೂ ತೆರೆಯಿರಿ",
    startAnother: "ಇನ್ನೊಂದು ವ್ಯಾಪಾರ ಆರಂಭಿಸಿ",
    logStart: "ನಿಮ್ಮ ವಿವರಣೆ ಓದಲಾಗಿದೆ",
    logAsked: "ನಿಮ್ಮನ್ನು ಕೇಳಿದ್ದು",
    logAnswered: "ನೀವು ಹೇಳಿದ್ದು",
    logSaved: "ನಿಮ್ಮ ವಿವರಗಳನ್ನು ಉಳಿಸಲಾಗಿದೆ: {items} ವಸ್ತುಗಳು, {faqs} ಪ್ರಶ್ನೆಗಳು",
    logReady: "ವೆಬ್‌ಸೈಟ್ ಸಿದ್ಧವಾಗಿದೆ. ಪ್ರಕಟಿಸಲು ನಿಮ್ಮ ಒಪ್ಪಿಗೆಗಾಗಿ ಕಾಯುತ್ತಿದೆ",
    logPublished: "ನಿಮ್ಮ ವೆಬ್‌ಸೈಟ್ ಪ್ರಕಟಿಸಲಾಗಿದೆ",
    logDeclined: "ನೀವು ಈಗ ಪ್ರಕಟಿಸದಿರಲು ಆಯ್ಕೆ ಮಾಡಿದ್ದೀರಿ",
    logRetry: "ಕೆಲವು ವಿವರಗಳನ್ನು ಸರಿಪಡಿಸಬೇಕಾಯಿತು; ಸರಿಪಡಿಸುತ್ತಿದ್ದೇವೆ",
    logStopped: "ಒಂದು ತೊಂದರೆಯಿಂದ ನಿಂತಿದೆ",
  },
  ta: {
    name: "தமிழ்",
    speech: "ta-IN",
    eyebrow: "சிறு தொழில்கள் மற்றும் கிரியேட்டர்களுக்கான லாஞ்ச் ஏஜென்ட்",
    language: "மொழி",
    tellUs: "உங்கள் தொழிலைப் பற்றி சொல்லுங்கள்",
    hint: "தட்டச்சு செய்யுங்கள் அல்லது பேசுங்கள். நீங்கள் என்ன செய்கிறீர்கள் அல்லது விற்கிறீர்கள், விலைகள் இருந்தால், எங்கு வேலை செய்கிறீர்கள், உங்கள் WhatsApp எண் ஆகியவற்றைச் சொல்லுங்கள்.",
    example:
      "நான் வீட்டிலிருந்து கேக் செய்கிறேன், சென்னையில். சாக்லேட் கேக் 1 கிலோ 850 ரூபாய், எக்லெஸ் 900. அடையார் மற்றும் மயிலாப்பூருக்கு இலவச டெலிவரி. 2 நாட்களுக்கு முன் ஆர்டர் வேண்டும். WhatsApp 9876543210.",
    speak: "பேசிச் சொல்லுங்கள்",
    stopListening: "கேட்பதை நிறுத்து",
    listening: "கேட்டுக்கொண்டிருக்கிறோம். உங்கள் வார்த்தைகள் மேலே உள்ள பெட்டியில் தோன்றும்.",
    speechUnsupported: "பேசும் வசதி Chrome மற்றும் Edge-இல் வேலை செய்யும்.",
    useExample: "உதாரணத்தைப் பயன்படுத்து",
    build: "என் இணையதளத்தை உருவாக்கு",
    working: "வேலை நடக்கிறது…",
    activity: "ஏஜென்ட் இதுவரை செய்தவை",
    sendAnswer: "பதிலை அனுப்பு",
    approveQuestion: "மக்கள் திறக்க உங்கள் இணையதளத்தை வெளியிடலாமா?",
    lookPreview: "முதலில் முன்னோட்டம் பாருங்கள்",
    publish: "வெளியிடு",
    notYet: "இப்போது வேண்டாம்",
    openSite: "உங்கள் இணையதளத்தைத் திற",
    openPreview: "முன்னோட்டத்தைத் திற",
    startAnother: "இன்னொரு தொழிலைத் தொடங்கு",
    logStart: "உங்கள் விவரம் படிக்கப்பட்டது",
    logAsked: "உங்களிடம் கேட்டது",
    logAnswered: "நீங்கள் சொன்னது",
    logSaved: "உங்கள் விவரங்கள் சேமிக்கப்பட்டன: {items} பொருட்கள், {faqs} கேள்விகள்",
    logReady: "இணையதளம் தயார். வெளியிட உங்கள் ஒப்புதலுக்காகக் காத்திருக்கிறது",
    logPublished: "உங்கள் இணையதளம் வெளியிடப்பட்டது",
    logDeclined: "இப்போது வெளியிட வேண்டாம் என்று தேர்ந்தெடுத்தீர்கள்",
    logRetry: "சில விவரங்களைச் சரிசெய்ய வேண்டியிருந்தது; சரிசெய்கிறோம்",
    logStopped: "ஒரு சிக்கலால் நின்றது",
  },
  te: {
    name: "తెలుగు",
    speech: "te-IN",
    eyebrow: "చిన్న వ్యాపారాలు మరియు క్రియేటర్ల కోసం లాంచ్ ఏజెంట్",
    language: "భాష",
    tellUs: "మీ వ్యాపారం గురించి చెప్పండి",
    hint: "టైప్ చేయండి లేదా మాట్లాడండి. మీరు ఏమి చేస్తారు లేదా అమ్ముతారు, ధరలు ఉంటే, ఎక్కడ పని చేస్తారు, మీ WhatsApp నంబర్ చెప్పండి.",
    example:
      "నేను ఇంటి నుండి కేకులు చేస్తాను, హైదరాబాద్‌లో. చాక్లెట్ కేక్ 1 కిలో 850 రూపాయలు, ఎగ్‌లెస్ 900. మాదాపూర్ మరియు గచ్చిబౌలికి ఉచిత డెలివరీ. 2 రోజుల ముందు ఆర్డర్ కావాలి. WhatsApp 9876543210.",
    speak: "మాట్లాడి చెప్పండి",
    stopListening: "వినడం ఆపు",
    listening: "వింటున్నాము. మీ మాటలు పై బాక్స్‌లో కనిపిస్తాయి.",
    speechUnsupported: "మాట్లాడే సౌకర్యం Chrome మరియు Edge లో పని చేస్తుంది.",
    useExample: "ఉదాహరణ వాడండి",
    build: "నా వెబ్‌సైట్ తయారు చేయండి",
    working: "పని జరుగుతోంది…",
    activity: "ఏజెంట్ ఇప్పటివరకు చేసినవి",
    sendAnswer: "జవాబు పంపండి",
    approveQuestion: "ప్రజలు తెరవడానికి మీ వెబ్‌సైట్‌ను ప్రచురించాలా?",
    lookPreview: "ముందు ప్రివ్యూ చూడండి",
    publish: "ప్రచురించు",
    notYet: "ఇప్పుడు వద్దు",
    openSite: "మీ వెబ్‌సైట్ తెరవండి",
    openPreview: "ప్రివ్యూ తెరవండి",
    startAnother: "మరో వ్యాపారం మొదలుపెట్టండి",
    logStart: "మీ వివరణ చదివాము",
    logAsked: "మిమ్మల్ని అడిగింది",
    logAnswered: "మీరు చెప్పింది",
    logSaved: "మీ వివరాలు సేవ్ చేశాము: {items} వస్తువులు, {faqs} ప్రశ్నలు",
    logReady: "వెబ్‌సైట్ సిద్ధంగా ఉంది. ప్రచురించడానికి మీ అనుమతి కోసం వేచి ఉంది",
    logPublished: "మీ వెబ్‌సైట్ ప్రచురించబడింది",
    logDeclined: "మీరు ఇప్పుడు ప్రచురించవద్దని ఎంచుకున్నారు",
    logRetry: "కొన్ని వివరాలు సరిచేయాల్సి వచ్చింది; సరిచేస్తున్నాము",
    logStopped: "ఒక సమస్య వల్ల ఆగిపోయింది",
  },
  mr: {
    name: "मराठी",
    speech: "mr-IN",
    eyebrow: "छोट्या व्यवसायांसाठी आणि क्रिएटर्ससाठी लाँच एजंट",
    language: "भाषा",
    tellUs: "तुमच्या व्यवसायाबद्दल सांगा",
    hint: "लिहा किंवा बोला. तुम्ही काय करता किंवा विकता, किंमती असल्यास, कुठे काम करता आणि तुमचा WhatsApp नंबर सांगा.",
    example:
      "मी घरून केक बनवते, पुण्यात. चॉकलेट केक 1 किलो 850 रुपये, एगलेस 900. कोथरूड आणि बाणेरला मोफत डिलिव्हरी. 2 दिवस आधी ऑर्डर हवी. WhatsApp 9876543210.",
    speak: "बोलून सांगा",
    stopListening: "ऐकणे थांबवा",
    listening: "ऐकत आहोत. तुमचे शब्द वरच्या बॉक्समध्ये दिसतील.",
    speechUnsupported: "बोलण्याची सोय Chrome आणि Edge मध्ये चालते.",
    useExample: "उदाहरण वापरा",
    build: "माझी वेबसाइट बनवा",
    working: "काम सुरू आहे…",
    activity: "एजंटने आतापर्यंत काय केले",
    sendAnswer: "उत्तर पाठवा",
    approveQuestion: "लोकांना उघडता यावी म्हणून वेबसाइट प्रकाशित करायची का?",
    lookPreview: "आधी प्रिव्ह्यू पहा",
    publish: "प्रकाशित करा",
    notYet: "आत्ता नको",
    openSite: "तुमची वेबसाइट उघडा",
    openPreview: "प्रिव्ह्यू उघडा",
    startAnother: "दुसरा व्यवसाय सुरू करा",
    logStart: "तुमचे वर्णन वाचले",
    logAsked: "तुम्हाला विचारले",
    logAnswered: "तुम्ही सांगितले",
    logSaved: "तुमची माहिती सेव्ह केली: {items} वस्तू, {faqs} प्रश्न",
    logReady: "वेबसाइट तयार आहे. प्रकाशित करण्यासाठी तुमच्या मंजुरीची वाट पाहत आहे",
    logPublished: "तुमची वेबसाइट प्रकाशित केली",
    logDeclined: "तुम्ही आत्ता प्रकाशित न करण्याचे निवडले",
    logRetry: "काही माहिती दुरुस्त करावी लागली; दुरुस्त करत आहोत",
    logStopped: "एका अडचणीमुळे थांबले",
  },
  bn: {
    name: "বাংলা",
    speech: "bn-IN",
    eyebrow: "ছোট ব্যবসা ও ক্রিয়েটরদের জন্য লঞ্চ এজেন্ট",
    language: "ভাষা",
    tellUs: "আপনার ব্যবসার কথা বলুন",
    hint: "লিখুন বা বলুন। আপনি কী করেন বা বিক্রি করেন, দাম থাকলে, কোথায় কাজ করেন এবং আপনার WhatsApp নম্বর জানান।",
    example:
      "আমি বাড়ি থেকে কেক বানাই, কলকাতায়। চকোলেট কেক ১ কেজি ৮৫০ টাকা, এগলেস ৯০০। সল্টলেক ও নিউটাউনে ফ্রি ডেলিভারি। ২ দিন আগে অর্ডার চাই। WhatsApp 9876543210।",
    speak: "বলে জানান",
    stopListening: "শোনা বন্ধ করুন",
    listening: "শুনছি। আপনার কথা উপরের বক্সে দেখা যাবে।",
    speechUnsupported: "কথা বলার সুবিধা Chrome ও Edge-এ কাজ করে।",
    useExample: "উদাহরণ ব্যবহার করুন",
    build: "আমার ওয়েবসাইট বানান",
    working: "কাজ চলছে…",
    activity: "এজেন্ট এ পর্যন্ত যা করেছে",
    sendAnswer: "উত্তর পাঠান",
    approveQuestion: "লোকে যাতে খুলতে পারে, ওয়েবসাইট প্রকাশ করব?",
    lookPreview: "আগে প্রিভিউ দেখুন",
    publish: "প্রকাশ করুন",
    notYet: "এখন নয়",
    openSite: "আপনার ওয়েবসাইট খুলুন",
    openPreview: "প্রিভিউ খুলুন",
    startAnother: "আরেকটি ব্যবসা শুরু করুন",
    logStart: "আপনার বিবরণ পড়া হয়েছে",
    logAsked: "আপনাকে জিজ্ঞাসা করা হয়েছে",
    logAnswered: "আপনি বলেছেন",
    logSaved: "আপনার তথ্য সেভ হয়েছে: {items}টি জিনিস, {faqs}টি প্রশ্ন",
    logReady: "ওয়েবসাইট তৈরি। প্রকাশের জন্য আপনার অনুমতির অপেক্ষায়",
    logPublished: "আপনার ওয়েবসাইট প্রকাশ করা হয়েছে",
    logDeclined: "আপনি এখন প্রকাশ না করা বেছে নিয়েছেন",
    logRetry: "কিছু তথ্য ঠিক করতে হয়েছে; ঠিক করা হচ্ছে",
    logStopped: "একটি সমস্যার কারণে থেমে গেছে",
  },
  gu: {
    name: "ગુજરાતી",
    speech: "gu-IN",
    eyebrow: "નાના વેપાર અને ક્રિએટર્સ માટે લોન્ચ એજન્ટ",
    language: "ભાષા",
    tellUs: "તમારા વેપાર વિશે જણાવો",
    hint: "લખો અથવા બોલો. તમે શું કરો છો કે વેચો છો, ભાવ હોય તો, ક્યાં કામ કરો છો અને તમારો WhatsApp નંબર જણાવો.",
    example:
      "હું ઘરેથી કેક બનાવું છું, અમદાવાદમાં. ચોકલેટ કેક 1 કિલો 850 રૂપિયા, એગલેસ 900. સેટેલાઇટ અને બોપલમાં ફ્રી ડિલિવરી. 2 દિવસ પહેલાં ઓર્ડર જોઈએ. WhatsApp 9876543210.",
    speak: "બોલીને જણાવો",
    stopListening: "સાંભળવાનું બંધ કરો",
    listening: "સાંભળીએ છીએ. તમારા શબ્દો ઉપરના બોક્સમાં દેખાશે.",
    speechUnsupported: "બોલવાની સુવિધા Chrome અને Edge માં ચાલે છે.",
    useExample: "ઉદાહરણ વાપરો",
    build: "મારી વેબસાઇટ બનાવો",
    working: "કામ ચાલુ છે…",
    activity: "એજન્ટે અત્યાર સુધી શું કર્યું",
    sendAnswer: "જવાબ મોકલો",
    approveQuestion: "લોકો ખોલી શકે તે માટે વેબસાઇટ પ્રકાશિત કરીએ?",
    lookPreview: "પહેલાં પ્રિવ્યૂ જુઓ",
    publish: "પ્રકાશિત કરો",
    notYet: "હમણાં નહીં",
    openSite: "તમારી વેબસાઇટ ખોલો",
    openPreview: "પ્રિવ્યૂ ખોલો",
    startAnother: "બીજો વેપાર શરૂ કરો",
    logStart: "તમારું વર્ણન વાંચ્યું",
    logAsked: "તમને પૂછ્યું",
    logAnswered: "તમે કહ્યું",
    logSaved: "તમારી માહિતી સેવ કરી: {items} વસ્તુઓ, {faqs} પ્રશ્નો",
    logReady: "વેબસાઇટ તૈયાર છે. પ્રકાશિત કરવા તમારી મંજૂરીની રાહ",
    logPublished: "તમારી વેબસાઇટ પ્રકાશિત થઈ",
    logDeclined: "તમે હમણાં પ્રકાશિત ન કરવાનું પસંદ કર્યું",
    logRetry: "કેટલીક માહિતી સુધારવી પડી; સુધારીએ છીએ",
    logStopped: "એક સમસ્યાને કારણે અટકી ગયું",
  },
};

const STORAGE_KEY = "bazaarready.language";

/** The saved choice, else the browser's language if we support it, else English. */
export function initialLanguage(): LanguageCode {
  try {
    const saved = window.localStorage.getItem(STORAGE_KEY);
    if (saved && saved in STRINGS) return saved as LanguageCode;
  } catch {
    // storage may be blocked; fall through
  }
  for (const preferred of navigator.languages ?? [navigator.language]) {
    const code = preferred.slice(0, 2).toLowerCase();
    if (code in STRINGS) return code as LanguageCode;
  }
  return "en";
}

export function rememberLanguage(code: LanguageCode) {
  try {
    window.localStorage.setItem(STORAGE_KEY, code);
  } catch {
    // not being able to remember the choice is fine
  }
}
