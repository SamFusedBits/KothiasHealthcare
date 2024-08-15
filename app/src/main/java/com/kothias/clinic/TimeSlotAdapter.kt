package com.kothias.clinic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class TimeSlotAdapter(
    // Adapter for the time slots in the RecyclerView
    private val timeSlots: MutableList<Pair<String, String>>,
    private val onSlotRemove: (position: Int) -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_time_slot, parent, false)
        return TimeSlotViewHolder(itemView)
    }

    // Set the contents of the view at the given position
    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        // Get the date and time at the given position
        val (date, time) = timeSlots[position]
        holder.bind(date, time)
    }

    // Return the size of your dataset
    override fun getItemCount(): Int = timeSlots.size

    // Update the slots in the adapter
    fun updateSlots(newSlots: List<Pair<String, String>>) {
        timeSlots.clear()
        timeSlots.addAll(newSlots)
        notifyDataSetChanged()
    }

    // Remove expired slots from the adapter
    fun removeExpiredSlots() {
        val iterator = timeSlots.iterator()
        while (iterator.hasNext()) {
            val (date, _) = iterator.next()
            if (isSlotDatePast(date)) {
                iterator.remove()
            }
        }
        // Notify the adapter that the data set has changed
        notifyDataSetChanged()
    }

    // Check if the slot date is in the past
    private fun isSlotDatePast(slotDate: String): Boolean {
        // Parse the slot date and compare it with the current date
        try {
            val date = dateFormat.parse(slotDate) ?: return true
            val currentDate = Calendar.getInstance().time
            return date.before(currentDate)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    // Inner class for the ViewHolder
    inner class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDateTime: TextView = itemView.findViewById(R.id.tvTimeSlot)

        // Bind the date and time to the view
        fun bind(date: String, time: String) {
            // Combine the date and time strings
            val dateTimeString = "$date $time"
            tvDateTime.text = dateTimeString
            itemView.setOnClickListener {
                onSlotRemove.invoke(adapterPosition)
            }
        }
    }
}