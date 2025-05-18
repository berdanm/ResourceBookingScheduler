package com.example.resourcebookingscheduler.ui

import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.resourcebookingscheduler.R
import com.example.resourcebookingscheduler.db.Booking
import com.example.resourcebookingscheduler.viewmodel.BookingViewModel

class EditBookingActivity : AppCompatActivity() {

    private val bookingViewModel: BookingViewModel by viewModels()
    private var bookingId: Int = 0  // we need to preserve ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        val resourceName = findViewById<EditText>(R.id.editResourceName)
        val bookedBy = findViewById<EditText>(R.id.editBookedBy)
        val date = findViewById<EditText>(R.id.editDate)
        val startTime = findViewById<EditText>(R.id.editStartTime)
        val endTime = findViewById<EditText>(R.id.editEndTime)
        val btnSave = findViewById<Button>(R.id.btnSaveBooking)

        // Get passed booking from intent
        val booking = intent.getSerializableExtra("booking") as Booking
        bookingId = booking.id

        resourceName.setText(booking.resourceName)
        bookedBy.setText(booking.bookedBy)
        date.setText(booking.date)
        startTime.setText(booking.startTime)
        endTime.setText(booking.endTime)

        btnSave.text = "Update Booking"

        btnSave.setOnClickListener {
            val updatedBooking = Booking(
                id = bookingId,
                resourceName = resourceName.text.toString(),
                bookedBy = bookedBy.text.toString(),
                date = date.text.toString(),
                startTime = startTime.text.toString(),
                endTime = endTime.text.toString()
            )
            bookingViewModel.updateBooking(updatedBooking)
            Toast.makeText(this, "Booking updated!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
