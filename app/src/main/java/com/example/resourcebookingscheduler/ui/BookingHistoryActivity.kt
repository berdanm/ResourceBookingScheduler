package com.example.resourcebookingscheduler.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.resourcebookingscheduler.R
import com.example.resourcebookingscheduler.db.Booking
import com.example.resourcebookingscheduler.ui.adapters.BookingAdapter
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class BookingHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BookingAdapter
    private val bookings = mutableListOf<Booking>()
    private val fetchScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_history)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerBookings)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = BookingAdapter(
            onDeleteClick = { booking ->
                deleteBookingOnServer(booking)
            },
            onItemClick = { booking ->
                val intent = Intent(this, EditBookingActivity::class.java)
                intent.putExtra("booking", booking)
                startActivity(intent)
            }
        )
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        fetchBookingsFromServer()
    }

    override fun onDestroy() {
        super.onDestroy()
        fetchScope.cancel()
    }

    private fun fetchBookingsFromServer() {
        Log.d("FetchBookings", "🏃‍♂️ fetchBookingsFromServer() called")
        fetchScope.launch {
            try {
                val url = URL("http://10.0.2.2:5000/get_bookings")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"

                val response = conn.inputStream.bufferedReader().readText()
                conn.disconnect()

                Log.d("FetchBookings", "📥 Raw response: $response")

                val jsonArray = JSONArray(response)
                bookings.clear()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    bookings.add(
                        Booking(
                            id = obj.getInt("id"),
                            resourceName = obj.getString("resourceName"),
                            bookedBy = obj.getString("bookedBy"),
                            date = obj.getString("date"),
                            startTime = obj.getString("startTime"),
                            endTime = obj.getString("endTime")
                        )
                    )
                }

                withContext(Dispatchers.Main) {
                    adapter.submitList(bookings.toList())
                    if (bookings.isEmpty()) {
                        Toast.makeText(this@BookingHistoryActivity,
                            "No bookings found", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: Exception) {
                Log.e("FetchBookings", "Error: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@BookingHistoryActivity,
                        "Failed to load bookings", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    private fun deleteBookingOnServer(booking: Booking) {
        fetchScope.launch {
            try {
                val url = URL("http://10.0.2.2:5000/delete_booking")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val body = JSONObject().put("id", booking.id).toString()
                BufferedWriter(OutputStreamWriter(conn.outputStream)).use {
                    it.write(body); it.flush()
                }

                val code = conn.responseCode
                conn.disconnect()
                Log.d("DeleteBooking", "Response code: $code")

                // refresh on success
                if (code in 200..299) fetchBookingsFromServer()
                else throw Exception("Server returned $code")
            } catch (e: Exception) {
                Log.e("DeleteBooking", "Error: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@BookingHistoryActivity,
                        "Delete failed: ${e.message}", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }
}
