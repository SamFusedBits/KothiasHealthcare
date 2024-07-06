package com.example.ksharsutra

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class TimeSlotAdapter(
    private val timeSlots: MutableList<Pair<String, String>>,
    private val onSlotRemove: (position: Int) -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_time_slot, parent, false)
        return TimeSlotViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        val (date, time) = timeSlots[position]
        holder.bind(date, time)
    }

    override fun getItemCount(): Int = timeSlots.size

    fun updateSlots(newSlots: List<Pair<String, String>>) {
        timeSlots.clear()
        timeSlots.addAll(newSlots)
        notifyDataSetChanged()
    }

    fun removeExpiredSlots() {
        val iterator = timeSlots.iterator()
        while (iterator.hasNext()) {
            val (date, _) = iterator.next()
            if (isSlotDatePast(date)) {
                iterator.remove()
            }
        }
        notifyDataSetChanged()
    }

    private fun isSlotDatePast(slotDate: String): Boolean {
        try {
            val date = dateFormat.parse(slotDate) ?: return true
            val currentDate = Calendar.getInstance().time
            return date.before(currentDate)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    inner class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDateTime: TextView = itemView.findViewById(R.id.tvTimeSlot)

        fun bind(date: String, time: String) {
            val dateTimeString = "$date $time"
            tvDateTime.text = dateTimeString
            itemView.setOnClickListener {
                onSlotRemove.invoke(adapterPosition)
            }
        }
    }
}