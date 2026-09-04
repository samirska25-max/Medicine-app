package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DoseRecordEntity
import com.example.data.DoseStatus
import com.example.data.MedicineEntity
import com.example.data.SlotCategory
import com.example.data.TimingSlot
import com.example.receiver.MedicineAlarmScheduler
import com.example.util.AlarmRingingManager
import com.example.util.AppLanguage
import com.example.util.LanguageManager
import com.example.util.MealScheduleManager
import com.example.util.MealTimes
import com.example.util.RingingAlarmInfo
import kotlinx.coroutines.Dispatchers
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

data class AdherenceStats(
    val totalDoses: Int = 0,
    val takenDoses: Int = 0,
    val skippedDoses: Int = 0,
    val pendingDoses: Int = 0,
    val adherencePercentage: Int = 0
)

class MedicineViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val dao = database.medicineDao()
    private val context = application.applicationContext

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDateString: String = sdf.format(Date())

    // All registered medicines
    val allMedicines: StateFlow<List<MedicineEntity>> = dao.getAllMedicines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Today's records
    val todayRecords: StateFlow<List<DoseRecordEntity>> = dao.getRecordsForDate(todayDateString)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Slot filter for Today's view
    private val _slotFilter = MutableStateFlow("ALL")
    val slotFilter: StateFlow<String> = _slotFilter.asStateFlow()

    // Active device alarm ringing state
    val ringingAlarm: StateFlow<RingingAlarmInfo?> = AlarmRingingManager.activeAlarmState

    // Indian language selector state
    private val _selectedLanguage = MutableStateFlow(LanguageManager.getSavedLanguage(application))
    val selectedLanguage: StateFlow<AppLanguage> = _selectedLanguage.asStateFlow()

    // Meal times customization state (Breakfast, Lunch, Dinner, Bedtime)
    private val _mealTimes = MutableStateFlow(MealScheduleManager.init(application))
    val mealTimes: StateFlow<MealTimes> = _mealTimes.asStateFlow()

    fun updateMealTimes(newMealTimes: MealTimes) {
        _mealTimes.value = newMealTimes
        MealScheduleManager.saveMealTimes(context, newMealTimes)
        viewModelScope.launch(Dispatchers.IO) {
            val active = dao.getActiveMedicinesList()
            for (med in active) {
                if (med.timingSlot != TimingSlot.CUSTOM.name) {
                    val updatedMed = med.copy()
                    // Re-schedule alarm with updated customized meal time
                    MedicineAlarmScheduler.scheduleMedicineAlarm(
                        context = context,
                        medicine = updatedMed,
                        timeStr = updatedMed.getResolvedTime(newMealTimes)
                    )
                }
            }
            // Update today's pending dose records with the new time and category
            val todayRecordsList = dao.getRecordsForDateSync(todayDateString)
            for (rec in todayRecordsList) {
                if (rec.status == DoseStatus.PENDING.name) {
                    val med = active.firstOrNull { it.id == rec.medicineId }
                    if (med != null && med.timingSlot != TimingSlot.CUSTOM.name) {
                        val newTime = med.getResolvedTime(newMealTimes)
                        val newCat = med.getResolvedCategory(newMealTimes).name
                        dao.updateRecordTimeAndCategory(rec.id, newTime, newCat)
                    }
                }
            }
            syncTodaySchedule()
        }
    }

    fun setLanguage(language: AppLanguage) {
        _selectedLanguage.value = language
        LanguageManager.saveLanguage(context, language)
    }

    fun stopAlarm() {
        AlarmRingingManager.stopRinging(context)
    }

    // History range in days: 7, 14, 30
    private val _historyRangeDays = MutableStateFlow(7)
    val historyRangeDays: StateFlow<Int> = _historyRangeDays.asStateFlow()

    // Filtered today records based on slot category
    val filteredTodayRecords: StateFlow<List<DoseRecordEntity>> = combine(
        todayRecords,
        _slotFilter
    ) { records, filter ->
        if (filter == "ALL") records
        else records.filter { it.slotCategory.equals(filter, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Today's adherence statistics
    val todayStats: StateFlow<AdherenceStats> = todayRecords.combine(_slotFilter) { records, _ ->
        val total = records.size
        val taken = records.count { it.isTaken() }
        val skipped = records.count { it.isSkipped() }
        val pending = records.count { it.isPending() || it.isSnoozed() }
        val percentage = if (total > 0) ((taken.toFloat() / total.toFloat()) * 100).toInt() else 0
        AdherenceStats(
            totalDoses = total,
            takenDoses = taken,
            skippedDoses = skipped,
            pendingDoses = pending,
            adherencePercentage = percentage
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdherenceStats())

    // History records flow
    private val _historyRecords = MutableStateFlow<List<DoseRecordEntity>>(emptyList())
    val historyRecords: StateFlow<List<DoseRecordEntity>> = _historyRecords.asStateFlow()

    val historyStats: StateFlow<AdherenceStats> = _historyRecords.combine(_historyRangeDays) { records, _ ->
        val total = records.size
        val taken = records.count { it.isTaken() }
        val skipped = records.count { it.isSkipped() }
        val pending = records.count { it.isPending() || it.isSnoozed() }
        val percentage = if (total > 0) ((taken.toFloat() / total.toFloat()) * 100).toInt() else 0
        AdherenceStats(
            totalDoses = total,
            takenDoses = taken,
            skippedDoses = skipped,
            pendingDoses = pending,
            adherencePercentage = percentage
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdherenceStats())

    init {
        syncTodaySchedule()
        loadHistoryRange(7)
    }

    /**
     * Ensures all active medicines due today have an entry in dose_records.
     */
    fun syncTodaySchedule() {
        viewModelScope.launch(Dispatchers.IO) {
            val active = dao.getActiveMedicinesList()
            val todayCal = Calendar.getInstance()
            val existing = dao.getRecordsForDateSync(todayDateString)

            for (med in active) {
                if (med.isDueOnDate(todayCal)) {
                    val timeStr = med.getResolvedTime()
                    val alreadyCreated = existing.any { it.medicineId == med.id && it.scheduledTime == timeStr }
                    if (!alreadyCreated) {
                        dao.insertDoseRecord(
                            DoseRecordEntity(
                                medicineId = med.id,
                                medicineName = med.name,
                                dosage = med.dosage,
                                scheduledDate = todayDateString,
                                scheduledTime = timeStr,
                                slotName = med.getResolvedSlotTitle(),
                                slotCategory = med.getResolvedCategory().name,
                                status = DoseStatus.PENDING.name,
                                instructions = med.instructions
                            )
                        )
                    }
                    // Ensure alarm is scheduled for today
                    MedicineAlarmScheduler.scheduleMedicineAlarm(context, med)
                }
            }
        }
    }

    fun setSlotFilter(filter: String) {
        _slotFilter.value = filter
    }

    fun setHistoryRange(days: Int) {
        _historyRangeDays.value = days
        loadHistoryRange(days)
    }

    private fun loadHistoryRange(days: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val cal = Calendar.getInstance()
            val endDate = sdf.format(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, -(days - 1))
            val startDate = sdf.format(cal.time)

            dao.getRecordsInRange(startDate, endDate).collect { records ->
                _historyRecords.value = records
            }
        }
    }

    fun addMedicine(medicine: MedicineEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = dao.insertMedicine(medicine)
            val savedMed = medicine.copy(id = id)
            MedicineAlarmScheduler.scheduleMedicineAlarm(context, savedMed)

            val todayCal = Calendar.getInstance()
            if (savedMed.isDueOnDate(todayCal)) {
                dao.insertDoseRecord(
                    DoseRecordEntity(
                        medicineId = id,
                        medicineName = savedMed.name,
                        dosage = savedMed.dosage,
                        scheduledDate = todayDateString,
                        scheduledTime = savedMed.getResolvedTime(),
                        slotName = savedMed.getResolvedSlotTitle(),
                        slotCategory = savedMed.getResolvedCategory().name,
                        status = DoseStatus.PENDING.name,
                        instructions = savedMed.instructions
                    )
                )
            }
        }
    }

    fun updateMedicine(medicine: MedicineEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateMedicine(medicine)
            MedicineAlarmScheduler.scheduleMedicineAlarm(context, medicine)
        }
    }

    fun deleteMedicine(medicine: MedicineEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            MedicineAlarmScheduler.cancelAlarm(context, medicine.id, medicine.getResolvedTime())
            dao.deleteRecordsForMedicine(medicine.id)
            dao.deleteMedicine(medicine)
        }
    }

    fun markDoseTaken(record: DoseRecordEntity) {
        AlarmRingingManager.stopRinging(context)
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateDoseStatus(record.id, DoseStatus.TAKEN.name, System.currentTimeMillis())
            dao.decrementStock(record.medicineId)
            val medicine = dao.getMedicineById(record.medicineId)
            if (medicine != null && !medicine.isRegular) {
                val nextDue = medicine.computeNextDueDate()
                dao.updateNextDueDate(record.medicineId, nextDue)
                MedicineAlarmScheduler.scheduleMedicineAlarm(context, medicine.copy(nextDueDate = nextDue))
            }
        }
    }

    fun handleAlarmActionTaken(info: RingingAlarmInfo) {
        AlarmRingingManager.stopRinging(context)
        viewModelScope.launch(Dispatchers.IO) {
            dao.decrementStock(info.medicineId)
            if (info.recordId > 0) {
                dao.updateDoseStatus(info.recordId, DoseStatus.TAKEN.name, System.currentTimeMillis())
            } else {
                val existing = dao.findRecord(info.medicineId, todayDateString, info.scheduledTime)
                if (existing != null) {
                    dao.updateDoseStatus(existing.id, DoseStatus.TAKEN.name, System.currentTimeMillis())
                }
            }
            val medicine = dao.getMedicineById(info.medicineId)
            if (medicine != null && !medicine.isRegular) {
                val nextDue = medicine.computeNextDueDate()
                dao.updateNextDueDate(info.medicineId, nextDue)
                MedicineAlarmScheduler.scheduleMedicineAlarm(context, medicine.copy(nextDueDate = nextDue))
            }
        }
    }

    fun stopRingingAlarm() {
        stopAlarm()
    }

    fun handleAlarmTaken(info: RingingAlarmInfo) {
        handleAlarmActionTaken(info)
    }

    fun handleAlarmSnooze(info: RingingAlarmInfo, minutes: Int = 10) {
        handleAlarmActionSnooze(info, minutes)
    }

    fun handleAlarmActionSnooze(info: RingingAlarmInfo, minutes: Int = 10) {
        AlarmRingingManager.stopRinging(context)
        MedicineAlarmScheduler.scheduleSnooze(
            context = context,
            medicineId = info.medicineId,
            medicineName = info.medicineName,
            dosage = info.dosage,
            slotName = info.slotName,
            instructions = info.instructions,
            recordId = info.recordId,
            snoozeMinutes = minutes
        )
    }

    fun skipDose(record: DoseRecordEntity) {
        AlarmRingingManager.stopRinging(context)
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateDoseStatus(record.id, DoseStatus.SKIPPED.name, System.currentTimeMillis())
        }
    }

    fun snoozeDose(record: DoseRecordEntity, minutes: Int = 10) {
        AlarmRingingManager.stopRinging(context)
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateDoseStatus(record.id, DoseStatus.SNOOZED.name, System.currentTimeMillis())
            MedicineAlarmScheduler.scheduleSnooze(
                context = context,
                medicineId = record.medicineId,
                medicineName = record.medicineName,
                dosage = record.dosage,
                slotName = record.slotName,
                instructions = record.instructions,
                recordId = record.id,
                snoozeMinutes = minutes
            )
        }
    }

    fun triggerTestAlarm(medicine: MedicineEntity) {
        MedicineAlarmScheduler.triggerImmediateTestAlarm(context, medicine)
    }

    fun resetTodaySchedule() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.resetRecordsForDate(todayDateString)
            syncTodaySchedule()
        }
    }
}
