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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
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
import com.example.data.MedicineForm
import com.example.data.TimingSlot
import com.example.util.AppLanguage
import com.example.util.LanguageManager
import com.example.util.MealScheduleManager
import com.example.util.MealTimes
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditMedicineDialog(
    initialMedicine: MedicineEntity? = null,
    language: AppLanguage = AppLanguage.ENGLISH,
    mealTimes: MealTimes = MealScheduleManager.getCachedMealTimes(),
    onDismiss: () -> Unit,
    onSave: (MedicineEntity) -> Unit
) {
    val isEditing = initialMedicine != null
    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var name by remember { mutableStateOf(initialMedicine?.name ?: "") }

    // Medicine Form
    var selectedForm by remember {
        mutableStateOf(
            MedicineForm.fromName(initialMedicine?.medicineForm ?: MedicineForm.TABLET.name)
        )
    }

    // Dosage Preset & Custom string
    var dosagePreset by remember {
        mutableStateOf(initialMedicine?.dosagePreset ?: if (selectedForm == MedicineForm.LIQUID) "10 ml (2 tsp)" else "1 (Full)")
    }
    var customDosageText by remember { mutableStateOf(initialMedicine?.dosage ?: "") }

    // Regular vs Periodic
    var isRegular by remember { mutableStateOf(initialMedicine?.isRegular ?: true) }
    var intervalDays by remember { mutableIntStateOf(initialMedicine?.intervalDays ?: 7) }
    var customIntervalText by remember {
        mutableStateOf(
            if ((initialMedicine?.intervalDays ?: 7) !in listOf(7, 10, 14, 15, 30)) {
                (initialMedicine?.intervalDays ?: 7).toString()
            } else ""
        )
    }

    // Timing Slot
    var selectedSlot by remember {
        mutableStateOf(
            TimingSlot.entries.firstOrNull { it.name == initialMedicine?.timingSlot } ?: TimingSlot.AFTER_BREAKFAST
        )
    }
    var customTime by remember { mutableStateOf(initialMedicine?.customTime ?: "08:30") }

    // Frequency Type (when regular)
    var frequencyType by remember {
        mutableStateOf(
            FrequencyType.entries.firstOrNull { it.name == initialMedicine?.frequencyType } ?: FrequencyType.DAILY
        )
    }

    // Days of week state
    val initialDays = initialMedicine?.daysOfWeek?.split(",")?.map { it.trim() }?.toSet()
        ?: setOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
    var selectedDays by remember { mutableStateOf(initialDays) }

    // Stock & Low stock
    var stockCountText by remember { mutableStateOf(initialMedicine?.stockCount?.toString() ?: "30") }
    var lowStockText by remember { mutableStateOf(initialMedicine?.lowStockThreshold?.toString() ?: "5") }

    // Instructions
    var instructions by remember { mutableStateOf(initialMedicine?.instructions ?: "") }

    var nameError by remember { mutableStateOf(false) }

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
                    text = if (isEditing) LanguageManager.get("edit", language) + " " + LanguageManager.get("medicine_name", language)
                    else LanguageManager.get("add_medicine", language),
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
                    label = { Text(LanguageManager.get("medicine_name", language) + " *") },
                    placeholder = { Text("e.g. Paracetamol, Cough Syrup, Vitamin D3") },
                    isError = nameError,
                    supportingText = {
                        if (nameError) Text("Please enter medicine name")
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_medicine_name")
                )

                // 1. Medicine Form (Tablet vs Liquid / Syrup vs Drops vs Injection)
                Text(
                    text = LanguageManager.get("form_label", language),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val forms = listOf(
                        MedicineForm.TABLET to "💊 " + LanguageManager.get("tablet_capsule", language),
                        MedicineForm.LIQUID to "💧 " + LanguageManager.get("liquid_syrup", language),
                        MedicineForm.DROPS to "💧 " + LanguageManager.get("drops", language),
                        MedicineForm.INJECTION to "💉 " + LanguageManager.get("injection", language),
                        MedicineForm.OTHER to "🏷 " + LanguageManager.get("other", language)
                    )

                    forms.forEach { (form, label) ->
                        val isSelected = selectedForm == form
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedForm = form
                                // Auto-adjust default dosage preset
                                dosagePreset = when (form) {
                                    MedicineForm.LIQUID -> "10 ml (2 tsp)"
                                    MedicineForm.DROPS -> "5 Drops"
                                    MedicineForm.TABLET -> "1 (Full)"
                                    else -> "1 Dose"
                                }
                            },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                // 2. Dosage Options based on Form
                Text(
                    text = LanguageManager.get("dosage", language),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                val presets = when (selectedForm) {
                    MedicineForm.TABLET -> listOf(
                        "1/4 (" + LanguageManager.get("quarter", language) + ")",
                        "1/2 (" + LanguageManager.get("half", language) + ")",
                        "1 (" + LanguageManager.get("full", language) + ")",
                        "2 Tablets",
                        LanguageManager.get("custom", language)
                    )
                    MedicineForm.LIQUID -> listOf(
                        "2.5 ml (1/2 tsp)",
                        "5 ml (1 tsp)",
                        "10 ml (2 tsp)",
                        "15 ml (1 tbsp)",
                        LanguageManager.get("custom", language)
                    )
                    MedicineForm.DROPS -> listOf(
                        "2 Drops",
                        "5 Drops",
                        "10 Drops",
                        LanguageManager.get("custom", language)
                    )
                    else -> listOf(
                        "1 Dose",
                        "2 Doses",
                        LanguageManager.get("custom", language)
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presets.forEach { preset ->
                        val isSelected = dosagePreset == preset
                        FilterChip(
                            selected = isSelected,
                            onClick = { dosagePreset = preset },
                            label = { Text(preset, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                            )
                        )
                    }
                }

                // If Custom dosage is selected or custom text entered
                val isCustomDosage = dosagePreset == LanguageManager.get("custom", language)
                if (isCustomDosage || customDosageText.isNotBlank()) {
                    OutlinedTextField(
                        value = customDosageText,
                        onValueChange = { customDosageText = it },
                        label = { Text(LanguageManager.get("dosage", language) + " (" + LanguageManager.get("custom", language) + ")") },
                        placeholder = {
                            Text(
                                if (selectedForm == MedicineForm.LIQUID) "e.g. 7.5 ml or 20 ml"
                                else "e.g. 1.5 Tablets or 500 mg"
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_medicine_dosage")
                    )
                }

                // 3. Regular vs Periodic Scheduling
                Text(
                    text = LanguageManager.get("schedule_type", language),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isRegular,
                        onClick = { isRegular = true },
                        label = { Text("✓ " + LanguageManager.get("regular_medicine", language)) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )

                    FilterChip(
                        selected = !isRegular,
                        onClick = { isRegular = false },
                        label = { Text("⏳ " + LanguageManager.get("periodic_medicine", language)) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                            selectedLabelColor = MaterialTheme.colorScheme.onTertiary
                        )
                    )
                }

                // If Periodic (Every 7, 10, 15 days, etc.)
                if (!isRegular) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = LanguageManager.get("dose_interval_label", language),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )

                            val periodicIntervals = listOf(7, 10, 14, 15, 30)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                periodicIntervals.forEach { days ->
                                    val isSelected = intervalDays == days && customIntervalText.isBlank()
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            intervalDays = days
                                            customIntervalText = ""
                                        },
                                        label = {
                                            Text(
                                                "$days " + LanguageManager.get("days", language),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    )
                                }
                            }

                            // Custom days
                            OutlinedTextField(
                                value = customIntervalText,
                                onValueChange = {
                                    customIntervalText = it.filter { ch -> ch.isDigit() }
                                    it.toIntOrNull()?.let { d -> if (d > 0) intervalDays = d }
                                },
                                label = { Text(LanguageManager.get("custom_days", language)) },
                                placeholder = { Text("e.g. 21 or 45") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            val cal = Calendar.getInstance().apply {
                                add(Calendar.DAY_OF_YEAR, intervalDays)
                            }
                            val nextDueDateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(cal.time)
                            Text(
                                text = "Next dose will trigger on: $nextDueDateStr (in $intervalDays days)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }

                // Timing Slot Selection
                Text(
                    text = LanguageManager.get("timing_slot", language),
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
                        val slotResolvedTime = mealTimes.getTimeForSlot(slot)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSlot = slot },
                            label = {
                                Text(
                                    text = if (slot == TimingSlot.CUSTOM) "Custom Time" else "${slot.title} ($slotResolvedTime)",
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

                // Frequency Selection (if regular)
                if (isRegular) {
                    Text(
                        text = LanguageManager.get("frequency", language),
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
                }

                // Stock & Low Stock Counter
                Text(
                    text = LanguageManager.get("stock_count", language),
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
                        label = {
                            Text(
                                if (selectedForm == MedicineForm.LIQUID) "Syrup (ml)" else "Units / Pills"
                            )
                        },
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
                        label = { Text("Low Alert Level") },
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
                    label = { Text(LanguageManager.get("instructions", language)) },
                    placeholder = { Text("e.g. Take after warm meals, drink water") },
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

                    // Resolve final dosage string
                    val finalDosage = when {
                        customDosageText.isNotBlank() -> customDosageText.trim()
                        dosagePreset.isNotBlank() -> dosagePreset
                        selectedForm == MedicineForm.LIQUID -> "10 ml"
                        else -> "1 Tablet"
                    }

                    val medicine = MedicineEntity(
                        id = initialMedicine?.id ?: 0L,
                        name = name.trim(),
                        dosage = finalDosage,
                        medicineForm = selectedForm.name,
                        dosagePreset = dosagePreset,
                        timingSlot = selectedSlot.name,
                        customTime = if (selectedSlot == TimingSlot.CUSTOM) customTime.ifBlank { "08:30" } else mealTimes.getTimeForSlot(selectedSlot),
                        frequencyType = if (isRegular) frequencyType.name else FrequencyType.DAILY.name,
                        daysOfWeek = selectedDays.joinToString(","),
                        intervalHours = 8,
                        isRegular = isRegular,
                        intervalDays = if (!isRegular) intervalDays else 1,
                        startDate = initialMedicine?.startDate?.ifBlank { todayDateStr } ?: todayDateStr,
                        nextDueDate = if (!isRegular) {
                            initialMedicine?.nextDueDate?.ifBlank { todayDateStr } ?: todayDateStr
                        } else "",
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
                Text(
                    if (isEditing) LanguageManager.get("save_changes", language)
                    else LanguageManager.get("add_medicine", language)
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_medicine")
            ) {
                Text(LanguageManager.get("cancel", language))
            }
        }
    )
}
