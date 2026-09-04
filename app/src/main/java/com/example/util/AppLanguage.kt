package com.example.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "Hindi", "हिंदी"),
    BENGALI("bn", "Bengali", "বাংলা"),
    TELUGU("te", "Telugu", "తెలుగు"),
    MARATHI("mr", "Marathi", "मराठी"),
    TAMIL("ta", "Tamil", "தமிழ்"),
    GUJARATI("gu", "Gujarati", "ગુજરાતી"),
    KANNADA("kn", "Kannada", "ಕನ್ನಡ"),
    MALAYALAM("ml", "Malayalam", "മലയാളം"),
    PUNJABI("pa", "Punjabi", "ਪੰਜਾਬੀ");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}

enum class StringKey {
    APP_TITLE,
    TAB_TODAY,
    TAB_MEDICINES,
    TAB_HISTORY,
    
    // Slots
    SLOT_MORNING,
    SLOT_AFTERNOON,
    SLOT_EVENING,
    SLOT_NIGHT,
    SLOT_ALL,
    
    // Status
    STATUS_PENDING,
    STATUS_TAKEN,
    STATUS_SKIPPED,
    STATUS_SNOOZED,
    
    // Actions
    ACTION_TAKE,
    ACTION_SKIP,
    ACTION_SNOOZE,
    ACTION_CHANGE,
    ACTION_ADD_MEDICINE,
    ACTION_EDIT_MEDICINE,
    ACTION_DELETE,
    ACTION_CANCEL,
    ACTION_SAVE,
    ACTION_TEST_ALARM,
    ACTION_STOP_ALARM,
    
    // Medicine Form
    FORM_LABEL,
    FORM_TABLET,
    FORM_LIQUID,
    FORM_DROPS,
    FORM_INJECTION,
    FORM_OTHER,
    
    // Dosage
    DOSAGE_LABEL,
    DOSAGE_QUARTER,
    DOSAGE_HALF,
    DOSAGE_FULL,
    DOSAGE_ONE_HALF,
    DOSAGE_TWO,
    DOSAGE_CUSTOM,
    
    // Routine / Regularity
    ROUTINE_TYPE,
    ROUTINE_REGULAR,
    ROUTINE_PERIODIC,
    INTERVAL_QUESTION,
    EVERY_X_DAYS,
    NEXT_DOSE_IN,
    DUE_TODAY,
    SCHEDULE_TIME,
    
    // Stock
    STOCK_LABEL,
    LOW_STOCK_ALERT,
    STOCK_REMAINING,
    
    // Alarm
    ALARM_RINGING_TITLE,
    ALARM_RINGING_SUBTITLE,
    
    // Language
    SELECT_LANGUAGE,
    LANGUAGE_LABEL,
    
    // Headers / Empty states
    TODAY_SCHEDULE_TITLE,
    NO_DOSES_TODAY,
    ADHERENCE_RATE,
    ALL_MEDICINES_TITLE,
    NO_MEDICINES_ADDED,
    DAILY_LOGS_TITLE,
    NO_HISTORY_LOGS,
    INSTRUCTIONS_LABEL,
    DAYS_LABEL,

    // Meal Times Customization
    MEAL_SCHEDULE_TITLE,
    MEAL_BREAKFAST,
    MEAL_LUNCH,
    MEAL_DINNER,
    MEAL_BEDTIME,
    MEAL_SCHEDULE_DESC,
    ACTION_SAVE_TIMES
}

object LanguageManager {
    private const val PREFS_NAME = "app_language_prefs"
    private const val KEY_LANG = "selected_language_code"

    fun getSavedLanguage(context: Context): AppLanguage {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val code = prefs.getString(KEY_LANG, AppLanguage.ENGLISH.code) ?: AppLanguage.ENGLISH.code
        return AppLanguage.fromCode(code)
    }

    fun saveLanguage(context: Context, language: AppLanguage) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, language.code).apply()
    }

    fun getString(key: StringKey, lang: AppLanguage): String {
        return translations[key]?.get(lang)
            ?: translations[key]?.get(AppLanguage.ENGLISH)
            ?: key.name
    }

    private val stringKeyMap: Map<String, StringKey> = mapOf(
        "app_title" to StringKey.APP_TITLE,
        "today" to StringKey.TAB_TODAY,
        "medicines" to StringKey.TAB_MEDICINES,
        "history" to StringKey.TAB_HISTORY,
        "morning" to StringKey.SLOT_MORNING,
        "afternoon" to StringKey.SLOT_AFTERNOON,
        "evening" to StringKey.SLOT_EVENING,
        "night" to StringKey.SLOT_NIGHT,
        "all_doses" to StringKey.SLOT_ALL,
        "pending" to StringKey.STATUS_PENDING,
        "taken" to StringKey.STATUS_TAKEN,
        "skipped" to StringKey.STATUS_SKIPPED,
        "snooze" to StringKey.ACTION_SNOOZE,
        "skip" to StringKey.ACTION_SKIP,
        "mark_taken" to StringKey.ACTION_TAKE,
        "add_medicine" to StringKey.ACTION_ADD_MEDICINE,
        "edit" to StringKey.ACTION_EDIT_MEDICINE,
        "delete" to StringKey.ACTION_DELETE,
        "cancel" to StringKey.ACTION_CANCEL,
        "save_changes" to StringKey.ACTION_SAVE,
        "test_alarm" to StringKey.ACTION_TEST_ALARM,
        "stop_alarm" to StringKey.ACTION_STOP_ALARM,
        "form_label" to StringKey.FORM_LABEL,
        "tablet_capsule" to StringKey.FORM_TABLET,
        "liquid_syrup" to StringKey.FORM_LIQUID,
        "drops" to StringKey.FORM_DROPS,
        "injection" to StringKey.FORM_INJECTION,
        "other" to StringKey.FORM_OTHER,
        "dosage" to StringKey.DOSAGE_LABEL,
        "quarter" to StringKey.DOSAGE_QUARTER,
        "half" to StringKey.DOSAGE_HALF,
        "full" to StringKey.DOSAGE_FULL,
        "custom" to StringKey.DOSAGE_CUSTOM,
        "schedule_type" to StringKey.ROUTINE_TYPE,
        "regular_medicine" to StringKey.ROUTINE_REGULAR,
        "periodic_medicine" to StringKey.ROUTINE_PERIODIC,
        "dose_interval_label" to StringKey.INTERVAL_QUESTION,
        "days" to StringKey.DAYS_LABEL,
        "timing_slot" to StringKey.SCHEDULE_TIME,
        "frequency" to StringKey.ROUTINE_TYPE,
        "stock_count" to StringKey.STOCK_LABEL,
        "instructions" to StringKey.INSTRUCTIONS_LABEL,
        "alarm_active" to StringKey.ALARM_RINGING_TITLE,
        "today_schedule" to StringKey.TODAY_SCHEDULE_TITLE,
        "meal_schedule_title" to StringKey.MEAL_SCHEDULE_TITLE,
        "meal_breakfast" to StringKey.MEAL_BREAKFAST,
        "meal_lunch" to StringKey.MEAL_LUNCH,
        "meal_dinner" to StringKey.MEAL_DINNER,
        "meal_bedtime" to StringKey.MEAL_BEDTIME,
        "meal_schedule_desc" to StringKey.MEAL_SCHEDULE_DESC,
        "save_meal_times" to StringKey.ACTION_SAVE_TIMES
    )

    fun get(key: String, lang: AppLanguage): String {
        val stringKey = stringKeyMap[key]
        if (stringKey != null) {
            val trans = getString(stringKey, lang)
            if (trans != stringKey.name) return trans
        }
        return when (key) {
            "meal_schedule_title" -> when (lang) {
                AppLanguage.HINDI -> "भोजन का समय (नाश्ता, दोपहर, रात)"
                AppLanguage.BENGALI -> "খাবারের সময়সূচি (সকাল, দুপুর, রাত)"
                AppLanguage.TELUGU -> "భోజన సమయాలు (అల్పాహారం, లంచ్, డిన్నర్)"
                AppLanguage.MARATHI -> "जेवणाची वेळ (न्याहारी, दुपार, रात्री)"
                AppLanguage.TAMIL -> "உணவு நேரங்கள் (காலை, மதியம், இரவு)"
                AppLanguage.GUJARATI -> "ભોજનનો સમય (નાસ્તો, બપોર, સાંજ)"
                AppLanguage.KANNADA -> "ಊಟದ ಸಮಯ (ತಿಂಡಿ, ಮಧ್ಯಾಹ್ನ, ರಾತ್ರಿ)"
                AppLanguage.MALAYALAM -> "ഭക്ഷണ സമയം (പ്രഭാതഭക്ഷണം, ഉച്ചയൂണ്, അത്താഴം)"
                AppLanguage.PUNJABI -> "ਖਾਣੇ ਦਾ ਸਮਾਂ (ਨਾਸ਼ਤਾ, ਦੁਪਹਿਰ, ਰਾਤ)"
                else -> "Meal Times Schedule"
            }
            "meal_schedule_desc" -> when (lang) {
                AppLanguage.HINDI -> "नाश्ता, दोपहर व रात के खाने से पहले और बाद के अलार्म इन समयों के अनुसार चलेंगे।"
                AppLanguage.BENGALI -> "খাবারের আগে ও পরের অ্যালার্ম এই সময়ের সাথে স্বয়ংক্রিয়ভাবে মিলবে।"
                AppLanguage.TELUGU -> "భోజనానికి ముందు మరియు తరువాత వచ్చే అలారాలు ఈ సమయాల ప్రకారం మోగుతాయి."
                AppLanguage.MARATHI -> "जेवणापूर्वी व नंतरचे अलार्म या वेळेनुसार सेट केले जातील."
                AppLanguage.TAMIL -> "உணவுக்கு முன் மற்றும் பின் அலாரங்கள் இந்த நேரங்களுக்கு ஏற்ப இயங்கும்."
                AppLanguage.GUJARATI -> "જમવા પહેલા અને પછીના એલાર્મ આ સમય મુજબ આપોઆપ સેટ થશે."
                AppLanguage.KANNADA -> "ಊಟಕ್ಕೆ ಮುಂಚೆ ಮತ್ತು ನಂತರದ ಅಲಾರಾಂಗಳು ಈ ಸಮಯಕ್ಕೆ ಹೊಂದಿಕೊಳ್ಳುತ್ತವೆ."
                AppLanguage.MALAYALAM -> "ഭക്ഷണത്തിന് മുമ്പും ശേഷവുമുള്ള അലാറങ്ങൾ ഈ സമയത്തിന് അനുസൃതമായി പ്രവർത്തിക്കും."
                AppLanguage.PUNJABI -> "ਖਾਣੇ ਤੋਂ ਪਹਿਲਾਂ ਅਤੇ ਬਾਅਦ ਦੇ ਅਲਾਰਮ ਇਹਨਾਂ ਸਮਿਆਂ ਮੁਤਾਬਕ ਸੈੱਟ ਹੋਣਗੇ।"
                else -> "Medication alarms for 'Before/After Breakfast, Lunch & Dinner' will automatically adjust to these meal times."
            }
            "meal_breakfast" -> when (lang) {
                AppLanguage.HINDI -> "🍳 नाश्ता (Breakfast)"
                AppLanguage.BENGALI -> "🍳 প্রাতরাশ (Breakfast)"
                AppLanguage.TELUGU -> "🍳 అల్పాహారం (Breakfast)"
                AppLanguage.MARATHI -> "🍳 न्याहारी (Breakfast)"
                AppLanguage.TAMIL -> "🍳 காலை உணவு (Breakfast)"
                AppLanguage.GUJARATI -> "🍳 સવારનો નાસ્તો (Breakfast)"
                AppLanguage.KANNADA -> "🍳 ಉಪಾಹಾರ (Breakfast)"
                AppLanguage.MALAYALAM -> "🍳 പ്രഭാതഭക്ഷണം (Breakfast)"
                AppLanguage.PUNJABI -> "🍳 ਨਾਸ਼ਤਾ (Breakfast)"
                else -> "🍳 Breakfast Time"
            }
            "meal_lunch" -> when (lang) {
                AppLanguage.HINDI -> "🥗 दोपहर का खाना (Lunch)"
                AppLanguage.BENGALI -> "🥗 দুপুরের খাবার (Lunch)"
                AppLanguage.TELUGU -> "🥗 మధ్యాహ్న భోజనం (Lunch)"
                AppLanguage.MARATHI -> "🥗 दुपारचे जेवण (Lunch)"
                AppLanguage.TAMIL -> "🥗 மதிய உணவு (Lunch)"
                AppLanguage.GUJARATI -> "🥗 બપોરનું ભોજન (Lunch)"
                AppLanguage.KANNADA -> "🥗 ಮಧ್ಯಾಹ್ನದ ಊಟ (Lunch)"
                AppLanguage.MALAYALAM -> "🥗 ഉച്ചഭക്ഷണം (Lunch)"
                AppLanguage.PUNJABI -> "🥗 ਦੁਪਹਿਰ ਦਾ ਖਾਣਾ (Lunch)"
                else -> "🥗 Lunch Time"
            }
            "meal_dinner" -> when (lang) {
                AppLanguage.HINDI -> "🍲 रात का खाना (Dinner)"
                AppLanguage.BENGALI -> "🍲 রাতের খাবার (Dinner)"
                AppLanguage.TELUGU -> "🍲 రాత్రి భోజనం (Dinner)"
                AppLanguage.MARATHI -> "🍲 रात्रीचे जेवण (Dinner)"
                AppLanguage.TAMIL -> "🍲 இரவு உணவு (Dinner)"
                AppLanguage.GUJARATI -> "🍲 સાંજનું ભોજન (Dinner)"
                AppLanguage.KANNADA -> "🍲 ರಾತ್ರಿಯ ಊಟ (Dinner)"
                AppLanguage.MALAYALAM -> "🍲 അത്താഴം (Dinner)"
                AppLanguage.PUNJABI -> "🍲 ਰਾਤ ਦਾ ਖਾਣਾ (Dinner)"
                else -> "🍲 Dinner Time"
            }
            "meal_bedtime" -> when (lang) {
                AppLanguage.HINDI -> "🌙 सोने का समय (Bedtime)"
                AppLanguage.BENGALI -> "🌙 ঘুমানোর সময় (Bedtime)"
                AppLanguage.TELUGU -> "🌙 పడుకునే సమయం (Bedtime)"
                AppLanguage.MARATHI -> "🌙 झोपण्याची वेळ (Bedtime)"
                AppLanguage.TAMIL -> "🌙 தூங்கும் நேரம் (Bedtime)"
                AppLanguage.GUJARATI -> "🌙 સૂવાનો સમય (Bedtime)"
                AppLanguage.KANNADA -> "🌙 ಮಲಗುವ ಸಮಯ (Bedtime)"
                AppLanguage.MALAYALAM -> "🌙 ഉറങ്ങുന്ന സമയം (Bedtime)"
                AppLanguage.PUNJABI -> "🌙 ਸੌਣ ਦਾ ਸਮਾਂ (Bedtime)"
                else -> "🌙 Bedtime"
            }
            "save_meal_times" -> when (lang) {
                AppLanguage.HINDI -> "समय सहेजें और अलार्म अपडेट करें"
                AppLanguage.BENGALI -> "সংরক্ষণ করুন এবং অ্যালার্ম আপডেট করুন"
                AppLanguage.TELUGU -> "సమయాన్ని సేవ్ చేసి అలారాలు అప్‌డేట్ చేయండి"
                AppLanguage.MARATHI -> "वेळ जतन करा आणि अलार्म अपडेट करा"
                AppLanguage.TAMIL -> "நேரத்தை சேமித்து அலாரத்தை புதுப்பிக்கவும்"
                AppLanguage.GUJARATI -> "સમય સાચવો અને એલાર્મ અપડેટ કરો"
                AppLanguage.KANNADA -> "ಸಮಯವನ್ನು ಉಳಿಸಿ ಮತ್ತು ಅಲಾರಾಂ ನವೀಕರಿಸಿ"
                AppLanguage.MALAYALAM -> "സമയം സേവ് ചെയ്ത് അലാറം അപ്ഡേറ്റ് ചെയ്യുക"
                AppLanguage.PUNJABI -> "ਸਮਾਂ ਸੰਭਾਲੋ ਅਤੇ ਅਲਾਰਮ ਅਪਡੇਟ ਕਰੋ"
                else -> "Save & Update Alarms"
            }
            "customize_meal_times_btn" -> when (lang) {
                AppLanguage.HINDI -> "भोजन समय सेट करें"
                AppLanguage.BENGALI -> "খাবারের সময় পরিবর্তন"
                AppLanguage.TELUGU -> "భోజన సమయాలు మార్చండి"
                AppLanguage.MARATHI -> "जेवणाची वेळ बदला"
                AppLanguage.TAMIL -> "உணவு நேரத்தை மாற்று"
                AppLanguage.GUJARATI -> "ભોજન સમય બદલો"
                AppLanguage.KANNADA -> "ಊಟದ ಸಮಯ ಬದಲಾಯಿಸಿ"
                AppLanguage.MALAYALAM -> "ഭക്ഷണ സമയം മാറ്റുക"
                AppLanguage.PUNJABI -> "ਖਾਣੇ ਦਾ ਸਮਾਂ ਬਦਲੋ"
                else -> "Meal Times"
            }
            "periodic_interval" -> when (lang) {
                AppLanguage.HINDI -> "प्रत्येक {d} दिन"
                AppLanguage.BENGALI -> "প্রতি {d} দিন"
                AppLanguage.TELUGU -> "ప్రతి {d} రోజులు"
                AppLanguage.MARATHI -> "दर {d} दिवसांनी"
                AppLanguage.TAMIL -> "ஒவ்வொரு {d} நாட்களும்"
                AppLanguage.GUJARATI -> "દર {d} દિવસે"
                AppLanguage.KANNADA -> "ಪ್ರತಿ {d} ದಿನಗಳು"
                AppLanguage.MALAYALAM -> "ഓരോ {d} ദിവസവും"
                AppLanguage.PUNJABI -> "ਹਰ {d} ਦਿਨ"
                else -> "Every {d} days"
            }
            "medicine_name" -> when (lang) {
                AppLanguage.HINDI -> "दवा का नाम"
                AppLanguage.BENGALI -> "ওষুধের নাম"
                AppLanguage.TELUGU -> "మందు పేరు"
                AppLanguage.MARATHI -> "औषधाचे नाव"
                AppLanguage.TAMIL -> "மருந்து பெயர்"
                AppLanguage.GUJARATI -> "દવાનું નામ"
                AppLanguage.KANNADA -> "ಔಷಧಿಯ ಹೆಸರು"
                AppLanguage.MALAYALAM -> "മരുന്നിന്റെ പേര്"
                AppLanguage.PUNJABI -> "ਦਵਾਈ ਦਾ ਨਾਮ"
                else -> "Medicine Name"
            }
            "custom_days" -> when (lang) {
                AppLanguage.HINDI -> "कस्टम दिन दर्ज करें"
                else -> "Custom Days"
            }
            "low_stock_warning" -> when (lang) {
                AppLanguage.HINDI -> "स्टॉक कम है! जल्दी नया लाएं"
                AppLanguage.BENGALI -> "ওষুধ শেষ হতে চলেছে! শীঘ্রই আনুন"
                AppLanguage.TELUGU -> "స్టాక్ తక్కువగా ఉంది! రీఫిల్ చేయండి"
                AppLanguage.MARATHI -> "औषध संपत आले आहे! लवकर आणा"
                AppLanguage.TAMIL -> "மருந்து கையிருப்பு குறைவு! வாங்கவும்"
                AppLanguage.GUJARATI -> "સ્ટોક ઓછો છે! નવું લાવો"
                AppLanguage.KANNADA -> "ಔಷಧಿ ಕಡಿಮೆ ಇದೆ! ಶೀಘ್ರವಾಗಿ ತನ್ನಿ"
                AppLanguage.MALAYALAM -> "മരുന്ന് തീരാറായി! ഉടൻ വാങ്ങുക"
                AppLanguage.PUNJABI -> "ਦਵਾਈ ਖਤਮ ਹੋਣ ਵਾਲੀ ਹੈ! ਨਵੀਂ ਲਿਆਓ"
                else -> "Low Stock! Refill Soon"
            }
            "refill_stock" -> when (lang) {
                AppLanguage.HINDI -> "स्टॉक जोड़ें (Refill)"
                AppLanguage.BENGALI -> "স্টক যোগ করুন (Refill)"
                AppLanguage.TELUGU -> "రీఫిల్ చేయండి"
                AppLanguage.MARATHI -> "औषध स्टॉक भरा"
                AppLanguage.TAMIL -> "ரீபில் செய்யவும்"
                AppLanguage.GUJARATI -> "સ્ટોક ઉમેરો"
                AppLanguage.KANNADA -> "ರೀಫಿಲ್ ಮಾಡಿ"
                AppLanguage.MALAYALAM -> "സ്റ്റോക്ക് റീഫിൽ ചെയ്യുക"
                AppLanguage.PUNJABI -> "ਸਟਾਕ ਸ਼ਾਮਲ ਕਰੋ"
                else -> "Refill Stock"
            }
            "in_stock" -> when (lang) {
                AppLanguage.HINDI -> "स्टॉक में है"
                AppLanguage.BENGALI -> "স্টকে আছে"
                AppLanguage.TELUGU -> "స్టాక్ ఉంది"
                AppLanguage.MARATHI -> "शिल्लक आहे"
                AppLanguage.TAMIL -> "கையிருப்பில் உள்ளது"
                AppLanguage.GUJARATI -> "સ્ટોકમાં છે"
                AppLanguage.KANNADA -> "ದಾಸ್ತಾನು ಇದೆ"
                AppLanguage.MALAYALAM -> "സ്റ്റോക്കുണ്ട്"
                AppLanguage.PUNJABI -> "ਸਟਾਕ ਵਿੱਚ ਹੈ"
                else -> "In Stock"
            }
            "out_of_stock" -> when (lang) {
                AppLanguage.HINDI -> "स्टॉक समाप्त (Out of Stock)"
                AppLanguage.BENGALI -> "স্টক শেষ"
                AppLanguage.TELUGU -> "స్టాక్ అయిపోయింది"
                AppLanguage.MARATHI -> "स्टॉक संपला"
                AppLanguage.TAMIL -> "கையிருப்பு இல்லை"
                AppLanguage.GUJARATI -> "સ્ટોક ખતમ"
                AppLanguage.KANNADA -> "ದಾಸ್ತಾನು ಮುಗಿದಿದೆ"
                AppLanguage.MALAYALAM -> "സ്റ്റോക്ക് തീർന്നു"
                AppLanguage.PUNJABI -> "ਸਟਾਕ ਖਤਮ"
                else -> "Out of Stock"
            }
            "stock_input_tablets" -> when (lang) {
                AppLanguage.HINDI -> "गोलियों / कैप्सूल की संख्या (Tablets/Capsules)"
                AppLanguage.BENGALI -> "ট্যাবলেট / ক্যাপসুল সংখ্যা"
                AppLanguage.TELUGU -> "టాబ్లెట్లు / క్యాప్సూల్స్ సంఖ్య"
                AppLanguage.MARATHI -> "गोळ्या / कॅप्सूलची संख्या"
                AppLanguage.TAMIL -> "மாத்திரைகள் / காப்ஸ்யூல்கள் எண்ணிக்கை"
                AppLanguage.GUJARATI -> "ગોળીઓ / કેપ્સ્યુલની સંખ્યા"
                AppLanguage.KANNADA -> "ಮಾತ್ರೆಗಳು / ಕ್ಯಾಪ್ಸುಲ್ಗಳ ಸಂಖ್ಯೆ"
                AppLanguage.MALAYALAM -> "ഗുളികകളുടെ എണ്ണം"
                AppLanguage.PUNJABI -> "ਗੋਲੀਆਂ / ਕੈਪਸੂਲਾਂ ਦੀ ਗਿਣਤੀ"
                else -> "No. of Tablets / Capsules"
            }
            "stock_input_liquid" -> when (lang) {
                AppLanguage.HINDI -> "सिरप / लिक्विड की कुल मात्रा (ml)"
                AppLanguage.BENGALI -> "সিরাপ / তরল ওষুধের পরিমাণ (ml)"
                AppLanguage.TELUGU -> "సిరప్ / ద్రవ ఔషధ పరిమాణం (ml)"
                AppLanguage.MARATHI -> "सिरप / द्रवाचे प्रमाण (ml)"
                AppLanguage.TAMIL -> "சிரப் / திரவ மருந்தின் அளவு (ml)"
                AppLanguage.GUJARATI -> "સીરપ / પ્રવાહી જથ્થો (ml)"
                AppLanguage.KANNADA -> "ಸಿರಪ್ / ದ್ರವದ ಪ್ರಮಾಣ (ml)"
                AppLanguage.MALAYALAM -> "സിറപ്പ് / ദ്രാവക അളവ് (ml)"
                AppLanguage.PUNJABI -> "ਸ਼ਰਬਤ / ਤਰਲ ਦਵਾਈ ਦੀ ਮਾਤਰਾ (ml)"
                else -> "Liquid Medicine Volume (ml)"
            }
            "low_stock_threshold" -> when (lang) {
                AppLanguage.HINDI -> "न्यूनतम अलर्ट सीमा (इससे कम होने पर सूचित करें)"
                AppLanguage.BENGALI -> "কম স্টকের সতর্কতা সীমা"
                AppLanguage.TELUGU -> "తక్కువ స్టాక్ హెచ్చరిక స్థాయి"
                AppLanguage.MARATHI -> "कमी स्टॉक इशारा मर्यादा"
                AppLanguage.TAMIL -> "குறைந்த கையிருப்பு எச்சரிக்கை அளவு"
                AppLanguage.GUJARATI -> "ઓછા સ્ટોકની ચેતવણી મર્યાદા"
                AppLanguage.KANNADA -> "ಕಡಿಮೆ ದಾಸ್ತಾನು ಎಚ್ಚರಿಕೆ ಮಟ್ಟ"
                AppLanguage.MALAYALAM -> "കുറഞ്ഞ സ്റ്റോക്ക് മുന്നറിയിപ്പ് ലെവൽ"
                AppLanguage.PUNJABI -> "ਘੱਟ ਸਟਾਕ ਚੇਤਾਵਨੀ ਪੱਧਰ"
                else -> "Low Stock Alert Level"
            }
            else -> key
        }
    }

    private val translations: Map<StringKey, Map<AppLanguage, String>> = mapOf(
        StringKey.APP_TITLE to mapOf(
            AppLanguage.ENGLISH to "Medicine Alarm & Tracker",
            AppLanguage.HINDI to "दवा अलार्म और ट्रैकर",
            AppLanguage.BENGALI to "ওষুধ অ্যালার্ম ও ট্র্যাকার",
            AppLanguage.TELUGU to "మందుల అలారం & ట్రాకర్",
            AppLanguage.MARATHI to "औषध अलार्म आणि ट्रॅकर",
            AppLanguage.TAMIL to "மருந்து அலாரம் & நினைவூட்டல்",
            AppLanguage.GUJARATI to "દવા એલાર્મ અને ટ્રેકર",
            AppLanguage.KANNADA to "ಔಷಧಿ ಅಲಾರಾಂ & ಟ್ರ್ಯಾಕರ್",
            AppLanguage.MALAYALAM to "മരുന്ന് അലാറവും ട്രാക്കറും",
            AppLanguage.PUNJABI to "ਦਵਾਈ ਅਲਾਰਮ ਅਤੇ ਟ੍ਰੈਕਰ"
        ),
        StringKey.TAB_TODAY to mapOf(
            AppLanguage.ENGLISH to "Today",
            AppLanguage.HINDI to "आज",
            AppLanguage.BENGALI to "আজ",
            AppLanguage.TELUGU to "ఈరోజు",
            AppLanguage.MARATHI to "आज",
            AppLanguage.TAMIL to "இன்று",
            AppLanguage.GUJARATI to "આજે",
            AppLanguage.KANNADA to "ಇಂದು",
            AppLanguage.MALAYALAM to "ഇന്ന്",
            AppLanguage.PUNJABI to "ਅੱਜ"
        ),
        StringKey.TAB_MEDICINES to mapOf(
            AppLanguage.ENGLISH to "Medicines",
            AppLanguage.HINDI to "दवाएं",
            AppLanguage.BENGALI to "ওষুধসমূহ",
            AppLanguage.TELUGU to "మందులు",
            AppLanguage.MARATHI to "औषधे",
            AppLanguage.TAMIL to "மருந்துகள்",
            AppLanguage.GUJARATI to "દવાઓ",
            AppLanguage.KANNADA to "ಔಷಧಿಗಳು",
            AppLanguage.MALAYALAM to "മരുന്നുകൾ",
            AppLanguage.PUNJABI to "ਦਵਾਈਆਂ"
        ),
        StringKey.TAB_HISTORY to mapOf(
            AppLanguage.ENGLISH to "History",
            AppLanguage.HINDI to "इतिहास",
            AppLanguage.BENGALI to "ইতিহাস",
            AppLanguage.TELUGU to "చరిత్ర",
            AppLanguage.MARATHI to "इतिहास",
            AppLanguage.TAMIL to "வரலாறு",
            AppLanguage.GUJARATI to "ઇતિહાસ",
            AppLanguage.KANNADA to "ಇತಿಹಾಸ",
            AppLanguage.MALAYALAM to "ചരിത്രം",
            AppLanguage.PUNJABI to "ਇਤਿਹਾਸ"
        ),
        StringKey.SLOT_MORNING to mapOf(
            AppLanguage.ENGLISH to "Morning",
            AppLanguage.HINDI to "सुबह",
            AppLanguage.BENGALI to "সকাল",
            AppLanguage.TELUGU to "ఉదయం",
            AppLanguage.MARATHI to "सकाळ",
            AppLanguage.TAMIL to "காலை",
            AppLanguage.GUJARATI to "સવાર",
            AppLanguage.KANNADA to "ಬೆಳಿಗ್ಗೆ",
            AppLanguage.MALAYALAM to "രാവിലെ",
            AppLanguage.PUNJABI to "ਸਵੇਰ"
        ),
        StringKey.SLOT_AFTERNOON to mapOf(
            AppLanguage.ENGLISH to "Afternoon",
            AppLanguage.HINDI to "दोपहर",
            AppLanguage.BENGALI to "দুপুর",
            AppLanguage.TELUGU to "మధ్యాహ్నం",
            AppLanguage.MARATHI to "दुपार",
            AppLanguage.TAMIL to "மதியம்",
            AppLanguage.GUJARATI to "બપોર",
            AppLanguage.KANNADA to "ಮಧ್ಯಾಹ್ನ",
            AppLanguage.MALAYALAM to "ഉച്ചയ്ക്ക്",
            AppLanguage.PUNJABI to "ਦੁਪਹਿਰ"
        ),
        StringKey.SLOT_EVENING to mapOf(
            AppLanguage.ENGLISH to "Evening",
            AppLanguage.HINDI to "शाम",
            AppLanguage.BENGALI to "সন্ধ্যা",
            AppLanguage.TELUGU to "సాయంత్రం",
            AppLanguage.MARATHI to "संध्याकाळ",
            AppLanguage.TAMIL to "மாலை",
            AppLanguage.GUJARATI to "સાંજ",
            AppLanguage.KANNADA to "ಸಂಜೆ",
            AppLanguage.MALAYALAM to "വൈകുന്നേരം",
            AppLanguage.PUNJABI to "ਸ਼ਾਮ"
        ),
        StringKey.SLOT_NIGHT to mapOf(
            AppLanguage.ENGLISH to "Night",
            AppLanguage.HINDI to "रात",
            AppLanguage.BENGALI to "রাত",
            AppLanguage.TELUGU to "రాత్రి",
            AppLanguage.MARATHI to "रात्र",
            AppLanguage.TAMIL to "இரவு",
            AppLanguage.GUJARATI to "રાત",
            AppLanguage.KANNADA to "ರಾತ್ರಿ",
            AppLanguage.MALAYALAM to "രാത്രി",
            AppLanguage.PUNJABI to "ਰਾਤ"
        ),
        StringKey.SLOT_ALL to mapOf(
            AppLanguage.ENGLISH to "All",
            AppLanguage.HINDI to "सभी",
            AppLanguage.BENGALI to "সব",
            AppLanguage.TELUGU to "అన్నీ",
            AppLanguage.MARATHI to "सर्व",
            AppLanguage.TAMIL to "அனைத்தும்",
            AppLanguage.GUJARATI to "બધું",
            AppLanguage.KANNADA to "ಎಲ್ಲಾ",
            AppLanguage.MALAYALAM to "എല്ലാം",
            AppLanguage.PUNJABI to "ਸਭ"
        ),
        StringKey.STATUS_PENDING to mapOf(
            AppLanguage.ENGLISH to "Pending",
            AppLanguage.HINDI to "बाकी",
            AppLanguage.BENGALI to "বাকি",
            AppLanguage.TELUGU to "బాకీ",
            AppLanguage.MARATHI to "प्रलंबित",
            AppLanguage.TAMIL to "நிலுவையில்",
            AppLanguage.GUJARATI to "બાકી",
            AppLanguage.KANNADA to "ಬಾಕಿ",
            AppLanguage.MALAYALAM to "ബാക്കി",
            AppLanguage.PUNJABI to "ਬਾਕੀ"
        ),
        StringKey.STATUS_TAKEN to mapOf(
            AppLanguage.ENGLISH to "Taken",
            AppLanguage.HINDI to "ले ली",
            AppLanguage.BENGALI to "নেওয়া হয়েছে",
            AppLanguage.TELUGU to "తీసుకున్నారు",
            AppLanguage.MARATHI to "घेतले",
            AppLanguage.TAMIL to "எடுக்கப்பட்டது",
            AppLanguage.GUJARATI to "લીધી",
            AppLanguage.KANNADA to "ತೆಗೆದುಕೊಂಡಿದೆ",
            AppLanguage.MALAYALAM to "കഴിച്ചു",
            AppLanguage.PUNJABI to "ਲੈ ਲਈ"
        ),
        StringKey.STATUS_SKIPPED to mapOf(
            AppLanguage.ENGLISH to "Skipped",
            AppLanguage.HINDI to "छोड़ दी",
            AppLanguage.BENGALI to "বাদ দেওয়া হয়েছে",
            AppLanguage.TELUGU to "దాటవేశారు",
            AppLanguage.MARATHI to "वगळले",
            AppLanguage.TAMIL to "தவிர்க்கப்பட்டது",
            AppLanguage.GUJARATI to "છોડી દીધી",
            AppLanguage.KANNADA to "ಬಿಡಲಾಗಿದೆ",
            AppLanguage.MALAYALAM to "ഒഴിവാക്കി",
            AppLanguage.PUNJABI to "ਛੱਡ ਦਿੱਤੀ"
        ),
        StringKey.STATUS_SNOOZED to mapOf(
            AppLanguage.ENGLISH to "Snoozed",
            AppLanguage.HINDI to "स्नूज़ की",
            AppLanguage.BENGALI to "স্নুজ করা",
            AppLanguage.TELUGU to "స్నూజ్ చేసారు",
            AppLanguage.MARATHI to "स्नूझ केले",
            AppLanguage.TAMIL to "ஸ்னூஸ் செய்யப்பட்டது",
            AppLanguage.GUJARATI to "સ્નૂઝ કર્યું",
            AppLanguage.KANNADA to "ಸ್ನೂಜ್ ಮಾಡಲಾಗಿದೆ",
            AppLanguage.MALAYALAM to "സ്‌നൂസ് ചെയ്തു",
            AppLanguage.PUNJABI to "ਸਨੂਜ਼ ਕੀਤਾ"
        ),
        StringKey.ACTION_TAKE to mapOf(
            AppLanguage.ENGLISH to "Take",
            AppLanguage.HINDI to "दवा लें",
            AppLanguage.BENGALI to "নিন",
            AppLanguage.TELUGU to "తీసుకోండి",
            AppLanguage.MARATHI to "घ्या",
            AppLanguage.TAMIL to "எடு",
            AppLanguage.GUJARATI to "લો",
            AppLanguage.KANNADA to "ತೆಗೆದುಕೊಳ್ಳಿ",
            AppLanguage.MALAYALAM to "കഴിക്കുക",
            AppLanguage.PUNJABI to "ਲਓ"
        ),
        StringKey.ACTION_SKIP to mapOf(
            AppLanguage.ENGLISH to "Skip",
            AppLanguage.HINDI to "छोड़ें",
            AppLanguage.BENGALI to "বাদ দিন",
            AppLanguage.TELUGU to "దాటవేయి",
            AppLanguage.MARATHI to "वगळा",
            AppLanguage.TAMIL to "தவிர்",
            AppLanguage.GUJARATI to "છોડો",
            AppLanguage.KANNADA to "ಬಿಟ್ಟುಬಿಡಿ",
            AppLanguage.MALAYALAM to "ഒഴിവാക്കുക",
            AppLanguage.PUNJABI to "ਛੱਡੋ"
        ),
        StringKey.ACTION_SNOOZE to mapOf(
            AppLanguage.ENGLISH to "Snooze (10m)",
            AppLanguage.HINDI to "स्नूज़ (10 मिनट)",
            AppLanguage.BENGALI to "স্নুজ (১০ মিনিট)",
            AppLanguage.TELUGU to "స్నూజ్ (10 నిమిషాలు)",
            AppLanguage.MARATHI to "स्नूझ (१० मि)",
            AppLanguage.TAMIL to "ஸ்னூஸ் (10 நிமிடம்)",
            AppLanguage.GUJARATI to "સ્નૂઝ (10 મિનિટ)",
            AppLanguage.KANNADA to "ಸ್ನೂಜ್ (10 ನಿಮಿಷ)",
            AppLanguage.MALAYALAM to "സ്‌നൂസ് (10 മിനിറ്റ്)",
            AppLanguage.PUNJABI to "ਸਨੂਜ਼ (10 ਮਿੰਟ)"
        ),
        StringKey.ACTION_CHANGE to mapOf(
            AppLanguage.ENGLISH to "Change",
            AppLanguage.HINDI to "बदलें",
            AppLanguage.BENGALI to "পরিবর্তন",
            AppLanguage.TELUGU to "మార్చండి",
            AppLanguage.MARATHI to "बदला",
            AppLanguage.TAMIL to "மாற்று",
            AppLanguage.GUJARATI to "બદલો",
            AppLanguage.KANNADA to "ಬದಲಾಯಿಸಿ",
            AppLanguage.MALAYALAM to "മാറ്റുക",
            AppLanguage.PUNJABI to "ਬਦਲੋ"
        ),
        StringKey.ACTION_ADD_MEDICINE to mapOf(
            AppLanguage.ENGLISH to "Add Medication",
            AppLanguage.HINDI to "दवा जोड़ें",
            AppLanguage.BENGALI to "ওষুধ যোগ করুন",
            AppLanguage.TELUGU to "మందును జోడించండి",
            AppLanguage.MARATHI to "औषध जोडा",
            AppLanguage.TAMIL to "மருந்து சேர்க்கவும்",
            AppLanguage.GUJARATI to "દવા ઉમેરો",
            AppLanguage.KANNADA to "ಔಷಧಿ ಸೇರಿಸಿ",
            AppLanguage.MALAYALAM to "മരുന്ന് ചേർക്കുക",
            AppLanguage.PUNJABI to "ਦਵਾਈ ਸ਼ਾਮਲ ਕਰੋ"
        ),
        StringKey.ACTION_EDIT_MEDICINE to mapOf(
            AppLanguage.ENGLISH to "Edit Medication",
            AppLanguage.HINDI to "दवा संपादित करें",
            AppLanguage.BENGALI to "ওষুধ পরিবর্তন করুন",
            AppLanguage.TELUGU to "మందును సవరించండి",
            AppLanguage.MARATHI to "औषध संपादित करा",
            AppLanguage.TAMIL to "மருந்தை மாற்றுக",
            AppLanguage.GUJARATI to "દવા સંપાદિત કરો",
            AppLanguage.KANNADA to "ಔಷಧಿ ತಿದ್ದುಪಡಿ ಮಾಡಿ",
            AppLanguage.MALAYALAM to "മരുന്ന് എഡിറ്റ് ചെയ്യുക",
            AppLanguage.PUNJABI to "ਦਵਾਈ ਸੋਧੋ"
        ),
        StringKey.ACTION_SAVE to mapOf(
            AppLanguage.ENGLISH to "Save Medicine",
            AppLanguage.HINDI to "दवा सहेजें",
            AppLanguage.BENGALI to "সংরক্ষণ করুন",
            AppLanguage.TELUGU to "భద్రపరచండి",
            AppLanguage.MARATHI to "जतन करा",
            AppLanguage.TAMIL to "சேமிக்கவும்",
            AppLanguage.GUJARATI to "સાચવો",
            AppLanguage.KANNADA to "ಉಳಿಸಿ",
            AppLanguage.MALAYALAM to "സൂക്ഷിക്കുക",
            AppLanguage.PUNJABI to "ਸੰਭਾਲੋ"
        ),
        StringKey.ACTION_CANCEL to mapOf(
            AppLanguage.ENGLISH to "Cancel",
            AppLanguage.HINDI to "रद्द करें",
            AppLanguage.BENGALI to "বাতিল",
            AppLanguage.TELUGU to "రద్దు చేయండి",
            AppLanguage.MARATHI to "रद्द करा",
            AppLanguage.TAMIL to "ரத்து செய்",
            AppLanguage.GUJARATI to "રદ કરો",
            AppLanguage.KANNADA to "ರದ್ದುಮಾಡಿ",
            AppLanguage.MALAYALAM to "റദ്ദാക്കുക",
            AppLanguage.PUNJABI to "ਰੱਦ ਕਰੋ"
        ),
        StringKey.ACTION_STOP_ALARM to mapOf(
            AppLanguage.ENGLISH to "Stop Alarm",
            AppLanguage.HINDI to "अलार्म बंद करें",
            AppLanguage.BENGALI to "অ্যালার্ম বন্ধ করুন",
            AppLanguage.TELUGU to "అలారం ఆపండి",
            AppLanguage.MARATHI to "अलार्म थांबवा",
            AppLanguage.TAMIL to "அலாரத்தை நிறுத்து",
            AppLanguage.GUJARATI to "એલાર્મ બંધ કરો",
            AppLanguage.KANNADA to "ಅಲಾರಾಂ ನಿಲ್ಲಿಸಿ",
            AppLanguage.MALAYALAM to "അലാറം നിർത്തുക",
            AppLanguage.PUNJABI to "ਅਲਾਰਮ ਬੰਦ ਕਰੋ"
        ),
        StringKey.ACTION_TEST_ALARM to mapOf(
            AppLanguage.ENGLISH to "Test Ringing Alarm",
            AppLanguage.HINDI to "अलार्म बजाकर देखें",
            AppLanguage.BENGALI to "অ্যালার্ম পরীক্ষা করুন",
            AppLanguage.TELUGU to "అలారంను పరీక్షించండి",
            AppLanguage.MARATHI to "अलार्म तपासा",
            AppLanguage.TAMIL to "அலாரத்தை சோதிக்கவும்",
            AppLanguage.GUJARATI to "એલાર્મ ચકાસો",
            AppLanguage.KANNADA to "ಅಲಾರಾಂ ಪರೀಕ್ಷಿಸಿ",
            AppLanguage.MALAYALAM to "അലാറം പരിശോധിക്കുക",
            AppLanguage.PUNJABI to "ਅਲਾਰਮ ਚੈੱਕ ਕਰੋ"
        ),
        StringKey.FORM_LABEL to mapOf(
            AppLanguage.ENGLISH to "Medicine Form",
            AppLanguage.HINDI to "दवा का प्रकार (Form)",
            AppLanguage.BENGALI to "ওষুধের ধরন",
            AppLanguage.TELUGU to "మందు రకం",
            AppLanguage.MARATHI to "औषधाचा प्रकार",
            AppLanguage.TAMIL to "மருந்து வகை",
            AppLanguage.GUJARATI to "દવાનો પ્રકાર",
            AppLanguage.KANNADA to "ಔಷಧಿಯ ಪ್ರಕಾರ",
            AppLanguage.MALAYALAM to "മരുന്നിന്റെ രൂപം",
            AppLanguage.PUNJABI to "ਦਵਾਈ ਦੀ ਕਿਸਮ"
        ),
        StringKey.FORM_TABLET to mapOf(
            AppLanguage.ENGLISH to "Tablet / Capsule",
            AppLanguage.HINDI to "गोली / कैप्सूल (Tablet)",
            AppLanguage.BENGALI to "ট্যাবলেট / ক্যাপসুল",
            AppLanguage.TELUGU to "టాబ్లెట్ / క్యాప్సూల్",
            AppLanguage.MARATHI to "गोळी / कॅप्सूल",
            AppLanguage.TAMIL to "மாத்திரை / காப்ஸ்யூல்",
            AppLanguage.GUJARATI to "ગોળી / કેપ્સ્યુલ",
            AppLanguage.KANNADA to "ಮಾತ್ರೆ / ಕ್ಯಾಪ್ಸುಲ್",
            AppLanguage.MALAYALAM to "ഗുളിക / ക്യാപ്സ്യൂൾ",
            AppLanguage.PUNJABI to "ਗੋਲੀ / ਕੈਪਸੂਲ"
        ),
        StringKey.FORM_LIQUID to mapOf(
            AppLanguage.ENGLISH to "Liquid / Syrup",
            AppLanguage.HINDI to "सिरप / लिक्विड (Liquid)",
            AppLanguage.BENGALI to "সিরাপ / তরল ওষুধ",
            AppLanguage.TELUGU to "సిరప్ / ద్రవ మందు",
            AppLanguage.MARATHI to "सिरप / पातळ औषध",
            AppLanguage.TAMIL to "சிரப் / திரவ மருந்து",
            AppLanguage.GUJARATI to "સીરપ / પ્રવાહી દવા",
            AppLanguage.KANNADA to "ಸಿರಪ್ / ದ್ರವ ಔಷಧಿ",
            AppLanguage.MALAYALAM to "സിറപ്പ് / ദ്രാവക മരുന്ന്",
            AppLanguage.PUNJABI to "ਸੀਰਪ / ਤਰਲ ਦਵਾਈ"
        ),
        StringKey.FORM_DROPS to mapOf(
            AppLanguage.ENGLISH to "Drops",
            AppLanguage.HINDI to "ड्रॉप्स (Drops)",
            AppLanguage.BENGALI to "ড্রপস",
            AppLanguage.TELUGU to "డ్రాప్స్",
            AppLanguage.MARATHI to "थेंब (Drops)",
            AppLanguage.TAMIL to "சொட்டு மருந்து",
            AppLanguage.GUJARATI to "ટીપાં",
            AppLanguage.KANNADA to "ಹನಿಗಳು",
            AppLanguage.MALAYALAM to "ഡ്രോപ്സ്",
            AppLanguage.PUNJABI to "ਡਰਾਪਸ"
        ),
        StringKey.FORM_INJECTION to mapOf(
            AppLanguage.ENGLISH to "Injection",
            AppLanguage.HINDI to "इंजेक्शन",
            AppLanguage.BENGALI to "ইনজেকশন",
            AppLanguage.TELUGU to "ఇంజెక్షన్",
            AppLanguage.MARATHI to "इंजेक्शन",
            AppLanguage.TAMIL to "ஊசி மருந்து",
            AppLanguage.GUJARATI to "ઇન્જેક્શન",
            AppLanguage.KANNADA to "ಇಂಜೆಕ್ಷನ್",
            AppLanguage.MALAYALAM to "ഇഞ്ചക്ഷൻ",
            AppLanguage.PUNJABI to "ਟੀਕਾ"
        ),
        StringKey.FORM_OTHER to mapOf(
            AppLanguage.ENGLISH to "Other",
            AppLanguage.HINDI to "अन्य",
            AppLanguage.BENGALI to "অন্যান্য",
            AppLanguage.TELUGU to "ఇతర",
            AppLanguage.MARATHI to "इतर",
            AppLanguage.TAMIL to "பிற",
            AppLanguage.GUJARATI to "અન્ય",
            AppLanguage.KANNADA to "ಇತರೆ",
            AppLanguage.MALAYALAM to "മറ്റുള്ളവ",
            AppLanguage.PUNJABI to "ਹੋਰ"
        ),
        StringKey.DOSAGE_LABEL to mapOf(
            AppLanguage.ENGLISH to "Dosage",
            AppLanguage.HINDI to "खुराक (Dosage)",
            AppLanguage.BENGALI to "ডোজ (Dosage)",
            AppLanguage.TELUGU to "మోతాదు",
            AppLanguage.MARATHI to "डोस (मात्रा)",
            AppLanguage.TAMIL to "மருந்தளவு",
            AppLanguage.GUJARATI to "માત્રા (Dosage)",
            AppLanguage.KANNADA to "ಡೋಸ್ (ಪ್ರಮಾಣ)",
            AppLanguage.MALAYALAM to "ഡോസ്",
            AppLanguage.PUNJABI to "ਖੁਰਾਕ"
        ),
        StringKey.DOSAGE_QUARTER to mapOf(
            AppLanguage.ENGLISH to "1/4 (Quarter)",
            AppLanguage.HINDI to "1/4 (चौथाई)",
            AppLanguage.BENGALI to "১/৪ (এক চতুর্থাংশ)",
            AppLanguage.TELUGU to "1/4 (పావు వంతు)",
            AppLanguage.MARATHI to "1/4 (पाव)",
            AppLanguage.TAMIL to "1/4 (கால்)",
            AppLanguage.GUJARATI to "1/4 (ચોથો ભાગ)",
            AppLanguage.KANNADA to "1/4 (ಕಾಲು)",
            AppLanguage.MALAYALAM to "1/4 (കാൽ)",
            AppLanguage.PUNJABI to "1/4 (ਚੌਥਾਈ)"
        ),
        StringKey.DOSAGE_HALF to mapOf(
            AppLanguage.ENGLISH to "1/2 (Half)",
            AppLanguage.HINDI to "1/2 (आधा)",
            AppLanguage.BENGALI to "১/২ (অর্ধেক)",
            AppLanguage.TELUGU to "1/2 (సగం)",
            AppLanguage.MARATHI to "1/2 (अर्धी)",
            AppLanguage.TAMIL to "1/2 (அரை)",
            AppLanguage.GUJARATI to "1/2 (અડધી)",
            AppLanguage.KANNADA to "1/2 (ಅರ್ಧ)",
            AppLanguage.MALAYALAM to "1/2 (പകുതി)",
            AppLanguage.PUNJABI to "1/2 (ਅੱਧੀ)"
        ),
        StringKey.DOSAGE_FULL to mapOf(
            AppLanguage.ENGLISH to "1 (Full)",
            AppLanguage.HINDI to "1 (पूरी)",
            AppLanguage.BENGALI to "১ (পুরো)",
            AppLanguage.TELUGU to "1 (పూర్తి)",
            AppLanguage.MARATHI to "1 (पूर्ण)",
            AppLanguage.TAMIL to "1 (முழு)",
            AppLanguage.GUJARATI to "1 (આખી)",
            AppLanguage.KANNADA to "1 (ಪೂರ್ಣ)",
            AppLanguage.MALAYALAM to "1 (പൂർണ്ണം)",
            AppLanguage.PUNJABI to "1 (ਪੂਰੀ)"
        ),
        StringKey.DOSAGE_ONE_HALF to mapOf(
            AppLanguage.ENGLISH to "1.5 (One & Half)",
            AppLanguage.HINDI to "1.5 (डेढ़)",
            AppLanguage.BENGALI to "১.৫ (দেড়)",
            AppLanguage.TELUGU to "1.5 (ఒకటిన్నర)",
            AppLanguage.MARATHI to "1.5 (दीड)",
            AppLanguage.TAMIL to "1.5 (ஒன்றரை)",
            AppLanguage.GUJARATI to "1.5 (દોઢ)",
            AppLanguage.KANNADA to "1.5 (ಒಂದೂವರೆ)",
            AppLanguage.MALAYALAM to "1.5 (ഒന്നര)",
            AppLanguage.PUNJABI to "1.5 (ਡੇਢ)"
        ),
        StringKey.DOSAGE_TWO to mapOf(
            AppLanguage.ENGLISH to "2 (Two)",
            AppLanguage.HINDI to "2 (दो गोली)",
            AppLanguage.BENGALI to "২ (দুটি)",
            AppLanguage.TELUGU to "2 (రెండు)",
            AppLanguage.MARATHI to "2 (दोन)",
            AppLanguage.TAMIL to "2 (இரண்டு)",
            AppLanguage.GUJARATI to "2 (બે)",
            AppLanguage.KANNADA to "2 (ಎರಡು)",
            AppLanguage.MALAYALAM to "2 (രണ്ട്)",
            AppLanguage.PUNJABI to "2 (ਦੋ)"
        ),
        StringKey.DOSAGE_CUSTOM to mapOf(
            AppLanguage.ENGLISH to "Custom",
            AppLanguage.HINDI to "कस्टम (अपनी मात्रा लिखें)",
            AppLanguage.BENGALI to "কাস্টম",
            AppLanguage.TELUGU to "కస్టమ్",
            AppLanguage.MARATHI to "कस्टम",
            AppLanguage.TAMIL to "தனிப்பயன்",
            AppLanguage.GUJARATI to "કસ્ટમ",
            AppLanguage.KANNADA to "ಕಸ್ಟಮ್",
            AppLanguage.MALAYALAM to "കസ്റ്റം",
            AppLanguage.PUNJABI to "ਕਸਟਮ"
        ),
        StringKey.ROUTINE_TYPE to mapOf(
            AppLanguage.ENGLISH to "Schedule Routine",
            AppLanguage.HINDI to "शेड्यूल रूटीन",
            AppLanguage.BENGALI to "সময়সূচি",
            AppLanguage.TELUGU to "షెడ్యూల్ రకం",
            AppLanguage.MARATHI to "वेळापत्रक प्रकार",
            AppLanguage.TAMIL to "அட்டவணை வகை",
            AppLanguage.GUJARATI to "સમયપત્રક",
            AppLanguage.KANNADA to "ವೇಳಾಪಟ್ಟಿ ಪ್ರಕಾರ",
            AppLanguage.MALAYALAM to "ഷെഡ്യൂൾ തരം",
            AppLanguage.PUNJABI to "ਸ਼ਡਿਊਲ ਰੂਟੀਨ"
        ),
        StringKey.ROUTINE_REGULAR to mapOf(
            AppLanguage.ENGLISH to "Regular (Daily / Weekly)",
            AppLanguage.HINDI to "नियमित (रोज / सप्ताह)",
            AppLanguage.BENGALI to "নিয়মিত (প্রতিদিন / সাপ্তাহিক)",
            AppLanguage.TELUGU to "క్రమం తప్పకుండా (రోజూ / వారం)",
            AppLanguage.MARATHI to "नियमित (रोज / आठवडा)",
            AppLanguage.TAMIL to "வழக்கமான (தினசரி / வாரம்)",
            AppLanguage.GUJARATI to "નિયમિત (રોજ / સાપ્તાહિક)",
            AppLanguage.KANNADA to "ನಿಯಮಿತ (ದಿನನಿತ್ಯ / ವಾರಕ್ಕೊಮ್ಮೆ)",
            AppLanguage.MALAYALAM to "സ്ഥിരം (ദിവസവും / ആഴ്ചയിൽ)",
            AppLanguage.PUNJABI to "ਰੋਜ਼ਾਨਾ / ਨਿਯਮਤ"
        ),
        StringKey.ROUTINE_PERIODIC to mapOf(
            AppLanguage.ENGLISH to "Periodic (After X Days)",
            AppLanguage.HINDI to "अंतराल पर (हर कुछ दिनों बाद)",
            AppLanguage.BENGALI to "নির্দিষ্ট দিন পর পর (Periodic)",
            AppLanguage.TELUGU to "నిర్దిష్ట రోజుల తర్వాత (Periodic)",
            AppLanguage.MARATHI to "काही दिवसांच्या अंतराने",
            AppLanguage.TAMIL to "குறிப்பிட்ட நாட்களுக்குப் பிறகு",
            AppLanguage.GUJARATI to "અમુક દિવસોના અંતરે",
            AppLanguage.KANNADA to "ನಿರ್ದಿಷ್ಟ ದಿನಗಳ ನಂತರ",
            AppLanguage.MALAYALAM to "നിശ്ചിത ദിവസങ്ങൾക്ക് ശേഷം",
            AppLanguage.PUNJABI to "ਕੁਝ ਦਿਨਾਂ ਬਾਅਦ (Periodic)"
        ),
        StringKey.INTERVAL_QUESTION to mapOf(
            AppLanguage.ENGLISH to "Next dose after how many days?",
            AppLanguage.HINDI to "अगली खुराक कितने दिनों बाद?",
            AppLanguage.BENGALI to "পরবর্তী ডোজ কত দিন পর পর?",
            AppLanguage.TELUGU to "తదుపరి మోతాదు ఎన్ని రోజుల తర్వాత?",
            AppLanguage.MARATHI to "पुढील डोस किती दिवसांनंतर?",
            AppLanguage.TAMIL to "அடுத்த டோஸ் எத்தனை நாட்களுக்குப் பிறகு?",
            AppLanguage.GUJARATI to "આગલો ડોઝ કેટલા દિવસ પછી?",
            AppLanguage.KANNADA to "ಮುಂದಿನ ಡೋಸ್ ಎಷ್ಟು ದಿನಗಳ ನಂತರ?",
            AppLanguage.MALAYALAM to "അടുത്ത ഡോസ് എത്ര ദിവസങ്ങൾക്ക് ശേഷം?",
            AppLanguage.PUNJABI to "ਅਗਲੀ ਖੁਰਾਕ ਕਿੰਨੇ ਦਿਨਾਂ ਬਾਅਦ?"
        ),
        StringKey.EVERY_X_DAYS to mapOf(
            AppLanguage.ENGLISH to "Every %d days",
            AppLanguage.HINDI to "हर %d दिन बाद",
            AppLanguage.BENGALI to "প্রতি %d দিন পর",
            AppLanguage.TELUGU to "ప్రతి %d రోజులకు",
            AppLanguage.MARATHI to "दर %d दिवसांनी",
            AppLanguage.TAMIL to "ஒவ்வொரு %d நாட்களுக்கு",
            AppLanguage.GUJARATI to "દર %d દિવસે",
            AppLanguage.KANNADA to "ಪ್ರತಿ %d ದಿನಗಳಿಗೆ",
            AppLanguage.MALAYALAM to "ഓരോ %d ദിവസത്തിലും",
            AppLanguage.PUNJABI to "ਹਰ %d ਦਿਨਾਂ ਬਾਅਦ"
        ),
        StringKey.NEXT_DOSE_IN to mapOf(
            AppLanguage.ENGLISH to "Next due: %s",
            AppLanguage.HINDI to "अगली तारीख: %s",
            AppLanguage.BENGALI to "পরবর্তী তারিখ: %s",
            AppLanguage.TELUGU to "తదుపరి తేదీ: %s",
            AppLanguage.MARATHI to "पुढील तारीख: %s",
            AppLanguage.TAMIL to "அடுத்த தேதி: %s",
            AppLanguage.GUJARATI to "આગલી તારીખ: %s",
            AppLanguage.KANNADA to "ಮುಂದಿನ ದಿನಾಂಕ: %s",
            AppLanguage.MALAYALAM to "അടുത്ത തീയതി: %s",
            AppLanguage.PUNJABI to "ਅਗਲੀ ਤਾਰੀਖ: %s"
        ),
        StringKey.DUE_TODAY to mapOf(
            AppLanguage.ENGLISH to "Due Today",
            AppLanguage.HINDI to "आज लेनी है",
            AppLanguage.BENGALI to "আজকের ডোজ",
            AppLanguage.TELUGU to "ఈరోజు తీసుకోవాలి",
            AppLanguage.MARATHI to "आज घ्यायचे आहे",
            AppLanguage.TAMIL to "இன்று எடுக்க வேண்டும்",
            AppLanguage.GUJARATI to "આજે લેવાની છે",
            AppLanguage.KANNADA to "ಇಂದು ತೆಗೆದುಕೊಳ್ಳಬೇಕು",
            AppLanguage.MALAYALAM to "ഇന്ന് കഴിക്കണം",
            AppLanguage.PUNJABI to "ਅੱਜ ਲੈਣੀ ਹੈ"
        ),
        StringKey.SCHEDULE_TIME to mapOf(
            AppLanguage.ENGLISH to "Reminder Time & Meal Timing",
            AppLanguage.HINDI to "अलार्म का समय व भोजन समय",
            AppLanguage.BENGALI to "অ্যালার্মের সময় ও খাওয়ার সময়",
            AppLanguage.TELUGU to "రిమైండర్ సమయం",
            AppLanguage.MARATHI to "वेळ आणि जेवणाची वेळ",
            AppLanguage.TAMIL to "நேரம் மற்றும் உணவு நேரம்",
            AppLanguage.GUJARATI to "સમય અને ભોજન સમય",
            AppLanguage.KANNADA to "ಜ್ಞಾಪನೆ ಸಮಯ",
            AppLanguage.MALAYALAM to "ഓർമ്മപ്പെടുത്തൽ സമയം",
            AppLanguage.PUNJABI to "ਸਮਾਂ ਅਤੇ ਖਾਣ ਦਾ ਸਮਾਂ"
        ),
        StringKey.STOCK_LABEL to mapOf(
            AppLanguage.ENGLISH to "Inventory / Stock Count",
            AppLanguage.HINDI to "दवा का स्टॉक (Stock Count)",
            AppLanguage.BENGALI to "ওষুধের মজুত সংখ্যা",
            AppLanguage.TELUGU to "మందుల నిల్వ",
            AppLanguage.MARATHI to "औषधांचा साठा",
            AppLanguage.TAMIL to "மருந்து இருப்பு",
            AppLanguage.GUJARATI to "દવાનો જથ્થો",
            AppLanguage.KANNADA to "ಔಷಧಿ ದಾಸ್ತಾನು",
            AppLanguage.MALAYALAM to "മരുന്ന് സ്റ്റോക്ക്",
            AppLanguage.PUNJABI to "ਦਵਾਈ ਦਾ ਸਟਾਕ"
        ),
        StringKey.LOW_STOCK_ALERT to mapOf(
            AppLanguage.ENGLISH to "Low Stock Alert",
            AppLanguage.HINDI to "कम स्टॉक चेतावनी",
            AppLanguage.BENGALI to "কম মজুত সতর্কতা",
            AppLanguage.TELUGU to "తక్కువ నిల్వ హెచ్చరిక",
            AppLanguage.MARATHI to "कमी साठा सूचना",
            AppLanguage.TAMIL to "குறைந்த இருப்பு எச்சரிக்கை",
            AppLanguage.GUJARATI to "ઓછા જથ્થાની ચેતવણી",
            AppLanguage.KANNADA to "ಕಡಿಮೆ ದಾಸ್ತಾನು ಎಚ್ಚರಿಕೆ",
            AppLanguage.MALAYALAM to "കുറഞ്ഞ സ്റ്റോക്ക് മുന്നറിയിപ്പ്",
            AppLanguage.PUNJABI to "ਘੱਟ ਸਟਾਕ ਚੇਤਾਵਨੀ"
        ),
        StringKey.STOCK_REMAINING to mapOf(
            AppLanguage.ENGLISH to "%d left",
            AppLanguage.HINDI to "%d बची हैं",
            AppLanguage.BENGALI to "%d বাকি",
            AppLanguage.TELUGU to "%d మిగిలి ఉన్నాయి",
            AppLanguage.MARATHI to "%d शिल्लक",
            AppLanguage.TAMIL to "%d மீதம்",
            AppLanguage.GUJARATI to "%d બાકી",
            AppLanguage.KANNADA to "%d ಉಳಿದಿವೆ",
            AppLanguage.MALAYALAM to "%d ബാക്കി",
            AppLanguage.PUNJABI to "%d ਬਚੀਆਂ"
        ),
        StringKey.ALARM_RINGING_TITLE to mapOf(
            AppLanguage.ENGLISH to "⏰ Device Alarm Ringing!",
            AppLanguage.HINDI to "⏰ डिवाइस अलार्म बज रहा है!",
            AppLanguage.BENGALI to "⏰ ডিভাইসের অ্যালার্ম বাজছে!",
            AppLanguage.TELUGU to "⏰ డివైస్ అలారం మోగుతోంది!",
            AppLanguage.MARATHI to "⏰ डिव्हाइस अलार्म वाजत आहे!",
            AppLanguage.TAMIL to "⏰ சாதன அலாரம் ஒலிக்கிறது!",
            AppLanguage.GUJARATI to "⏰ ડિવાઇસ એલાર્મ વાગી રહ્યું છે!",
            AppLanguage.KANNADA to "⏰ ಸಾಧನದ ಅಲಾರಾಂ ರಿಂಗ್ ಆಗುತ್ತಿದೆ!",
            AppLanguage.MALAYALAM to "⏰ ഉപകരണ അലാറം മുഴങ്ങുന്നു!",
            AppLanguage.PUNJABI to "⏰ ਡਿਵਾਈਸ ਅਲਾਰਮ ਵੱਜ ਰਿਹਾ ਹੈ!"
        ),
        StringKey.ALARM_RINGING_SUBTITLE to mapOf(
            AppLanguage.ENGLISH to "Time for your scheduled medication:",
            AppLanguage.HINDI to "आपकी निर्धारित दवा का समय हो गया है:",
            AppLanguage.BENGALI to "আপনার নির্ধারিত ওষুধের সময় হয়েছে:",
            AppLanguage.TELUGU to "మీ మందు తీసుకునే సమయం అయింది:",
            AppLanguage.MARATHI to "तुमच्या औषधाची वेळ झाली आहे:",
            AppLanguage.TAMIL to "உங்கள் மருந்துக்கான நேரம் வந்துவிட்டது:",
            AppLanguage.GUJARATI to "તમારી દવા લેવાનો સમય થઈ ગયો છે:",
            AppLanguage.KANNADA to "ನಿಮ್ಮ ಔಷಧಿ ತೆಗೆದುಕೊಳ್ಳುವ ಸಮಯ:",
            AppLanguage.MALAYALAM to "മരുന്ന് കഴിക്കാനുള്ള സമയം ആയി:",
            AppLanguage.PUNJABI to "ਤੁਹਾਡੀ ਦਵਾਈ ਲੈਣ ਦਾ ਸਮਾਂ ਹੋ ਗਿਆ ਹੈ:"
        ),
        StringKey.SELECT_LANGUAGE to mapOf(
            AppLanguage.ENGLISH to "Select Language",
            AppLanguage.HINDI to "अपनी भाषा चुनें (Language)",
            AppLanguage.BENGALI to "ভাষা বেছে নিন",
            AppLanguage.TELUGU to "భాషను ఎంచుకోండి",
            AppLanguage.MARATHI to "भाषा निवडा",
            AppLanguage.TAMIL to "மொழியைத் தேர்ந்தெடுக்கவும்",
            AppLanguage.GUJARATI to "ભાષા પસંદ કરો",
            AppLanguage.KANNADA to "ಭಾಷೆ ಆಯ್ಕೆಮಾಡಿ",
            AppLanguage.MALAYALAM to "ഭാഷ തിരഞ്ഞെടുക്കുക",
            AppLanguage.PUNJABI to "ਭਾਸ਼ਾ ਚੁਣੋ"
        ),
        StringKey.LANGUAGE_LABEL to mapOf(
            AppLanguage.ENGLISH to "Language",
            AppLanguage.HINDI to "भाषा",
            AppLanguage.BENGALI to "ভাষা",
            AppLanguage.TELUGU to "భాష",
            AppLanguage.MARATHI to "भाषा",
            AppLanguage.TAMIL to "மொழி",
            AppLanguage.GUJARATI to "ભાષા",
            AppLanguage.KANNADA to "ಭಾಷೆ",
            AppLanguage.MALAYALAM to "ഭാഷ",
            AppLanguage.PUNJABI to "ਭਾਸ਼ਾ"
        ),
        StringKey.TODAY_SCHEDULE_TITLE to mapOf(
            AppLanguage.ENGLISH to "Today's Schedule",
            AppLanguage.HINDI to "आज का शेड्यूल",
            AppLanguage.BENGALI to "আজকের রুটিন",
            AppLanguage.TELUGU to "ఈరోజు షెడ్యూల్",
            AppLanguage.MARATHI to "आजचे वेळापत्रक",
            AppLanguage.TAMIL to "இன்றைய அட்டவணை",
            AppLanguage.GUJARATI to "આજનું શેડ્યૂલ",
            AppLanguage.KANNADA to "ಇಂದಿನ ವೇಳಾಪಟ್ಟಿ",
            AppLanguage.MALAYALAM to "ഇന്നത്തെ ഷെഡ്യൂൾ",
            AppLanguage.PUNJABI to "ਅੱਜ ਦਾ ਸ਼ਡਿਊਲ"
        ),
        StringKey.NO_DOSES_TODAY to mapOf(
            AppLanguage.ENGLISH to "No medication doses scheduled for this filter.",
            AppLanguage.HINDI to "इस फिल्टर के लिए कोई दवा शेड्यूल नहीं है।",
            AppLanguage.BENGALI to "কোন ওষুধ নির্ধারিত নেই।",
            AppLanguage.TELUGU to "ఈ ఫిల్టర్‌లో మందులు లేవు.",
            AppLanguage.MARATHI to "कोणतेही औषध प्रलंबित नाही.",
            AppLanguage.TAMIL to "மருந்துகள் எதுவும் இல்லை.",
            AppLanguage.GUJARATI to "કોઈ દવા શેડ્યૂલ નથી.",
            AppLanguage.KANNADA to "ಯಾವುದೇ ಔಷಧಿ ನಿಗದಿಯಾಗಿಲ್ಲ.",
            AppLanguage.MALAYALAM to "മരുന്നുകൾ ഷെഡ്യൂൾ ചെയ്തിട്ടില്ല.",
            AppLanguage.PUNJABI to "ਕੋਈ ਦਵਾਈ ਸ਼ਡਿਊਲ ਨਹੀਂ ਹੈ।"
        ),
        StringKey.ADHERENCE_RATE to mapOf(
            AppLanguage.ENGLISH to "Adherence Rate",
            AppLanguage.HINDI to "नियमितता दर (Adherence)",
            AppLanguage.BENGALI to "নিয়মিততার হার",
            AppLanguage.TELUGU to "సమ్మతి శాతం",
            AppLanguage.MARATHI to "औषध नियमितता दर",
            AppLanguage.TAMIL to "பின்பற்றும் வீதம்",
            AppLanguage.GUJARATI to "નિયમિતતા દર",
            AppLanguage.KANNADA to "ಅನುಸರಣೆ ಪ್ರಮಾಣ",
            AppLanguage.MALAYALAM to "പാലിക്കൽ നിരക്ക്",
            AppLanguage.PUNJABI to "ਨਿਯਮਤਤਾ ਦਰ"
        ),
        StringKey.ALL_MEDICINES_TITLE to mapOf(
            AppLanguage.ENGLISH to "Your Medications",
            AppLanguage.HINDI to "आपकी दवाइयां",
            AppLanguage.BENGALI to "আপনার ওষুধসমূহ",
            AppLanguage.TELUGU to "మీ మందుల జాబితా",
            AppLanguage.MARATHI to "तुमची औषधे",
            AppLanguage.TAMIL to "உங்கள் மருந்துகள்",
            AppLanguage.GUJARATI to "તમારી દવાઓ",
            AppLanguage.KANNADA to "ನಿಮ್ಮ ಔಷಧಿಗಳು",
            AppLanguage.MALAYALAM to "നിങ്ങളുടെ മരുന്നുകൾ",
            AppLanguage.PUNJABI to "ਤੁਹਾਡੀਆਂ ਦਵਾਈਆਂ"
        ),
        StringKey.NO_MEDICINES_ADDED to mapOf(
            AppLanguage.ENGLISH to "No medicines added yet. Tap + to add one.",
            AppLanguage.HINDI to "अभी तक कोई दवा नहीं जोड़ी गई। + दबाकर जोड़ें।",
            AppLanguage.BENGALI to "এখনও কোনো ওষুধ যোগ করা হয়নি। + চাপুন।",
            AppLanguage.TELUGU to "ఇంకా మందులు జోడించబడలేదు. + నొక్కండి.",
            AppLanguage.MARATHI to "अद्याप कोणतीही औषधे जोडलेली नाहीत. + दाबा.",
            AppLanguage.TAMIL to "மருந்துகள் சேர்க்கப்படவில்லை. + ஐ அழுத்தவும்.",
            AppLanguage.GUJARATI to "હજી સુધી કોઈ દવા ઉમેરાઈ નથી. + દબાવો.",
            AppLanguage.KANNADA to "ಇನ್ನೂ ಔಷಧಿ ಸೇರಿಸಲಾಗಿಲ್ಲ. + ಒತ್ತಿರಿ.",
            AppLanguage.MALAYALAM to "മരുന്നുകൾ ചേർത്തിട്ടില്ല. + അമർത്തുക.",
            AppLanguage.PUNJABI to "ਕੋਈ ਦਵਾਈ ਸ਼ਾਮਲ ਨਹੀਂ ਕੀਤੀ ਗਈ। + ਦਬਾਓ।"
        ),
        StringKey.DAILY_LOGS_TITLE to mapOf(
            AppLanguage.ENGLISH to "Daily Intake Logs",
            AppLanguage.HINDI to "दैनिक खुराक लॉग",
            AppLanguage.BENGALI to "দৈনিক ডোজের ইতিহাস",
            AppLanguage.TELUGU to "రోజువారీ లాగ్‌లు",
            AppLanguage.MARATHI to "दैनिक नोंदी",
            AppLanguage.TAMIL to "தினசரி பதிவுகள்",
            AppLanguage.GUJARATI to "દૈનિક લોગ",
            AppLanguage.KANNADA to "ದೈನಂದಿನ ಲಾಗ್‌ಗಳು",
            AppLanguage.MALAYALAM to "ദിവസേനയുള്ള ലോഗുകൾ",
            AppLanguage.PUNJABI to "ਰੋਜ਼ਾਨਾ ਲੌਗ"
        ),
        StringKey.NO_HISTORY_LOGS to mapOf(
            AppLanguage.ENGLISH to "No intake records for this period.",
            AppLanguage.HINDI to "इस अवधि के लिए कोई रिकॉर्ड नहीं मिला।",
            AppLanguage.BENGALI to "এই সময়ের জন্য কোনো রেকর্ড নেই।",
            AppLanguage.TELUGU to "ఈ కాలానికి రికార్డులు లేవు.",
            AppLanguage.MARATHI to "या कालावधीसाठी नोंदी नाहीत.",
            AppLanguage.TAMIL to "பதிவுகள் இல்லை.",
            AppLanguage.GUJARATI to "આ સમયગાળા માટે કોઈ રેકોર્ડ નથી.",
            AppLanguage.KANNADA to "ಯಾವುದೇ ದಾಖಲೆಗಳಿಲ್ಲ.",
            AppLanguage.MALAYALAM to "രേഖകളൊന്നുമില്ല.",
            AppLanguage.PUNJABI to "ਕੋਈ ਰਿਕਾਰਡ ਨਹੀਂ ਹੈ।"
        ),
        StringKey.INSTRUCTIONS_LABEL to mapOf(
            AppLanguage.ENGLISH to "Instructions (e.g. after meals)",
            AppLanguage.HINDI to "निर्देश (उदा. खाने के बाद लें)",
            AppLanguage.BENGALI to "নির্দেশনা (যেমন খাওয়ার পর)",
            AppLanguage.TELUGU to "సూచనలు (ఉదా. భోజనం తర్వాత)",
            AppLanguage.MARATHI to "सूचना (उदा. जेवणानंतर)",
            AppLanguage.TAMIL to "வழிமுறைகள் (எ.கா. உணவுக்குப் பின்)",
            AppLanguage.GUJARATI to "સૂચનાઓ (દા.ત. જમ્યા પછી)",
            AppLanguage.KANNADA to "ಸೂಚನೆಗಳು (ಊಟದ ನಂತರ)",
            AppLanguage.MALAYALAM to "നിർദ്ദേശങ്ങൾ (ഭക്ഷണത്തിന് ശേഷം)",
            AppLanguage.PUNJABI to "ਹਿਦਾਇਤਾਂ (ਜਿਵੇਂ ਖਾਣ ਤੋਂ ਬਾਅਦ)"
        ),
        StringKey.DAYS_LABEL to mapOf(
            AppLanguage.ENGLISH to "Days of Week",
            AppLanguage.HINDI to "सप्ताह के दिन",
            AppLanguage.BENGALI to "সপ্তাহের দিনগুলি",
            AppLanguage.TELUGU to "వారంలోని రోజులు",
            AppLanguage.MARATHI to "आठवड्याचे दिवस",
            AppLanguage.TAMIL to "வாரத்தின் நாட்கள்",
            AppLanguage.GUJARATI to "અઠવાડિયાના દિવસો",
            AppLanguage.KANNADA to "ವಾರದ ದಿನಗಳು",
            AppLanguage.MALAYALAM to "ആഴ്ചയിലെ ദിവസങ്ങൾ",
            AppLanguage.PUNJABI to "ਹਫ਼ਤੇ ਦੇ ਦਿਨ"
        )
    )
}
