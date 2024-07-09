package com.example.ksharsutra

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.ktx.firestoreSettings
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ManageAppointmentsActivity() : AppCompatActivity() {

    private lateinit var rvTodayAppointments: RecyclerView
    private lateinit var rvFutureAppointments: RecyclerView
    private lateinit var tvTodayAppointmentsEmpty: TextView
    private lateinit var tvFutureAppointmentsEmpty: TextView
    private lateinit var tvTimeSlotsEmpty: TextView

    private lateinit var todayAdapter: ManageAppointmentAdapter
    private lateinit var futureAdapter: ManageAppointmentAdapter

    private lateinit var timeSlotAdapter: TimeSlotAdapter
    private lateinit var timeSlots: MutableList<Pair<String, String>>

    private lateinit var rvTimeSlots: RecyclerView

    private lateinit var checkBoxSlot1: CheckBox
    private lateinit var checkBoxSlot2: CheckBox
    private lateinit var checkBoxSlot3: CheckBox

    private var db = FirebaseFirestore.getInstance()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private lateinit var progressBarTodayAppointments: ProgressBar
    private lateinit var progressBarFutureAppointments: ProgressBar
    private lateinit var progressBarTimeSlots: ProgressBar

    private val calendar = Calendar.getInstance()
    private var selectedDate: String = "" // Track selected date

    // Cached lists for appointments and time slots
    private var cachedTodayAppointments: MutableList<ManageAppointment>? = null
    private var cachedFutureAppointments: MutableList<ManageAppointment>? = null
    private var cachedTimeSlots: MutableMap<String, MutableList<Pair<String, String>>> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_appointments)

        // Initialize RecyclerView for time slots
        rvTimeSlots = findViewById(R.id.rvTimeSlots)
        rvTimeSlots.layoutManager = LinearLayoutManager(this)
        timeSlots = mutableListOf()
        timeSlotAdapter = TimeSlotAdapter(timeSlots) { position -> removeSelectedSlot(position) }
        rvTimeSlots.adapter = timeSlotAdapter

        // Configure Firestore settings with offline persistence
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true) // Enable offline persistence
            .build()

        db.firestoreSettings = settings

        rvTodayAppointments = findViewById(R.id.rvTodayAppointments)
        rvFutureAppointments = findViewById(R.id.rvFutureAppointments)
        progressBarTodayAppointments = findViewById(R.id.progressBarTodayAppointments)
        progressBarFutureAppointments = findViewById(R.id.progressBarFutureAppointments)
        progressBarTimeSlots = findViewById(R.id.progressBarTimeSlots)
        tvTodayAppointmentsEmpty = findViewById(R.id.tvTodayAppointmentsEmpty)
        tvFutureAppointmentsEmpty = findViewById(R.id.tvFutureAppointmentsEmpty)
        tvTimeSlotsEmpty = findViewById(R.id.tvTimeSlotsEmpty)

        // Initialize checkboxes and button
        checkBoxSlot1 = findViewById(R.id.checkBoxSlot1)
        checkBoxSlot2 = findViewById(R.id.checkBoxSlot2)
        checkBoxSlot3 = findViewById(R.id.checkBoxSlot3)

        // Initialize RecyclerView for time slots
        rvTimeSlots = findViewById(R.id.rvTimeSlots)
        rvTimeSlots.layoutManager = LinearLayoutManager(this)
        timeSlots = mutableListOf()
        timeSlotAdapter = TimeSlotAdapter(timeSlots) { position -> removeSelectedSlot(position) }
        rvTimeSlots.adapter = timeSlotAdapter

        rvTodayAppointments.layoutManager = LinearLayoutManager(this)
        rvFutureAppointments.layoutManager = LinearLayoutManager(this)

        todayAdapter = ManageAppointmentAdapter() { appointment, action -> handleAppointmentAction(appointment, action) }
        futureAdapter = ManageAppointmentAdapter() { appointment, action -> handleAppointmentAction(appointment, action) }

        rvTodayAppointments.adapter = todayAdapter
        rvFutureAppointments.adapter = futureAdapter

        // Initialize RecyclerView for time slots
        val rvTimeSlots = findViewById<RecyclerView>(R.id.rvTimeSlots)
        rvTimeSlots.layoutManager = LinearLayoutManager(this)
        timeSlots = mutableListOf()
        timeSlotAdapter = TimeSlotAdapter(timeSlots) { position -> removeSelectedSlot(position) }
        rvTimeSlots.adapter = timeSlotAdapter

        // Setup schedule dates
        val layoutScheduleDates = findViewById<LinearLayout>(R.id.layout_schedule_dates)
        setupScheduleDates(layoutScheduleDates)

        loadAppointments()
        loadTimeSlots(selectedDate)

        // Navigation to ManageReports Page
        val navigation_report = findViewById<ImageView>(R.id.navigation_reports)
        navigation_report.setOnClickListener{
            val intent = Intent(this, ManageReportsActivity::class.java)
            startActivity(intent)
        }

        // Button to add selected time slots
        val btnAddTimeSlot = findViewById<Button>(R.id.btnAddSlot)
        btnAddTimeSlot.setOnClickListener {
            addSelectedTimeSlots(selectedDate) // Pass selected date when adding time slots
        }
    }

    private fun setupScheduleDates(layoutScheduleDates: LinearLayout) {
        val displayDateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        val queryDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Total dates to display excluding Sundays
        val numDates = 6
        val numCols = 3
        var datesDisplayed = 0
        var currentRow: LinearLayout? = null

        var previousSelectedView: TextView? = null

        tvTimeSlotsEmpty.text = "Please select a date to view slots"
        tvTimeSlotsEmpty.visibility = View.VISIBLE
        rvTimeSlots.visibility = View.GONE

        while (datesDisplayed < numDates) {
            // Skip Sundays
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                continue
            }

            // Create a new row for every `numCols` items
            if (datesDisplayed % numCols == 0) {
                currentRow = LinearLayout(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.CENTER_VERTICAL
                    }
                    orientation = LinearLayout.HORIZONTAL
                    layoutScheduleDates.addView(this)
                }
            }

            // Create TextView for each date
            val dayTextView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx()) // Adjust margins as needed
                }
                val currentDisplayDate = displayDateFormat.format(calendar.time)
                val currentQueryDate = queryDateFormat.format(calendar.time)
                text = currentDisplayDate
                setBackgroundResource(R.drawable.day_background) // Replace with your drawable
                gravity = Gravity.CENTER
                setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx()) // Adjust padding as needed
                setTextColor(Color.BLACK)
                tag = currentQueryDate // Store the query format date as tag

                setOnClickListener {
                    // Change the background color of the previously selected date back to default
                    previousSelectedView?.setBackgroundResource(R.drawable.day_background)
                    previousSelectedView?.setTextColor(Color.BLACK)

                    // Change the background color of the currently selected date
                    setBackgroundColor(Color.parseColor("#4CAF50"))
                    setTextColor(Color.WHITE)

                    // Update the previously selected view to the current one
                    previousSelectedView = this

                    // Update selectedDate
                    selectedDate = currentQueryDate
                    loadTimeSlots(selectedDate)
                }
            }

            // Add TextView to the current row
            currentRow?.addView(dayTextView)

            // Move to the next day
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            datesDisplayed++
        }
    }

    // Extension function to convert dp to pixels
    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    // Function to load appointments
    private fun loadAppointments() {
        progressBarTodayAppointments.visibility = View.VISIBLE
        progressBarFutureAppointments.visibility = View.VISIBLE

        val todayDate = dateFormat.format(Date())

        // Use cached data if available
        cachedTodayAppointments?.let { todayAppointments ->
            cachedFutureAppointments?.let { futureAppointments ->
                todayAdapter.submitList(todayAppointments)
                futureAdapter.submitList(futureAppointments)
                updateEmptyStateViews(todayAppointments.isEmpty(), futureAppointments.isEmpty())
                progressBarTodayAppointments.visibility = View.GONE
                progressBarFutureAppointments.visibility = View.GONE
                return
            }
        }

        // Otherwise, fetch from Firestore
        db.collection("appointments")
            .get()
            .addOnSuccessListener { result ->
                val todayAppointments = mutableListOf<ManageAppointment>()
                val futureAppointments = mutableListOf<ManageAppointment>()

                for (document in result) {
                    val appointment = document.toObject(ManageAppointment::class.java)
                    appointment.id = document.id

                    if (appointment.schedule.startsWith(todayDate)) {
                        todayAppointments.add(appointment)
                    } else {
                        futureAppointments.add(appointment)
                    }
                }

                // Update cached data
                cachedTodayAppointments = todayAppointments
                cachedFutureAppointments = futureAppointments

                todayAdapter.submitList(todayAppointments)
                futureAdapter.submitList(futureAppointments)

                updateEmptyStateViews(todayAppointments.isEmpty(), futureAppointments.isEmpty())

                progressBarTodayAppointments.visibility = View.GONE
                progressBarFutureAppointments.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                progressBarTodayAppointments.visibility = View.GONE
                progressBarFutureAppointments.visibility = View.GONE
                Toast.makeText(this, "Error loading appointments: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateEmptyStateViews(todayAppointmentsEmpty: Boolean, futureAppointmentsEmpty: Boolean) {
        if (todayAppointmentsEmpty) {
            tvTodayAppointmentsEmpty.visibility = View.VISIBLE
            rvTodayAppointments.visibility = View.GONE
        } else {
            tvTodayAppointmentsEmpty.visibility = View.GONE
            rvTodayAppointments.visibility = View.VISIBLE
        }

        if (futureAppointmentsEmpty) {
            tvFutureAppointmentsEmpty.visibility = View.VISIBLE
            rvFutureAppointments.visibility = View.GONE
        } else {
            tvFutureAppointmentsEmpty.visibility = View.GONE
            rvFutureAppointments.visibility = View.VISIBLE
        }
    }

    private fun loadTimeSlots(selectedDate: String) {
        progressBarTimeSlots.visibility = View.VISIBLE
        if (cachedTimeSlots.containsKey(selectedDate)) {
            // Use cached data
            val slots = cachedTimeSlots[selectedDate]!!
            timeSlots.clear()
            timeSlots.addAll(slots)
            timeSlotAdapter.notifyDataSetChanged()
            updateEmptyStateViewForTimeSlots(slots.isEmpty())
            return
        }

        db.collection("time_slots")
            .whereEqualTo("date", selectedDate)
            .get()
            .addOnSuccessListener { result ->
                val slots = mutableListOf<Pair<String, String>>()
                for (document in result) {
                    val slot = document.getString("time_slot") ?: continue
                    slots.add(Pair(selectedDate, slot))
                }

                cachedTimeSlots[selectedDate] = slots

                // Update the adapter with new slots
                timeSlots.clear()
                timeSlots.addAll(slots)
                timeSlotAdapter.notifyDataSetChanged()

                updateEmptyStateViewForTimeSlots(slots.isEmpty())

                progressBarTimeSlots.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                progressBarTimeSlots.visibility = View.GONE
                Toast.makeText(this, "Failed to load time slots: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun refreshData() {
        cachedTodayAppointments = null
        cachedFutureAppointments = null
        cachedTimeSlots.clear()
        loadAppointments()
        loadTimeSlots(selectedDate)
    }

    private fun updateEmptyStateViewForTimeSlots(slotsEmpty: Boolean) {
        if (slotsEmpty) {
            tvTimeSlotsEmpty.visibility = View.VISIBLE
            rvTimeSlots.visibility = View.GONE
        } else {
            tvTimeSlotsEmpty.visibility = View.GONE
            rvTimeSlots.visibility = View.VISIBLE
        }
    }

    private fun removeSelectedSlot(position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            val selectedSlot = timeSlots[position]

            // Delete slot from Firestore
            db.collection("time_slots").whereEqualTo("date", selectedSlot.first)
                .whereEqualTo("time_slot", selectedSlot.second)
                .get()
                .addOnSuccessListener { result ->
                    if (!result.isEmpty) {
                        val documentId = result.documents[0].id
                        db.collection("time_slots").document(documentId)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Slot removed successfully.", Toast.LENGTH_SHORT).show()
                                timeSlots.removeAt(position) // Remove slot from local list
                                timeSlotAdapter.notifyItemRemoved(position) // Notify adapter of item removal
                                updateEmptyStateViewForTimeSlots(timeSlots.isEmpty())
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(this, "Failed to remove slot: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Slot not found in Firestore.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to find slot in Firestore: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Please select a slot to remove.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addSelectedTimeSlots(selectedDate: String) {
        val selectedSlots = mutableListOf<String>()

        if (checkBoxSlot1.isChecked) {
            selectedSlots.add(checkBoxSlot1.text.toString())
        }
        if (checkBoxSlot2.isChecked) {
            selectedSlots.add(checkBoxSlot2.text.toString())
        }
        if (checkBoxSlot3.isChecked) {
            selectedSlots.add(checkBoxSlot3.text.toString())
        }

        if (selectedSlots.isEmpty()) {
            Toast.makeText(this, "Please select at least one time slot.", Toast.LENGTH_SHORT).show()
            return
        }

        val batch = db.batch() // Use batch operation for multiple writes

        // Use a set to keep track of slots already added
        val addedSlots = mutableSetOf<String>()

        for (slot in selectedSlots) {
            // Check if the slot already exists
            db.collection("time_slots")
                .whereEqualTo("date", selectedDate)
                .whereEqualTo("time_slot", slot)
                .get()
                .addOnSuccessListener { result ->
                    if (result.isEmpty) {
                        // Slot does not exist, add it
                        val slotData = mapOf(
                            "date" to selectedDate,
                            "time_slot" to slot
                        )
                        val newDocRef = db.collection("time_slots").document()
                        batch.set(newDocRef, slotData)
                        addedSlots.add(slot) // Track added slots
                    } else {
                        // Slot already exists, notify user or handle accordingly
                        Toast.makeText(this, "Time slot $slot already exists for $selectedDate.", Toast.LENGTH_SHORT).show()
                    }

                    // Commit batch write after processing all slots
                    if (addedSlots.size == selectedSlots.size) {
                        batch.commit()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Time slots added successfully.", Toast.LENGTH_SHORT).show()
                                // Update local list and RecyclerView adapter if needed
                                for (slot in addedSlots) {
                                    if (!timeSlots.any { it.second == slot }) {
                                        timeSlots.add(Pair(selectedDate, slot))
                                    }
                                }
                                timeSlotAdapter.notifyDataSetChanged()
                                updateEmptyStateViewForTimeSlots(false)
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(this, "Failed to add time slots: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to check existing slots: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun handleAppointmentAction(appointment: ManageAppointment, action: String) {
        val subject = "Appointment ${if (action == "approve") "Approved" else "Declined"}"
        val content = when (action) {
            "approve" -> """
            Dear ${appointment.name},

            We are pleased to inform you that your appointment request has been approved. Please find the details of your appointment below:

            Appointment Date and Time: ${appointment.schedule}
            Location: Saurabh Building, Kshara Sutra Hospital, Domnic Colony Rd Number 1, near N.L. High School, Malad, Daruwala Compound, Malad West, Mumbai, Maharashtra 400064.

            If you have any questions or need further assistance, please do not hesitate to contact us.

            Thank you for choosing our clinic.

            Best regards,
            Dr. Kothia's Clinic
            """
            "decline" -> {
                // Remove the declined appointment from the list and Firestore
                db.collection("appointments").document(appointment.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Appointment declined successfully.", Toast.LENGTH_SHORT).show()
                        loadAppointments() // Reload appointments after deletion
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Failed to decline appointment: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                return
            }
            else -> "We are sorry, your appointment has not been approved. Please contact us for further details."
        }

        // Update appointment status to approved in Firestore
        if (action == "approve") {
            db.collection("appointments").document(appointment.id)
                .update("status", "Approved")
                .addOnSuccessListener {
                    Toast.makeText(this, "Appointment approved successfully.", Toast.LENGTH_SHORT).show()
                    loadAppointments() // Reload appointments after approval
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to approve appointment: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }

        try {
            //SendGridHelper.sendEmail(appointment.email, subject, content)
            Toast.makeText(this, "Email sent successfully.", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            Toast.makeText(this, "Failed to send email: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}
