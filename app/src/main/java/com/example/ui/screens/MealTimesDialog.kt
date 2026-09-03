package com.example.ui.screens

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.data.MealSchedule
import com.example.data.RoutineSlot
import java.util.Calendar
import java.util.Locale

@Composable
fun MealTimesDialog(
    initialSchedule: MealSchedule,
    onDismiss: () -> Unit,
    onSave: (
        breakfast: String,
        lunch: String,
        snacks: String,
        dinner: String,
        bedtime: String,
        detailedSlots: Map<RoutineSlot, String>
    ) -> Unit
) {
    val context = LocalContext.current

    var breakfast by remember { mutableStateOf(initialSchedule.breakfastTime) }
    var lunch by remember { mutableStateOf(initialSchedule.lunchTime) }
    var snacks by remember { mutableStateOf(initialSchedule.snacksTime) }
    var dinner by remember { mutableStateOf(initialSchedule.dinnerTime) }
    var bedtime by remember { mutableStateOf(initialSchedule.bedtime) }

    // Detailed routine slots
    var beforeB by remember { mutableStateOf(initialSchedule.beforeBreakfastTime) }
    var afterB by remember { mutableStateOf(initialSchedule.afterBreakfastTime) }
    var beforeL by remember { mutableStateOf(initialSchedule.beforeLunchTime) }
    var afterL by remember { mutableStateOf(initialSchedule.afterLunchTime) }
    var beforeD by remember { mutableStateOf(initialSchedule.beforeDinnerTime) }
    var afterD by remember { mutableStateOf(initialSchedule.afterDinnerTime) }

    var showAdvancedSlots by remember { mutableStateOf(false) }

    fun openPicker(currentTime: String, onPicked: (String) -> Unit) {
        val parts = currentTime.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val formatted = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                onPicked(formatted)
            },
            h,
            m,
            false
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .testTag("meal_times_dialog"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Customize Meal Times",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Adjust the times for your daily meals. Scheduled medication routine slots (Before / After meals) will automatically adjust.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 1. Breakfast Card
                MealTimeItemRow(
                    title = "Breakfast Time",
                    time = breakfast,
                    subtitle = "Before: $beforeB • After: $afterB",
                    icon = Icons.Default.Coffee,
                    testTag = "meal_time_breakfast",
                    onClick = {
                        openPicker(breakfast) { newTime ->
                            breakfast = newTime
                            beforeB = MealSchedule.offsetTime(newTime, -30)
                            afterB = MealSchedule.offsetTime(newTime, 30)
                        }
                    }
                )

                // 2. Lunch Card
                MealTimeItemRow(
                    title = "Lunch Time",
                    time = lunch,
                    subtitle = "Before: $beforeL • After: $afterL",
                    icon = Icons.Default.Restaurant,
                    testTag = "meal_time_lunch",
                    onClick = {
                        openPicker(lunch) { newTime ->
                            lunch = newTime
                            beforeL = MealSchedule.offsetTime(newTime, -30)
                            afterL = MealSchedule.offsetTime(newTime, 30)
                        }
                    }
                )

                // 3. Snacks Card
                MealTimeItemRow(
                    title = "Snacks Time",
                    time = snacks,
                    subtitle = "Afternoon / Evening Snack",
                    icon = Icons.Default.Fastfood,
                    testTag = "meal_time_snacks",
                    onClick = {
                        openPicker(snacks) { newTime ->
                            snacks = newTime
                        }
                    }
                )

                // 4. Dinner Card
                MealTimeItemRow(
                    title = "Dinner Time",
                    time = dinner,
                    subtitle = "Before: $beforeD • After: $afterD",
                    icon = Icons.Default.DinnerDining,
                    testTag = "meal_time_dinner",
                    onClick = {
                        openPicker(dinner) { newTime ->
                            dinner = newTime
                            beforeD = MealSchedule.offsetTime(newTime, -30)
                            afterD = MealSchedule.offsetTime(newTime, 30)
                        }
                    }
                )

                // 5. Bedtime Card
                MealTimeItemRow(
                    title = "Bedtime",
                    time = bedtime,
                    subtitle = "Night dose routine",
                    icon = Icons.Default.Bedtime,
                    testTag = "meal_time_bedtime",
                    onClick = {
                        openPicker(bedtime) { newTime ->
                            bedtime = newTime
                        }
                    }
                )

                // Expandable Section: Fine-tune Individual Routine Slots
                OutlinedButton(
                    onClick = { showAdvancedSlots = !showAdvancedSlots },
                    modifier = Modifier.fillMaxWidth().testTag("toggle_advanced_slots")
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (showAdvancedSlots) "Hide Routine Slots" else "Fine-tune Individual Slots (Before / After)")
                }

                AnimatedVisibility(visible = showAdvancedSlots) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Individual Slot Overrides:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )

                        SlotOverrideRow("Before Breakfast", beforeB) {
                            openPicker(beforeB) { beforeB = it }
                        }
                        SlotOverrideRow("After Breakfast", afterB) {
                            openPicker(afterB) { afterB = it }
                        }
                        SlotOverrideRow("Before Lunch", beforeL) {
                            openPicker(beforeL) { beforeL = it }
                        }
                        SlotOverrideRow("After Lunch", afterL) {
                            openPicker(afterL) { afterL = it }
                        }
                        SlotOverrideRow("Before Dinner", beforeD) {
                            openPicker(beforeD) { beforeD = it }
                        }
                        SlotOverrideRow("After Dinner", afterD) {
                            openPicker(afterD) { afterD = it }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val map = mapOf(
                        RoutineSlot.BEFORE_BREAKFAST to beforeB,
                        RoutineSlot.AFTER_BREAKFAST to afterB,
                        RoutineSlot.BEFORE_LUNCH to beforeL,
                        RoutineSlot.AFTER_LUNCH to afterL,
                        RoutineSlot.AFTERNOON_SNACK to snacks,
                        RoutineSlot.BEFORE_DINNER to beforeD,
                        RoutineSlot.AFTER_DINNER to afterD,
                        RoutineSlot.BEDTIME to bedtime
                    )
                    onSave(breakfast, lunch, snacks, dinner, bedtime, map)
                },
                modifier = Modifier.testTag("save_meal_times_button")
            ) {
                Text("Save Meal Times")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_meal_times_button")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun MealTimeItemRow(
    title: String,
    time: String,
    subtitle: String,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.padding(10.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clickable(onClick = onClick)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = time,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun SlotOverrideRow(
    title: String,
    time: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = time,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
