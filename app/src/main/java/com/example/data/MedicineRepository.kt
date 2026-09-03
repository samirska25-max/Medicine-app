package com.example.data

import kotlinx.coroutines.flow.Flow

class MedicineRepository(private val dao: MedicineDao) {

    val allMedicines: Flow<List<Medicine>> = dao.getAllMedicines()

    suspend fun getMedicineById(id: Long): Medicine? = dao.getMedicineById(id)

    suspend fun insertMedicine(medicine: Medicine): Long = dao.insertMedicine(medicine)

    suspend fun updateMedicine(medicine: Medicine) = dao.updateMedicine(medicine)

    suspend fun deleteMedicine(medicine: Medicine) {
        dao.deleteIntakeRecordsForMedicine(medicine.id)
        dao.deleteMedicine(medicine)
    }

    fun getIntakeRecordsForDate(dateString: String): Flow<List<IntakeRecord>> =
        dao.getIntakeRecordsForDate(dateString)

    suspend fun setIntakeStatus(medicineId: Long, slot: String, dateString: String, taken: Boolean) {
        val record = IntakeRecord(
            medicineId = medicineId,
            routineSlot = slot,
            dateString = dateString,
            taken = taken,
            takenTimestamp = if (taken) System.currentTimeMillis() else null
        )
        dao.upsertIntakeRecord(record)
    }

    suspend fun resetIntakeForDate(dateString: String) {
        dao.resetIntakeRecordsForDate(dateString)
    }
}
