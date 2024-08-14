package com.example.ksharsutra

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
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

    private lateinit var rvAppointments: RecyclerView
    private lateinit var tvAppointmentsEmpty: TextView
    private lateinit var tvTimeSlotsEmpty: TextView

    private lateinit var AppointmentsAdapter: ManageAppointmentAdapter

    private lateinit var timeSlotAdapter: TimeSlotAdapter
    private lateinit var timeSlots: MutableList<Pair<String, String>>

    private lateinit var rvTimeSlots: RecyclerView

    private lateinit var checkBoxSlot1: CheckBox
    private lateinit var checkBoxSlot2: CheckBox
    private lateinit var checkBoxSlot3: CheckBox

    // Initialize Firestore
    private var db = FirebaseFirestore.getInstance()

    // Date format for Firestore queries
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Progress bars for loading appointments and time slots
    private lateinit var progressBarAppointments: ProgressBar
    private lateinit var progressBarTimeSlots: ProgressBar

    // Calendar instance for date manipulation
    private val calendar = Calendar.getInstance()
    private var selectedDate: String = "" // Track selected date

    // Cached lists for appointments and time slots
    private var cachedAllAppointments: MutableList<ManageAppointment>? = null
    private var cachedTimeSlots: MutableMap<String, MutableList<Pair<String, String>>> = mutableMapOf()

    private lateinit var logoutButton: Button
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_appointments)

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance()
        logoutButton = findViewById(R.id.logoutButton)
        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Initialize Google Sign-In client
        googleSignInClient = GoogleSignIn.getClient(this, gso)

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

        // Set Firestore settings
        db.firestoreSettings = settings

        // Initialize views
        rvAppointments = findViewById(R.id.rvAppointments)
        progressBarAppointments = findViewById(R.id.progressBarAppointments)
        progressBarTimeSlots = findViewById(R.id.progressBarTimeSlots)
        tvAppointmentsEmpty = findViewById(R.id.tvAppointmentsEmpty)
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

        // Setup schedule dates
        rvAppointments.layoutManager = LinearLayoutManager(this)

        // Initialize adapter for appointments
        AppointmentsAdapter = ManageAppointmentAdapter() { appointment, action -> handleAppointmentAction(appointment, action) }
        rvAppointments.adapter = AppointmentsAdapter

        // Initialize RecyclerView for time slots
        val rvTimeSlots = findViewById<RecyclerView>(R.id.rvTimeSlots)
        rvTimeSlots.layoutManager = LinearLayoutManager(this)
        timeSlots = mutableListOf()
        timeSlotAdapter = TimeSlotAdapter(timeSlots) { position -> removeSelectedSlot(position) }
        rvTimeSlots.adapter = timeSlotAdapter

        // Setup schedule dates
        val layoutScheduleDates = findViewById<LinearLayout>(R.id.layout_schedule_dates)
        setupScheduleDates(layoutScheduleDates)

        // Load appointments and time slots
        loadAppointments()
        loadTimeSlots(selectedDate)

        // Remove past appointments when activity starts
        removePastAppointments()

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

        logoutButton.setOnClickListener {
            logoutUser()
        }
    }

    // Function to log out the user
    private fun logoutUser() {
        // Log out the user from Firebase Authentication
        mAuth.signOut()

        // Clear cached Google Sign-In account information
        googleSignInClient.signOut().addOnCompleteListener {
            // Redirect the user to the login screen
            val intent = Intent(this, MainActivity::class.java)
            // Clear the back stack and start the new activity
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    // Function to setup schedule dates
    private fun setupScheduleDates(layoutScheduleDates: LinearLayout) {
        // Date formats for display and Firestore queries
        val displayDateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        val queryDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Total dates to display excluding Sundays
        val numDates = 6
        val numCols = 3
        var datesDisplayed = 0
        var currentRow: LinearLayout? = null

        // Track the previously selected date view
        var previousSelectedView: TextView? = null

        tvTimeSlotsEmpty.text = "Please select a date to view slots"
        tvTimeSlotsEmpty.visibility = View.VISIBLE
        rvTimeSlots.visibility = View.GONE

        // Loop through the next `numDates` dates
        while (datesDisplayed < numDates) {
            // Skip Sundays
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                continue
            }

            // Create a new row for every `numCols` items
            if (datesDisplayed % numCols == 0) {
                // Create a new row
                currentRow = LinearLayout(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        // Adjust layout parameters as needed
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
                    // Adjust layout parameters as needed
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx()) // Adjust margins as needed
                }
                // Format the date and set it as text
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
        // Show progress bar while loading appointments
        progressBarAppointments.visibility = View.VISIBLE

        // Use cached data if available
        cachedAllAppointments?.let {
            // Update the adapter with cached data
            AppointmentsAdapter.submitList(it)
            // Update empty state views
            updateEmptyStateViews(it.isEmpty())
            // Hide progress bar
            progressBarAppointments.visibility = View.GONE
            return
        }

        // Otherwise, fetch from Firestore
        db.collection("appointments")
            .get()
            .addOnSuccessListener { result ->
                // List to store all appointments
                val allAppointments = mutableListOf<ManageAppointment>()

                // Loop through the result and add each document to the list
                for (document in result) {
                    // Convert each document to a ManageAppointment object
                    val appointment = document.toObject(ManageAppointment::class.java)
                    // Set the document ID as the appointment ID
                    appointment.id = document.id
                    // Add the appointment to the list
                    allAppointments.add(appointment)
                }

                // Update cached data
                cachedAllAppointments = allAppointments

                // Update the adapter with new data
                AppointmentsAdapter.submitList(allAppointments)
                updateEmptyStateViews(allAppointments.isEmpty())

                progressBarAppointments.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                progressBarAppointments.visibility = View.GONE
                Toast.makeText(this, "Error loading appointments: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to update empty state views
    private fun updateEmptyStateViews(appointmentsEmpty: Boolean) {
        if (appointmentsEmpty) {
            tvAppointmentsEmpty.visibility = View.VISIBLE
            rvAppointments.visibility = View.GONE
        } else {
            tvAppointmentsEmpty.visibility = View.GONE
            rvAppointments.visibility = View.VISIBLE
        }
    }

    // Function to load time slots
    private fun loadTimeSlots(selectedDate: String) {
        progressBarTimeSlots.visibility = View.VISIBLE
        // Check if the time slots for the selected date are already cached
        if (cachedTimeSlots.containsKey(selectedDate)) {
            // Use cached data
            val slots = cachedTimeSlots[selectedDate]!!
            // Update the adapter with cached data
            timeSlots.clear()
            timeSlots.addAll(slots)
            timeSlotAdapter.notifyDataSetChanged()
            // Update empty state view
            updateEmptyStateViewForTimeSlots(slots.isEmpty())
            return
        }

        // Fetch time slots from Firestore
        db.collection("time_slots")
            .whereEqualTo("date", selectedDate)
            .get()
            .addOnSuccessListener { result ->
                val slots = mutableListOf<Pair<String, String>>()
                // Loop through the result and add each time slot to the list
                for (document in result) {
                    // Get the date and time slot from the document
                    val slot = document.getString("time_slot") ?: continue
                    slots.add(Pair(selectedDate, slot))
                }

                // Update cached data
                cachedTimeSlots[selectedDate] = slots

                // Update the adapter with new slots
                timeSlots.clear()
                timeSlots.addAll(slots)
                timeSlotAdapter.notifyDataSetChanged()

                // Update empty state view
                updateEmptyStateViewForTimeSlots(slots.isEmpty())

                // Hide progress bar
                progressBarTimeSlots.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                progressBarTimeSlots.visibility = View.GONE
                Toast.makeText(this, "Failed to load time slots: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to refresh data
    private fun refreshData() {
        // Clear cached data
        cachedAllAppointments = null
        // Clear cached time slots
        cachedTimeSlots.clear()
        // Reload appointments and time slots
        loadAppointments()
        // Load time slots for the selected date
        loadTimeSlots(selectedDate)
    }

    // Function to update empty state view for time slots
    private fun updateEmptyStateViewForTimeSlots(slotsEmpty: Boolean) {
        if (slotsEmpty) {
            tvTimeSlotsEmpty.visibility = View.VISIBLE
            rvTimeSlots.visibility = View.GONE
        } else {
            tvTimeSlotsEmpty.visibility = View.GONE
            rvTimeSlots.visibility = View.VISIBLE
        }
    }

    // Function to remove selected slot
    private fun removeSelectedSlot(position: Int) {
        // Check if a slot is selected
        if (position != RecyclerView.NO_POSITION) {
            // Get the selected slot
            val selectedSlot = timeSlots[position]

            // Delete slot from Firestore
            db.collection("time_slots").whereEqualTo("date", selectedSlot.first)
                .whereEqualTo("time_slot", selectedSlot.second)
                .get()
                .addOnSuccessListener { result ->
                    // Check if the slot exists
                    if (!result.isEmpty) {
                        // Get the document ID of the slot
                        val documentId = result.documents[0].id
                        // Delete the slot document
                        db.collection("time_slots").document(documentId)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Slot removed successfully.", Toast.LENGTH_SHORT).show()
                                timeSlots.removeAt(position) // Remove slot from local list
                                timeSlotAdapter.notifyItemRemoved(position) // Notify adapter of item removal
                                updateEmptyStateViewForTimeSlots(timeSlots.isEmpty()) // Update empty state view
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

    // Function to add selected time slots
    private fun addSelectedTimeSlots(selectedDate: String) {
        // Check if a date is selected
        val selectedSlots = mutableListOf<String>()

        // Check if any slot is selected
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

        // Loop through selected slots and add them to Firestore
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
                        // Create a new document reference
                        val newDocRef = db.collection("time_slots").document()
                        // Add the slot data to the batch
                        batch.set(newDocRef, slotData)
                        addedSlots.add(slot) // Track added slots
                    } else {
                        // Slot already exists, notify user or handle accordingly
                        Toast.makeText(this, "Time slot $slot already exists for $selectedDate.", Toast.LENGTH_SHORT).show()
                    }

                    // Commit batch write after processing all slots
                    if (addedSlots.size == selectedSlots.size) {
                        // Execute the batch write
                        batch.commit()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Time slots added successfully.", Toast.LENGTH_SHORT).show()
                                // Update local list and RecyclerView adapter if needed
                                for (slot in addedSlots) {
                                    if (!timeSlots.any { it.second == slot }) {
                                        // Add the new slot to the local list
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

    // Function to remove past appointments
    private fun removePastAppointments() {
        // Get the current date
        val todayDate = dateFormat.format(Date())

        db.collection("appointments")
            .whereLessThan("schedule", todayDate)
            .get()
            .addOnSuccessListener { result ->
                // Use batch operation to delete multiple documents
                val batch = db.batch()
                for (document in result) {
                    batch.delete(document.reference)
                }
                // Commit the batch operation
                batch.commit()
                    .addOnSuccessListener {
                        // Refresh data after deletion
                        refreshData()
                        Log.d("ManageAppointments", "Past appointments removed successfully.")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("ManageAppointments", "Failed to remove past appointments: ${exception.message}")
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("ManageAppointments", "Failed to fetch past appointments: ${exception.message}")
            }
    }

    // Function to handle appointment actions
    private fun handleAppointmentAction(appointment: ManageAppointment, action: String) {
        // Email subject and content based on action
        val subject = "Appointment ${if (action == "approve") "Approved" else "Declined"}"
        val content = when (action) {
            "approve" -> """
            <html>
                <body>
                    <p>Dear ${appointment.name},</p>
                    <p>We are pleased to inform you that your appointment request has been approved. Please find the details of your appointment below:</p>
                    <p>Appointment Date and Time: ${appointment.schedule}</p>
                    <p>Location: Saurabh Building, Kshara Sutra Hospital, Domnic Colony Rd Number 1, near N.L. High School, Malad, Daruwala Compound, Malad West, Mumbai, Maharashtra 400064.</p>
                    <p>If you have any questions or need further assistance, please do not hesitate to contact us.</p>
                    <p>Thank you for choosing us.</p>
                    <p>Best regards,<br>Dr. Kothia's Clinic</p>
                </body>
            </html>
        """
            "decline" -> """
            <html>
                <body>
                    <p>Dear ${appointment.name},</p>
                    <p>Thank you for choosing Dr. Kothia's Clinic. Unfortunately, we were unable to accommodate your appointment request at this time.</p>
                    <p>If you have any questions or need further assistance, please feel free to contact us.</p>
                    <p>We appreciate your understanding and look forward to serving you in the future.</p>
                    <p>Warm regards,<br>Dr. Kothia's Clinic</p>
                </body>
            </html>
        """.trimIndent().also {
                // Remove the declined appointment from the list and Firestore
                db.collection("appointments").document(appointment.id)
                    .delete()
                    .addOnSuccessListener {
                        // Find the appointment in the local list and remove it
                        val localAppointmentIndex = cachedAllAppointments?.indexOfFirst { it.id == appointment.id }
                        // Remove the appointment from the local list
                        if (localAppointmentIndex != null && localAppointmentIndex != -1) {
                            cachedAllAppointments?.removeAt(localAppointmentIndex)

                            // Notify the adapter of the change
                            AppointmentsAdapter.notifyItemRemoved(localAppointmentIndex)
                        }
                        Toast.makeText(this, "Appointment declined successfully.", Toast.LENGTH_SHORT).show()
                        loadAppointments() // Reload appointments after deletion
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Failed to decline appointment: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            else -> "We are sorry, your appointment has not been approved. Please contact us for further details."
        }

        // Update appointment status to approved in Firestore
        if (action == "approve") {
            db.collection("appointments").document(appointment.id)
                .update("status", "Approved")
                .addOnSuccessListener {
                    // Find the appointment in the local list and update its status
                    val localAppointment = cachedAllAppointments?.find { it.id == appointment.id }
                    localAppointment?.status = "Approved"

                    // Notify the adapter of the change
                    AppointmentsAdapter.notifyDataSetChanged()

                    Toast.makeText(this, "Appointment approved successfully.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to approve appointment: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Send email to the patient based on action (approve or decline)
        SendinblueHelper.sendEmail(appointment.email, subject, content) { success, message ->
            runOnUiThread {
                if (success) {
                    Toast.makeText(this, "Email sent successfully.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Failed to send email: $message", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}