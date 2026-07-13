package com.siamoonga.attendance.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.siamoonga.attendance.model.Course
import kotlinx.coroutines.tasks.await

class CourseRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val currentUid: String?
        get() = auth.currentUser?.uid

    suspend fun createCourse(name: String, code: String): Result<Unit> {
        val uid = currentUid ?: return Result.failure(Exception("Not signed in"))
        return try {
            val data = mapOf(
                "name" to name.trim(),
                "code" to code.trim().uppercase(),
                "lecturerId" to uid,
                "studentCount" to 0,
                "createdAt" to com.google.firebase.Timestamp.now()
            )
            db.collection("courses").add(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyCourses(): Result<List<Course>> {
        val uid = currentUid ?: return Result.failure(Exception("Not signed in"))
        return try {
            val snapshot = db.collection("courses")
                .whereEqualTo("lecturerId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val courses = snapshot.documents.map { doc ->
                Course(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    code = doc.getString("code") ?: "",
                    lecturerId = doc.getString("lecturerId") ?: "",
                    studentCount = (doc.getLong("studentCount") ?: 0L).toInt()
                )
            }
            Result.success(courses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}