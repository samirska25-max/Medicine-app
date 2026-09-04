package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.data.FrequencyType
import com.example.data.MedicineEntity
import com.example.data.TimingSlot

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditMedicineDialog(
    initialMedicine: MedicineEntity? = null,
    onDismiss: () -> Unit,
    onSave: (MedicineEntity) -> Unit
) {
    val isEditing = initialMedicine != null

    var name by remember { mutableStateOf(initialMedicine?.name ?: "") }
    var dosage by remember { mutableStateOf(initialMedicine?.dosage ?: "") }
    var selectedSlot by remember {
        mutableStateOf(
            TimingSlot.entries.firstOrNull { it.name == initialMedicine?.timingSlot } ?: TimingSlot.AFTER_BREAKFAST
        )
    }
    var customTime by remember { mutableStateOf(initialMedicine?.customTime ?: "08:30") }
    var frequencyType by remember {
        mutableStateOf(
            FrequencyType.entries.firstOrNull { it.name == initialMedicine?.frequencyType } ?: FrequencyType.DAILY
        )
    }

    // Days of week state
    val initialDays = initialMedicine?.daysOfWeek?.split(",")?.map { it.trim() }?.toSet()
        ?: setOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
    var selectedDays by remember { mutableStateOf(initialDays) }

    // Interval hours
    var intervalHours by remember { mutableIntStateOf(initialMedicine?.intervalHours ?: 8) }

    // Stock & Low stock
    var stockCountText by remember { mutableStateOf(initialMedicine?.stockCount?.toString() ?: "30") }
    var lowStockText by remember { mutableStateOf(initialMedicine?.lowStockThreshold?.toString() ?: "5") }

    // Instructions
    var instructions by remember { mutableStateOf(initialMedicine?.instructions ?: "") }

    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 20.dp)
            .testTag("dialog_add_edit_medicine"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Medication,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isEditing) "Edit Medication" else "Add Medication",
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
                // Medicine Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = false
                    },
                    label = { Text("Medicine Name *") },
                    placeholder = { Text("e.g. Paracetamol, Amoxicillin") },
                    isError = nameError,
                    supportingText = {
                        if (nameError) Text("Please enter medicine name")
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_medicine_name")
                )

                // Dosage
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage / Strength *") },
                    placeholder = { Text("e.g. 500mg, 1 Tablet, 10ml") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_medicine_dosage")
                )

                // Timing Slot Selection
                Text(
                    text = "Timing Slot",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TimingSlot.entries.forEach { slot ->
                        val isSelected = selectedSlot == slot
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSlot = slot },
                            label = {
                                Text(
                                    text = if (slot == TimingSlot.CUSTOM) "Custom Time" else "${slot.title} (${slot.defaultTime})",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                // If Custom Time selected
                if (selectedSlot == TimingSlot.CUSTOM) {
                    OutlinedTextField(
                        value = customTime,
                        onValueChange = { customTime = it },
                        label = { Text("Custom Time (HH:mm, 24-hour)") },
                        placeholder = { Text("08:30") },
                        leadingIcon = { Icon(Icons.Default.Alarm, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_custom_time")
                    )
                }

                // Frequency Selection
                Text(
                    text = "Frequency",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FrequencyType.entries.forEach { freq ->
                        val isSelected = frequencyType == freq
                        FilterChip(
                            selected = isSelected,
                            onClick = { frequencyType = freq },
                            label = { Text(freq.title, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // If Specific Days of week selected
                if (frequencyType == FrequencyType.DAYS_OF_WEEK) {
                    Text(
                        text = "Select Days:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val days = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        days.forEach { day ->
                            val isChecked = selectedDays.contains(day)
                            FilterChip(
                                selected = isChecked,
                                onClick = {
                                    selectedDays = if (isChecked) {
                                        if (selectedDays.size > 1) selectedDays - day else selectedDays
                                    } else {
                                        selectedDays + day
                                    }
                                },
                                label = { Text(day, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }

                // If Interval selected
                if (frequencyType == FrequencyType.INTERVAL_HOURS) {
                    Text(
                        text = "Interval Hours:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val intervals = listOf(4, 6, 8, 12, 24)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        intervals.forEach { hours ->
                            val isChecked = intervalHours == hours
                            FilterChip(
                                selected = isChecked,
                                onClick = { intervalHours = hours },
                                label = { Text("Every ${hours}h", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Stock & Low Stock Counter
                Text(
                    text = "Stock / Pill Counter (Local)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = stockCountText,
                        onValueChange = { stockCountText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Pill / Stock Count") },
                        placeholder = { Text("30") },
                        leadingIcon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_stock_count")
                    )

                    OutlinedTextField(
                        value = lowStockText,
                        onValueChange = { lowStockText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Low Stock Alert at") },
                        placeholder = { Text("5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_low_stock")
                    )
                }

                // Special Instructions
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Special Instructions") },
                    placeholder = { Text("e.g. Take with warm water, avoid dairy") },
                    leadingIcon = { Icon(Icons.Default.NoteAlt, contentDescription = null) },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_instructions")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }

                    val stockInt = stockCountText.toIntOrNull() ?: 30
                    val lowStockInt = lowStockText.toIntOrNull() ?: 5

                    val medicine = MedicineEntity(
                        id = initialMedicine?.id ?: 0L,
                        name = name.trim(),
                        dosage = dosage.ifBlank { "1 Dose" }.trim(),
                        timingSlot = selectedSlot.name,
                        customTime = customTime.ifBlank { "08:30" },
                        frequencyType = frequencyType.name,
                        daysOfWeek = selectedDays.joinToString(","),
                        intervalHours = intervalHours,
                        stockCount = stockInt,
                        lowStockThreshold = lowStockInt,
                        instructions = instructions.trim(),
                        isActive = true,
                        createdAt = initialMedicine?.createdAt ?: System.currentTimeMillis()
                    )

                    onSave(medicine)
                },
                modifier = Modifier.testTag("btn_save_medicine")
            ) {
                Text(if (isEditing) "Save Changes" else "Add Medication")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_medicine")
            ) {
                Text("Cancel")
            }
        }
    )
}
