package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Medicine::class, IntakeRecord::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicineDao(): MedicineDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medication_reminder_db"
                )
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
                        populateInitialMedicines(database.medicineDao())
                    }
                }
            }

            suspend fun populateInitialMedicines(dao: MedicineDao) {
                val initialMedicines = listOf(
                    Medicine(
                        name = "Paracetamol",
                        form = "Tablet",
                        dosageQuantity = "1/2",
                        dosageUnit = "Tablet",
                        routineSlots = "AFTER_BREAKFAST,AFTER_DINNER",
                        alertTime = "08:30",
                        specialInstructions = "Take with lukewarm water after meal"
                    ),
                    Medicine(
                        name = "Vitamin D3",
                        form = "Capsule",
                        dosageQuantity = "1",
                        dosageUnit = "Capsule",
                        routineSlots = "BEFORE_BREAKFAST",
                        alertTime = "07:30",
                        specialInstructions = "Best taken with plenty of water"
                    ),
                    Medicine(
                        name = "Cough Syrup",
                        form = "Liquid / Syrup",
                        dosageQuantity = "5",
                        dosageUnit = "ml",
                        routineSlots = "BEFORE_LUNCH,BEDTIME",
                        alertTime = "12:30",
                        specialInstructions = "Shake well before using"
                    ),
                    Medicine(
                        name = "Omega-3 Fish Oil",
                        form = "Capsule",
                        dosageQuantity = "1",
                        dosageUnit = "Capsule",
                        routineSlots = "AFTER_LUNCH",
                        alertTime = "13:30",
                        specialInstructions = "Take directly after finishing lunch"
                    )
                )
                for (med in initialMedicines) {
                    dao.insertMedicine(med)
                }
            }
        }
    }
}
