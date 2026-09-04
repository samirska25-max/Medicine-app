package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Database(
    entities = [MedicineEntity::class, DoseRecordEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicineDao(): MedicineDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "offline_medication_tracker_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.medicineDao())
                    }
                }
            }

            suspend fun populateInitialData(dao: MedicineDao) {
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                val med1 = MedicineEntity(
                    name = "Paracetamol 500mg",
                    dosage = "1 Tablet",
                    timingSlot = TimingSlot.AFTER_BREAKFAST.name,
                    customTime = "08:30",
                    frequencyType = FrequencyType.DAILY.name,
                    daysOfWeek = "MON,TUE,WED,THU,FRI,SAT,SUN",
                    intervalHours = 8,
                    stockCount = 28,
                    lowStockThreshold = 5,
                    instructions = "Take with a glass of water after food"
                )

                val med2 = MedicineEntity(
                    name = "Amoxicillin 250mg",
                    dosage = "1 Capsule",
                    timingSlot = TimingSlot.AFTER_LUNCH.name,
                    customTime = "13:30",
                    frequencyType = FrequencyType.DAILY.name,
                    daysOfWeek = "MON,TUE,WED,THU,FRI,SAT,SUN",
                    intervalHours = 8,
                    stockCount = 14,
                    lowStockThreshold = 4,
                    instructions = "Complete full antibiotic course"
                )

                val med3 = MedicineEntity(
                    name = "Atorvastatin 20mg",
                    dosage = "1 Tablet",
                    timingSlot = TimingSlot.BEDTIME.name,
                    customTime = "22:00",
                    frequencyType = FrequencyType.DAILY.name,
                    daysOfWeek = "MON,TUE,WED,THU,FRI,SAT,SUN",
                    intervalHours = 24,
                    stockCount = 4, // low stock test
                    lowStockThreshold = 5,
                    instructions = "Take at bedtime consistently"
                )

                val id1 = dao.insertMedicine(med1)
                val id2 = dao.insertMedicine(med2)
                val id3 = dao.insertMedicine(med3)

                // Populate today's initial dose records
                val records = listOf(
                    DoseRecordEntity(
                        medicineId = id1,
                        medicineName = med1.name,
                        dosage = med1.dosage,
                        scheduledDate = todayStr,
                        scheduledTime = "08:30",
                        slotName = "After Breakfast",
                        slotCategory = SlotCategory.MORNING.name,
                        status = DoseStatus.TAKEN.name,
                        actionTimestamp = System.currentTimeMillis() - 7200000L,
                        instructions = med1.instructions
                    ),
                    DoseRecordEntity(
                        medicineId = id2,
                        medicineName = med2.name,
                        dosage = med2.dosage,
                        scheduledDate = todayStr,
                        scheduledTime = "13:30",
                        slotName = "After Lunch",
                        slotCategory = SlotCategory.AFTERNOON.name,
                        status = DoseStatus.PENDING.name,
                        instructions = med2.instructions
                    ),
                    DoseRecordEntity(
                        medicineId = id3,
                        medicineName = med3.name,
                        dosage = med3.dosage,
                        scheduledDate = todayStr,
                        scheduledTime = "22:00",
                        slotName = "Bedtime",
                        slotCategory = SlotCategory.NIGHT.name,
                        status = DoseStatus.PENDING.name,
                        instructions = med3.instructions
                    )
                )
                dao.insertDoseRecords(records)

                // Populate history for the past 6 days to give immediate adherence graph!
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val cal = Calendar.getInstance()
                val pastRecords = mutableListOf<DoseRecordEntity>()
                for (i in 1..6) {
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                    val pastDate = sdf.format(cal.time)
                    val isAllTaken = i % 2 == 0
                    pastRecords.add(
                        DoseRecordEntity(
                            medicineId = id1,
                            medicineName = med1.name,
                            dosage = med1.dosage,
                            scheduledDate = pastDate,
                            scheduledTime = "08:30",
                            slotName = "After Breakfast",
                            slotCategory = SlotCategory.MORNING.name,
                            status = DoseStatus.TAKEN.name,
                            actionTimestamp = cal.timeInMillis + 30600000L,
                            instructions = med1.instructions
                        )
                    )
                    pastRecords.add(
                        DoseRecordEntity(
                            medicineId = id2,
                            medicineName = med2.name,
                            dosage = med2.dosage,
                            scheduledDate = pastDate,
                            scheduledTime = "13:30",
                            slotName = "After Lunch",
                            slotCategory = SlotCategory.AFTERNOON.name,
                            status = if (isAllTaken) DoseStatus.TAKEN.name else DoseStatus.SKIPPED.name,
                            actionTimestamp = cal.timeInMillis + 48600000L,
                            instructions = med2.instructions
                        )
                    )
                    pastRecords.add(
                        DoseRecordEntity(
                            medicineId = id3,
                            medicineName = med3.name,
                            dosage = med3.dosage,
                            scheduledDate = pastDate,
                            scheduledTime = "22:00",
                            slotName = "Bedtime",
                            slotCategory = SlotCategory.NIGHT.name,
                            status = DoseStatus.TAKEN.name,
                            actionTimestamp = cal.timeInMillis + 79200000L,
                            instructions = med3.instructions
                        )
                    )
                }
                dao.insertDoseRecords(pastRecords)
            }
        }
    }
}
