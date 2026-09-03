package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class MealSchedule(
    val breakfastTime: String = "08:00",
    val lunchTime: String = "13:00",
    val snacksTime: String = "17:00",
    val dinnerTime: String = "20:00",
    val bedtime: String = "22:00",
    val beforeBreakfastTime: String = "07:30",
    val afterBreakfastTime: String = "08:30",
    val beforeLunchTime: String = "12:30",
    val afterLunchTime: String = "13:30",
    val afternoonSnackTime: String = "17:00",
    val beforeDinnerTime: String = "19:30",
    val afterDinnerTime: String = "20:30"
) {
    fun getTimeForSlot(slot: RoutineSlot): String {
        return when (slot) {
            RoutineSlot.BEFORE_BREAKFAST -> beforeBreakfastTime
            RoutineSlot.AFTER_BREAKFAST -> afterBreakfastTime
            RoutineSlot.BEFORE_LUNCH -> beforeLunchTime
            RoutineSlot.AFTER_LUNCH -> afterLunchTime
            RoutineSlot.AFTERNOON_SNACK -> afternoonSnackTime
            RoutineSlot.BEFORE_DINNER -> beforeDinnerTime
            RoutineSlot.AFTER_DINNER -> afterDinnerTime
            RoutineSlot.BEDTIME -> bedtime
        }
    }

    companion object {
        fun offsetTime(timeStr: String, offsetMinutes: Int): String {
            return try {
                val parts = timeStr.split(":")
                val h = parts[0].toInt()
                val m = parts[1].toInt()
                var total = h * 60 + m + offsetMinutes
                if (total < 0) total += 24 * 60
                total %= (24 * 60)
                val newH = total / 60
                val newM = total % 60
                String.format(Locale.getDefault(), "%02d:%02d", newH, newM)
            } catch (_: Exception) {
                timeStr
            }
        }
    }
}

class MealScheduleManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("meal_schedule_prefs", Context.MODE_PRIVATE)

    private val _schedule = MutableStateFlow(loadSchedule())
    val schedule: StateFlow<MealSchedule> = _schedule.asStateFlow()

    private fun loadSchedule(): MealSchedule {
        val breakfast = prefs.getString("breakfast_time", "08:00") ?: "08:00"
        val lunch = prefs.getString("lunch_time", "13:00") ?: "13:00"
        val snacks = prefs.getString("snacks_time", "17:00") ?: "17:00"
        val dinner = prefs.getString("dinner_time", "20:00") ?: "20:00"
        val bedtime = prefs.getString("bedtime", "22:00") ?: "22:00"

        val beforeB = prefs.getString("before_breakfast", MealSchedule.offsetTime(breakfast, -30))
            ?: MealSchedule.offsetTime(breakfast, -30)
        val afterB = prefs.getString("after_breakfast", MealSchedule.offsetTime(breakfast, 30))
            ?: MealSchedule.offsetTime(breakfast, 30)

        val beforeL = prefs.getString("before_lunch", MealSchedule.offsetTime(lunch, -30))
            ?: MealSchedule.offsetTime(lunch, -30)
        val afterL = prefs.getString("after_lunch", MealSchedule.offsetTime(lunch, 30))
            ?: MealSchedule.offsetTime(lunch, 30)

        val afternoonS = prefs.getString("afternoon_snack", snacks) ?: snacks

        val beforeD = prefs.getString("before_dinner", MealSchedule.offsetTime(dinner, -30))
            ?: MealSchedule.offsetTime(dinner, -30)
        val afterD = prefs.getString("after_dinner", MealSchedule.offsetTime(dinner, 30))
            ?: MealSchedule.offsetTime(dinner, 30)

        return MealSchedule(
            breakfastTime = breakfast,
            lunchTime = lunch,
            snacksTime = snacks,
            dinnerTime = dinner,
            bedtime = bedtime,
            beforeBreakfastTime = beforeB,
            afterBreakfastTime = afterB,
            beforeLunchTime = beforeL,
            afterLunchTime = afterL,
            afternoonSnackTime = afternoonS,
            beforeDinnerTime = beforeD,
            afterDinnerTime = afterD
        )
    }

    fun updateMealTimes(
        breakfast: String,
        lunch: String,
        snacks: String,
        dinner: String,
        bedtime: String = _schedule.value.bedtime,
        autoRecalculateSlots: Boolean = true
    ) {
        val beforeB = if (autoRecalculateSlots) MealSchedule.offsetTime(breakfast, -30) else _schedule.value.beforeBreakfastTime
        val afterB = if (autoRecalculateSlots) MealSchedule.offsetTime(breakfast, 30) else _schedule.value.afterBreakfastTime
        val beforeL = if (autoRecalculateSlots) MealSchedule.offsetTime(lunch, -30) else _schedule.value.beforeLunchTime
        val afterL = if (autoRecalculateSlots) MealSchedule.offsetTime(lunch, 30) else _schedule.value.afterLunchTime
        val afternoonS = snacks
        val beforeD = if (autoRecalculateSlots) MealSchedule.offsetTime(dinner, -30) else _schedule.value.beforeDinnerTime
        val afterD = if (autoRecalculateSlots) MealSchedule.offsetTime(dinner, 30) else _schedule.value.afterDinnerTime

        prefs.edit()
            .putString("breakfast_time", breakfast)
            .putString("lunch_time", lunch)
            .putString("snacks_time", snacks)
            .putString("dinner_time", dinner)
            .putString("bedtime", bedtime)
            .putString("before_breakfast", beforeB)
            .putString("after_breakfast", afterB)
            .putString("before_lunch", beforeL)
            .putString("after_lunch", afterL)
            .putString("afternoon_snack", afternoonS)
            .putString("before_dinner", beforeD)
            .putString("after_dinner", afterD)
            .apply()

        _schedule.value = MealSchedule(
            breakfastTime = breakfast,
            lunchTime = lunch,
            snacksTime = snacks,
            dinnerTime = dinner,
            bedtime = bedtime,
            beforeBreakfastTime = beforeB,
            afterBreakfastTime = afterB,
            beforeLunchTime = beforeL,
            afterLunchTime = afterL,
            afternoonSnackTime = afternoonS,
            beforeDinnerTime = beforeD,
            afterDinnerTime = afterD
        )
    }

    fun updateSpecificSlotTime(slot: RoutineSlot, time: String) {
        val current = _schedule.value
        val editor = prefs.edit()
        val updated = when (slot) {
            RoutineSlot.BEFORE_BREAKFAST -> {
                editor.putString("before_breakfast", time)
                current.copy(beforeBreakfastTime = time)
            }
            RoutineSlot.AFTER_BREAKFAST -> {
                editor.putString("after_breakfast", time)
                current.copy(afterBreakfastTime = time)
            }
            RoutineSlot.BEFORE_LUNCH -> {
                editor.putString("before_lunch", time)
                current.copy(beforeLunchTime = time)
            }
            RoutineSlot.AFTER_LUNCH -> {
                editor.putString("after_lunch", time)
                current.copy(afterLunchTime = time)
            }
            RoutineSlot.AFTERNOON_SNACK -> {
                editor.putString("afternoon_snack", time).putString("snacks_time", time)
                current.copy(afternoonSnackTime = time, snacksTime = time)
            }
            RoutineSlot.BEFORE_DINNER -> {
                editor.putString("before_dinner", time)
                current.copy(beforeDinnerTime = time)
            }
            RoutineSlot.AFTER_DINNER -> {
                editor.putString("after_dinner", time)
                current.copy(afterDinnerTime = time)
            }
            RoutineSlot.BEDTIME -> {
                editor.putString("bedtime", time)
                current.copy(bedtime = time)
            }
        }
        editor.apply()
        _schedule.value = updated
    }
}
