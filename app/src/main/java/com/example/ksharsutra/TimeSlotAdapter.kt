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
    private val timeSlots: MutableList<String>,
    private val onSlotRemove: (position: Int) -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val todayDate = Calendar.getInstance().time
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_time_slot, parent, false)
        return TimeSlotViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        val slot = timeSlots[position]
        holder.bind(slot)
    }

    override fun getItemCount(): Int = timeSlots.size

    fun updateSlots(newSlots: List<String>) {
        timeSlots.clear()
        timeSlots.addAll(newSlots)
        notifyDataSetChanged()
    }

    fun removeExpiredSlots() {
        val iterator = timeSlots.iterator()
        while (iterator.hasNext()) {
            val slot = iterator.next()
            if (isSlotDatePast(slot)) {
                iterator.remove()
            }
        }
        notifyDataSetChanged()
    }

    private fun isSlotDatePast(slot: String): Boolean {
        try {
            val slotDate = dateFormat.parse(slot) ?: return true
            val currentDate = Calendar.getInstance().time
            return slotDate.before(currentDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return true
    }

    inner class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTimeSlot: TextView = itemView.findViewById(R.id.tvTimeSlot)

        fun bind(slot: String) {
            tvTimeSlot.text = slot
            itemView.setOnClickListener {
                onSlotRemove.invoke(adapterPosition)
            }
        }
    }
}