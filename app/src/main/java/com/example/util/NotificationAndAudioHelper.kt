package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.data.Medicine

class NotificationAndAudioHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "medication_alarm_channel"
        const val CHANNEL_NAME = "Medication Alarms"
        const val CHANNEL_DESC = "Alarm tone notifications and alerts for scheduled medication doses"
    }

    private var activeRingtone: Ringtone? = null

    init {
        createNotificationChannel()
    }

    private fun getAlarmSoundUri() =
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alarmSoundUri = getAlarmSoundUri()
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                setSound(alarmSoundUri, audioAttributes)
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Plays the phone's native alarm ringtone using USAGE_ALARM stream.
     */
    fun playAlarmTone() {
        stopAlarmTone()
        try {
            val alarmUri = getAlarmSoundUri()
            val ringtone = RingtoneManager.getRingtone(context.applicationContext, alarmUri)
            if (ringtone != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ringtone.audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                }
                ringtone.play()
                activeRingtone = ringtone
                return
            }
        } catch (_: Exception) {
            // Fallback if system ringtone fails
        }

        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 2000)
        } catch (_: Exception) {
            // Ignore if audio device is unavailable
        }
    }

    fun playSoftAudioAlert() {
        playAlarmTone()
    }

    /**
     * Stops the alarm tone when dismissed or marked as taken.
     */
    fun stopAlarmTone() {
        try {
            if (activeRingtone?.isPlaying == true) {
                activeRingtone?.stop()
            }
        } catch (_: Exception) {
            // Ignore error
        } finally {
            activeRingtone = null
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

        val alarmSoundUri = getAlarmSoundUri()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Medication Alarm: ${medicine.name}")
            .setContentText("${medicine.getFormattedDose()} • $slotTitle")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "${medicine.name} — ${medicine.getFormattedDose()}\n" +
                                "Slot: $slotTitle\n" +
                                if (medicine.specialInstructions.isNotBlank()) "Instructions: ${medicine.specialInstructions}" else ""
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(alarmSoundUri, AudioManager.STREAM_ALARM)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
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
