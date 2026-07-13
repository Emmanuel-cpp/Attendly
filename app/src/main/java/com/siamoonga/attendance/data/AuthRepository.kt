package com.siamoonga.attendance.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.siamoonga.attendance.model.Role
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun signIn(email: String, password: String): Result<Role> {
        return try {
            Log.d(TAG, "1. Starting auth…")
            val authResult = withTimeout(15_000) {
                auth.signInWithEmailAndPassword(email.trim(), password).await()
            }
            val uid = authResult.user?.uid
                ?: return Result.failure(Exception("Could not read user id"))
            Log.d(TAG, "2. Auth OK, uid=$uid — fetching role…")

            val snapshot = withTimeout(15_000) {
                db.collection("users").document(uid).get().await()
            }
            Log.d(TAG, "3. Firestore OK, exists=${snapshot.exists()}")

            val roleString = snapshot.getString("role")
                ?: return Result.failure(Exception("No role set for this account"))

            Result.success(Role.valueOf(roleString.uppercase()))
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "TIMEOUT — network never answered")
            Result.failure(Exception("Network timeout. Check the phone's internet."))
        } catch (e: Exception) {
            Log.e(TAG, "FAILED: ${e.javaClass.simpleName}: ${e.message}")
            Result.failure(e)
        }
    }

    private companion object { const val TAG = "AttendlyAuth" }
}