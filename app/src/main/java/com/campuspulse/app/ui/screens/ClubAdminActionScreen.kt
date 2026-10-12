package com.campuspulse.app.ui.screens

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** Direct Firestore actions. Authorization is also enforced by firestore.rules. */
class ClubAdminActions {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private fun stamp() = FieldValue.serverTimestamp()
    private fun refId(value: Any?): String {
        val id = value as? String ?: ""
        require(id.isNotBlank() && !id.contains('/') && id != "." && id != "..") {
            "Invalid document ID. Close this panel and try again."
        }
        return id
    }
    private fun text(values: Map<String, Any>, key: String, max: Int, required: Boolean = false): String {
        val value = (values[key] as? String).orEmpty().trim()
        require(value.length <= max && (!required || value.isNotBlank())) {
            "$key must ${if (required) "contain text and " else ""}be at most $max characters."
        }
        return value
    }
    private fun finish(task: Task<*>, success: (Map<*, *>) -> Unit, failure: (String) -> Unit) {
        task.addOnSuccessListener { success(mapOf("ok" to true)) }
            .addOnFailureListener { failure(it.message ?: "Action failed. Check your connection.") }
    }
    fun call(
        clubId: String,
        action: String,
        values: Map<String, Any> = emptyMap(),
        success: (Map<*, *>) -> Unit,
        failure: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) { failure("Sign in again."); return }
        try { refId(clubId) } catch (e: Exception) { failure(e.message.orEmpty()); return }
        // Reading the caller's own assignment is allowed even after revocation.
        db.collection("adminAccess").document(uid).get()
            .addOnSuccessListener { access ->
                if (access.getBoolean("isActive") != true || access.getString("clubId") != clubId) {
                    failure("Your admin assignment is inactive or belongs to another club.")
                    return@addOnSuccessListener
                }
                try {
                    when (action) {
                        "postPublish" -> publish(clubId, uid, values, success, failure)
                        "eventSave" -> eventSave(clubId, uid, values, success, failure)
                        "eventDelete" -> finish(db.collection("events").document(refId(values["id"])).delete(), success, failure)
                        "clubSave" -> {
                            val categories = mapOf("Sport" to "SPORT", "Culture & Arts" to "CULTURE",
                                "Advocacy & Politics" to "ADVOCACY", "Identity & Community" to "IDENTITY",
                                "Faith & Academic" to "ACADEMIC", "Academic" to "ACADEMIC")
                            val input = text(values, "category", 50, true)
                            val category = categories[input] ?: input
                            require(category in listOf("SPORT","CULTURE","ADVOCACY","IDENTITY","ACADEMIC")) { "Choose a valid club category." }
                            require(values["bannerUrl"] == null && values["profileImageUrl"] == null) { "Image changes require Storage setup." }
                            finish(db.collection("clubs").document(clubId).update(mapOf(
                                "name" to text(values,"name",150,true),
                                "description" to text(values,"description",5000),
                                "history" to text(values,"history",10000),
                                "category" to category, "updatedAt" to stamp()
                            )),success,failure)
                        }
                        "notificationPreference" -> {
                            val enabled = values["enabled"] as? Boolean ?: error("Invalid notification setting.")
                            finish(db.collection("adminAccess").document(uid).update("notificationsEnabled",enabled),success,failure)
                        }
                        "members" -> members(clubId,success,failure)
                        "member" -> memberChange(clubId, values, success, failure)
                        "admins" -> db.collection("adminAccess").whereEqualTo("clubId",clubId)
                            .whereEqualTo("isActive",true).get()
                            .addOnSuccessListener { snapshot -> success(mapOf("items" to snapshot.documents.map {
                                mapOf("uid" to it.id,"name" to "Admin (${it.id.take(8)})",
                                    "owner" to (it.getBoolean("canManageAdmins") == true),"self" to (it.id == uid))
                            })) }
                            .addOnFailureListener { failure(it.message.orEmpty()) }
                        "adminGrant", "adminRevoke" -> {
                            require(access.getBoolean("canManageAdmins") == true) { "Only the designated club owner can manage admins." }
                            adminChange(clubId,uid,access,action,values,success,failure)
                        }
                        "notifications" -> notices(clubId,uid,success,failure)
                        "readNotification" -> finish(db.collection("adminAccess").document(uid)
                            .collection("notificationReads").document(refId(values["id"]))
                            .set(mapOf("readAt" to stamp())),success,failure)
                        else -> failure("Unknown action: $action")
                    }
                } catch (e: Exception) { failure(e.message ?: "Invalid input.") }
            }.addOnFailureListener { failure(it.message ?: "Unable to check admin access.") }
    }
    private fun publish(c: String, uid: String, v: Map<String,Any>, ok: (Map<*,*>) -> Unit, fail: (String) -> Unit) {
        require(v["type"] == "TEXT") { "Photo and video publishing requires Storage setup." }
        val body = text(v,"text",5000,true)
        val id = refId(v["id"])
        db.collection("clubs").document(c).get().addOnSuccessListener { club ->
            finish(db.collection("posts").document(id).set(mapOf(
                "clubId" to c,"clubName" to club.getString("name").orEmpty(),
                "clubShortName" to (club.getString("shortName") ?: club.getString("name").orEmpty()),
                "authorId" to uid,"type" to "TEXT","text" to body,"imageUrl" to "",
                "status" to "PUBLISHED","isActive" to true,"createdAt" to stamp(),"updatedAt" to stamp()
            )),ok,fail)
        }.addOnFailureListener { fail(it.message.orEmpty()) }
    }
    private fun eventSave(c: String, uid: String, v: Map<String,Any>, ok: (Map<*,*>) -> Unit, fail: (String) -> Unit) {
        val id = refId(v["id"])
        val date = text(v,"date",10,true)
        val time = text(v,"time",5,true)
        require(date.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) && time.matches(Regex("\\d{2}:\\d{2}"))) { "Use YYYY-MM-DD and HH:mm." }
        val parser = SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.US).apply {
            isLenient = false; timeZone = TimeZone.getTimeZone("Africa/Johannesburg")
        }
        val whenDate = parser.parse("$date $time") ?: error("Invalid event date.")
        val endDate = text(v,"endDate",10,true)
        val endTime = text(v,"endTime",5,true)
        require(endDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) && endTime.matches(Regex("\\d{2}:\\d{2}"))) { "Use YYYY-MM-DD and HH:mm for the event end." }
        val endWhen = parser.parse("$endDate $endTime") ?: error("Invalid event end date.")
        require(endWhen.after(whenDate)) { "Event end must be after its start. For overnight events, use the next date." }
        val link = text(v,"link",2000)
        require(link.isBlank() || link.startsWith("https://") || link.startsWith("http://")) { "RSVP links must start with https:// or http://." }
        val changes = mapOf<String,Any>(
            "title" to text(v,"title",150,true),"description" to text(v,"description",5000),
            "category" to text(v,"category",100,true),"location" to text(v,"venue",300,true),
            "date" to date,"time" to time,"eventDate" to Timestamp(whenDate),
            "endDate" to endDate,"endTime" to endTime,"eventEndDate" to Timestamp(endWhen),
            "rsvpEnabled" to (v["rsvpEnabled"] == true),"rsvpUrl" to link,"updatedAt" to stamp()
        )
        val event = db.collection("events").document(id)
        // Rules permit reading absent event IDs only for assigned admins.
        db.runTransaction { t ->
            val club = t.get(db.collection("clubs").document(c))
            val old = t.get(event)
            if (old.exists()) {
                require(old.getString("clubId") == c) { "This event belongs to another club." }
                t.update(event,changes)
            } else {
                require((v["mediaUrl"] as? String).isNullOrBlank()) { "New event media requires Storage setup." }
                t.set(event,changes + mapOf(
                    "clubId" to c,"clubName" to club.getString("name").orEmpty(),
                    "clubShortName" to (club.getString("shortName") ?: club.getString("name").orEmpty()),
                    "authorId" to uid,"status" to "PUBLISHED","isActive" to true,
                    "mediaType" to (if (v["mediaType"] == "VIDEO") "VIDEO" else "PHOTO"),
                    "mediaUrl" to "","imageUrl" to "","videoUrl" to "","createdAt" to stamp()
                ))
            }
            id
        }.addOnSuccessListener { ok(mapOf("id" to it)) }.addOnFailureListener { fail(it.message.orEmpty()) }
    }
    private fun members(c: String, ok: (Map<*,*>) -> Unit, fail: (String) -> Unit) {
        db.collection("clubMembers").whereEqualTo("clubId",c).get().addOnSuccessListener { snapshot ->
            ok(mapOf("items" to snapshot.documents.filter { it.getString("status") in listOf("ACTIVE","PENDING","SUSPENDED") }.map {
                mapOf("id" to it.id,"userId" to it.getString("userId").orEmpty(),
                    "name" to (it.getString("name") ?: "Member (${it.getString("userId").orEmpty().take(8)})"),
                    "email" to "","studentNumber" to "Not available","course" to "Not available","year" to "Not available",
                    "role" to "Member","status" to (it.getString("status") ?: "ACTIVE"),
                    "joinedAt" to (it.getTimestamp("joinedAt")?.toDate()?.time ?: 0L))
            }))
        }.addOnFailureListener { fail(it.message.orEmpty()) }
    }
    private fun memberChange(c: String, v: Map<String,Any>, ok: (Map<*,*>) -> Unit, fail: (String) -> Unit) {
        val reference = db.collection("clubMembers").document(refId(v["id"]))
        val operation = v["operation"] as? String ?: ""
        db.runTransaction { t ->
            val member = t.get(reference)
            require(member.exists() && member.getString("clubId") == c) { "Member not found in this club." }
            val uid = refId(member.getString("userId"))
            val access = t.get(db.collection("adminAccess").document(uid))
            require(access.getBoolean("isActive") != true) { "Revoke this member's admin access before changing their status." }
            val restriction = db.collection("clubRestrictions").document(c).collection("users").document(uid)
            val previousRestriction = t.get(restriction)
            val current = member.getString("status")
            val next = when {
                operation == "approve" && current == "PENDING" -> "ACTIVE"
                operation == "reject" && current == "PENDING" -> "REJECTED"
                operation == "remove" && current in listOf("ACTIVE","SUSPENDED","PENDING") -> "REMOVED"
                else -> error("Member status changed. Refresh and try again.")
            }
            if (next == "ACTIVE" && previousRestriction.exists()) {
                require(previousRestriction.getString("membershipId") == reference.id) { "This restriction belongs to another membership. Ask the platform administrator to review it." }
            }
            t.update(reference,mapOf("status" to next,"updatedAt" to stamp()))
            if (next == "ACTIVE") {
                if (previousRestriction.exists()) t.delete(restriction)
            } else {
                t.set(restriction,mapOf("status" to next,"membershipId" to reference.id,"updatedAt" to stamp()))
            }
            true
        }.addOnSuccessListener { ok(mapOf("ok" to true)) }.addOnFailureListener { fail(it.message.orEmpty()) }
    }
    private fun adminChange(c: String, uid: String, owner: DocumentSnapshot, action: String, v: Map<String,Any>, ok: (Map<*,*>) -> Unit, fail: (String) -> Unit) {
        val target = refId(v["userId"])
        require(target != uid) { "You cannot change your own admin assignment." }
        val reference = db.collection("adminAccess").document(target)
        if (action == "adminRevoke") {
            finish(reference.update(mapOf("isActive" to false,"updatedAt" to stamp())),ok,fail)
            return
        }
        db.collection("clubMembers").whereEqualTo("clubId",c).whereEqualTo("userId",target)
            .whereEqualTo("status","ACTIVE").get().addOnSuccessListener { result ->
                val membership = result.documents.firstOrNull()
                if (membership == null) { fail("Only active club members can become admins."); return@addOnSuccessListener }
                finish(reference.set(mapOf(
                    "clubId" to c,"clubCode" to owner.getString("clubCode").orEmpty(),
                    "isActive" to true,"canManageAdmins" to false,"notificationsEnabled" to true,
                    "membershipId" to membership.id,"updatedAt" to stamp()
                )),ok,fail)
            }.addOnFailureListener { fail(it.message.orEmpty()) }
    }
    private fun notices(c: String, uid: String, ok: (Map<*,*>) -> Unit, fail: (String) -> Unit) {
        db.collection("adminNotifications").whereEqualTo("clubId",c).limit(200).get()
            .addOnSuccessListener { notices ->
                db.collection("adminAccess").document(uid).collection("notificationReads").get()
                    .addOnSuccessListener { reads ->
                        val seen = reads.documents.map { it.id }.toSet()
                        ok(mapOf("items" to notices.documents.map {
                            mapOf("id" to it.id,"title" to (it.getString("title") ?: "Announcement"),
                                "message" to it.getString("message").orEmpty(),"read" to (it.id in seen),
                                "createdAt" to (it.getTimestamp("createdAt")?.toDate()?.time ?: 0L))
                        }.sortedByDescending { (it["createdAt"] as? Long) ?: 0L }))
                    }.addOnFailureListener { fail(it.message.orEmpty()) }
            }.addOnFailureListener { fail(it.message.orEmpty()) }
    }
    @Suppress("UNUSED_PARAMETER")
    fun upload(context: Context, clubId: String, uri: String?, kind: String, success: (String) -> Unit, failure: (String) -> Unit) {
        if (uri.isNullOrBlank()) success("")
        else failure("Uploading photos or videos requires an enabled Storage service. Save without a new image or video.")
    }
}
