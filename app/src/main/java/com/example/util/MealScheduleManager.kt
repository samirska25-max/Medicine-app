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
                val parts = timeStr.split(":")
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
