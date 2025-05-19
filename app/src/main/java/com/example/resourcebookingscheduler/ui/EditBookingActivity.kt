package com.example.resourcebookingscheduler.ui

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

class EditBookingActivity : AppCompatActivity() {

    private val bookingViewModel: BookingViewModel by viewModels()
    private var bookingId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_booking)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val resourceName = findViewById<EditText>(R.id.editResourceName)
        val bookedBy = findViewById<EditText>(R.id.editBookedBy)
        val date = findViewById<EditText>(R.id.editDate)
        val startTime = findViewById<EditText>(R.id.editStartTime)
        val endTime = findViewById<EditText>(R.id.editEndTime)
        val btnUpdate = findViewById<Button>(R.id.btnUpdateBooking)

        val booking = intent.getSerializableExtra("booking") as Booking
        bookingId = booking.id

        resourceName.setText(booking.resourceName)
        bookedBy.setText(booking.bookedBy)
        date.setText(booking.date)
        startTime.setText(booking.startTime)
        endTime.setText(booking.endTime)

        btnUpdate.setOnClickListener {
            val updatedBooking = Booking(
                id = bookingId,
                resourceName = resourceName.text.toString(),
                bookedBy = bookedBy.text.toString(),
                date = date.text.toString(),
                startTime = startTime.text.toString(),
                endTime = endTime.text.toString()
            )

            // Update local DB
            bookingViewModel.updateBooking(updatedBooking)

            // Update MySQL via PHP
            updateBookingOnServer(updatedBooking)

            Toast.makeText(this, "Booking updated!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun updateBookingOnServer(booking: Booking) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("http://10.0.2.2:5000/update")

                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val json = JSONObject()
                json.put("id", booking.id)
                json.put("resourceName", booking.resourceName)
                json.put("bookedBy", booking.bookedBy)
                json.put("date", booking.date)
                json.put("startTime", booking.startTime)
                json.put("endTime", booking.endTime)

                val writer = BufferedWriter(OutputStreamWriter(connection.outputStream))
                writer.write(json.toString())
                writer.flush()
                writer.close()

                val code = connection.responseCode
                Log.d("UPDATE_BOOKING", "Response: $code")

                connection.disconnect()
            } catch (e: Exception) {
                Log.e("UPDATE_BOOKING", "Error: ${e.message}")
            }
        }
    }
}
