package com.example.resourcebookingscheduler.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BookingDao {

    @Query("SELECT * FROM bookings ORDER BY date")
    fun getAllBookings(): LiveData<List<Booking>>

    @Insert
    suspend fun insertBooking(booking: Booking)

    @Update
    suspend fun updateBooking(booking: Booking)

    @Delete
    suspend fun deleteBooking(booking: Booking)
}
