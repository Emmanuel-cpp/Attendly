package com.siamoonga.attendance.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.siamoonga.attendance.model.AttendanceRecord
import com.siamoonga.attendance.model.Course
import com.siamoonga.attendance.model.Session
import com.siamoonga.attendance.util.QrCodeUtil
import kotlinx.coroutines.tasks.await

class SessionRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun startSession(course: Course): Result<Session> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not signed in"))
        return try {
            val secret = QrCodeUtil.generateSecret()
            val data = mapOf(
                "courseId" to course.id,
                "courseCode" to course.code,
                "courseName" to course.name,
                "lecturerId" to uid,
                "secret" to secret,
                "isOpen" to true,
                "createdAt" to com.google.firebase.Timestamp.now()
            )
            val ref = db.collection("sessions").add(data).await()
            Result.success(
                Session(
                    id = ref.id,
                    courseId = course.id,
                    courseCode = course.code,
                    courseName = course.name,
                    lecturerId = uid,
                    secret = secret,
                    isOpen = true
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun endSession(sessionId: String): Result<Unit> {
        return try {
            db.collection("sessions").document(sessionId)
                .update("isOpen", false)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun listenToAttendance(
        sessionId: String,
        onChange: (List<AttendanceRecord>) -> Unit
    ): ListenerRegistration {
        return db.collection("sessions").document(sessionId)
            .collection("attendance")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val records = snapshot?.documents?.map { doc ->
                    AttendanceRecord(
                        studentUid = doc.id,
                        studentName = doc.getString("studentName") ?: "Student",
                        timestamp = doc.getTimestamp("timestamp")
                    )
                } ?: emptyList()
                onChange(records)
            }
    }
}