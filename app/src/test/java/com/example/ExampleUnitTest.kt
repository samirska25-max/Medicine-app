package com.example

import com.example.data.MedicineEntity
import com.example.data.MedicineForm
import com.example.data.TimingSlot
import com.example.util.AppLanguage
import com.example.util.LanguageManager
import com.example.util.MealTimes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ExampleUnitTest {

    @Test
    fun testMealTimesDefaultAndOffset() {
        val defaultMeals = MealTimes()
        assertEquals("08:00", defaultMeals.breakfast)
        assertEquals("13:00", defaultMeals.lunch)
        assertEquals("20:00", defaultMeals.dinner)
        assertEquals("22:00", defaultMeals.bedtime)

        // Test offset calculations
        assertEquals("07:30", MealTimes.offsetMinutes("08:00", -30))
        assertEquals("08:30", MealTimes.offsetMinutes("08:00", 30))
        assertEquals("12:30", MealTimes.offsetMinutes("13:00", -30))
        assertEquals("13:30", MealTimes.offsetMinutes("13:00", 30))
        assertEquals("19:30", MealTimes.offsetMinutes("20:00", -30))
        assertEquals("20:30", MealTimes.offsetMinutes("20:00", 30))
    }

    @Test
    fun testCustomMealTimesResolution() {
        // User customizes breakfast to 09:15, lunch to 14:00, dinner to 21:00
        val customMeals = MealTimes(
            breakfast = "09:15",
            lunch = "14:00",
            dinner = "21:00",
            bedtime = "23:00"
        )

        val beforeBreakfastMed = MedicineEntity(
            name = "Thyroxine",
            dosage = "1 Tablet",
            timingSlot = TimingSlot.BEFORE_BREAKFAST.name
        )
        val afterBreakfastMed = MedicineEntity(
            name = "Vitamin C",
            dosage = "1 Tablet",
            timingSlot = TimingSlot.AFTER_BREAKFAST.name
        )
        val beforeLunchMed = MedicineEntity(
            name = "Antacid",
            dosage = "10 ml",
            timingSlot = TimingSlot.BEFORE_LUNCH.name
        )
        val afterDinnerMed = MedicineEntity(
            name = "Metformin",
            dosage = "1 Tablet",
            timingSlot = TimingSlot.AFTER_DINNER.name
        )

        // Verify resolved times dynamically adapt to custom meal schedules
        assertEquals("08:45", beforeBreakfastMed.getResolvedTime(customMeals))
        assertEquals("09:45", afterBreakfastMed.getResolvedTime(customMeals))
        assertEquals("13:30", beforeLunchMed.getResolvedTime(customMeals))
        assertEquals("21:30", afterDinnerMed.getResolvedTime(customMeals))
    }

    @Test
    fun testPeriodicMedicineDosing() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startCal = Calendar.getInstance()
        startCal.set(2026, Calendar.SEPTEMBER, 1)
        val startStr = sdf.format(startCal.time)

        // Medicine taken every 7 days (Periodic, non-regular)
        val periodicMed7Days = MedicineEntity(
            name = "Methotrexate",
            dosage = "1 Tablet",
            isRegular = false,
            intervalDays = 7,
            startDate = startStr
        )

        // On day 0 (Sep 1): Due!
        val testCal0 = Calendar.getInstance().apply { set(2026, Calendar.SEPTEMBER, 1) }
        assertTrue(periodicMed7Days.isDueOnDate(testCal0))

        // On day 3 (Sep 4): Not due
        val testCal3 = Calendar.getInstance().apply { set(2026, Calendar.SEPTEMBER, 4) }
        assertFalse(periodicMed7Days.isDueOnDate(testCal3))

        // On day 7 (Sep 8): Due!
        val testCal7 = Calendar.getInstance().apply { set(2026, Calendar.SEPTEMBER, 8) }
        assertTrue(periodicMed7Days.isDueOnDate(testCal7))

        // On day 14 (Sep 15): Due!
        val testCal14 = Calendar.getInstance().apply { set(2026, Calendar.SEPTEMBER, 15) }
        assertTrue(periodicMed7Days.isDueOnDate(testCal14))
    }

    @Test
    fun testIndianLanguagesTranslations() {
        // Test Hindi
        assertEquals("दवा अलार्म और ट्रैकर", LanguageManager.get("app_title", AppLanguage.HINDI))
        assertEquals("आज", LanguageManager.get("today", AppLanguage.HINDI))
        assertEquals("दवाएं", LanguageManager.get("medicines", AppLanguage.HINDI))
        assertEquals("प्रत्येक {d} दिन", LanguageManager.get("periodic_interval", AppLanguage.HINDI))
        assertEquals("दवा लें", LanguageManager.get("mark_taken", AppLanguage.HINDI))
        assertEquals("ले ली", LanguageManager.get("taken", AppLanguage.HINDI))

        // Test Tamil
        assertEquals("இன்று", LanguageManager.get("today", AppLanguage.TAMIL))
        assertEquals("ஒவ்வொரு {d} நாட்களும்", LanguageManager.get("periodic_interval", AppLanguage.TAMIL))

        // Test Bengali
        assertEquals("আজ", LanguageManager.get("today", AppLanguage.BENGALI))
        assertEquals("প্রতি {d} দিন", LanguageManager.get("periodic_interval", AppLanguage.BENGALI))

        // Test Telugu
        assertEquals("ఈరోజు", LanguageManager.get("today", AppLanguage.TELUGU))
        assertEquals("ప్రతి {d} రోజులు", LanguageManager.get("periodic_interval", AppLanguage.TELUGU))

        // Test all Indian languages in enum
        val expectedLanguages = listOf(
            AppLanguage.HINDI,
            AppLanguage.BENGALI,
            AppLanguage.TELUGU,
            AppLanguage.MARATHI,
            AppLanguage.TAMIL,
            AppLanguage.GUJARATI,
            AppLanguage.KANNADA,
            AppLanguage.MALAYALAM,
            AppLanguage.PUNJABI
        )
        for (lang in expectedLanguages) {
            val appTitle = LanguageManager.get("app_title", lang)
            assertNotNull(appTitle)
            assertTrue(appTitle.isNotBlank())
        }
    }
}

