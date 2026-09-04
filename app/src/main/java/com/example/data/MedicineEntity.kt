package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class SlotCategory(val displayName: String, val timeRange: String) {
    MORNING("Morning", "06:00 - 11:59"),
    AFTERNOON("Afternoon", "12:00 - 16:59"),
    EVENING("Evening", "17:00 - 20:59"),
    NIGHT("Night", "21:00 - 05:59");

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

@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dosage: String, // e.g. "Paracetamol 500mg" or "500mg"
    val timingSlot: String = TimingSlot.AFTER_BREAKFAST.name,
    val customTime: String = "08:30", // "HH:mm"
    val frequencyType: String = FrequencyType.DAILY.name,
    val daysOfWeek: String = "MON,TUE,WED,THU,FRI,SAT,SUN", // comma separated
    val intervalHours: Int = 8,
    val stockCount: Int = 30, // Pill counter
    val lowStockThreshold: Int = 5,
    val instructions: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getResolvedTime(): String {
        val slot = TimingSlot.entries.firstOrNull { it.name == timingSlot }
        return if (slot == null || slot == TimingSlot.CUSTOM) {
            customTime.ifBlank { "08:30" }
        } else {
            slot.defaultTime
        }
    }

    fun getResolvedSlotTitle(): String {
        val slot = TimingSlot.entries.firstOrNull { it.name == timingSlot }
        return slot?.title ?: "Custom ($customTime)"
    }

    fun getResolvedCategory(): SlotCategory {
        val slot = TimingSlot.entries.firstOrNull { it.name == timingSlot }
        return if (slot != null && slot != TimingSlot.CUSTOM) {
            slot.category
        } else {
            SlotCategory.fromTime(customTime)
        }
    }

    fun isDueOnDate(calendar: Calendar): Boolean {
        if (!isActive) return false
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
