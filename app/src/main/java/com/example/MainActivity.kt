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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.MedicineEntity
import com.example.ui.screens.AddEditMedicineDialog
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MedicineListScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MedicineViewModel

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

    // Dialog state
    var showAddEditDialog by remember { mutableStateOf(false) }
    var medicineToEdit by remember { mutableStateOf<MedicineEntity?>(null) }

    // State from ViewModel
    val allMedicines by viewModel.allMedicines.collectAsStateWithLifecycle()
    val filteredTodayRecords by viewModel.filteredTodayRecords.collectAsStateWithLifecycle()
    val todayStats by viewModel.todayStats.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.slotFilter.collectAsStateWithLifecycle()
    val historyRecords by viewModel.historyRecords.collectAsStateWithLifecycle()
    val historyStats by viewModel.historyStats.collectAsStateWithLifecycle()
    val historyDays by viewModel.historyRangeDays.collectAsStateWithLifecycle()

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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Medicine Tracker & Reminder",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    if (allMedicines.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                viewModel.triggerTestAlarm(allMedicines.first())
                            },
                            modifier = Modifier.testTag("top_bar_test_alarm")
                        ) {
                            Icon(
                                Icons.Default.NotificationsActive,
                                contentDescription = "Test Alarm Notification"
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
                    label = { Text("Today") },
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
                    label = { Text("Medicines") },
                    modifier = Modifier.testTag("nav_item_medicines")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.History, contentDescription = "Adherence History") },
                    label = { Text("History") },
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
                    onFilterSelect = { viewModel.setSlotFilter(it) },
                    onTake = { viewModel.markDoseTaken(it) },
                    onSkip = { viewModel.skipDose(it) },
                    onSnooze = { viewModel.snoozeDose(it, 10) },
                    onResetToday = { viewModel.resetTodaySchedule() },
                    onAddMedicine = {
                        medicineToEdit = null
                        showAddEditDialog = true
                    }
                )

                1 -> MedicineListScreen(
                    medicines = allMedicines,
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
                    }
                )

                2 -> HistoryScreen(
                    historyRecords = historyRecords,
                    stats = historyStats,
                    selectedRangeDays = historyDays,
                    onRangeSelect = { viewModel.setHistoryRange(it) }
                )
            }
        }

        // Add or Edit Medication Dialog
        if (showAddEditDialog) {
            AddEditMedicineDialog(
                initialMedicine = medicineToEdit,
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
    }
}

// Composable for greeting screenshot tests
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
