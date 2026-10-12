package com.campuspulse.app.data

import com.campuspulse.app.model.Category
import com.campuspulse.app.model.Club
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/** Assignments are provisioned by the platform operator, never by the app. */
class ClubAdminRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun login(email: String, password: String, clubCode: String,
              onSuccess: (Club) -> Unit, onError: (String) -> Unit) {
        if (email.isBlank() || password.isBlank() || clubCode.isBlank()) {
            onError("Enter your email, password and club code.")
            return
        }
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnFailureListener { onError("Unable to sign in. Check your details and connection.") }
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid == null) { onError("Unable to load your account."); return@addOnSuccessListener }
                fun denied(message: String) { auth.signOut(); onError(message) }
                db.collection("adminAccess").document(uid).get()
                    .addOnFailureListener { denied("Unable to verify your club assignment. Check your connection and access rules.") }
                    .addOnSuccessListener assignment@ { assignment ->
                        val clubId = assignment.getString("clubId").orEmpty()
                        val assignedCode = assignment.getString("clubCode").orEmpty()
                        if (!assignment.exists() || assignment.getBoolean("isActive") != true ||
                            clubId.isBlank() || assignedCode.isBlank() ||
                            !assignedCode.equals(clubCode.trim(), ignoreCase = true)) {
                            denied("No active assignment matches this club code.")
                            return@assignment
                        }
                        db.collection("clubs").document(clubId).get()
                            .addOnFailureListener { denied("Unable to load your assigned club.") }
                            .addOnSuccessListener clubResult@ { document ->
                                if (!document.exists() || document.getString("status") != "APPROVED" ||
                                    document.getBoolean("isActive") != true) {
                                    denied("Your assigned club is unavailable."); return@clubResult
                                }
                                if (auth.currentUser?.uid != uid) return@clubResult
                                val category = Category.values().firstOrNull {
                                    it.name == document.getString("category")
                                } ?: Category.ACADEMIC
                                onSuccess(Club(
                                    id = document.id,
                                    name = document.getString("name").orEmpty(),
                                    category = category,
                                    members = (document.getLong("members") ?: 0L).toInt(),
                                    description = document.getString("description").orEmpty(),
                                    location = document.getString("location") ?: "Mbombela Campus"
                                ))
                            }
                    }
            }
    }
}
