package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

enum class RoutineSlot(
    val slotId: String,
    val title: String,
    val defaultTime: String,
    val period: String
) {
    BEFORE_BREAKFAST(
        slotId = "BEFORE_BREAKFAST",
        title = "Before Breakfast",
        defaultTime = "07:30",
        period = "Morning"
    ),
    AFTER_BREAKFAST(
        slotId = "AFTER_BREAKFAST",
        title = "After Breakfast",
        defaultTime = "08:30",
        period = "Morning"
    ),
    BEFORE_LUNCH(
        slotId = "BEFORE_LUNCH",
        title = "Before Lunch",
        defaultTime = "12:30",
        period = "Afternoon"
    ),
    AFTER_LUNCH(
        slotId = "AFTER_LUNCH",
        title = "After Lunch",
        defaultTime = "13:30",
        period = "Afternoon"
    ),
    AFTERNOON_SNACK(
        slotId = "AFTERNOON_SNACK",
        title = "Afternoon / Evening Snack",
        defaultTime = "17:00",
        period = "Evening"
    ),
    BEFORE_DINNER(
        slotId = "BEFORE_DINNER",
        title = "Before Dinner",
        defaultTime = "19:30",
        period = "Night"
    ),
    AFTER_DINNER(
        slotId = "AFTER_DINNER",
        title = "After Dinner",
        defaultTime = "20:30",
        period = "Night"
    ),
    BEDTIME(
        slotId = "BEDTIME",
        title = "Bedtime",
        defaultTime = "22:00",
        period = "Night"
    );

    companion object {
        fun fromId(id: String): RoutineSlot {
            return entries.firstOrNull { it.slotId.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) }
                ?: BEFORE_BREAKFAST
        }
    }
}
