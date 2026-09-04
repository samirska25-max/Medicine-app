package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DoseRecordEntity
import com.example.data.MedicineEntity
import com.example.data.SlotCategory
import com.example.ui.viewmodel.AdherenceStats
import com.example.util.AppLanguage
import com.example.util.LanguageManager
import com.example.util.MealScheduleManager
import com.example.util.MealTimes
import com.example.util.RingingAlarmInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    todayRecords: List<DoseRecordEntity>,
    allMedicines: List<MedicineEntity>,
    stats: AdherenceStats,
    selectedSlotFilter: String,
    language: AppLanguage = AppLanguage.ENGLISH,
    activeAlarm: RingingAlarmInfo? = null,
    mealTimes: MealTimes = MealScheduleManager.getCachedMealTimes(),
    onFilterSelect: (String) -> Unit,
    onTake: (DoseRecordEntity) -> Unit,
    onSkip: (DoseRecordEntity) -> Unit,
    onSnooze: (DoseRecordEntity) -> Unit,
    onResetToday: () -> Unit,
    onAddMedicine: () -> Unit,
    onStopAlarm: () -> Unit = {},
    onCustomizeMealTimes: () -> Unit = {},
    onRefillStock: (Long, Int) -> Unit = { _, _ -> }
) {
    val dateDisplay = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())
    val medicinesMap = allMedicines.associateBy { it.id }
    var medicineToRefill by remember { mutableStateOf<MedicineEntity?>(null) }
    val lowStockMeds = allMedicines.filter { it.isLowStock() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Active Ringing Alarm Banner if device alarm is active
        if (activeAlarm != null) {
            item {
                ActiveAlarmBanner(
                    alarm = activeAlarm,
                    language = language,
                    onStop = onStopAlarm
                )
            }
        }

        // Low Stock Alert Banner
        if (lowStockMeds.isNotEmpty()) {
            item {
                LowStockAlertCard(
                    lowStockMedicines = lowStockMeds,
                    language = language,
                    onRefillClick = { med -> medicineToRefill = med }
                )
            }
        }

        // Today Summary & Progress Card
        item {
            TodayProgressCard(
                dateDisplay = dateDisplay,
                stats = stats,
                language = language,
                onResetToday = onResetToday
            )
        }

        // Meal Times Summary Card (Breakfast, Lunch, Dinner)
        item {
            MealTimesSummaryCard(
                mealTimes = mealTimes,
                language = language,
                onCustomize = onCustomizeMealTimes
            )
        }

        // Timing Slot Filter Chips
        item {
            SlotFilterRow(
                selectedFilter = selectedSlotFilter,
                language = language,
                onFilterSelect = onFilterSelect
            )
        }

        // Slot Groups: Morning, Afternoon, Evening, Night
        val categoriesToShow = if (selectedSlotFilter == "ALL") {
            listOf(SlotCategory.MORNING, SlotCategory.AFTERNOON, SlotCategory.EVENING, SlotCategory.NIGHT)
        } else {
            listOfNotNull(SlotCategory.entries.firstOrNull { it.name == selectedSlotFilter })
        }

        var anyItemsShown = false
        for (category in categoriesToShow) {
            val itemsForCategory = todayRecords.filter {
                it.slotCategory.equals(category.name, ignoreCase = true)
            }

            if (itemsForCategory.isNotEmpty()) {
                anyItemsShown = true
                item(key = "header_${category.name}") {
                    CategoryHeader(category = category, count = itemsForCategory.size)
                }

                items(itemsForCategory, key = { "dose_${it.id}" }) { record ->
                    val medicine = medicinesMap[record.medicineId]
                    DoseCard(
                        record = record,
                        medicine = medicine,
                        language = language,
                        onTake = { onTake(record) },
                        onSkip = { onSkip(record) },
                        onSnooze = { onSnooze(record) },
                        onRefillClick = {
                            if (medicine != null) {
                                medicineToRefill = medicine
                            }
                        }
                    )
                }
            }
        }

        if (!anyItemsShown) {
            item {
                EmptyTodayView(
                    language = language,
                    onAddMedicine = onAddMedicine
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }

    if (medicineToRefill != null) {
        val med = medicineToRefill!!
        QuickRefillDialog(
            medicine = med,
            language = language,
            onDismiss = { medicineToRefill = null },
            onRefill = { amount ->
                onRefillStock(med.id, amount)
                medicineToRefill = null
            }
        )
    }
}

@Composable
fun ActiveAlarmBanner(
    alarm: RingingAlarmInfo,
    language: AppLanguage,
    onStop: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_alarm_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = "⏰ " + LanguageManager.get("alarm_active", language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "${alarm.medicineName} • ${alarm.dosage} (${alarm.scheduledTime})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                    )
                }
            }

            Button(
                onClick = onStop,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = LanguageManager.get("stop_alarm", language),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TodayProgressCard(
    dateDisplay: String,
    stats: AdherenceStats,
    language: AppLanguage,
    onResetToday: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("today_progress_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
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
                        text = LanguageManager.get("today_schedule", language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = dateDisplay,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                IconButton(
                    onClick = onResetToday,
                    modifier = Modifier.testTag("btn_reset_today")
                ) {
                    Icon(
                        Icons.Default.RestartAlt,
                        contentDescription = "Reset Today",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Adherence Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${stats.takenDoses} / ${stats.totalDoses} " + LanguageManager.get("mark_taken", language),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${stats.adherencePercentage}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                LinearProgressIndicator(
                    progress = { if (stats.totalDoses > 0) stats.takenDoses.toFloat() / stats.totalDoses else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                )
            }

            // Metric Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusPill(label = LanguageManager.get("taken", language), count = stats.takenDoses, color = Color(0xFF16A34A))
                StatusPill(label = LanguageManager.get("pending", language), count = stats.pendingDoses, color = Color(0xFFD97706))
                StatusPill(label = LanguageManager.get("skipped", language), count = stats.skippedDoses, color = Color(0xFFDC2626))
            }
        }
    }
}

@Composable
fun StatusPill(label: String, count: Int, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = "$label: $count",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun SlotFilterRow(
    selectedFilter: String,
    language: AppLanguage,
    onFilterSelect: (String) -> Unit
) {
    val filters = listOf(
        "ALL" to LanguageManager.get("all_doses", language),
        "MORNING" to "🌅 " + LanguageManager.get("morning", language),
        "AFTERNOON" to "☀️ " + LanguageManager.get("afternoon", language),
        "EVENING" to "🌆 " + LanguageManager.get("evening", language),
        "NIGHT" to "🌙 " + LanguageManager.get("night", language)
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.testTag("slot_filter_row")
    ) {
        items(filters) { (key, label) ->
            val isSelected = selectedFilter == key
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelect(key) },
                label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
fun CategoryHeader(category: SlotCategory, count: Int) {
    val (icon, color) = when (category) {
        SlotCategory.MORNING -> Icons.Default.WbSunny to Color(0xFFEA580C)
        SlotCategory.AFTERNOON -> Icons.Default.LightMode to Color(0xFFD97706)
        SlotCategory.EVENING -> Icons.Default.DarkMode to Color(0xFF7C3AED)
        SlotCategory.NIGHT -> Icons.Default.NightsStay to Color(0xFF1E3A8A)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "(${category.timeRange})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.weight(1f))
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun DoseCard(
    record: DoseRecordEntity,
    medicine: MedicineEntity?,
    language: AppLanguage,
    onTake: () -> Unit,
    onSkip: () -> Unit,
    onSnooze: () -> Unit,
    onRefillClick: () -> Unit = {}
) {
    val isTaken = record.isTaken()
    val isSkipped = record.isSkipped()
    val isSnoozed = record.isSnoozed()

    val cardBorderColor by animateColorAsState(
        targetValue = when {
            isTaken -> Color(0xFF16A34A).copy(alpha = 0.5f)
            isSkipped -> Color(0xFFDC2626).copy(alpha = 0.3f)
            isSnoozed -> Color(0xFFD97706).copy(alpha = 0.5f)
            else -> MaterialTheme.colorScheme.outlineVariant
        }, label = "border"
    )

    val formIcon = if (medicine?.medicineForm.equals("LIQUID", ignoreCase = true)) "💧" else "💊"
    val formBadgeText = when (medicine?.medicineForm) {
        "LIQUID" -> LanguageManager.get("liquid_syrup", language)
        "DROPS" -> LanguageManager.get("drops", language)
        "INJECTION" -> LanguageManager.get("injection", language)
        else -> LanguageManager.get("tablet_capsule", language)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dose_card_${record.id}"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, cardBorderColor),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isTaken -> Color(0xFFF0FDF4)
                isSkipped -> Color(0xFFFEF2F2)
                isSnoozed -> Color(0xFFFFFBEB)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Medicine Name & Timing Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "$formIcon ${record.medicineName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = record.dosage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = formBadgeText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (medicine != null && !medicine.isRegular) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "⏳ " + LanguageManager.get("periodic_interval", language).replace("{d}", medicine.intervalDays.toString()),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Slot Timing Tag
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Alarm,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "${record.slotName} (${MealTimes.formatTo12Hour(record.scheduledTime)})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Instructions & Stock Notice
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (record.instructions.isNotBlank()) {
                    Text(
                        text = record.instructions,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Stock Counter Pill
                if (medicine != null) {
                    val isLowStock = medicine.isLowStock()
                    val isOutOfStock = medicine.isOutOfStock()
                    val unit = medicine.getStockUnitShort()
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when {
                            isOutOfStock -> Color(0xFFFEE2E2)
                            isLowStock -> Color(0xFFFEF3C7)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        border = if (isLowStock || isOutOfStock) BorderStroke(1.dp, if (isOutOfStock) Color(0xFFDC2626) else Color(0xFFD97706)) else null,
                        modifier = Modifier
                            .clickable { onRefillClick() }
                            .testTag("dose_stock_badge_${record.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isLowStock || isOutOfStock) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Low Stock",
                                    tint = if (isOutOfStock) Color(0xFFDC2626) else Color(0xFFD97706),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text = when {
                                    isOutOfStock -> "Out of stock! • Refill"
                                    isLowStock -> "Low: ${medicine.stockCount} $unit • Refill"
                                    else -> "Stock: ${medicine.stockCount} $unit"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = when {
                                    isOutOfStock -> Color(0xFFDC2626)
                                    isLowStock -> Color(0xFFB45309)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                fontWeight = if (isLowStock || isOutOfStock) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Status Bar & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status badge
                when {
                    isTaken -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Taken",
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = LanguageManager.get("taken", language) + " ${record.getFormattedActionTime()}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF16A34A)
                            )
                        }
                    }
                    isSkipped -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Skipped",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = LanguageManager.get("skipped", language),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }
                    isSnoozed -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Snooze,
                                contentDescription = "Snoozed",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = LanguageManager.get("snooze", language) + " (10m)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706)
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = "Due ${MealTimes.formatTo12Hour(record.scheduledTime)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Actions: Take, Skip, Snooze
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!isTaken) {
                        OutlinedButton(
                            onClick = onSkip,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("btn_skip_${record.id}")
                        ) {
                            Text(LanguageManager.get("skip", language), style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = onSnooze,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("btn_snooze_${record.id}")
                        ) {
                            Icon(
                                Icons.Default.Snooze,
                                contentDescription = "Snooze 10 min",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("10m", style = MaterialTheme.typography.labelMedium)
                        }

                        Button(
                            onClick = onTake,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("btn_take_${record.id}")
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = LanguageManager.get("mark_taken", language),
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        TextButton(
                            onClick = onSkip,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Change", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyTodayView(
    language: AppLanguage,
    onAddMedicine: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Default.Medication,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "No doses scheduled for this slot",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Add a new medication or check other timing slots above.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onAddMedicine,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("+ " + LanguageManager.get("add_medicine", language))
            }
        }
    }
}

@Composable
fun MealTimesSummaryCard(
    mealTimes: MealTimes,
    language: AppLanguage,
    onCustomize: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCustomize() }
            .testTag("meal_times_summary_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = LanguageManager.get("meal_schedule_title", language),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                TextButton(
                    onClick = onCustomize,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = LanguageManager.get("customize_meal_times_btn", language),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Badges row: Breakfast, Lunch, Dinner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MealBadge(
                    label = LanguageManager.get("meal_breakfast", language),
                    time = mealTimes.breakfast,
                    modifier = Modifier.weight(1f)
                )
                MealBadge(
                    label = LanguageManager.get("meal_lunch", language),
                    time = mealTimes.lunch,
                    modifier = Modifier.weight(1f)
                )
                MealBadge(
                    label = LanguageManager.get("meal_dinner", language),
                    time = mealTimes.dinner,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MealBadge(
    label: String,
    time: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = MealTimes.formatTo12Hour(time),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun LowStockAlertCard(
    lowStockMedicines: List<MedicineEntity>,
    language: AppLanguage,
    onRefillClick: (MedicineEntity) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("low_stock_alert_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFEF2F2) // soft red/amber alert container
        ),
        border = BorderStroke(1.dp, Color(0xFFF87171))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "${lowStockMedicines.size} " + LanguageManager.get("low_stock_alert", language),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF991B1B)
                )
            }

            Text(
                text = "Restock the following medications before running out:",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF7F1D1D)
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                lowStockMedicines.forEach { med ->
                    val isLiquid = med.medicineForm.equals("LIQUID", ignoreCase = true)
                    val formIcon = if (isLiquid) "💧" else "💊"
                    val unit = med.getStockUnitShort()
                    val isOut = med.isOutOfStock()

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        border = BorderStroke(0.5.dp, Color(0xFFFCA5A5))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "$formIcon ${med.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2937)
                                )
                                Text(
                                    text = if (isOut) "Out of stock (0 $unit)! Alert: <= ${med.lowStockThreshold}"
                                    else "Only ${med.stockCount} $unit remaining (Alert at: <= ${med.lowStockThreshold})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isOut) Color(0xFFDC2626) else Color(0xFFB45309),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Button(
                                onClick = { onRefillClick(med) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("btn_refill_${med.id}")
                            ) {
                                Text(
                                    text = "+ " + LanguageManager.get("btn_refill", language),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickRefillDialog(
    medicine: MedicineEntity,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onRefill: (Int) -> Unit
) {
    val isLiquid = medicine.medicineForm.equals("LIQUID", ignoreCase = true)
    val unitLabel = medicine.getStockUnit()
    val unitShort = medicine.getStockUnitShort()
    var inputAmount by remember { mutableStateOf("") }
    val defaultPresets = if (isLiquid) {
        listOf(30, 60, 100, 200)
    } else {
        listOf(10, 20, 30, 50, 100)
    }

    val parsedAmount = inputAmount.toIntOrNull() ?: 0
    val newTotal = (medicine.stockCount + parsedAmount).coerceAtLeast(0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = (if (isLiquid) "💧 " else "💊 ") + LanguageManager.get("btn_refill", language) + ": ${medicine.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Current stock status
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Current Stock: ${medicine.stockCount} $unitLabel",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Low-Stock Warning Level: <= ${medicine.lowStockThreshold} $unitLabel",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Quick presets
                Text(
                    text = "Quick Presets (+ $unitShort):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    defaultPresets.forEach { preset ->
                        OutlinedButton(
                            onClick = { inputAmount = preset.toString() },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Text("+$preset $unitShort", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                // Custom amount text field
                OutlinedTextField(
                    value = inputAmount,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() } && newValue.length <= 5) {
                            inputAmount = newValue
                        }
                    },
                    label = { Text("Quantity to add ($unitShort)") },
                    placeholder = { Text(if (isLiquid) "e.g. 100" else "e.g. 30") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_quick_refill_amount")
                )

                // Projected total
                if (parsedAmount > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFDCFCE7)
                    ) {
                        Text(
                            text = "New Projected Stock: $newTotal $unitLabel",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF166534),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (parsedAmount > 0) {
                        onRefill(parsedAmount)
                    }
                },
                enabled = parsedAmount > 0,
                modifier = Modifier.testTag("btn_confirm_quick_refill")
            ) {
                Text("+ " + LanguageManager.get("add_to_stock", language))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_quick_refill")
            ) {
                Text(LanguageManager.get("cancel", language))
            }
        }
    )
}


