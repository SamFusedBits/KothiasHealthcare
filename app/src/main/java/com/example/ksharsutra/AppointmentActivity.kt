package com.example.ksharsutra

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.gridlayout.widget.GridLayout
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AppointmentActivity : AppCompatActivity() {

    private lateinit var scheduleGrid: GridLayout
    private lateinit var timeSlotsGrid: GridLayout
    private var selectedSchedule: String? = null
    private var selectedTimeSlot: String? = null
    private var selectedTextView: TextView? = null
    private var selectedScheduleTextView: TextView? = null
    private var selectedTimeSlotTextView: TextView? = null

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment)

        scheduleGrid = findViewById(R.id.scheduleGrid)
        timeSlotsGrid = findViewById(R.id.timeSlotsGrid)

        // Setup schedule grid
        setupScheduleGrid()

        findViewById<View>(R.id.bookNowButton).setOnClickListener {
            // Handle booking process
            if (selectedSchedule != null && selectedTimeSlot != null) {
                val intent = Intent(this, AppointmentBookingActivity::class.java).apply {
                    putExtra("selected_schedule", "$selectedSchedule | $selectedTimeSlot")
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please select schedule and a time slot", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to set up schedule grid
    private fun setupScheduleGrid() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Adjust date format to match Firestore

        for (i in 0..5) {
            // Skip Sundays
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            val dayTextView = TextView(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = GridLayout.LayoutParams.WRAP_CONTENT
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    setMargins(4.dpToPx(), 4.dpToPx(), 4.dpToPx(), 4.dpToPx())
                }
                text = dateFormat.format(calendar.time)
                setBackgroundResource(R.drawable.day_background)
                gravity = Gravity.CENTER
                setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())

                setOnClickListener {
                    selectSchedule(this)
                }
            }

            // Add the TextView to the grid
            scheduleGrid.addView(dayTextView)

            // Move to the next day
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    // Function to set up time slots grid based on selected date
    private fun setupTimeSlotsGrid(selectedDate: String) {
        timeSlotsGrid.removeAllViews()

        // Fetch time slots from Firestore for the selected date
        db.collection("time_slots")
            .whereEqualTo("date", selectedDate)
            .get()
            .addOnSuccessListener { result ->
                val slots = mutableListOf<String>()
                for (document in result) {
                    val timeSlot = document.getString("time_slot")
                    if (timeSlot != null) {
                        slots.add(timeSlot)
                    }
                }

                if (slots.isNotEmpty()) {
                    for (timeSlot in slots) {
                        val timeSlotTextView = TextView(this).apply {
                            layoutParams = GridLayout.LayoutParams().apply {
                                width = GridLayout.LayoutParams.WRAP_CONTENT
                                height = GridLayout.LayoutParams.WRAP_CONTENT
                                setMargins(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                            }
                            text = timeSlot
                            setBackgroundResource(R.drawable.time_background)
                            gravity = Gravity.CENTER
                            setPadding(12.dpToPx(), 12.dpToPx(), 12.dpToPx(), 12.dpToPx())

                            setOnClickListener {
                                selectTimeSlot(this)
                            }
                        }

                        timeSlotsGrid.addView(timeSlotTextView)
                    }
                } else {
                    // Display a message indicating no slots available for the day
                    val noSlotsTextView = TextView(this).apply {
                        layoutParams = GridLayout.LayoutParams().apply {
                            width = GridLayout.LayoutParams.WRAP_CONTENT
                            height = GridLayout.LayoutParams.WRAP_CONTENT
                            setMargins(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                        }
                        text = "No slots available for $selectedDate"
                        gravity = Gravity.CENTER
                        setTextColor(Color.BLACK)
                        setPadding(12.dpToPx(), 12.dpToPx(), 12.dpToPx(), 12.dpToPx())
                    }
                    timeSlotsGrid.addView(noSlotsTextView)
                }
            }
            .addOnFailureListener { exception ->
                // Log the failure message for debugging
                Log.e("AppointmentActivity", "Failed to load time slots: ${exception.message}", exception)
                Toast.makeText(this, "Failed to load time slots: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun selectSchedule(scheduleTextView: TextView) {
        // Deselect the previously selected schedule
        selectedScheduleTextView?.apply {
            setBackgroundResource(R.drawable.day_background) // Reset to default background
            setTextColor(Color.BLACK) // Reset text color to default
        }

        // Select the new schedule
        scheduleTextView.apply {
            setBackgroundResource(R.drawable.selected_day_background) // Set selected background
            setTextColor(Color.WHITE) // Set selected text color
        }

        // Update the selected schedule
        selectedSchedule = scheduleTextView.text.toString()
        selectedScheduleTextView = scheduleTextView

        setupTimeSlotsGrid(selectedSchedule!!)
    }

    private fun selectTimeSlot(timeSlotTextView: TextView) {
        // Deselect the previously selected time slot
        selectedTextView?.apply {
            setBackgroundResource(R.drawable.time_background) // Reset to default background
            setTextColor(Color.BLACK) // Reset text color to default
        }

        // Select the new time slot
        timeSlotTextView.apply {
            setBackgroundResource(R.drawable.selected_time_background) // Set selected background
            setTextColor(Color.WHITE) // Set selected text color
        }

        // Update the selected time slot
        selectedTimeSlot = timeSlotTextView.text.toString()
        selectedTextView = timeSlotTextView
    }

    // Extension function to convert dp to pixels
    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}
