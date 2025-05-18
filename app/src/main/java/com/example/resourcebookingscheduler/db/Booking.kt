package com.example.resourcebookingscheduler.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val resourceName: String,
    val bookedBy: String,
    val date: String,
    val startTime: String,
    val endTime: String
) : Serializable  // ✅ MAKE IT SERIALIZABLE
