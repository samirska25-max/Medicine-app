package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.RestartAlt
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DoseRecordEntity
import com.example.data.MedicineEntity
import com.example.data.SlotCategory
import com.example.ui.viewmodel.AdherenceStats
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    todayRecords: List<DoseRecordEntity>,
    allMedicines: List<MedicineEntity>,
    stats: AdherenceStats,
    selectedSlotFilter: String,
    onFilterSelect: (String) -> Unit,
    onTake: (DoseRecordEntity) -> Unit,
    onSkip: (DoseRecordEntity) -> Unit,
    onSnooze: (DoseRecordEntity) -> Unit,
    onResetToday: () -> Unit,
    onAddMedicine: () -> Unit
) {
    val dateDisplay = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())

    val medicinesMap = allMedicines.associateBy { it.id }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Today Summary & Progress Card
        item {
            TodayProgressCard(
                dateDisplay = dateDisplay,
                stats = stats,
                onResetToday = onResetToday
            )
        }

        // Timing Slot Filter Chips
        item {
            SlotFilterRow(
                selectedFilter = selectedSlotFilter,
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
                        stockCount = medicine?.stockCount,
                        lowStockThreshold = medicine?.lowStockThreshold ?: 5,
                        onTake = { onTake(record) },
                        onSkip = { onSkip(record) },
                        onSnooze = { onSnooze(record) }
                    )
                }
            }
        }

        if (!anyItemsShown) {
            item {
                EmptyTodayView(onAddMedicine = onAddMedicine)
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
fun TodayProgressCard(
    dateDisplay: String,
    stats: AdherenceStats,
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
                        text = "Today's Schedule",
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
                        text = "${stats.takenDoses} of ${stats.totalDoses} doses taken",
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
                StatusPill(label = "Taken", count = stats.takenDoses, color = Color(0xFF16A34A))
                StatusPill(label = "Pending", count = stats.pendingDoses, color = Color(0xFFD97706))
                StatusPill(label = "Skipped", count = stats.skippedDoses, color = Color(0xFFDC2626))
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
    onFilterSelect: (String) -> Unit
) {
    val filters = listOf(
        "ALL" to "All Doses",
        "MORNING" to "Morning 🌅",
        "AFTERNOON" to "Afternoon ☀️",
        "EVENING" to "Evening 🌆",
        "NIGHT" to "Night 🌙"
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
    stockCount: Int?,
    lowStockThreshold: Int,
    onTake: () -> Unit,
    onSkip: () -> Unit,
    onSnooze: () -> Unit
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
                    Text(
                        text = record.medicineName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = record.dosage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
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
                            text = "${record.slotName} (${record.scheduledTime})",
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
                if (stockCount != null) {
                    val isLowStock = stockCount <= lowStockThreshold
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isLowStock) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isLowStock) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Low Stock",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text = if (isLowStock) "Low Stock: $stockCount left" else "Stock: $stockCount left",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isLowStock) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isLowStock) FontWeight.Bold else FontWeight.Normal
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
                                text = "Taken ${record.getFormattedActionTime()}",
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
                                text = "Skipped",
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
                                text = "Snoozed (10m)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706)
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = "Due ${record.scheduledTime}",
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
                            Text("Skip", style = MaterialTheme.typography.labelMedium)
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
                            Text("Take", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Option to retake or mark skipped if user tapped mistakenly
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
fun EmptyTodayView(onAddMedicine: () -> Unit) {
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
                Text("+ Add Medication")
            }
        }
    }
}
