package com.example.ksharsutra

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class ManageAppointmentsActivity : AppCompatActivity() {

    private lateinit var rvTodayAppointments: RecyclerView
    private lateinit var rvFutureAppointments: RecyclerView

    private lateinit var todayAdapter: ManageAppointmentAdapter
    private lateinit var futureAdapter: ManageAppointmentAdapter

    private lateinit var timeSlotAdapter: TimeSlotAdapter
    private lateinit var timeSlots: MutableList<String>
    private val db = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_appointments)

        rvTodayAppointments = findViewById(R.id.rvTodayAppointments)
        rvFutureAppointments = findViewById(R.id.rvFutureAppointments)

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

        loadAppointments()
        loadTimeSlots()

        // Navigation to ManageReports Page
        val navigation_report = findViewById<ImageView>(R.id.navigation_reports)
        navigation_report.setOnClickListener{
            val intent = Intent(this, ManageReportsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadAppointments() {
        val todayDate = dateFormat.format(Date())

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

                todayAdapter.submitList(todayAppointments)
                futureAdapter.submitList(futureAppointments)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error loading appointments: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun handleAppointmentAction(appointment: ManageAppointment, action: String) {
        val message = when (action) {
            "approve" -> """
            Dear ${appointment.name},

            We are pleased to inform you that your appointment request has been approved. Please find the details of your appointment below:

            Appointment Date and Time: ${appointment.schedule}
            Location: Saurabh Building, Kshara Sutra Hospital, Domnic Colony Rd Number 1, near N.L. High School, Malad, Daruwala Compound, Malad West, Mumbai, Maharashtra 400064.

            If you have any questions or need further assistance, please do not hesitate to contact us.

            Thank you for choosing our clinic.

            Best regards,
            Dr. Kothia's Clinic
            022 2807 0167
        """.trimIndent()

            "decline" -> """
            Dear ${appointment.name},

            We regret to inform you that we are unable to accommodate your appointment request at the specified date and time due to unavailability of slots. Please consider selecting an alternative date and time for your appointment.

            We apologize for any inconvenience this may cause and appreciate your understanding.

            For further assistance or to reschedule, please contact us.

            Thank you for choosing our clinic.

            Best regards,
            Dr. Kothia's Clinic
            022 2807 0167
        """.trimIndent()

            else -> ""
        }

        db.collection("appointments")
            .document(appointment.id)
            .update("status", action)
            .addOnSuccessListener {
                try {
                    SendGridHelper.sendEmail(appointment.email, "Appointment Status", message)
                    Toast.makeText(this, "Email sent to ${appointment.email}", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error sending email: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                loadAppointments()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error updating appointment: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun loadTimeSlots() {
        db.collection("time_slots")
            .get()
            .addOnSuccessListener { result ->
                val slots = mutableListOf<String>()
                for (document in result) {
                    val slot = document.id // Assuming document ID is the slot key
                    slots.add(slot)
                }

                // Update the adapter with new slots and remove expired ones
                timeSlots.clear()
                timeSlots.addAll(slots)
                timeSlotAdapter.removeExpiredSlots()
                timeSlotAdapter.notifyDataSetChanged() // Notify adapter of data change
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to load time slots: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeSelectedSlot(position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            val selectedSlot = timeSlots[position]

            db.collection("time_slots").document(selectedSlot)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Slot removed successfully.", Toast.LENGTH_SHORT).show()
                    timeSlots.removeAt(position) // Remove slot from local list
                    timeSlotAdapter.notifyItemRemoved(position) // Notify adapter of item removal
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to remove slot: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Please select a slot to remove.", Toast.LENGTH_SHORT).show()
        }
    }

    fun addTimeSlot(view: View) {
        val selectedDate = findViewById<EditText>(R.id.etSelectedDate).text.toString().trim()
        val timeSlot = findViewById<EditText>(R.id.etTimeSlot).text.toString().trim()

        // Validate selected date to prevent adding slots for past dates
        if (isPastDate(selectedDate) && !isToday(selectedDate)) {
            Toast.makeText(this, "Cannot add slot for past dates.", Toast.LENGTH_SHORT).show()
            return
        }

        // Add the new slot to Firestore
        val slotKey = "$selectedDate $timeSlot"

        db.collection("time_slots").document(slotKey)
            .set(mapOf("time_slot" to timeSlot))
            .addOnSuccessListener {
                Toast.makeText(this, "Time slot added successfully.", Toast.LENGTH_SHORT).show()
                // Update local list and RecyclerView adapter
                timeSlots.add(slotKey)
                timeSlotAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to add time slot: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun isPastDate(selectedDate: String): Boolean {
        val currentDate = Date()
        val dateSelected = dateFormat.parse(selectedDate) ?: return true
        return dateSelected.before(currentDate)
    }

    private fun isToday(selectedDate: String): Boolean {
        val dateSelected = dateFormat.parse(selectedDate) ?: return false
        val calSelected = Calendar.getInstance().apply { time = dateSelected }
        val calCurrent = Calendar.getInstance()
        return calSelected.get(Calendar.YEAR) == calCurrent.get(Calendar.YEAR) &&
                calSelected.get(Calendar.MONTH) == calCurrent.get(Calendar.MONTH) &&
                calSelected.get(Calendar.DAY_OF_MONTH) == calCurrent.get(Calendar.DAY_OF_MONTH)
    }

    private fun sanitizeDate(selectedDate: String): String {
        // Format selectedDate to match Firebase database key requirements
        return selectedDate.replace("-", "")
    }
}
