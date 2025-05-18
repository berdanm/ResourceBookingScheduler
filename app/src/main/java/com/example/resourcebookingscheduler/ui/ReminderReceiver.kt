package com.example.resourcebookingscheduler.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.resourcebookingscheduler.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val resource = intent.getStringExtra("resource")
        val time = intent.getStringExtra("time")

        val builder = NotificationCompat.Builder(context, "booking_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Upcoming Booking")
            .setContentText("You have $resource at $time")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(context)) {
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}
