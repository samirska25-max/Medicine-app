package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.data.Medicine

class NotificationAndAudioHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "medication_reminder_channel"
        const val CHANNEL_NAME = "Medication Reminders"
        const val CHANNEL_DESC = "Notifications and audio alerts for scheduled medication doses"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                setSound(soundUri, audioAttributes)
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun playSoftAudioAlert() {
        try {
            // Attempt to play default notification ringtone
            val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context.applicationContext, alertUri)
            if (ringtone != null) {
                ringtone.play()
                return
            }
        } catch (_: Exception) {
            // Fallback to ToneGenerator
        }

        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 500)
        } catch (_: Exception) {
            // Ignore if audio device is unavailable
        }
    }

    fun showMedicationNotification(medicine: Medicine, slotTitle: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            medicine.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Medication Reminder: ${medicine.name}")
            .setContentText("${medicine.getFormattedDose()} • $slotTitle")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "${medicine.name} — ${medicine.getFormattedDose()}\n" +
                                "Slot: $slotTitle\n" +
                                if (medicine.specialInstructions.isNotBlank()) "Instructions: ${medicine.specialInstructions}" else ""
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(medicine.id.toInt() + 1000, notification)
        } catch (_: SecurityException) {
            // Notification permission might not be granted yet
        }
    }
}
