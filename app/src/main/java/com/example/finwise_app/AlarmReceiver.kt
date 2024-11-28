package com.example.finwise_app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.finwise_app.Login
import com.example.finwise_app.R


class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val label = intent.getStringExtra("label")
        val description = intent.getStringExtra("description")
        Log.d("AlarmReceiver", "Received Label: $label, Description: $description")


        val serviceIntent = Intent(context, AlarmService::class.java).apply{
            putExtra("label", label)
            putExtra("description", description)
        }
        context.startForegroundService(serviceIntent)
    }


}

