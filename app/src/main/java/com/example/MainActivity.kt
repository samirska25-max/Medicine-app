package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.MedicineEntity
import com.example.ui.screens.AddEditMedicineDialog
import com.example.ui.screens.CustomizeMealTimesDialog
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MedicineListScreen
import com.example.ui.screens.RingingAlarmDialog
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MedicineViewModel
import com.example.util.AppLanguage
import com.example.util.LanguageManager

class MainActivity : ComponentActivity() {

    private val viewModel: MedicineViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MedicineTrackerApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineTrackerApp(viewModel: MedicineViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    // Dialog states
    var showAddEditDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showMealTimesDialog by remember { mutableStateOf(false) }
    var medicineToEdit by remember { mutableStateOf<MedicineEntity?>(null) }

    // State from ViewModel
    val allMedicines by viewModel.allMedicines.collectAsStateWithLifecycle()
    val filteredTodayRecords by viewModel.filteredTodayRecords.collectAsStateWithLifecycle()
    val todayStats by viewModel.todayStats.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.slotFilter.collectAsStateWithLifecycle()
    val historyRecords by viewModel.historyRecords.collectAsStateWithLifecycle()
    val historyStats by viewModel.historyStats.collectAsStateWithLifecycle()
    val historyDays by viewModel.historyRangeDays.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val mealTimes by viewModel.mealTimes.collectAsStateWithLifecycle()
    val ringingAlarm by viewModel.ringingAlarm.collectAsStateWithLifecycle()

    // Notification permission request for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = LanguageManager.get("app_title", currentLanguage),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "100% Offline • ${currentLanguage.nativeName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        // Customize Meal Times Button
                        IconButton(
                            onClick = { showMealTimesDialog = true },
                            modifier = Modifier.testTag("top_bar_meal_times_btn")
                        ) {
                            Icon(
                                Icons.Default.Restaurant,
                                contentDescription = "Customize Meal Times",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Language Chooser Icon
                        IconButton(
                            onClick = { showLanguageDialog = true },
                            modifier = Modifier.testTag("top_bar_language_btn")
                        ) {
                            Icon(
                                Icons.Default.Language,
                                contentDescription = "Change Language",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Test Alarm Icon
                        if (allMedicines.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    viewModel.triggerTestAlarm(allMedicines.first())
                                },
                                modifier = Modifier.testTag("top_bar_test_alarm")
                            ) {
                                Icon(
                                    Icons.Default.NotificationsActive,
                                    contentDescription = "Test Device Alarm",
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        medicineToEdit = null
                        showAddEditDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_add_medicine")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Medication")
                }
            },
            bottomBar = {
                NavigationBar(modifier = Modifier.testTag("bottom_navigation")) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = {
                            BadgedBox(badge = {
                                if (todayStats.pendingDoses > 0) {
                                    Badge { Text("${todayStats.pendingDoses}") }
                                }
                            }) {
                                Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Today Schedule")
                            }
                        },
                        label = { Text(LanguageManager.get("today", currentLanguage)) },
                        modifier = Modifier.testTag("nav_item_today")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            BadgedBox(badge = {
                                if (allMedicines.isNotEmpty()) {
                                    Badge { Text("${allMedicines.size}") }
                                }
                            }) {
                                Icon(Icons.Default.Medication, contentDescription = "Medications")
                            }
                        },
                        label = { Text(LanguageManager.get("medicines", currentLanguage)) },
                        modifier = Modifier.testTag("nav_item_medicines")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.History, contentDescription = "Adherence History") },
                        label = { Text(LanguageManager.get("history", currentLanguage)) },
                        modifier = Modifier.testTag("nav_item_history")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> HomeScreen(
                        todayRecords = filteredTodayRecords,
                        allMedicines = allMedicines,
                        stats = todayStats,
                        selectedSlotFilter = selectedFilter,
                        language = currentLanguage,
                        activeAlarm = ringingAlarm,
                        mealTimes = mealTimes,
                        onFilterSelect = { viewModel.setSlotFilter(it) },
                        onTake = { viewModel.markDoseTaken(it) },
                        onSkip = { viewModel.skipDose(it) },
                        onSnooze = { viewModel.snoozeDose(it, 10) },
                        onResetToday = { viewModel.resetTodaySchedule() },
                        onAddMedicine = {
                            medicineToEdit = null
                            showAddEditDialog = true
                        },
                        onStopAlarm = { viewModel.stopRingingAlarm() },
                        onCustomizeMealTimes = { showMealTimesDialog = true },
                        onRefillStock = { medId, amount -> viewModel.refillStock(medId, amount) }
                    )

                    1 -> MedicineListScreen(
                        medicines = allMedicines,
                        language = currentLanguage,
                        onAddMedicine = {
                            medicineToEdit = null
                            showAddEditDialog = true
                        },
                        onEditMedicine = { med ->
                            medicineToEdit = med
                            showAddEditDialog = true
                        },
                        onDeleteMedicine = { med ->
                            viewModel.deleteMedicine(med)
                        },
                        onTestAlarm = { med ->
                            viewModel.triggerTestAlarm(med)
                        },
                        onRefillStock = { medId, amount -> viewModel.refillStock(medId, amount) }
                    )

                    2 -> HistoryScreen(
                        historyRecords = historyRecords,
                        stats = historyStats,
                        selectedRangeDays = historyDays,
                        language = currentLanguage,
                        onRangeSelect = { viewModel.setHistoryRange(it) }
                    )
                }
            }

            // Add or Edit Medication Dialog
            if (showAddEditDialog) {
                AddEditMedicineDialog(
                    initialMedicine = medicineToEdit,
                    language = currentLanguage,
                    mealTimes = mealTimes,
                    onDismiss = {
                        showAddEditDialog = false
                        medicineToEdit = null
                    },
                    onSave = { savedMed ->
                        if (medicineToEdit == null) {
                            viewModel.addMedicine(savedMed)
                        } else {
                            viewModel.updateMedicine(savedMed)
                        }
                        showAddEditDialog = false
                        medicineToEdit = null
                    }
                )
            }

            // Customize Meal Times Dialog (Breakfast, Lunch, Dinner, Bedtime)
            if (showMealTimesDialog) {
                CustomizeMealTimesDialog(
                    currentMealTimes = mealTimes,
                    language = currentLanguage,
                    onSave = { updatedTimes ->
                        viewModel.updateMealTimes(updatedTimes)
                        showMealTimesDialog = false
                    },
                    onDismiss = { showMealTimesDialog = false }
                )
            }

            // Language Selection Dialog (Indian Languages + English)
            if (showLanguageDialog) {
                LanguageSelectionDialog(
                    currentLanguage = currentLanguage,
                    onSelectLanguage = { lang ->
                        viewModel.setLanguage(lang)
                        showLanguageDialog = false
                    },
                    onDismiss = { showLanguageDialog = false }
                )
            }
        }

        // Full-screen / active device alarm overlay when ringing!
        if (ringingAlarm != null) {
            val alarm = ringingAlarm!!
            RingingAlarmDialog(
                alarmInfo = alarm,
                language = currentLanguage,
                onTake = { viewModel.handleAlarmTaken(alarm) },
                onSnooze = { viewModel.handleAlarmSnooze(alarm, 10) },
                onDismiss = { viewModel.stopRingingAlarm() }
            )
        }
    }
}

@Composable
fun LanguageSelectionDialog(
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Select Language / भाषा चुनें",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AppLanguage.entries) { lang ->
                    val isSelected = lang == currentLanguage
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectLanguage(lang) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = lang.nativeName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = lang.displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelectLanguage(lang) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// Composable for greeting screenshot tests
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
