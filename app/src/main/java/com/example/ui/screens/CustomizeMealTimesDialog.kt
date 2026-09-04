package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.util.AppLanguage
import com.example.util.LanguageManager
import com.example.util.MealTimes

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomizeMealTimesDialog(
    currentMealTimes: MealTimes,
    language: AppLanguage,
    onSave: (MealTimes) -> Unit,
    onDismiss: () -> Unit
) {
    var breakfast by remember { mutableStateOf(currentMealTimes.breakfast) }
    var lunch by remember { mutableStateOf(currentMealTimes.lunch) }
    var dinner by remember { mutableStateOf(currentMealTimes.dinner) }
    var bedtime by remember { mutableStateOf(currentMealTimes.bedtime) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        ),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 20.dp)
            .imePadding()
            .testTag("customize_meal_times_dialog"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = LanguageManager.get("meal_schedule_title", language),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Customize your daily meal routine",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info banner explaining dynamic synchronization
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = LanguageManager.get("meal_schedule_desc", language),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            lineHeight = 18.sp
                        )
                    }
                }

                // 1. Breakfast Section
                MealItemCard(
                    title = LanguageManager.get("meal_breakfast", language),
                    time = breakfast,
                    icon = Icons.Default.WbSunny,
                    accentColor = Color(0xFFEA580C),
                    presets = listOf("07:00", "07:30", "08:00", "08:30", "09:00"),
                    subtext = "Before Breakfast: ${MealTimes.formatTo12Hour(MealTimes.offsetMinutes(breakfast, -30))} • After Breakfast: ${MealTimes.formatTo12Hour(MealTimes.offsetMinutes(breakfast, 30))}",
                    onTimeChange = { breakfast = it }
                )

                // 2. Lunch Section
                MealItemCard(
                    title = LanguageManager.get("meal_lunch", language),
                    time = lunch,
                    icon = Icons.Default.LightMode,
                    accentColor = Color(0xFFD97706),
                    presets = listOf("12:00", "12:30", "13:00", "13:30", "14:00"),
                    subtext = "Before Lunch: ${MealTimes.formatTo12Hour(MealTimes.offsetMinutes(lunch, -30))} • After Lunch: ${MealTimes.formatTo12Hour(MealTimes.offsetMinutes(lunch, 30))}",
                    onTimeChange = { lunch = it }
                )

                // 3. Dinner Section
                MealItemCard(
                    title = LanguageManager.get("meal_dinner", language),
                    time = dinner,
                    icon = Icons.Default.DarkMode,
                    accentColor = Color(0xFF7C3AED),
                    presets = listOf("19:00", "19:30", "20:00", "20:30", "21:00"),
                    subtext = "Before Dinner: ${MealTimes.formatTo12Hour(MealTimes.offsetMinutes(dinner, -30))} • After Dinner: ${MealTimes.formatTo12Hour(MealTimes.offsetMinutes(dinner, 30))}",
                    onTimeChange = { dinner = it }
                )

                // 4. Bedtime Section
                MealItemCard(
                    title = LanguageManager.get("meal_bedtime", language),
                    time = bedtime,
                    icon = Icons.Default.DarkMode,
                    accentColor = Color(0xFF1E3A8A),
                    presets = listOf("21:30", "22:00", "22:30", "23:00", "23:30"),
                    subtext = "Bedtime Dose Alarm: ${MealTimes.formatTo12Hour(bedtime)}",
                    onTimeChange = { bedtime = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = MealTimes(
                        breakfast = breakfast.trim().ifBlank { "08:00" },
                        lunch = lunch.trim().ifBlank { "13:00" },
                        dinner = dinner.trim().ifBlank { "20:00" },
                        bedtime = bedtime.trim().ifBlank { "22:00" }
                    )
                    onSave(updated)
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_meal_times_btn")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = LanguageManager.get("save_meal_times", language),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("cancel_meal_times_btn")
            ) {
                Text(LanguageManager.get("cancel", language))
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MealItemCard(
    title: String,
    time: String,
    icon: ImageVector,
    accentColor: Color,
    presets: List<String>,
    subtext: String,
    onTimeChange: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Icon + Title + Current Time Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = accentColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                        }
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Time Display with quick stepper
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { onTimeChange(MealTimes.offsetMinutes(time, -15)) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Minus 15m", modifier = Modifier.size(16.dp))
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Text(
                            text = MealTimes.formatTo12Hour(time),
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = { onTimeChange(MealTimes.offsetMinutes(time, 15)) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Plus 15m", modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Quick preset chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                presets.forEach { preset ->
                    val isSelected = time == preset
                    FilterChip(
                        selected = isSelected,
                        onClick = { onTimeChange(preset) },
                        label = { Text(MealTimes.formatTo12Hour(preset), style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Before / After slot preview subtext
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}
