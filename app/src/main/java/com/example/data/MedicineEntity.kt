package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class SlotCategory(val displayName: String, val timeRange: String) {
    MORNING("Morning", "06:00 AM - 11:59 AM"),
    AFTERNOON("Afternoon", "12:00 PM - 04:59 PM"),
    EVENING("Evening", "05:00 PM - 08:59 PM"),
    NIGHT("Night", "09:00 PM - 05:59 AM");

    companion object {
        fun fromTime(timeStr: String): SlotCategory {
            val hour = try {
                timeStr.split(":").firstOrNull()?.toIntOrNull() ?: 8
            } catch (e: Exception) {
                8
            }
            return when (hour) {
                in 5..11 -> MORNING
                in 12..16 -> AFTERNOON
                in 17..20 -> EVENING
                else -> NIGHT
            }
        }
    }
}

enum class TimingSlot(
    val title: String,
    val defaultTime: String,
    val category: SlotCategory
) {
    BEFORE_BREAKFAST("Before Breakfast", "07:30", SlotCategory.MORNING),
    AFTER_BREAKFAST("After Breakfast", "08:30", SlotCategory.MORNING),
    BEFORE_LUNCH("Before Lunch", "12:30", SlotCategory.AFTERNOON),
    AFTER_LUNCH("After Lunch", "13:30", SlotCategory.AFTERNOON),
    BEFORE_DINNER("Before Dinner", "19:30", SlotCategory.EVENING),
    AFTER_DINNER("After Dinner", "20:30", SlotCategory.EVENING),
    BEDTIME("Bedtime", "22:00", SlotCategory.NIGHT),
    CUSTOM("Custom Specific Time", "09:00", SlotCategory.MORNING);

    companion object {
        fun fromName(name: String): TimingSlot {
            return entries.firstOrNull { it.name == name || it.title.equals(name, ignoreCase = true) } ?: AFTER_BREAKFAST
        }
    }
}

enum class FrequencyType(val title: String) {
    DAILY("Daily"),
    DAYS_OF_WEEK("Specific Days of Week"),
    INTERVAL_HOURS("Interval (Every X Hours)")
}

enum class DoseStatus(val displayName: String) {
    PENDING("Pending"),
    TAKEN("Taken"),
    SKIPPED("Skipped"),
    SNOOZED("Snoozed")
}

enum class MedicineForm(val title: String, val defaultUnit: String) {
    TABLET("Tablet / Capsule", "tablet"),
    LIQUID("Liquid / Syrup", "ml"),
    DROPS("Drops", "drops"),
    INJECTION("Injection", "units"),
    OTHER("Other", "dose");

    companion object {
        fun fromName(name: String): MedicineForm {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: TABLET
        }
    }
}

@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dosage: String, // e.g. "1 Tablet", "1/2 Tablet", "5 ml", "10 ml"
    val medicineForm: String = MedicineForm.TABLET.name, // TABLET, LIQUID, DROPS, INJECTION, OTHER
    val dosagePreset: String = "1 (Full)", // "Quarter", "Half", "Full", "5 ml", etc.
    val timingSlot: String = TimingSlot.AFTER_BREAKFAST.name,
    val customTime: String = "08:30", // "HH:mm"
    val frequencyType: String = FrequencyType.DAILY.name,
    val daysOfWeek: String = "MON,TUE,WED,THU,FRI,SAT,SUN", // comma separated
    val intervalHours: Int = 8,
    val isRegular: Boolean = true, // true = daily/weekly; false = every X days (7, 10, 15 days)
    val intervalDays: Int = 1, // number of days between doses if not regular
    val startDate: String = "", // "yyyy-MM-dd"
    val nextDueDate: String = "", // "yyyy-MM-dd"
    val stockCount: Int = 30, // Pill / ml counter
    val lowStockThreshold: Int = 5,
    val instructions: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getResolvedTime(customMealTimes: com.example.util.MealTimes? = null): String {
        val slot = TimingSlot.entries.firstOrNull { it.name == timingSlot }
        return if (slot == null || slot == TimingSlot.CUSTOM) {
            customTime.ifBlank { "08:30" }
        } else {
            val mt = customMealTimes ?: com.example.util.MealScheduleManager.getCachedMealTimes()
            mt.getTimeForSlot(slot)
        }
    }

    fun getResolvedTime12Hour(customMealTimes: com.example.util.MealTimes? = null): String {
        val time = getResolvedTime(customMealTimes)
        return com.example.util.MealTimes.formatTo12Hour(time)
    }

    fun getResolvedSlotTitle(): String {
        val slot = TimingSlot.entries.firstOrNull { it.name == timingSlot }
        return slot?.title ?: "Custom (${com.example.util.MealTimes.formatTo12Hour(customTime)})"
    }

    fun getStockUnit(): String {
        return when (MedicineForm.fromName(medicineForm)) {
            MedicineForm.TABLET -> if (stockCount == 1) "tablet/capsule" else "tablets/capsules"
            MedicineForm.LIQUID -> "ml"
            MedicineForm.DROPS -> "drops"
            MedicineForm.INJECTION -> "units"
            MedicineForm.OTHER -> "doses"
        }
    }

    fun getStockUnitShort(): String {
        return when (MedicineForm.fromName(medicineForm)) {
            MedicineForm.TABLET -> "pills"
            MedicineForm.LIQUID -> "ml"
            MedicineForm.DROPS -> "drops"
            MedicineForm.INJECTION -> "units"
            MedicineForm.OTHER -> "units"
        }
    }

    fun isLowStock(): Boolean = stockCount <= lowStockThreshold
    fun isOutOfStock(): Boolean = stockCount <= 0

    fun getDoseDecrementAmount(): Int {
        if (medicineForm.equals(MedicineForm.LIQUID.name, ignoreCase = true) || dosage.contains("ml", ignoreCase = true)) {
            val mlMatch = Regex("""(\d+(?:\.\d+)?)\s*ml""", RegexOption.IGNORE_CASE).find(dosage)
            if (mlMatch != null) {
                val value = mlMatch.groupValues[1].toDoubleOrNull() ?: 5.0
                return kotlin.math.ceil(value).toInt().coerceAtLeast(1)
            }
        }
        val digitMatch = Regex("""(\d+)""").find(dosage)
        if (digitMatch != null) {
            return digitMatch.groupValues[1].toIntOrNull()?.coerceAtLeast(1) ?: 1
        }
        return 1
    }

    fun getResolvedCategory(customMealTimes: com.example.util.MealTimes? = null): SlotCategory {
        val slot = TimingSlot.entries.firstOrNull { it.name == timingSlot }
        return if (slot != null && slot != TimingSlot.CUSTOM) {
            val time = getResolvedTime(customMealTimes)
            SlotCategory.fromTime(time)
        } else {
            SlotCategory.fromTime(customTime)
        }
    }

    fun isDueOnDate(calendar: Calendar): Boolean {
        if (!isActive) return false
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val targetDateStr = sdf.format(calendar.time)

        if (!isRegular) {
            if (nextDueDate.isNotBlank()) {
                return nextDueDate == targetDateStr
            }
            if (startDate.isNotBlank()) {
                return try {
                    val start = sdf.parse(startDate) ?: return false
                    val diffDays = ((calendar.time.time - start.time) / (1000 * 60 * 60 * 24)).toInt()
                    diffDays >= 0 && (intervalDays <= 1 || diffDays % intervalDays == 0)
                } catch (e: Exception) {
                    false
                }
            }
            return true
        }

        return when (frequencyType) {
            FrequencyType.DAILY.name -> true
            FrequencyType.DAYS_OF_WEEK.name -> {
                val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.MONDAY -> "MON"
                    Calendar.TUESDAY -> "TUE"
                    Calendar.WEDNESDAY -> "WED"
                    Calendar.THURSDAY -> "THU"
                    Calendar.FRIDAY -> "FRI"
                    Calendar.SATURDAY -> "SAT"
                    Calendar.SUNDAY -> "SUN"
                    else -> "MON"
                }
                daysOfWeek.contains(dayOfWeek)
            }
            FrequencyType.INTERVAL_HOURS.name -> true
            else -> true
        }
    }

    fun computeNextDueDate(fromCalendar: Calendar = Calendar.getInstance()): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance().apply {
            time = fromCalendar.time
            add(Calendar.DAY_OF_YEAR, if (intervalDays > 0) intervalDays else 1)
        }
        return sdf.format(cal.time)
    }
}

@Entity(tableName = "dose_records")
data class DoseRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicineId: Long,
    val medicineName: String,
    val dosage: String,
    val scheduledDate: String, // "yyyy-MM-dd"
    val scheduledTime: String, // "HH:mm"
    val slotName: String, // e.g. "After Breakfast"
    val slotCategory: String, // "MORNING", "AFTERNOON", "EVENING", "NIGHT"
    val status: String = DoseStatus.PENDING.name,
    val actionTimestamp: Long? = null,
    val instructions: String = ""
) {
    fun isTaken(): Boolean = status == DoseStatus.TAKEN.name
    fun isSkipped(): Boolean = status == DoseStatus.SKIPPED.name
    fun isSnoozed(): Boolean = status == DoseStatus.SNOOZED.name
    fun isPending(): Boolean = status == DoseStatus.PENDING.name

    fun getFormattedActionTime(): String {
        return actionTimestamp?.let {
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            sdf.format(it)
        } ?: ""
    }
}
