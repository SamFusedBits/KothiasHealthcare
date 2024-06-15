package com.example.ksharsutra

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ManageAppointmentAdapter(
    private val onActionClick: (ManageAppointment, String) -> Unit
) : RecyclerView.Adapter<ManageAppointmentAdapter.AppointmentViewHolder>() {

    private var appointments = mutableListOf<ManageAppointment>()

    fun submitList(newAppointments: List<ManageAppointment>) {
        appointments.clear()
        appointments.addAll(newAppointments)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.manage_appointment_item, parent, false)
        return AppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        holder.bind(appointments[position])
    }

    override fun getItemCount(): Int = appointments.size

    inner class AppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvContact: TextView = itemView.findViewById(R.id.tvContact)
        private val tvSchedule: TextView = itemView.findViewById(R.id.tvSchedule)
        private val btnApprove: Button = itemView.findViewById(R.id.btnApprove)
        private val btnDecline: Button = itemView.findViewById(R.id.btnDecline)

        fun bind(appointment: ManageAppointment) {
            tvName.text = appointment.name
            tvContact.text = if (appointment.email.isNotEmpty()) appointment.email else appointment.phone
            tvSchedule.text = appointment.schedule

            btnApprove.setOnClickListener { onActionClick(appointment, "approve") }
            btnDecline.setOnClickListener { onActionClick(appointment, "decline") }
        }
    }
}
