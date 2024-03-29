package com.example.finwise_app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class ReminderAlarmManager(private val context: Context) {


    companion object {
        const val ALARM_PREFS_KEY = "alarm_prefs"
    }

    private val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val alarmPrefs: SharedPreferences = context.getSharedPreferences(ALARM_PREFS_KEY, Context.MODE_PRIVATE)

    fun isAlarmSetForReminder(reminderId: String): Boolean {
        return alarmPrefs.getBoolean(reminderId, true)
    }

    fun setAlarmForReminder(reminderId: String, reminderTime: Long) {
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        alarmIntent.putExtra("reminder_id", reminderId)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(), // Use a unique request code for each PendingIntent
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE // Specify the FLAG_IMMUTABLE flag
        )
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
        alarmPrefs.edit().putBoolean(reminderId, true).apply()
        Log.d("ReminderAlarmManager", "Alarm set for reminder: $reminderId")

    }

    fun cancelAlarmForReminder(reminderId: String) {
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(), // Use the same request code used when setting the alarm
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE // Specify the FLAG_IMMUTABLE flag
        )
        alarmManager.cancel(pendingIntent)
        alarmPrefs.edit().remove(reminderId).apply()
        Log.d("ReminderAlarmManager", "Alarm cancelled for reminder: $reminderId")
    }

    fun setAlarmForNextMonth(reminderId: String, reminderTime: Long) {
        // Calculate the next month's date based on the current date
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = reminderTime
        calendar.add(Calendar.MONTH, 1) // Move to next month

        // Extract the day, hour, and minute from the reminder time
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        // Set the alarm for the same date next month
        val nextMonthReminderTime = Calendar.getInstance()
        nextMonthReminderTime.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        nextMonthReminderTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
        nextMonthReminderTime.set(Calendar.MINUTE, minute)
        nextMonthReminderTime.set(Calendar.SECOND, 0)
        nextMonthReminderTime.set(Calendar.MILLISECOND, 0)

        // Convert the next month's reminder time to milliseconds
        val nextMonthTimeInMillis = nextMonthReminderTime.timeInMillis

        // Call the setAlarmForReminder function to set the alarm
        setAlarmForReminder(reminderId, nextMonthTimeInMillis)
    }


}
