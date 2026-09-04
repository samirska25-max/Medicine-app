package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.FrequencyType
import com.example.data.MedicineEntity
import com.example.util.AppLanguage
import com.example.util.LanguageManager

@Composable
fun MedicineListScreen(
    medicines: List<MedicineEntity>,
    language: AppLanguage = AppLanguage.ENGLISH,
    onAddMedicine: () -> Unit,
    onEditMedicine: (MedicineEntity) -> Unit,
    onDeleteMedicine: (MedicineEntity) -> Unit,
    onTestAlarm: (MedicineEntity) -> Unit
) {
    var medicineToDelete by remember { mutableStateOf<MedicineEntity?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("medicine_list_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = LanguageManager.get("medicines", language),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${medicines.size} " + LanguageManager.get("medicines", language),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onAddMedicine,
                    modifier = Modifier.testTag("btn_top_add_medicine")
                ) {
                    Text("+ " + LanguageManager.get("add_medicine", language))
                }
            }
        }

        if (medicines.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
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
                            text = "No medications registered",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Tap '+ Add' to configure your offline medication reminders and stock.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(medicines, key = { it.id }) { medicine ->
                MedicineManagementCard(
                    medicine = medicine,
                    language = language,
                    onEdit = { onEditMedicine(medicine) },
                    onDelete = { medicineToDelete = medicine },
                    onTestAlarm = { onTestAlarm(medicine) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }

    // Delete Confirmation Dialog
    if (medicineToDelete != null) {
        val med = medicineToDelete!!
        AlertDialog(
            onDismissRequest = { medicineToDelete = null },
            title = { Text(LanguageManager.get("delete", language) + " ${med.name}?") },
            text = {
                Text("This will remove this medication, its local alarms, and future scheduled reminders.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMedicine(med)
                        medicineToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text(LanguageManager.get("delete", language), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { medicineToDelete = null }) {
                    Text(LanguageManager.get("cancel", language))
                }
            }
        )
    }
}

@Composable
fun MedicineManagementCard(
    medicine: MedicineEntity,
    language: AppLanguage,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTestAlarm: () -> Unit
) {
    val isLowStock = medicine.stockCount <= medicine.lowStockThreshold
    val formIcon = if (medicine.medicineForm.equals("LIQUID", ignoreCase = true)) "💧" else "💊"
    val formBadgeText = when (medicine.medicineForm) {
        "LIQUID" -> LanguageManager.get("liquid_syrup", language)
        "DROPS" -> LanguageManager.get("drops", language)
        "INJECTION" -> LanguageManager.get("injection", language)
        else -> LanguageManager.get("tablet_capsule", language)
    }
    val stockUnit = if (medicine.medicineForm.equals("LIQUID", ignoreCase = true)) "ml" else "units"

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("med_card_${medicine.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Title & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$formIcon ${medicine.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = medicine.dosage,
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

                        if (!medicine.isRegular) {
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626))
                    }
                }
            }

            // Schedule info tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
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
                            text = "${medicine.getResolvedSlotTitle()} (${medicine.getResolvedTime()})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                val freqLabel = if (!medicine.isRegular) {
                    "Every ${medicine.intervalDays} days"
                } else {
                    when (medicine.frequencyType) {
                        FrequencyType.DAILY.name -> "Daily"
                        FrequencyType.DAYS_OF_WEEK.name -> medicine.daysOfWeek
                        FrequencyType.INTERVAL_HOURS.name -> "Every ${medicine.intervalHours}h"
                        else -> "Daily"
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = freqLabel,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Stock Count with low stock indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isLowStock) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (isLowStock) BorderStroke(1.dp, Color(0xFFDC2626)) else null
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isLowStock) Icons.Default.Warning else Icons.Default.Inventory,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isLowStock) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isLowStock) "Low Stock: ${medicine.stockCount} $stockUnit remaining!" else "Stock: ${medicine.stockCount} $stockUnit",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isLowStock) FontWeight.Bold else FontWeight.Normal,
                            color = if (isLowStock) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Test Alarm button
                OutlinedButton(
                    onClick = onTestAlarm,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.NotificationsActive,
                        contentDescription = "Test Alarm",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(LanguageManager.get("test_alarm", language), style = MaterialTheme.typography.labelSmall)
                }
            }

            if (medicine.instructions.isNotBlank()) {
                Text(
                    text = "${LanguageManager.get("instructions", language)}: ${medicine.instructions}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
