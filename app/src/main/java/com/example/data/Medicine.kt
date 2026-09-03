package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MedicineForm(val displayName: String) {
    TABLET("Tablet"),
    CAPSULE("Capsule"),
    LIQUID("Liquid / Syrup"),
    OTHER("Other")
}

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val form: String, // "Tablet", "Capsule", "Liquid / Syrup", "Other"
    val dosageQuantity: String, // e.g. "1/2", "5", "10", "1"
    val dosageUnit: String, // "Tablet", "Capsule", "ml", "Puff", "Drops"
    val routineSlots: String, // Comma-separated RoutineSlot names
    val alertTime: String, // "HH:mm" (24-hour format, e.g. "08:30")
    val specialInstructions: String = "", // e.g. "Take with lukewarm water"
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getFormattedDose(): String {
        return when (form) {
            "Liquid / Syrup" -> "$dosageQuantity $dosageUnit"
            "Tablet" -> {
                val unit = if (dosageQuantity == "1" || dosageQuantity == "1/2" || dosageQuantity == "1/4") "Tablet" else "Tablets"
                "$dosageQuantity $unit"
            }
            "Capsule" -> {
                val unit = if (dosageQuantity == "1" || dosageQuantity == "1/2" || dosageQuantity == "1/4") "Capsule" else "Capsules"
                "$dosageQuantity $unit"
            }
            else -> {
                if (dosageUnit.isNotBlank()) "$dosageQuantity $dosageUnit" else dosageQuantity
            }
        }
    }

    fun getSlotsList(): List<String> {
        return if (routineSlots.isBlank()) emptyList() else routineSlots.split(",").map { it.trim() }
    }
}

@Entity(
    tableName = "intake_records",
    primaryKeys = ["medicineId", "routineSlot", "dateString"]
)
data class IntakeRecord(
    val medicineId: Long,
    val routineSlot: String,
    val dateString: String, // format: "yyyy-MM-dd"
    val taken: Boolean,
    val takenTimestamp: Long? = null
)
