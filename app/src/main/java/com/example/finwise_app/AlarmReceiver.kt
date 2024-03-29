package com.example.finwise_app

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import android.app.PendingIntent

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val NOTIFICATION_ID = 123
        private const val CHANNEL_ID = "Alarm_Channel"
        private const val STOP_ACTION = "STOP_ALARM"
    }

    private var ringtone: Ringtone? = null

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "Alarm received!")

        // Check if the intent has the reminder ID
        if (intent.hasExtra("reminder_id")) {
            val reminderId = intent.getStringExtra("reminder_id")
            Log.d("AlarmReceiver", "Reminder ID: $reminderId")

            // Here you can handle the alarm functionality
            // For example, play a ringtone
            playRingtone(context)

            // Show notification with action to stop the alarm
            showNotification(context, reminderId ?: "")
        } else if (intent.action == STOP_ACTION) {
            // If the intent action is to stop the alarm
            val reminderId = intent.getStringExtra("reminder_id")
            stopAlarm(context, reminderId ?: "")
        }
    }

    private fun playRingtone(context: Context) {
        // Get the default ringtone URI
        val ringtoneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        // Create a Ringtone object from the ringtone URI
        ringtone = RingtoneManager.getRingtone(context, ringtoneUri)

        // Start playing the ringtone
        ringtone?.play()
    }

    private fun showNotification(context: Context, reminderId: String) {
        // Create an intent to handle the stop action
        val stopIntent = Intent(context, AlarmReceiver::class.java)
        stopIntent.action = STOP_ACTION
        stopIntent.putExtra("reminder_id", reminderId)

        // Add FLAG_IMMUTABLE to the PendingIntent creation
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Alarm")
            .setContentText("Click to stop alarm")
            .setSmallIcon(R.drawable.ic_alarm)
            .addAction(R.drawable.adjust_24, "Stop", stopPendingIntent)
            .setAutoCancel(true)

        // Show the notification
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }


    // This method is called when the user clicks on the stop action in the notification
    fun stopAlarm(context: Context, reminderId: String) {
        Log.d("AlarmReceiver", "Stopping alarm for reminder ID: $reminderId")
        // Check if the ringtone is not null before stopping it
        ringtone?.let {
            // Stop the ringtone
            it.stop()
        }

        // Cancel the notification
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)

        // Perform any additional actions you need to stop the alarm
    }

}
