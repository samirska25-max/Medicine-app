package com.example.ui.screens

import android.app.TimePickerDialog
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.Medicine
import com.example.data.MedicineForm
import com.example.data.RoutineSlot
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MedicineFormDialog(
    initialMedicine: Medicine? = null,
    onDismiss: () -> Unit,
    onSave: (Medicine) -> Unit
) {
    val context = LocalContext.current
    val isEdit = initialMedicine != null

    var name by remember { mutableStateOf(initialMedicine?.name ?: "") }
    var selectedForm by remember { mutableStateOf(initialMedicine?.form ?: MedicineForm.TABLET.displayName) }
    var formMenuExpanded by remember { mutableStateOf(false) }

    // Dynamic Dosage State
    val tabletOptions = listOf("1/4", "1/2", "1", "1.5", "2", "3", "Custom")
    val liquidOptions = listOf("2.5", "5", "7.5", "10", "15", "Custom")

    var selectedTabletDose by remember {
        val current = initialMedicine?.dosageQuantity ?: "1"
        mutableStateOf(if (tabletOptions.contains(current)) current else "Custom")
    }
    var customTabletDose by remember {
        val current = initialMedicine?.dosageQuantity ?: ""
        mutableStateOf(if (!tabletOptions.contains(current) && current.isNotBlank()) current else "")
    }

    var selectedLiquidDose by remember {
        val current = initialMedicine?.dosageQuantity ?: "5"
        mutableStateOf(if (liquidOptions.contains(current)) current else "Custom")
    }
    var customLiquidDose by remember {
        val current = initialMedicine?.dosageQuantity ?: ""
        mutableStateOf(if (!liquidOptions.contains(current) && current.isNotBlank()) current else "")
    }

    var otherDoseQuantity by remember {
        mutableStateOf(if (initialMedicine?.form == MedicineForm.OTHER.displayName) initialMedicine.dosageQuantity else "1")
    }
    var otherDoseUnit by remember {
        mutableStateOf(if (initialMedicine?.form == MedicineForm.OTHER.displayName) initialMedicine.dosageUnit else "Puff")
    }

    // Routine Slots multi-selection
    val selectedSlots = remember {
        mutableStateListOf<String>().apply {
            if (initialMedicine != null) {
                addAll(initialMedicine.getSlotsList())
            } else {
                add(RoutineSlot.AFTER_BREAKFAST.name)
            }
        }
    }

    // Alert Time
    var alertTime by remember { mutableStateOf(initialMedicine?.alertTime ?: "08:30") }

    // Special Instructions
    var specialInstructions by remember { mutableStateOf(initialMedicine?.specialInstructions ?: "") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .testTag("medicine_form_dialog"),
        title = {
            Text(
                text = if (isEdit) "Edit Medication" else "Add New Medication",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (errorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Medicine Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Medicine Name *") },
                    placeholder = { Text("e.g. Paracetamol, Vitamin D3") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_medicine_name"),
                    singleLine = true
                )

                // Medicine Form Dropdown
                ExposedDropdownMenuBox(
                    expanded = formMenuExpanded,
                    onExpandedChange = { formMenuExpanded = !formMenuExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedForm,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Medicine Form *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formMenuExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("dropdown_medicine_form")
                    )
                    ExposedDropdownMenu(
                        expanded = formMenuExpanded,
                        onDismissRequest = { formMenuExpanded = false }
                    ) {
                        MedicineForm.entries.forEach { form ->
                            DropdownMenuItem(
                                text = { Text(form.displayName) },
                                onClick = {
                                    selectedForm = form.displayName
                                    formMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Dynamic Dosage Section based on Form
                Text(
                    text = "Dosage Quantity *",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                when (selectedForm) {
                    MedicineForm.TABLET.displayName, MedicineForm.CAPSULE.displayName -> {
                        Text(
                            text = "Select fractional or whole quantity:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            tabletOptions.forEach { opt ->
                                val label = when (opt) {
                                    "1/4" -> "1/4 (Quarter)"
                                    "1/2" -> "1/2 (Half)"
                                    else -> opt
                                }
                                FilterChip(
                                    selected = selectedTabletDose == opt,
                                    onClick = { selectedTabletDose = opt },
                                    label = { Text(label) },
                                    leadingIcon = if (selectedTabletDose == opt) {
                                        {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null
                                )
                            }
                        }

                        if (selectedTabletDose == "Custom") {
                            OutlinedTextField(
                                value = customTabletDose,
                                onValueChange = { customTabletDose = it },
                                label = { Text("Custom Count (e.g. 4, 0.75)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_custom_tablet_dose"),
                                singleLine = true
                            )
                        }
                    }

                    MedicineForm.LIQUID.displayName -> {
                        Text(
                            text = "Volume in milliliters (ml):",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            liquidOptions.forEach { opt ->
                                val label = if (opt == "Custom") "Custom ml" else "$opt ml"
                                FilterChip(
                                    selected = selectedLiquidDose == opt,
                                    onClick = { selectedLiquidDose = opt },
                                    label = { Text(label) },
                                    leadingIcon = if (selectedLiquidDose == opt) {
                                        {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null
                                )
                            }
                        }

                        if (selectedLiquidDose == "Custom") {
                            OutlinedTextField(
                                value = customLiquidDose,
                                onValueChange = { customLiquidDose = it },
                                label = { Text("Custom Volume in ml (e.g. 20, 25)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_custom_liquid_dose"),
                                singleLine = true
                            )
                        }
                    }

                    else -> { // Other
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = otherDoseQuantity,
                                onValueChange = { otherDoseQuantity = it },
                                label = { Text("Quantity") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = otherDoseUnit,
                                onValueChange = { otherDoseUnit = it },
                                label = { Text("Unit (Puff, Drops, etc.)") },
                                modifier = Modifier.weight(1.5f),
                                singleLine = true
                            )
                        }
                    }
                }

                // Assigned Routine Slots
                Text(
                    text = "Assigned Routine Slots *",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Check all times when this medication should be taken:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    RoutineSlot.entries.forEach { slot ->
                        val isChecked = selectedSlots.contains(slot.name) || selectedSlots.contains(slot.slotId)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) {
                                        selectedSlots.remove(slot.name)
                                        selectedSlots.remove(slot.slotId)
                                    } else {
                                        selectedSlots.add(slot.name)
                                    }
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        if (!selectedSlots.contains(slot.name)) selectedSlots.add(slot.name)
                                    } else {
                                        selectedSlots.remove(slot.name)
                                        selectedSlots.remove(slot.slotId)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = slot.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Scheduled: ${slot.defaultTime}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Target Alert Time Picker
                Text(
                    text = "Target Alert Time *",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedButton(
                    onClick = {
                        val cal = Calendar.getInstance()
                        val parts = alertTime.split(":")
                        val h = if (parts.size == 2) parts[0].toIntOrNull() ?: 8 else 8
                        val m = if (parts.size == 2) parts[1].toIntOrNull() ?: 30 else 30

                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                alertTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                            },
                            h,
                            m,
                            false
                        ).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("button_time_picker")
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Alert Time: $alertTime",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Special Instructions
                OutlinedTextField(
                    value = specialInstructions,
                    onValueChange = { specialInstructions = it },
                    label = { Text("Special Instructions (Optional)") },
                    placeholder = { Text("e.g. Take with lukewarm water after meals") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_special_instructions"),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Please enter a medicine name."
                        return@Button
                    }
                    if (selectedSlots.isEmpty()) {
                        errorMessage = "Please assign at least one daily routine slot."
                        return@Button
                    }

                    val finalQuantity = when (selectedForm) {
                        MedicineForm.TABLET.displayName, MedicineForm.CAPSULE.displayName -> {
                            if (selectedTabletDose == "Custom") {
                                if (customTabletDose.isBlank()) "1" else customTabletDose.trim()
                            } else selectedTabletDose
                        }
                        MedicineForm.LIQUID.displayName -> {
                            if (selectedLiquidDose == "Custom") {
                                if (customLiquidDose.isBlank()) "5" else customLiquidDose.trim()
                            } else selectedLiquidDose
                        }
                        else -> {
                            if (otherDoseQuantity.isBlank()) "1" else otherDoseQuantity.trim()
                        }
                    }

                    val finalUnit = when (selectedForm) {
                        MedicineForm.TABLET.displayName -> "Tablet"
                        MedicineForm.CAPSULE.displayName -> "Capsule"
                        MedicineForm.LIQUID.displayName -> "ml"
                        else -> if (otherDoseUnit.isBlank()) "unit" else otherDoseUnit.trim()
                    }

                    val medicine = Medicine(
                        id = initialMedicine?.id ?: 0L,
                        name = name.trim(),
                        form = selectedForm,
                        dosageQuantity = finalQuantity,
                        dosageUnit = finalUnit,
                        routineSlots = selectedSlots.joinToString(","),
                        alertTime = alertTime,
                        specialInstructions = specialInstructions.trim()
                    )
                    onSave(medicine)
                },
                modifier = Modifier.testTag("button_save_medicine")
            ) {
                Text(if (isEdit) "Save Changes" else "Add Medication")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("button_cancel_medicine")
            ) {
                Text("Cancel")
            }
        }
    )
}
