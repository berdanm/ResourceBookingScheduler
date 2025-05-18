// BookingActivity.kt - FULL with working local insert + remote upload
package com.example.resourcebookingscheduler.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.resourcebookingscheduler.R
import com.example.resourcebookingscheduler.db.Booking
import com.example.resourcebookingscheduler.viewmodel.BookingViewModel
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class BookingActivity : AppCompatActivity() {

    private val viewModel: BookingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val res = findViewById<EditText>(R.id.editResourceName)
        val by = findViewById<EditText>(R.id.editBookedBy)
        val date = findViewById<EditText>(R.id.editDate)
        val start = findViewById<EditText>(R.id.editStartTime)
        val end = findViewById<EditText>(R.id.editEndTime)
        val save = findViewById<Button>(R.id.btnSaveBooking)

        save.setOnClickListener {
            val booking = Booking(
                resourceName = res.text.toString(),
                bookedBy = by.text.toString(),
                date = date.text.toString(),
                startTime = start.text.toString(),
                endTime = end.text.toString()
            )

            viewModel.insertBooking(booking)
            scheduleReminder(booking)
            sendBookingToServer(booking)

            Toast.makeText(this, "Booking saved!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun scheduleReminder(booking: Booking) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val triggerTime = sdf.parse("${booking.date} ${booking.startTime}")?.time?.minus(10 * 60 * 1000)

        triggerTime?.let {
            val intent = Intent(this, ReminderReceiver::class.java).apply {
                putExtra("resource", booking.resourceName)
                putExtra("time", booking.startTime)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                this,
                booking.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, it, pendingIntent)
        }
    }

    private fun sendBookingToServer(booking: Booking) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("http://10.0.2.2:8079/bookings_api/insert.php")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonBody = JSONObject()
                jsonBody.put("resourceName", booking.resourceName)
                jsonBody.put("bookedBy", booking.bookedBy)
                jsonBody.put("date", booking.date)
                jsonBody.put("startTime", booking.startTime)
                jsonBody.put("endTime", booking.endTime)

                val output = BufferedWriter(OutputStreamWriter(connection.outputStream))
                output.write(jsonBody.toString())
                output.flush()
                output.close()

                val responseCode = connection.responseCode
                Log.d("UPLOAD", "Response code: $responseCode")
                Log.d("UPLOAD", "JSON Sent: $jsonBody")

                connection.disconnect()
            } catch (e: Exception) {
                Log.e("UPLOAD", "Error: ${e.message}")
            }
        }
    }
}
