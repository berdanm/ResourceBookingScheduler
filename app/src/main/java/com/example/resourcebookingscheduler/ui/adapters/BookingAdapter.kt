package com.example.resourcebookingscheduler.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.resourcebookingscheduler.R
import com.example.resourcebookingscheduler.db.Booking

class BookingAdapter(
    private val onDeleteClick: (Booking) -> Unit,
    private val onItemClick: (Booking) -> Unit
) : ListAdapter<Booking, BookingAdapter.BookingViewHolder>(DiffCallback()) {

    inner class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name = itemView.findViewById<TextView>(R.id.txtBookingName)
        private val date = itemView.findViewById<TextView>(R.id.txtBookingDate)
        private val time = itemView.findViewById<TextView>(R.id.txtBookingTime)
        private val deleteBtn = itemView.findViewById<Button>(R.id.btnDeleteBooking)

        fun bind(booking: Booking) {
            name.text = booking.resourceName
            date.text = booking.date
            time.text = "${booking.startTime} - ${booking.endTime}"

            deleteBtn.setOnClickListener {
                onDeleteClick(booking)
            }

            itemView.setOnClickListener {
                onItemClick(booking)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Booking>() {
        override fun areItemsTheSame(oldItem: Booking, newItem: Booking) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Booking, newItem: Booking) = oldItem == newItem
    }
}
