package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.DoseRecordEntity
import com.example.data.DoseStatus
import com.example.receiver.MedicineAlarmScheduler.ACTION_DISMISS
import com.example.receiver.MedicineAlarmScheduler.ACTION_MARK_TAKEN
import com.example.receiver.MedicineAlarmScheduler.ACTION_SNOOZE
import com.example.receiver.MedicineAlarmScheduler.ACTION_TRIGGER_ALARM
import com.example.receiver.MedicineAlarmScheduler.EXTRA_DOSAGE
import com.example.receiver.MedicineAlarmScheduler.EXTRA_INSTRUCTIONS
import com.example.receiver.MedicineAlarmScheduler.EXTRA_MEDICINE_ID
import com.example.receiver.MedicineAlarmScheduler.EXTRA_MEDICINE_NAME
import com.example.receiver.MedicineAlarmScheduler.EXTRA_NOTIFICATION_ID
import com.example.receiver.MedicineAlarmScheduler.EXTRA_RECORD_ID
import com.example.receiver.MedicineAlarmScheduler.EXTRA_SCHEDULED_TIME
import com.example.receiver.MedicineAlarmScheduler.EXTRA_SLOT_NAME
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"
        const val CHANNEL_ID = "medication_alarms_channel"
        const val CHANNEL_NAME = "Medication Alarms & Reminders"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val action = intent.action ?: return
        Log.d(TAG, "onReceive action=$action")

        val medicineId = intent.getLongExtra(EXTRA_MEDICINE_ID, -1L)
        val medicineName = intent.getStringExtra(EXTRA_MEDICINE_NAME) ?: "Medication"
        val dosage = intent.getStringExtra(EXTRA_DOSAGE) ?: ""
        val slotName = intent.getStringExtra(EXTRA_SLOT_NAME) ?: "Scheduled Dose"
        val scheduledTime = intent.getStringExtra(EXTRA_SCHEDULED_TIME) ?: ""
        val instructions = intent.getStringExtra(EXTRA_INSTRUCTIONS) ?: ""
        val recordId = intent.getLongExtra(EXTRA_RECORD_ID, 0L)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, (medicineId * 100).toInt())

        when (action) {
            ACTION_TRIGGER_ALARM -> {
                showAlarmNotification(
                    context = context,
                    medicineId = medicineId,
                    medicineName = medicineName,
                    dosage = dosage,
                    slotName = slotName,
                    scheduledTime = scheduledTime,
                    instructions = instructions,
                    recordId = recordId,
                    notificationId = notificationId
                )
            }

            ACTION_MARK_TAKEN -> {
                val pendingResult = goAsync()
                dismissNotification(context, notificationId)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getDatabase(context)
                        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                        // Decrement medicine stock count
                        if (medicineId > 0) {
                            db.medicineDao().decrementStock(medicineId)
                        }

                        // Update or insert dose record
                        if (recordId > 0) {
                            db.medicineDao().updateDoseStatus(recordId, DoseStatus.TAKEN.name, System.currentTimeMillis())
                        } else if (medicineId > 0) {
                            val existing = db.medicineDao().findRecord(medicineId, todayStr, scheduledTime)
                            if (existing != null) {
                                db.medicineDao().updateDoseStatus(existing.id, DoseStatus.TAKEN.name, System.currentTimeMillis())
                            } else {
                                db.medicineDao().insertDoseRecord(
                                    DoseRecordEntity(
                                        medicineId = medicineId,
                                        medicineName = medicineName,
                                        dosage = dosage,
                                        scheduledDate = todayStr,
                                        scheduledTime = scheduledTime,
                                        slotName = slotName,
                                        slotCategory = "MORNING",
                                        status = DoseStatus.TAKEN.name,
                                        actionTimestamp = System.currentTimeMillis(),
                                        instructions = instructions
                                    )
                                )
                            }
                        }
                        Log.d(TAG, "Dose marked as taken for medicineId=$medicineId")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error marking dose as taken", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            ACTION_SNOOZE -> {
                val pendingResult = goAsync()
                dismissNotification(context, notificationId)
                MedicineAlarmScheduler.scheduleSnooze(
                    context = context,
                    medicineId = medicineId,
                    medicineName = medicineName,
                    dosage = dosage,
                    slotName = slotName,
                    instructions = instructions,
                    recordId = recordId,
                    snoozeMinutes = 10
                )
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getDatabase(context)
                        if (recordId > 0) {
                            db.medicineDao().updateDoseStatus(recordId, DoseStatus.SNOOZED.name, System.currentTimeMillis())
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating snooze status", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            ACTION_DISMISS -> {
                dismissNotification(context, notificationId)
            }
        }
    }

    private fun showAlarmNotification(
        context: Context,
        medicineId: Long,
        medicineName: String,
        dosage: String,
        slotName: String,
        scheduledTime: String,
        instructions: String,
        recordId: Long,
        notificationId: Int
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        createNotificationChannel(notificationManager)

        // Open app intent
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Mark Taken
        val markTakenIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_MARK_TAKEN
            putExtra(EXTRA_MEDICINE_ID, medicineId)
            putExtra(EXTRA_MEDICINE_NAME, medicineName)
            putExtra(EXTRA_DOSAGE, dosage)
            putExtra(EXTRA_SLOT_NAME, slotName)
            putExtra(EXTRA_SCHEDULED_TIME, scheduledTime)
            putExtra(EXTRA_INSTRUCTIONS, instructions)
            putExtra(EXTRA_RECORD_ID, recordId)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val markTakenPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 1,
            markTakenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Snooze (10 min)
        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_MEDICINE_ID, medicineId)
            putExtra(EXTRA_MEDICINE_NAME, medicineName)
            putExtra(EXTRA_DOSAGE, dosage)
            putExtra(EXTRA_SLOT_NAME, slotName)
            putExtra(EXTRA_SCHEDULED_TIME, scheduledTime)
            putExtra(EXTRA_INSTRUCTIONS, instructions)
            putExtra(EXTRA_RECORD_ID, recordId)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val contentText = buildString {
            append("Scheduled: $slotName")
            if (scheduledTime.isNotBlank()) append(" ($scheduledTime)")
            if (dosage.isNotBlank()) append(" • $dosage")
            if (instructions.isNotBlank()) append(" • $instructions")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("💊 Time for $medicineName")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(alarmSound)
            .setVibrate(longArrayOf(0, 600, 300, 600, 300, 600))
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(android.R.drawable.checkbox_on_background, "✓ Mark Taken", markTakenPendingIntent)
            .addAction(android.R.drawable.ic_popup_sync, "⏰ Snooze (10m)", snoozePendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun dismissNotification(context: Context, notificationId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancel(notificationId)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existing = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existing == null) {
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()

                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "High-priority offline reminders and alarms for scheduled medication doses."
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 600, 300, 600, 300, 600)
                    setSound(soundUri, audioAttributes)
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}
