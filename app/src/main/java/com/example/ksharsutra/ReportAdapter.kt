package com.example.ksharsutra

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReportAdapter(private val reports: List<Report>, private val onReportClick: (String) -> Unit) :
    RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileNameTextView: TextView = itemView.findViewById(R.id.file_name_text_view)
        val usernameTextView: TextView = itemView.findViewById(R.id.username_text_view)
        val emailTextView: TextView = itemView.findViewById(R.id.email_text_view)
        val viewButton: Button = itemView.findViewById(R.id.view_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_manage_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]
        holder.fileNameTextView.text = "File Name: ${report.fileName}"
        holder.usernameTextView.text = "Patient Name: ${report.patientName ?: "Unknown"}"
        holder.emailTextView.text = "Patient Email: ${report.patientEmail ?: "Unknown"}"
        holder.viewButton.setOnClickListener { onReportClick(report.fileUrl) }
    }

    override fun getItemCount(): Int = reports.size
}