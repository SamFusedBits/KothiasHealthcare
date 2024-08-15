package com.kothias.clinic

data class Report(
    // Define the data model for the Report object
    val fileName: String,
    val patientName: String?,
    val patientEmail: String?,
    val fileUrl: String
)