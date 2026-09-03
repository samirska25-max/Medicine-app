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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.data.Medicine
import com.example.ui.screens.ActiveAlertDialog
import com.example.ui.screens.DailyChecklistScreen
import com.example.ui.screens.MedicineFormDialog
import com.example.ui.screens.MedicineManagementScreen
import com.example.ui.screens.WebAppPreviewScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MedicationViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MedicationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MedicationApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationApp(viewModel: MedicationViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    // Dialog state
    var showMedicineDialog by remember { mutableStateOf(false) }
    var medicineToEdit by remember { mutableStateOf<Medicine?>(null) }

    // State from ViewModel
    val dateDisplay by viewModel.displayDate.collectAsStateWithLifecycle()
    val progressStats by viewModel.dailyProgress.collectAsStateWithLifecycle()
    val groups by viewModel.checklistGroups.collectAsStateWithLifecycle()
    val allMedicines by viewModel.allMedicines.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilterSlot.collectAsStateWithLifecycle()
    val activeAlert by viewModel.activeAlert.collectAsStateWithLifecycle()

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
                        text = "Medication Reminder",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.testAudioAlert() },
                        modifier = Modifier.testTag("top_bar_test_alert")
                    ) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = "Test Alert Sound"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NavigationBar(modifier = Modifier.testTag("bottom_navigation")) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Daily Checklist") },
                    label = { Text("Checklist") },
                    modifier = Modifier.testTag("nav_item_checklist")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Medication, contentDescription = "Medicines") },
                    label = { Text("Medicines (${allMedicines.size})") },
                    modifier = Modifier.testTag("nav_item_medicines")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Language, contentDescription = "Web App View") },
                    label = { Text("Web Mode") },
                    modifier = Modifier.testTag("nav_item_web")
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
                0 -> DailyChecklistScreen(
                    dateDisplay = dateDisplay,
                    progressStats = progressStats,
                    groups = groups,
                    selectedFilter = selectedFilter,
                    onFilterSelect = { viewModel.setFilterSlot(it) },
                    onToggleTaken = { med, slot, currentTaken ->
                        viewModel.toggleDoseTaken(med, slot, currentTaken)
                    },
                    onResetToday = { viewModel.resetTodayChecklist() },
                    onTestAlarm = { viewModel.testAudioAlert() },
                    onAddMedicine = {
                        medicineToEdit = null
                        showMedicineDialog = true
                    }
                )

                1 -> MedicineManagementScreen(
                    medicines = allMedicines,
                    onAddMedicine = {
                        medicineToEdit = null
                        showMedicineDialog = true
                    },
                    onEditMedicine = { med ->
                        medicineToEdit = med
                        showMedicineDialog = true
                    },
                    onDeleteMedicine = { med ->
                        viewModel.deleteMedicine(med)
                    }
                )

                2 -> WebAppPreviewScreen()
            }
        }

        // Add / Edit Medicine Dialog
        if (showMedicineDialog) {
            MedicineFormDialog(
                initialMedicine = medicineToEdit,
                onDismiss = {
                    showMedicineDialog = false
                    medicineToEdit = null
                },
                onSave = { savedMed ->
                    viewModel.saveMedicine(savedMed)
                    showMedicineDialog = false
                    medicineToEdit = null
                }
            )
        }

        // Active Scheduled Alert Dialog
        activeAlert?.let { (med, slot) ->
            ActiveAlertDialog(
                medicine = med,
                slot = slot,
                onTakeNow = { viewModel.markAlertTaken() },
                onDismiss = { viewModel.dismissAlert() }
            )
        }
    }
}

// Fallback composable for GreetingScreenshotTest
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
