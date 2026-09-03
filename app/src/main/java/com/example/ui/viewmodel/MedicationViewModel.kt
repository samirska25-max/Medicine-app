package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.MealSchedule
import com.example.data.MealScheduleManager
import com.example.data.Medicine
import com.example.data.MedicineRepository
import com.example.data.RoutineSlot
import com.example.util.NotificationAndAudioHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class RoutineSlotGroup(
    val slot: RoutineSlot,
    val items: List<ChecklistItem>
)

data class ChecklistItem(
    val medicine: Medicine,
    val slot: RoutineSlot,
    val isTaken: Boolean
)

data class DailyProgressStats(
    val totalDoses: Int,
    val takenDoses: Int,
    val percentage: Int
)

class MedicationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MedicineRepository
    private val notificationHelper: NotificationAndAudioHelper
    private val mealScheduleManager: MealScheduleManager = MealScheduleManager(application)

    val mealSchedule: StateFlow<MealSchedule> = mealScheduleManager.schedule

    private val _currentDate = MutableStateFlow(getCurrentDateString())
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    private val _displayDate = MutableStateFlow(getFormattedDisplayDate())
    val displayDate: StateFlow<String> = _displayDate.asStateFlow()

    private val _activeAlert = MutableStateFlow<Pair<Medicine, RoutineSlot>?>(null)
    val activeAlert: StateFlow<Pair<Medicine, RoutineSlot>?> = _activeAlert.asStateFlow()

    private val _selectedFilterSlot = MutableStateFlow<String>("ALL")
    val selectedFilterSlot: StateFlow<String> = _selectedFilterSlot.asStateFlow()

    val allMedicines: StateFlow<List<Medicine>>

    val checklistGroups: StateFlow<List<RoutineSlotGroup>>
    val dailyProgress: StateFlow<DailyProgressStats>

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = MedicineRepository(database.medicineDao())
        notificationHelper = NotificationAndAudioHelper(application)

        allMedicines = repository.allMedicines.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Combine medicines and today's intake records
        val rawIntakes = repository.getIntakeRecordsForDate(_currentDate.value)

        checklistGroups = combine(allMedicines, rawIntakes, _selectedFilterSlot) { meds, intakes, filter ->
            val intakeMap = intakes.associate { "${it.medicineId}_${it.routineSlot}" to it.taken }

            RoutineSlot.entries
                .filter { slot ->
                    if (filter == "ALL") true else slot.name == filter
                }
                .mapNotNull { slot ->
                    val medsInSlot = meds.filter { med ->
                        med.getSlotsList().any { it.equals(slot.name, ignoreCase = true) || it.equals(slot.slotId, ignoreCase = true) }
                    }
                    if (medsInSlot.isEmpty()) null
                    else {
                        val items = medsInSlot.map { med ->
                            val key = "${med.id}_${slot.name}"
                            ChecklistItem(
                                medicine = med,
                                slot = slot,
                                isTaken = intakeMap[key] ?: false
                            )
                        }
                        RoutineSlotGroup(slot = slot, items = items)
                    }
                }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        dailyProgress = checklistGroups.combine(allMedicines) { groups, _ ->
            val allItems = groups.flatMap { it.items }
            val total = allItems.size
            val taken = allItems.count { it.isTaken }
            val percent = if (total > 0) (taken * 100) / total else 0
            DailyProgressStats(totalDoses = total, takenDoses = taken, percentage = percent)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DailyProgressStats(0, 0, 0)
        )

        startMidnightAndAlertTicker()
    }

    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getFormattedDisplayDate(): String {
        val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    fun setFilterSlot(slotName: String) {
        _selectedFilterSlot.value = slotName
    }

    fun updateMealTimes(
        breakfast: String,
        lunch: String,
        snacks: String,
        dinner: String,
        bedtime: String,
        detailedSlots: Map<RoutineSlot, String>
    ) {
        mealScheduleManager.updateMealTimes(
            breakfast = breakfast,
            lunch = lunch,
            snacks = snacks,
            dinner = dinner,
            bedtime = bedtime,
            autoRecalculateSlots = false
        )
        detailedSlots.forEach { (slot, time) ->
            mealScheduleManager.updateSpecificSlotTime(slot, time)
        }
    }

    fun toggleDoseTaken(medicine: Medicine, slot: RoutineSlot, currentTaken: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setIntakeStatus(
                medicineId = medicine.id,
                slot = slot.name,
                dateString = _currentDate.value,
                taken = !currentTaken
            )
        }
    }

    fun resetTodayChecklist() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.resetIntakeForDate(_currentDate.value)
        }
    }

    fun saveMedicine(medicine: Medicine) {
        viewModelScope.launch(Dispatchers.IO) {
            if (medicine.id == 0L) {
                repository.insertMedicine(medicine)
            } else {
                repository.updateMedicine(medicine)
            }
        }
    }

    fun deleteMedicine(medicine: Medicine) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMedicine(medicine)
        }
    }

    fun testAudioAlert() {
        notificationHelper.playAlarmTone()
        val firstMed = allMedicines.value.firstOrNull() ?: Medicine(
            name = "Paracetamol",
            form = "Tablet",
            dosageQuantity = "1/2",
            dosageUnit = "Tablet",
            routineSlots = "AFTER_BREAKFAST",
            alertTime = mealSchedule.value.afterBreakfastTime
        )
        _activeAlert.value = Pair(firstMed, RoutineSlot.AFTER_BREAKFAST)
        notificationHelper.showMedicationNotification(firstMed, RoutineSlot.AFTER_BREAKFAST.title)
    }

    fun dismissAlert() {
        notificationHelper.stopAlarmTone()
        _activeAlert.value = null
    }

    fun markAlertTaken() {
        notificationHelper.stopAlarmTone()
        val alert = _activeAlert.value
        if (alert != null) {
            toggleDoseTaken(alert.first, alert.second, false)
        }
        _activeAlert.value = null
    }

    fun stopAlarm() {
        notificationHelper.stopAlarmTone()
    }

    private fun startMidnightAndAlertTicker() {
        viewModelScope.launch {
            var lastCheckedMinute = -1
            while (true) {
                val cal = Calendar.getInstance()
                val currentMinute = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

                if (currentMinute != lastCheckedMinute) {
                    lastCheckedMinute = currentMinute

                    // Check for midnight rollover (00:00)
                    val today = getCurrentDateString()
                    if (today != _currentDate.value) {
                        _currentDate.value = today
                        _displayDate.value = getFormattedDisplayDate()
                    }

                    // Check time matching for scheduled medicines
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val currentTimeStr = timeFormat.format(cal.time)

                    allMedicines.value.forEach { med ->
                        if (med.alertTime == currentTimeStr) {
                            val slots = med.getSlotsList()
                            val firstSlot = slots.firstOrNull()?.let { RoutineSlot.fromId(it) } ?: RoutineSlot.BEFORE_BREAKFAST
                            notificationHelper.playAlarmTone()
                            notificationHelper.showMedicationNotification(med, firstSlot.title)
                            _activeAlert.value = Pair(med, firstSlot)
                        }
                    }
                }
                delay(10000) // check every 10 seconds
            }
        }
    }
}

