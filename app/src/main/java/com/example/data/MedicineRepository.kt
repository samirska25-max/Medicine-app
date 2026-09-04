package com.example.data

import kotlinx.coroutines.flow.Flow

class MedicineRepository(private val dao: MedicineDao) {

    val allMedicines: Flow<List<MedicineEntity>> = dao.getAllMedicines()
    val activeMedicines: Flow<List<MedicineEntity>> = dao.getActiveMedicines()

    suspend fun getMedicineById(id: Long): MedicineEntity? = dao.getMedicineById(id)

    suspend fun insertMedicine(medicine: MedicineEntity): Long = dao.insertMedicine(medicine)

    suspend fun updateMedicine(medicine: MedicineEntity) = dao.updateMedicine(medicine)

    suspend fun deleteMedicine(medicine: MedicineEntity) {
        dao.deleteRecordsForMedicine(medicine.id)
        dao.deleteMedicine(medicine)
    }

    fun getRecordsForDate(dateString: String): Flow<List<DoseRecordEntity>> =
        dao.getRecordsForDate(dateString)

    fun getRecordsInRange(startDate: String, endDate: String): Flow<List<DoseRecordEntity>> =
        dao.getRecordsInRange(startDate, endDate)

    suspend fun updateDoseStatus(recordId: Long, status: String, timestamp: Long?) {
        dao.updateDoseStatus(recordId, status, timestamp)
    }

    suspend fun resetRecordsForDate(dateString: String) {
        dao.resetRecordsForDate(dateString)
    }
}
