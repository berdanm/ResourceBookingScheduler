package com.example.resourcebookingscheduler.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_history)

        // ✅ Enable back arrow
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Bookings"

        recyclerView = findViewById(R.id.recyclerBookings)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = BookingAdapter(
            onDeleteClick = { booking ->
                deleteBookingFromServer(booking)
            },
            onItemClick = {}
        )

        recyclerView.adapter = adapter

        fetchBookingsFromServer()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun fetchBookingsFromServer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("http://10.0.2.2:8079/bookings_api/get_bookings.php")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val response = connection.inputStream.bufferedReader().readText()
                connection.disconnect()

                val jsonArray = JSONArray(response)
                bookings.clear()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val booking = Booking(
                        id = obj.getInt("id"),
                        resourceName = obj.getString("resourceName"),
                        bookedBy = obj.getString("bookedBy"),
                        date = obj.getString("date"),
                        startTime = obj.getString("startTime"),
                        endTime = obj.getString("endTime")
                    )
                    bookings.add(booking)
                }

                withContext(Dispatchers.Main) {
                    adapter.submitList(bookings.toList())
                }

            } catch (e: Exception) {
                Log.e("FetchBookings", "Error: ${e.message}")
            }
        }
    }

    private fun deleteBookingFromServer(booking: Booking) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("http://10.0.2.2:8079/bookings_api/delete.php")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val json = JSONObject()
                json.put("id", booking.id)

                val writer = BufferedWriter(OutputStreamWriter(connection.outputStream))
                writer.write(json.toString())
                writer.flush()
                writer.close()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    fetchBookingsFromServer()
                } else {
                    Log.e("DeleteBooking", "Failed with code $responseCode")
                }

                connection.disconnect()
            } catch (e: Exception) {
                Log.e("DeleteBooking", "Error: ${e.message}")
            }
        }
    }
}
