package com.bazaarready.shop;

import java.util.Map;

/**
 * The fixed words on a site page, in each supported language and its own script. The business's
 * own content (name, offerings, answers, section headings) is written by the agent in the same language.
 */
record ShopLabels(String offerings, String about, String details, String orderAhead, String hours,
                  String location, String faq, String contact, String contactButton, String orderMessage) {

    private static final Map<String, ShopLabels> BY_LANGUAGE = Map.of(
            "en", new ShopLabels("What we offer", "About us", "Good to know", "Please book %d days ahead.",
                    "Timings", "Where we are", "Common questions", "Get in touch", "Message on WhatsApp",
                    "Hello! I found %s online and would like to know more."),
            "hi", new ShopLabels("हम क्या देते हैं", "हमारे बारे में", "ज़रूरी जानकारी", "कृपया %d दिन पहले बताएं।",
                    "समय", "पता", "अक्सर पूछे जाने वाले सवाल", "संपर्क करें", "WhatsApp पर मैसेज करें",
                    "नमस्ते! मैंने %s को ऑनलाइन देखा और जानकारी चाहिए।"),
            "hinglish", new ShopLabels("Hum kya dete hain", "Hamare baare mein", "Zaroori jaankari",
                    "%d din pehle bata dein.", "Samay", "Pata", "Aksar pooche jaane wale sawaal",
                    "Sampark karein", "WhatsApp par message karein",
                    "Namaste! Maine %s ko online dekha, jaankari chahiye."),
            "kn", new ShopLabels("ನಾವು ಏನು ನೀಡುತ್ತೇವೆ", "ನಮ್ಮ ಬಗ್ಗೆ", "ತಿಳಿಯಬೇಕಾದದ್ದು",
                    "ದಯವಿಟ್ಟು %d ದಿನ ಮುಂಚಿತವಾಗಿ ತಿಳಿಸಿ.", "ಸಮಯ", "ವಿಳಾಸ", "ಸಾಮಾನ್ಯ ಪ್ರಶ್ನೆಗಳು",
                    "ಸಂಪರ್ಕಿಸಿ", "WhatsApp ನಲ್ಲಿ ಸಂದೇಶ ಕಳುಹಿಸಿ",
                    "ನಮಸ್ಕಾರ! ನಾನು %s ಅನ್ನು ಆನ್‌ಲೈನ್‌ನಲ್ಲಿ ನೋಡಿದೆ, ಹೆಚ್ಚಿನ ಮಾಹಿತಿ ಬೇಕು."),
            "ta", new ShopLabels("நாங்கள் வழங்குவது", "எங்களைப் பற்றி", "தெரிந்துகொள்ள வேண்டியவை",
                    "தயவுசெய்து %d நாட்களுக்கு முன் தெரிவியுங்கள்.", "நேரம்", "முகவரி",
                    "அடிக்கடி கேட்கப்படும் கேள்விகள்", "தொடர்பு கொள்ளுங்கள்", "WhatsApp-இல் செய்தி அனுப்புங்கள்",
                    "வணக்கம்! %s பற்றி ஆன்லைனில் பார்த்தேன், மேலும் தகவல் வேண்டும்."),
            "te", new ShopLabels("మేము అందించేవి", "మా గురించి", "తెలుసుకోవలసినవి",
                    "దయచేసి %d రోజుల ముందు తెలియజేయండి.", "సమయం", "చిరునామా", "తరచుగా అడిగే ప్రశ్నలు",
                    "సంప్రదించండి", "WhatsApp లో సందేశం పంపండి",
                    "నమస్కారం! నేను %s ను ఆన్‌లైన్‌లో చూశాను, మరిన్ని వివరాలు కావాలి."),
            "mr", new ShopLabels("आम्ही काय देतो", "आमच्याबद्दल", "महत्त्वाची माहिती", "कृपया %d दिवस आधी कळवा.",
                    "वेळ", "पत्ता", "नेहमी विचारले जाणारे प्रश्न", "संपर्क करा", "WhatsApp वर मेसेज करा",
                    "नमस्कार! मी %s ऑनलाइन पाहिले, अधिक माहिती हवी आहे."),
            "bn", new ShopLabels("আমরা যা দিই", "আমাদের সম্পর্কে", "জেনে রাখুন", "অনুগ্রহ করে %d দিন আগে জানান।",
                    "সময়", "ঠিকানা", "সাধারণ প্রশ্ন", "যোগাযোগ করুন", "WhatsApp-এ মেসেজ করুন",
                    "নমস্কার! আমি %s অনলাইনে দেখেছি, আরও জানতে চাই।"),
            "gu", new ShopLabels("અમે શું આપીએ છીએ", "અમારા વિશે", "જાણવા જેવું", "કૃપા કરીને %d દિવસ પહેલાં જણાવો.",
                    "સમય", "સરનામું", "વારંવાર પૂછાતા પ્રશ્નો", "સંપર્ક કરો", "WhatsApp પર મેસેજ કરો",
                    "નમસ્તે! મેં %s ઓનલાઇન જોયું, વધુ માહિતી જોઈએ છે."));

    static ShopLabels forLanguage(String code) {
        return BY_LANGUAGE.getOrDefault(code, BY_LANGUAGE.get("en"));
    }

    public String orderAheadText(int days) {
        return String.format(orderAhead, days);
    }
}
