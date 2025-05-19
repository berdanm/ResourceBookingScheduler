package com.example.resourcebookingscheduler.ui

import android.app.*
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
    private val uploadScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        // Toolbar with back arrow
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Form fields
        val res     = findViewById<EditText>(R.id.editResourceName)
        val by      = findViewById<EditText>(R.id.editBookedBy)
        val date    = findViewById<EditText>(R.id.editDate)
        val start   = findViewById<EditText>(R.id.editStartTime)
        val end     = findViewById<EditText>(R.id.editEndTime)
        val saveBtn = findViewById<Button>(R.id.btnSaveBooking)

        // Disable typing
        date.isFocusable = false;  date.isClickable = true
        start.isFocusable = false; start.isClickable = true
        end.isFocusable = false;   end.isClickable = true

        val calendar = Calendar.getInstance()

        // Date picker
        date.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                date.setText("%04d-%02d-%02d".format(y, m + 1, d))
            }, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Time picker helper
        fun showTimePicker(field: EditText) {
            TimePickerDialog(this, { _, h, m ->
                field.setText("%02d:%02d".format(h, m))
            }, calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }
        start.setOnClickListener { showTimePicker(start) }
        end.setOnClickListener   { showTimePicker(end)   }

        // Save button
        saveBtn.setOnClickListener {
            Log.d("UPLOAD", "▶️ Save button clicked")

            val booking = Booking(
                resourceName = res.text.toString(),
                bookedBy     = by.text.toString(),
                date         = date.text.toString(),
                startTime    = start.text.toString(),
                endTime      = end.text.toString()
            )

            // Save local and schedule reminder
            viewModel.insertBooking(booking)
            try { scheduleReminder(booking) }
            catch (e: Exception) {
                Log.e("Reminder", "Could not schedule: ${e.message}")
            }

            // Upload then finish
            uploadScope.launch {
                val ok = uploadBooking(booking)
                withContext(Dispatchers.Main) {
                    if (ok) {
                        Toast.makeText(this@BookingActivity, "Booking saved!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@BookingActivity, "Upload failed", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        uploadScope.cancel()
    }

    /** Returns true on HTTP 2xx from Flask */
    private suspend fun uploadBooking(booking: Booking): Boolean {
        return try {
            Log.d("UPLOAD", "Preparing to send booking to Flask")
            val url = URL("http://10.0.2.2:5000/insert_booking")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type","application/json")
                doOutput = true
            }
            conn.outputStream.bufferedWriter().use { out ->
                out.write(JSONObject().apply {
                    put("resourceName", booking.resourceName)
                    put("bookedBy",     booking.bookedBy)
                    put("date",         booking.date)
                    put("startTime",    booking.startTime)
                    put("endTime",      booking.endTime)
                }.toString())
            }
            val code = conn.responseCode
            Log.d("UPLOAD", "Response code: $code")
            conn.disconnect()
            code in 200..299
        } catch (e: Exception) {
            Log.e("UPLOAD", "Error: ${e.message}")
            false
        }
    }

    private fun scheduleReminder(booking: Booking) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val dt = sdf.parse("${booking.date} ${booking.startTime}")
                ?: throw IllegalArgumentException("Invalid date/time")
            val trigger = dt.time - 10 * 60 * 1000L

            val intent = Intent(this, ReminderReceiver::class.java).apply {
                putExtra("resource", booking.resourceName)
                putExtra("time",     booking.startTime)
            }
            val pi = PendingIntent.getBroadcast(
                this, booking.id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            (getSystemService(Context.ALARM_SERVICE) as AlarmManager)
                .setExact(AlarmManager.RTC_WAKEUP, trigger, pi)
        } catch (e: Exception) {
            Log.e("Reminder", "Failed to schedule: ${e.message}")
        }
    }
}
