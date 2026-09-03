package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MealSchedule
import com.example.data.Medicine
import com.example.data.RoutineSlot
import com.example.ui.theme.MedSuccessBg
import com.example.ui.theme.MedSuccessBorder
import com.example.ui.theme.MedSuccessDark
import com.example.ui.theme.MedSuccessGreen
import com.example.ui.viewmodel.ChecklistItem
import com.example.ui.viewmodel.DailyProgressStats
import com.example.ui.viewmodel.RoutineSlotGroup

@Composable
fun DailyChecklistScreen(
    dateDisplay: String,
    progressStats: DailyProgressStats,
    groups: List<RoutineSlotGroup>,
    selectedFilter: String,
    mealSchedule: MealSchedule = MealSchedule(),
    onOpenMealTimes: () -> Unit = {},
    onFilterSelect: (String) -> Unit,
    onToggleTaken: (Medicine, RoutineSlot, Boolean) -> Unit,
    onResetToday: () -> Unit,
    onTestAlarm: () -> Unit,
    onAddMedicine: () -> Unit
) {
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset Today's Checklist?") },
            text = { Text("This will mark all medicines as pending for today. Your routine schedules will remain intact.") },
            confirmButton = {
                Button(
                    onClick = {
                        onResetToday()
                        showResetConfirmDialog = false
                    },
                    modifier = Modifier.testTag("confirm_reset_button")
                ) {
                    Text("Reset Checklist")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("checklist_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Daily Progress Summary Card
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_progress_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = dateDisplay,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Auto-resets every midnight (00:00)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onTestAlarm,
                                modifier = Modifier.testTag("button_test_alarm")
                            ) {
                                Icon(
                                    Icons.Default.NotificationsActive,
                                    contentDescription = "Test Alert Sound",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = { showResetConfirmDialog = true },
                                modifier = Modifier.testTag("button_reset_today")
                            ) {
                                Icon(
                                    Icons.Default.RestartAlt,
                                    contentDescription = "Reset Today",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    // Progress Bar
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Doses Taken: ${progressStats.takenDoses} of ${progressStats.totalDoses}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${progressStats.percentage}% Completed",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (progressStats.percentage == 100) MedSuccessGreen else MaterialTheme.colorScheme.primary
                            )
                        }

                        LinearProgressIndicator(
                            progress = {
                                if (progressStats.totalDoses > 0)
                                    progressStats.takenDoses.toFloat() / progressStats.totalDoses.toFloat()
                                else 0f
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = if (progressStats.percentage == 100) MedSuccessGreen else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }

        // Customized Meal Times Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_meal_schedule"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Restaurant,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Customized Meal Times",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Text(
                            text = "Breakfast: ${mealSchedule.breakfastTime} • Lunch: ${mealSchedule.lunchTime} • Snacks: ${mealSchedule.snacksTime} • Dinner: ${mealSchedule.dinnerTime}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenMealTimes,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("button_customize_meal_times")
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Customize", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // Quick Routine Slot Filters
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { onFilterSelect("ALL") },
                        label = { Text("All Slots") },
                        modifier = Modifier.testTag("filter_all")
                    )
                }
                items(RoutineSlot.entries) { slot ->
                    FilterChip(
                        selected = selectedFilter == slot.name,
                        onClick = { onFilterSelect(slot.name) },
                        label = { Text(slot.title) }
                    )
                }
            }
        }

        // Empty state if no items
        if (groups.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.CheckCircleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "No medications scheduled for this filter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Tap '+ Add Medicine' to set up your routine",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onAddMedicine) {
                            Text("Add Medicine")
                        }
                    }
                }
            }
        }

        // Slot Groups & Cards
        items(groups, key = { it.slot.name }) { group ->
            RoutineSlotSection(
                group = group,
                mealSchedule = mealSchedule,
                onToggleTaken = onToggleTaken
            )
        }
    }
}

@Composable
fun RoutineSlotSection(
    group: RoutineSlotGroup,
    mealSchedule: MealSchedule,
    onToggleTaken: (Medicine, RoutineSlot, Boolean) -> Unit
) {
    val totalInSlot = group.items.size
    val takenInSlot = group.items.count { it.isTaken }
    val isSlotDone = totalInSlot > 0 && takenInSlot == totalInSlot

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("slot_section_${group.slot.name}"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Slot Header
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSlotDone) MedSuccessBg.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isSlotDone) MedSuccessGreen else MaterialTheme.colorScheme.primary)
                    )
                    Column {
                        Text(
                            text = group.slot.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Scheduled: ${mealSchedule.getTimeForSlot(group.slot)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text(
                        text = "$takenInSlot/$totalInSlot Taken",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSlotDone) MedSuccessDark else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // Medicine Cards inside this slot
        group.items.forEach { item ->
            MedicineChecklistCard(
                item = item,
                onToggle = { onToggleTaken(item.medicine, item.slot, item.isTaken) }
            )
        }
    }
}

@Composable
fun MedicineChecklistCard(
    item: ChecklistItem,
    onToggle: () -> Unit
) {
    val isTaken = item.isTaken
    val med = item.medicine

    val cardBgColor by animateColorAsState(
        targetValue = if (isTaken) MedSuccessBg else MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 250),
        label = "card_bg"
    )

    val cardBorderColor by animateColorAsState(
        targetValue = if (isTaken) MedSuccessBorder else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(durationMillis = 250),
        label = "card_border"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .testTag("checklist_card_${med.id}_${item.slot.name}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = BorderStroke(if (isTaken) 2.dp else 1.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isTaken) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Checkbox / Indicator Button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isTaken) MedSuccessGreen else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onToggle)
                    .testTag("checkbox_${med.id}_${item.slot.name}"),
                contentAlignment = Alignment.Center
            ) {
                if (isTaken) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Taken",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Medicine Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = med.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (isTaken) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (isTaken) MedSuccessDark else MaterialTheme.colorScheme.onSurface
                )

                // Exact Formatted Dose Tag (e.g. 'Paracetamol — 1/2 Tablet' or 'Cough Syrup — 5 ml')
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isTaken) MedSuccessBorder.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${med.name} — ${med.getFormattedDose()}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isTaken) MedSuccessDark else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Special instructions if provided
                if (med.specialInstructions.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = med.specialInstructions,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Scheduled alert time badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = med.alertTime,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
