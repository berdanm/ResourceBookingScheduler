package com.example.resourcebookingscheduler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.resourcebookingscheduler.ui.BookingActivity
import com.example.resourcebookingscheduler.ui.BookingHistoryActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()

        findViewById<Button>(R.id.btnMakeBooking).setOnClickListener {
            startActivity(Intent(this, BookingActivity::class.java))
        }

        findViewById<Button>(R.id.btnViewHistory).setOnClickListener {
            startActivity(Intent(this, BookingHistoryActivity::class.java))
        }
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "booking_channel",
                "Booking Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
