package com.campuspulse.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.DocumentSnapshot

class StudentProfileRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private fun text(profile: DocumentSnapshot, key: String) = profile.get(key)?.toString().orEmpty()
    private fun details(profile: DocumentSnapshot) = mapOf(
        "name" to text(profile, "name"), "studentNumber" to text(profile, "studentNumber"),
        "course" to text(profile, "course"), "year" to text(profile, "year")
    )
    fun observe(uid: String, onError: (String) -> Unit): () -> Unit {
        var active = true
        var profile: DocumentSnapshot? = null
        var memberships: List<String>? = null
        var lastValues: Map<String, String>? = null
        var lastIds: List<String>? = null
        fun sync() {
            val p = profile ?: return
            val ids = memberships ?: return
            if (!active || auth.currentUser?.uid != uid || !p.exists()) return
            val values = details(p)
            if (lastValues == values && lastIds == ids) return
            lastValues = values; lastIds = ids
            ids.chunked(8).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { id -> batch.set(db.collection("clubMembers").document(id).collection("profile").document("details"), values) }
                batch.commit().addOnFailureListener {
                    if (active && auth.currentUser?.uid == uid) {
                        lastValues = null; lastIds = null
                        onError("Member details could not sync (${it.message}). Publish the supplied Firestore rules and retry.")
                    }
                }
            }
        }
        val profileListener = db.collection("users").document(uid).addSnapshotListener { document, error ->
            if (active) {
                if (error != null) onError("Unable to load account details: ${error.message}")
                else { profile = document; sync() }
            }
        }
        val memberListener = db.collection("clubMembers").whereEqualTo("userId", uid).addSnapshotListener { snapshot, error ->
            if (active) {
                if (error != null) onError("Unable to load your memberships: ${error.message}")
                else {
                    memberships = snapshot?.documents?.filter { it.getString("status") in listOf("ACTIVE", "PENDING") }?.map { it.id }?.sorted() ?: emptyList()
                    sync()
                }
            }
        }
        return { active = false; profileListener.remove(); memberListener.remove() }
    }
    fun saveEditable(name: String, year: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val uid = auth.currentUser?.uid ?: run { onError("Sign in again before saving."); return }
        db.collection("users").document(uid).get().addOnSuccessListener { profile ->
            if (auth.currentUser?.uid != uid) { onError("Your account changed. Sign in again."); return@addOnSuccessListener }
            save(name, text(profile, "studentNumber"), text(profile, "course"), text(profile, "department"), year, onSuccess, onError)
        }.addOnFailureListener { onError("Unable to load your saved details: ${it.message}") }
    }
    fun save(name: String, number: String, course: String, department: String, year: String,
             onSuccess: () -> Unit, onError: (String) -> Unit) {
        val uid = auth.currentUser?.uid ?: run { onError("Sign in again before saving."); return }
        if (name.trim().length !in 2..80 || !number.trim().matches(Regex("[0-9]{6,20}"))
            || course.trim().length !in 2..150 || department.trim().length !in 2..150
            || !year.trim().matches(Regex("[1-8]"))) {
            onError("Enter all details, a numeric student number (6–20 digits), and year 1–8."); return
        }
        val values = mapOf("name" to name.trim(), "studentNumber" to number.trim(), "course" to course.trim(), "year" to year.trim())
        db.collection("clubMembers").whereEqualTo("userId", uid).get().addOnSuccessListener { snapshot ->
            if (auth.currentUser?.uid != uid) { onError("Your account changed. Sign in again."); return@addOnSuccessListener }
            val members = snapshot.documents.filter { it.getString("status") in listOf("ACTIVE", "PENDING") }
            val chunks = members.chunked(8).ifEmpty { listOf(emptyList()) }
            fun commitChunk(index: Int) {
                if (auth.currentUser?.uid != uid) { onError("Your account changed. Sign in again."); return }
                val batch = db.batch()
                if (index == 0) batch.update(db.collection("users").document(uid),
                    values + mapOf("department" to department.trim(), "updatedAt" to FieldValue.serverTimestamp()))
                chunks[index].forEach { batch.set(it.reference.collection("profile").document("details"), values) }
                batch.commit().addOnSuccessListener {
                    if (index + 1 < chunks.size) commitChunk(index + 1)
                    else if (auth.currentUser?.uid == uid) onSuccess()
                }.addOnFailureListener {
                    onError(if (index == 0) "Details were not saved: ${it.message}"
                    else "Your profile saved, but some club details could not sync. Save again to retry: ${it.message}")
                }
            }
            commitChunk(0)
        }.addOnFailureListener { onError("Unable to load memberships: ${it.message}") }
    }
}
