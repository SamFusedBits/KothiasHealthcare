package com.example.ksharsutra

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.gridlayout.widget.GridLayout

class AppointmentActivity : AppCompatActivity() {

    private var selectedTimeSlot: TextView? = null
    private var selectedScheduleSlot: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_appointment)

        val back = findViewById<ImageView>(R.id.back_arrow)
        back.setOnClickListener {
            finish()
        }

        val timeSlotsGrid = findViewById<GridLayout>(R.id.timeSlotsGrid)
        val scheduleGrid = findViewById<GridLayout>(R.id.scheduleGrid)

        setupClickListener(timeSlotsGrid, true)
        setupClickListener(scheduleGrid, false)
    }

    private fun setupClickListener(gridLayout: GridLayout, isTimeSlot: Boolean) {
        for (i in 0 until gridLayout.childCount) {
            val slot = gridLayout.getChildAt(i) as TextView
            slot.setOnClickListener {
                if (isTimeSlot) {
                    selectedTimeSlot?.let {
                        // Reset the previously selected time slot
                        it.setBackgroundResource(R.drawable.time_background)
                        it.setTextColor(Color.BLACK)
                    }
                    // Set the new selected time slot
                    selectedTimeSlot = slot
                    selectedTimeSlot?.setBackgroundResource(R.drawable.selected_time_background)
                    selectedTimeSlot?.setTextColor(Color.WHITE)
                } else {
                    selectedScheduleSlot?.let {
                        // Reset the previously selected schedule slot
                        it.setBackgroundResource(R.drawable.day_background)
                        it.setTextColor(Color.BLACK)
                    }
                    // Set the new selected schedule slot
                    selectedScheduleSlot = slot
                    selectedScheduleSlot?.setBackgroundResource(R.drawable.selected_day_background)
                    selectedScheduleSlot?.setTextColor(Color.WHITE)
                }
            }
        }
    }
}
