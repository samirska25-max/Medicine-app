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
    @Query("SELECT * FROM medicines ORDER BY createdAt DESC")
    fun getAllMedicines(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getMedicineById(id: Long): Medicine?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: Medicine): Long

    @Update
    suspend fun updateMedicine(medicine: Medicine)

    @Delete
    suspend fun deleteMedicine(medicine: Medicine)

    @Query("SELECT * FROM intake_records WHERE dateString = :dateString")
    fun getIntakeRecordsForDate(dateString: String): Flow<List<IntakeRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertIntakeRecord(record: IntakeRecord)

    @Query("DELETE FROM intake_records WHERE medicineId = :medicineId")
    suspend fun deleteIntakeRecordsForMedicine(medicineId: Long)

    @Query("DELETE FROM intake_records WHERE dateString = :dateString")
    suspend fun resetIntakeRecordsForDate(dateString: String)
}
