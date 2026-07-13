package com.siamoonga.attendance.model

data class Session(
    val id: String = "",
    val courseId: String = "",
    val courseCode: String = "",
    val courseName: String = "",
    val lecturerId: String = "",
    val secret: String = "",
    val isOpen: Boolean = true
)

data class AttendanceRecord(
    val studentUid: String = "",
    val studentName: String = "",
    val timestamp: com.google.firebase.Timestamp? = null
)