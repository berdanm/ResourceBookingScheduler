package com.example.resourcebookingscheduler.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.resourcebookingscheduler.db.AppDatabase
import com.example.resourcebookingscheduler.db.Booking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BookingViewModel(application: Application) : AndroidViewModel(application) {

    private val bookingDao = AppDatabase.getDatabase(application).bookingDao()

    val allBookings: LiveData<List<Booking>> = bookingDao.getAllBookings()

    fun insertBooking(booking: Booking) {
        viewModelScope.launch(Dispatchers.IO) {
            bookingDao.insertBooking(booking)
        }
    }

    fun updateBooking(booking: Booking) {
        viewModelScope.launch(Dispatchers.IO) {
            bookingDao.updateBooking(booking)
        }
    }

    fun deleteBooking(booking: Booking) {
        viewModelScope.launch(Dispatchers.IO) {
            bookingDao.deleteBooking(booking)
        }
    }
}
