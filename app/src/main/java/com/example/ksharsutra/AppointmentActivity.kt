package com.example.ksharsutra

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
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

    // Firestore instance
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment)

        scheduleGrid = findViewById(R.id.scheduleGrid)
        timeSlotsGrid = findViewById(R.id.timeSlotsGrid)

        val navigation_profile = findViewById<ImageView>(R.id.navigation_profile)
        val navigation_report = findViewById<ImageView>(R.id.navigation_report)
        val navigation_home = findViewById<ImageView>(R.id.navigation_home)

        navigation_profile.setOnClickListener {
            val intent = Intent(this, UserDetailsActivity::class.java)
            startActivity(intent)
        }

        navigation_report.setOnClickListener {
            val intent = Intent(this, NavigationReportActivity::class.java)
            startActivity(intent)
        }

        navigation_home.setOnClickListener {
            val intent = Intent(this, HomePageActivity::class.java)
            startActivity(intent)
        }

        // Setup schedule grid
        setupScheduleGrid()

        // Setup book now button
        findViewById<View>(R.id.bookNowButton).setOnClickListener {
            // Handle booking process
            if (selectedSchedule != null && selectedTimeSlot != null) {
                val selectedDate = selectedScheduleTextView?.tag.toString() // Get the date in database format
                val intent = Intent(this, AppointmentBookingActivity::class.java).apply {
                    // Pass the selected schedule and time slot to the next activity
                    putExtra("selected_schedule", "$selectedDate | $selectedTimeSlot")
                }
                startActivity(intent)
                // Reset the selected schedule and time slot
            } else if (selectedSchedule == null) {
                Toast.makeText(this, "Please select a schedule", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please select schedule and a time slot", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to set up schedule grid
    private fun setupScheduleGrid() {
        // Get the current date
        val calendar = Calendar.getInstance()
        val databaseDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Format for saving in Firestore
        val displayDateFormat = SimpleDateFormat("MMMM, dd", Locale.getDefault()) // Format for displaying to the user

        // Set the calendar to the current date
        for (i in 0..5) {
            // Skip Sundays
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            // Create a TextView for the day
            val dayTextView = TextView(this).apply {
                // Set the layout parameters
                layoutParams = GridLayout.LayoutParams().apply {
                    // Set the width and height to wrap content
                    width = GridLayout.LayoutParams.WRAP_CONTENT
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    // Set the margins to 4dp
                    setMargins(4.dpToPx(), 4.dpToPx(), 4.dpToPx(), 4.dpToPx())
                }
                // Format the date
                val dateForDatabase = databaseDateFormat.format(calendar.time)
                val dateForDisplay = displayDateFormat.format(calendar.time)
                // Set the text to the formatted date
                text = dateForDisplay
                tag = dateForDatabase // Store the database format date as tag
                setBackgroundResource(R.drawable.day_background)
                gravity = Gravity.CENTER
                setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                setTextColor(Color.BLACK) // Set the text color to black

                // Set the onClickListener to select the schedule
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
            // Query for the selected date
            .whereEqualTo("date", selectedDate)
            .get()
            // Get the time slots
            .addOnSuccessListener { result ->
                val slots = mutableListOf<String>()
                // Loop through the documents and get the time slots
                for (document in result) {
                    val timeSlot = document.getString("time_slot")
                    if (timeSlot != null) {
                        slots.add(timeSlot)
                    }
                }

                // Sort the time slots based on start time
                val sortedSlots = slots.sortedBy { parseTimeSlot(it) }

                // If time slots are available, display them
                if (sortedSlots.isNotEmpty()) {
                    for (timeSlot in sortedSlots) {
                        // Create a TextView for the time slot
                        val timeSlotTextView = TextView(this).apply {
                            layoutParams = GridLayout.LayoutParams().apply {
                                // Set the width and height to wrap content
                                width = GridLayout.LayoutParams.WRAP_CONTENT
                                height = GridLayout.LayoutParams.WRAP_CONTENT
                                setMargins(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                            }
                            text = timeSlot
                            setBackgroundResource(R.drawable.time_background)
                            gravity = Gravity.CENTER
                            setPadding(12.dpToPx(), 12.dpToPx(), 12.dpToPx(), 12.dpToPx())
                            setTextColor(Color.BLACK) // Set the text color to black

                            setOnClickListener {
                                selectTimeSlot(this)
                            }
                        }

                        // Add the TextView to the grid
                        timeSlotsGrid.addView(timeSlotTextView)
                    }
                } else {
                    // Ensure only one "No slots available" message is added
                    if (timeSlotsGrid.childCount == 0) {
                        // Display a message indicating no slots available for the day
                        val noSlotsTextView = TextView(this).apply {
                            // Set the layout parameters
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
            }
            .addOnFailureListener { exception ->
                // Log the failure message for debugging
                Log.e("AppointmentActivity", "Failed to load time slots: ${exception.message}", exception)
                Toast.makeText(this, "Failed to load time slots: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to parse time slots and return comparable values for sorting
    private fun parseTimeSlot(timeSlot: String): Int {
        val timeParts = timeSlot.split(" ")[0].split("-")
        val startTime = timeParts[0].toInt()
        val period = timeSlot.split(" ")[1] // AM or PM

        // Convert 12-hour format to 24-hour format for sorting
        return if (period == "PM" && startTime != 12) {
            startTime + 12
        } else if (period == "AM" && startTime == 12) {
            0
        } else {
            startTime
        }
    }

    // Function to select a schedule
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

        val selectedDateForDatabase = scheduleTextView.tag.toString() // Get the date in database format
        setupTimeSlotsGrid(selectedDateForDatabase)
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
