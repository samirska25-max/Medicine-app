package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {
    // Medicines
    @Query("SELECT * FROM medicines ORDER BY createdAt DESC")
    fun getAllMedicines(): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM medicines WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveMedicines(): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM medicines WHERE isActive = 1")
    suspend fun getActiveMedicinesList(): List<MedicineEntity>

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getMedicineById(id: Long): MedicineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: MedicineEntity): Long

    @Update
    suspend fun updateMedicine(medicine: MedicineEntity)

    @Delete
    suspend fun deleteMedicine(medicine: MedicineEntity)

    @Query("DELETE FROM medicines WHERE id = :id")
    suspend fun deleteMedicineById(id: Long)

    @Query("UPDATE medicines SET stockCount = :newStock WHERE id = :medicineId")
    suspend fun updateStock(medicineId: Long, newStock: Int)

    @Query("UPDATE medicines SET stockCount = CASE WHEN stockCount > 0 THEN stockCount - 1 ELSE 0 END WHERE id = :medicineId")
    suspend fun decrementStock(medicineId: Long)

    @Query("UPDATE medicines SET stockCount = CASE WHEN stockCount >= :amount THEN stockCount - :amount ELSE 0 END WHERE id = :medicineId")
    suspend fun decrementStockByAmount(medicineId: Long, amount: Int)

    @Query("UPDATE medicines SET stockCount = stockCount + :amount WHERE id = :medicineId")
    suspend fun addStock(medicineId: Long, amount: Int)

    @Query("UPDATE medicines SET nextDueDate = :nextDueDate WHERE id = :medicineId")
    suspend fun updateNextDueDate(medicineId: Long, nextDueDate: String)

    // Dose Records for Today & Date Range
    @Query("SELECT * FROM dose_records WHERE scheduledDate = :date ORDER BY scheduledTime ASC")
    fun getRecordsForDate(date: String): Flow<List<DoseRecordEntity>>

    @Query("SELECT * FROM dose_records WHERE scheduledDate = :date ORDER BY scheduledTime ASC")
    suspend fun getRecordsForDateSync(date: String): List<DoseRecordEntity>

    @Query("SELECT * FROM dose_records WHERE scheduledDate BETWEEN :startDate AND :endDate ORDER BY scheduledDate DESC, scheduledTime ASC")
    fun getRecordsInRange(startDate: String, endDate: String): Flow<List<DoseRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoseRecord(record: DoseRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoseRecords(records: List<DoseRecordEntity>)

    @Update
    suspend fun updateDoseRecord(record: DoseRecordEntity)

    @Query("UPDATE dose_records SET status = :status, actionTimestamp = :timestamp WHERE id = :recordId")
    suspend fun updateDoseStatus(recordId: Long, status: String, timestamp: Long?)

    @Query("SELECT * FROM dose_records WHERE medicineId = :medicineId AND scheduledDate = :date AND scheduledTime = :time LIMIT 1")
    suspend fun findRecord(medicineId: Long, date: String, time: String): DoseRecordEntity?

    @Query("SELECT * FROM dose_records WHERE medicineId = :medicineId AND scheduledDate = :date LIMIT 1")
    suspend fun findRecordForDate(medicineId: Long, date: String): DoseRecordEntity?

    @Query("DELETE FROM dose_records WHERE medicineId = :medicineId")
    suspend fun deleteRecordsForMedicine(medicineId: Long)

    @Query("DELETE FROM dose_records WHERE scheduledDate = :date")
    suspend fun resetRecordsForDate(date: String)

    @Query("UPDATE dose_records SET scheduledTime = :time, slotCategory = :category WHERE id = :id")
    suspend fun updateRecordTimeAndCategory(id: Long, time: String, category: String)
}
