package com.siamoonga.attendance.model

data class HistoryEntry(
    val id: String = "",
    val courseCode: String = "",
    val courseName: String = "",
    val timestamp: com.google.firebase.Timestamp? = null
)