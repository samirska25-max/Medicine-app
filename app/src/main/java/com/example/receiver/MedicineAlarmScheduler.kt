package com.example.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.AppDatabase
import com.example.data.MedicineEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object MedicineAlarmScheduler {
    private const val TAG = "MedAlarmScheduler"

    const val ACTION_TRIGGER_ALARM = "com.example.medreminder.ACTION_TRIGGER_ALARM"
    const val ACTION_MARK_TAKEN = "com.example.medreminder.ACTION_MARK_TAKEN"
    const val ACTION_SNOOZE = "com.example.medreminder.ACTION_SNOOZE"
    const val ACTION_DISMISS = "com.example.medreminder.ACTION_DISMISS"

    const val EXTRA_MEDICINE_ID = "extra_medicine_id"
    const val EXTRA_MEDICINE_NAME = "extra_medicine_name"
    const val EXTRA_DOSAGE = "extra_dosage"
    const val EXTRA_MEDICINE_FORM = "extra_medicine_form"
    const val EXTRA_SLOT_NAME = "extra_slot_name"
    const val EXTRA_SCHEDULED_TIME = "extra_scheduled_time"
    const val EXTRA_INSTRUCTIONS = "extra_instructions"
    const val EXTRA_RECORD_ID = "extra_record_id"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

    /**
     * Schedules exact alarm for a medication intake.
     */
    fun scheduleMedicineAlarm(
        context: Context,
        medicine: MedicineEntity,
        slotName: String = medicine.getResolvedSlotTitle(),
        timeStr: String = medicine.getResolvedTime(),
        recordId: Long = 0L
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val triggerMillis = if (!medicine.isRegular && medicine.nextDueDate.isNotBlank()) {
            calculateTargetDateMillis(timeStr, medicine.nextDueDate)
        } else {
            calculateTriggerMillis(timeStr)
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_ALARM
            putExtra(EXTRA_MEDICINE_ID, medicine.id)
            putExtra(EXTRA_MEDICINE_NAME, medicine.name)
            putExtra(EXTRA_DOSAGE, medicine.dosage)
            putExtra(EXTRA_MEDICINE_FORM, medicine.medicineForm)
            putExtra(EXTRA_SLOT_NAME, slotName)
            putExtra(EXTRA_SCHEDULED_TIME, timeStr)
            putExtra(EXTRA_INSTRUCTIONS, medicine.instructions)
            putExtra(EXTRA_RECORD_ID, recordId)
            putExtra(EXTRA_NOTIFICATION_ID, (medicine.id * 100 + timeStr.hashCode() % 100).toInt())
        }

        val requestCode = (medicine.id * 1000 + (timeStr.replace(":", "").toIntOrNull() ?: 0)).toInt()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleAlarmExactOrIdle(alarmManager, triggerMillis, pendingIntent)
        Log.d(TAG, "Scheduled alarm for ${medicine.name} at $timeStr (millis=$triggerMillis, code=$requestCode)")
    }

    /**
     * Re-triggers alarm after snooze (default 10 minutes).
     */
    fun scheduleSnooze(
        context: Context,
        medicineId: Long,
        medicineName: String,
        dosage: String,
        slotName: String,
        instructions: String = "",
        recordId: Long = 0L,
        snoozeMinutes: Int = 10
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerMillis = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_ALARM
            putExtra(EXTRA_MEDICINE_ID, medicineId)
            putExtra(EXTRA_MEDICINE_NAME, medicineName)
            putExtra(EXTRA_DOSAGE, dosage)
            putExtra(EXTRA_SLOT_NAME, slotName)
            putExtra(EXTRA_SCHEDULED_TIME, "Snoozed +${snoozeMinutes}m")
            putExtra(EXTRA_INSTRUCTIONS, instructions)
            putExtra(EXTRA_RECORD_ID, recordId)
            putExtra(EXTRA_NOTIFICATION_ID, (medicineId * 100 + 99).toInt())
        }

        val requestCode = (medicineId * 1000 + 999).toInt()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleAlarmExactOrIdle(alarmManager, triggerMillis, pendingIntent)
        Log.d(TAG, "Scheduled snooze for $medicineName in $snoozeMinutes min")
    }

    /**
     * Triggers an immediate test alarm within 1 second so the user can verify offline sound & heads-up notification.
     */
    fun triggerImmediateTestAlarm(context: Context, medicine: MedicineEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerMillis = System.currentTimeMillis() + 1000L

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_ALARM
            putExtra(EXTRA_MEDICINE_ID, medicine.id)
            putExtra(EXTRA_MEDICINE_NAME, medicine.name)
            putExtra(EXTRA_DOSAGE, medicine.dosage)
            putExtra(EXTRA_MEDICINE_FORM, medicine.medicineForm)
            putExtra(EXTRA_SLOT_NAME, medicine.getResolvedSlotTitle())
            putExtra(EXTRA_SCHEDULED_TIME, medicine.getResolvedTime())
            putExtra(EXTRA_INSTRUCTIONS, medicine.instructions)
            putExtra(EXTRA_RECORD_ID, 0L)
            putExtra(EXTRA_NOTIFICATION_ID, (medicine.id * 100 + 77).toInt())
        }

        val requestCode = (medicine.id * 1000 + 777).toInt()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleAlarmExactOrIdle(alarmManager, triggerMillis, pendingIntent)
    }

    fun cancelAlarm(context: Context, medicineId: Long, timeStr: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val requestCode = (medicineId * 1000 + (timeStr.replace(":", "").toIntOrNull() ?: 0)).toInt()

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_ALARM
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    /**
     * Reschedules all active alarms for medicines in database.
     */
    fun rescheduleAllAlarms(context: Context) {
        val db = AppDatabase.getDatabase(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val activeMedicines = db.medicineDao().getActiveMedicinesList()
                val todayCal = Calendar.getInstance()
                for (med in activeMedicines) {
                    if (med.isDueOnDate(todayCal)) {
                        scheduleMedicineAlarm(context, med)
                    }
                }
                Log.d(TAG, "Rescheduled alarms for ${activeMedicines.size} active medicines.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reschedule alarms", e)
            }
        }
    }

    private fun scheduleAlarmExactOrIdle(
        alarmManager: AlarmManager,
        triggerMillis: Long,
        pendingIntent: PendingIntent
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                try {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                } catch (e: SecurityException) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        }
    }

    private fun calculateTriggerMillis(timeStr: String): Long {
        val parts = timeStr.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 30

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If time already passed today, schedule for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.timeInMillis
    }

    private fun calculateTargetDateMillis(timeStr: String, targetDateStr: String): Long {
        val parts = timeStr.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 30

        val calendar = Calendar.getInstance()
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(targetDateStr)
            if (date != null) {
                calendar.time = date
            }
        } catch (e: Exception) {
            // fallback to current
        }
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // If time already passed, schedule for next interval
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.timeInMillis
    }
}
