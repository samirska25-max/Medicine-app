package com.example.util

import android.content.Context
import com.example.data.TimingSlot
import java.util.Locale

data class MealTimes(
    val breakfast: String = "08:00",
    val lunch: String = "13:00",
    val dinner: String = "20:00",
    val bedtime: String = "22:00"
) {
    fun getTimeForSlot(slot: TimingSlot): String {
        return when (slot) {
            TimingSlot.BEFORE_BREAKFAST -> offsetMinutes(breakfast, -30)
            TimingSlot.AFTER_BREAKFAST -> offsetMinutes(breakfast, 30)
            TimingSlot.BEFORE_LUNCH -> offsetMinutes(lunch, -30)
            TimingSlot.AFTER_LUNCH -> offsetMinutes(lunch, 30)
            TimingSlot.BEFORE_DINNER -> offsetMinutes(dinner, -30)
            TimingSlot.AFTER_DINNER -> offsetMinutes(dinner, 30)
            TimingSlot.BEDTIME -> bedtime
            TimingSlot.CUSTOM -> "09:00"
        }
    }

    companion object {
        fun offsetMinutes(timeStr: String, offsetMinutes: Int): String {
            return try {
                val clean = to24Hour(timeStr)
                val parts = clean.split(":")
                val h = parts[0].trim().toInt()
                val m = parts[1].trim().toInt()
                var total = h * 60 + m + offsetMinutes
                while (total < 0) total += 24 * 60
                total %= (24 * 60)
                val newH = total / 60
                val newM = total % 60
                String.format(Locale.US, "%02d:%02d", newH, newM)
            } catch (e: Exception) {
                timeStr
            }
        }

        /**
         * Formats any 24-hour or raw time string ("08:30", "13:00", "20:00") into
         * user-friendly 12-hour AM/PM format (e.g., "8:30 AM", "1:00 PM", "8:00 PM").
         */
        fun formatTo12Hour(timeStr: String): String {
            if (timeStr.isBlank()) return ""
            val clean = timeStr.trim()
            if (clean.contains("AM", ignoreCase = true) || clean.contains("PM", ignoreCase = true)) {
                return clean
            }
            return try {
                val parts = clean.split(":")
                val h = parts[0].trim().toInt()
                val m = if (parts.size > 1) parts[1].trim().toInt() else 0
                val amPm = if (h >= 12) "PM" else "AM"
                val h12 = when {
                    h == 0 -> 12
                    h > 12 -> h - 12
                    else -> h
                }
                String.format(Locale.US, "%d:%02d %s", h12, m, amPm)
            } catch (e: Exception) {
                timeStr
            }
        }

        /**
         * Parses 12-hour ("8:30 PM", "1:00 PM") or standard ("13:00") time string
         * into standardized 24-hour HH:mm string.
         */
        fun to24Hour(timeStr: String): String {
            if (timeStr.isBlank()) return "08:00"
            val clean = timeStr.trim().uppercase(Locale.US)
            return try {
                if (clean.contains("AM") || clean.contains("PM")) {
                    val isPm = clean.contains("PM")
                    val pureTime = clean.replace("AM", "").replace("PM", "").trim()
                    val parts = pureTime.split(":")
                    var h = parts[0].trim().toInt()
                    val m = if (parts.size > 1) parts[1].trim().toInt() else 0
                    if (isPm && h < 12) h += 12
                    if (!isPm && h == 12) h = 0
                    String.format(Locale.US, "%02d:%02d", h, m)
                } else {
                    val parts = clean.split(":")
                    val h = parts[0].trim().toInt()
                    val m = if (parts.size > 1) parts[1].trim().toInt() else 0
                    String.format(Locale.US, "%02d:%02d", h, m)
                }
            } catch (e: Exception) {
                timeStr
            }
        }
    }
}

object MealScheduleManager {
    private const val PREFS_NAME = "meal_schedule_prefs"
    private const val KEY_BREAKFAST = "meal_breakfast"
    private const val KEY_LUNCH = "meal_lunch"
    private const val KEY_DINNER = "meal_dinner"
    private const val KEY_BEDTIME = "meal_bedtime"

    @Volatile
    private var cachedMealTimes: MealTimes = MealTimes()

    fun init(context: Context): MealTimes {
        cachedMealTimes = getMealTimes(context)
        return cachedMealTimes
    }

    fun getCachedMealTimes(): MealTimes {
        return cachedMealTimes
    }

    fun getMealTimes(context: Context): MealTimes {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val breakfast = prefs.getString(KEY_BREAKFAST, "08:00") ?: "08:00"
        val lunch = prefs.getString(KEY_LUNCH, "13:00") ?: "13:00"
        val dinner = prefs.getString(KEY_DINNER, "20:00") ?: "20:00"
        val bedtime = prefs.getString(KEY_BEDTIME, "22:00") ?: "22:00"
        val result = MealTimes(
            breakfast = breakfast,
            lunch = lunch,
            dinner = dinner,
            bedtime = bedtime
        )
        cachedMealTimes = result
        return result
    }

    fun saveMealTimes(context: Context, mealTimes: MealTimes) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_BREAKFAST, mealTimes.breakfast)
            .putString(KEY_LUNCH, mealTimes.lunch)
            .putString(KEY_DINNER, mealTimes.dinner)
            .putString(KEY_BEDTIME, mealTimes.bedtime)
            .apply()
        cachedMealTimes = mealTimes
    }
}
