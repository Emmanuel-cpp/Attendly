package com.siamoonga.attendance.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.siamoonga.attendance.model.HistoryEntry
import com.siamoonga.attendance.util.QrCodeUtil
import kotlinx.coroutines.tasks.await

class AttendanceRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun markAttendance(payload: String): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not signed in"))

        val parts = payload.split("|")
        if (parts.size != 2) return Result.failure(Exception("This is not an Attendly session code"))
        val (sessionId, scannedToken) = parts

        return try {
            val sessionDoc = db.collection("sessions").document(sessionId).get().await()
            if (!sessionDoc.exists()) {
                return Result.failure(Exception("Session not found"))
            }
            if (sessionDoc.getBoolean("isOpen") != true) {
                return Result.failure(Exception("This session has ended"))
            }

            val secret = sessionDoc.getString("secret")
                ?: return Result.failure(Exception("Invalid session"))

            val slot = QrCodeUtil.currentSlot()
            val validNow = QrCodeUtil.tokenFor(secret, slot)
            val validPrev = QrCodeUtil.tokenFor(secret, slot - 1)
            if (scannedToken != validNow && scannedToken != validPrev) {
                return Result.failure(Exception("Code expired — scan the live QR in class"))
            }

            val attendanceRef = db.collection("sessions").document(sessionId)
                .collection("attendance").document(uid)

            val existing = attendanceRef.get().await()
            if (existing.exists()) {
                return Result.failure(Exception("You're already checked in for this session"))
            }

            val userDoc = db.collection("users").document(uid).get().await()
            val studentName = userDoc.getString("name") ?: "Student"

            val courseCode = sessionDoc.getString("courseCode") ?: ""
            val courseName = sessionDoc.getString("courseName") ?: ""

            attendanceRef.set(
                mapOf(
                    "studentUid" to uid,
                    "studentName" to studentName,
                    "courseCode" to courseCode,
                    "courseName" to courseName,
                    "timestamp" to com.google.firebase.Timestamp.now()
                )
            ).await()

            Result.success(courseCode)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyHistory(): Result<List<HistoryEntry>> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not signed in"))
        return try {
            val snapshot = db.collectionGroup("attendance")
                .whereEqualTo("studentUid", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val entries = snapshot.documents.map { doc ->
                HistoryEntry(
                    id = doc.reference.path,
                    courseCode = doc.getString("courseCode") ?: "",
                    courseName = doc.getString("courseName") ?: "",
                    timestamp = doc.getTimestamp("timestamp")
                )
            }
            Result.success(entries)
        } catch (e: Exception) {
            Result.failure(Exception("HISTORY ERROR: ${e.message}"))
        }
    }
}