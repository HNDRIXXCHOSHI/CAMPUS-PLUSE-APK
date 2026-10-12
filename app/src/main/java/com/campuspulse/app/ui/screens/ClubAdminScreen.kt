package com.campuspulse.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.campuspulse.app.model.Club
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import kotlinx.coroutines.delay

private data class AdminEvent(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val date: String,
    val time: String,
    val venue: String,
    val endDate: String = "",
    val endTime: String = "",
    val rsvpEnabled: Boolean = true,
    val link: String = "",
    val mediaUri: String? = null,
    val mediaType: String = "PHOTO",
    val mediaUrl: String = "",
    val status: String = "Upcoming"
)

private data class AdminMember(
    val id: String,
    val userId: String,
    val name: String,
    val email: String,
    val studentNumber: String,
    val course: String,
    val year: String,
    val joined: String,
    val role: String = "Member",
    val status: String = "Active"
)

private enum class AdminPostType {
    TEXT, PHOTO, VIDEO
}

private data class AdminPost(
    val id: String,
    val author: String,
    val caption: String,
    val type: AdminPostType,
    val time: String,
    val mediaUrl: String = ""
)

private val Bg = Color(0xFF00254A)
private val CardBg = Color(0xFF062F59)
private val CardSoft = Color(0xFF0B3B68)
private val Border = Color(0xFF315A7D)
private val Orange = Color(0xFFFF7A00)
private val Green = Color(0xFF27D79C)
private val Danger = Color(0xFFFF5252)
private val PrimaryText = Color.White
private val SecondaryText = Color(0xFFB8C7D9)

private fun formatAdminDate(date: Date, pattern: String): String {
    return SimpleDateFormat(pattern, Locale.US).apply {
        timeZone = TimeZone.getTimeZone("Africa/Johannesburg")
    }.format(date)
}

private fun adminEventStatus(event: AdminEvent, now: Long = System.currentTimeMillis()): String {
    fun parse(date: String, time: String): Long? = try {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone("Africa/Johannesburg")
        }.parse("$date $time")?.time
    } catch (_: Exception) { null }
    val start = parse(event.date, event.time) ?: return "Schedule not set"
    val end = parse(event.endDate, event.endTime)
    return when {
        now < start -> "Upcoming"
        end == null || end <= start -> "End time not set"
        now < end -> "Ongoing"
        else -> "Passed"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubAdminScreen(
    club: Club,
    onLogout: () -> Unit,
    onAddEvent: () -> Unit = {}
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val actions = remember { ClubAdminActions() }
    val userId = auth.currentUser?.uid

    var showLeadership by remember(club.id) { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var initialMemberTab by remember { mutableIntStateOf(0) }
    var initialEventFilter by remember { mutableStateOf("All") }

    var showEventEditor by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<AdminEvent?>(null) }
    var eventDraftId by remember { mutableStateOf(UUID.randomUUID().toString()) }

    var showPostEditor by remember { mutableStateOf(false) }
    var publishId by remember { mutableStateOf(UUID.randomUUID().toString()) }

    var selectedMember by remember { mutableStateOf<AdminMember?>(null) }
    var showClubEditor by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var deletingPost by remember { mutableStateOf<AdminPost?>(null) }

    var clubName by remember(club.id) { mutableStateOf(club.name) }
    var clubDescription by remember(club.id) { mutableStateOf(club.description) }
    var clubHistory by remember(club.id) { mutableStateOf("") }
    var clubCategory by remember(club.id) { mutableStateOf(club.category.label) }
    var clubBannerUrl by remember(club.id) { mutableStateOf("") }
    var clubProfileUrl by remember(club.id) { mutableStateOf("") }
    var notificationsEnabled by remember(club.id) { mutableStateOf(true) }

    var events by remember(club.id) {
        mutableStateOf(emptyList<AdminEvent>())
    }
    var members by remember(club.id) {
        mutableStateOf(emptyList<AdminMember>())
    }
    var pendingMembers by remember(club.id) {
        mutableStateOf(emptyList<AdminMember>())
    }
    var posts by remember(club.id) {
        mutableStateOf(emptyList<AdminPost>())
    }

    var loadingPosts by remember(club.id) { mutableStateOf(true) }
    var loadingMembers by remember(club.id) { mutableStateOf(false) }
    var saving by remember(club.id) { mutableStateOf(false) }
    var errorMessage by remember(club.id) { mutableStateOf<String?>(null) }
    var infoMessage by remember(club.id) { mutableStateOf<String?>(null) }
    var accessValid by remember(club.id) { mutableStateOf(false) }
    var adminUserIds by remember(club.id) { mutableStateOf(emptySet<String>()) }
    var owner by remember(club.id) { mutableStateOf(false) }
    var retry by remember { mutableIntStateOf(0) }
    var refreshMembers by remember { mutableIntStateOf(0) }

    var signedInAdminName by remember(userId) { mutableStateOf("") }
    DisposableEffect(userId, retry) {
        var active = true
        val uid = userId
        signedInAdminName = ""
        val listener = uid?.let {
            db.collection("users").document(it).addSnapshotListener { document, _ ->
                if (active) signedInAdminName = document?.getString("name").orEmpty()
            }
        }
        onDispose { active = false; listener?.remove() }
    }

    var accountItems by remember {
        mutableStateOf<List<Map<*, *>>?>(null)
    }
    var noticeItems by remember {
        mutableStateOf<List<Map<*, *>>?>(null)
    }
    var confirmation by remember {
        mutableStateOf<Pair<String, () -> Unit>?>(null)
    }

    fun perform(
        action: String,
        values: Map<String, Any> = emptyMap(),
        done: (Map<*, *>) -> Unit = {}
    ) {
        if (saving) return
        if (!accessValid) {
            errorMessage = "Your admin assignment is unavailable. Sign in again."
            return
        }

        saving = true
        errorMessage = null
        infoMessage = null

        actions.call(
            clubId = club.id,
            action = action,
            values = values,
            success = {
                saving = false
                refreshMembers++
                done(it)
            },
            failure = {
                saving = false
                errorMessage = it
            }
        )
    }

    fun changeMember(member: AdminMember, operation: String) {
        perform(
            action = "member",
            values = mapOf(
                "id" to member.id,
                "operation" to operation
            )
        )
    }

    DisposableEffect(club.id, userId, retry) {
        accessValid = false
        loadingPosts = true

        if (userId == null) {
            loadingPosts = false
            errorMessage = "Your session has ended. Sign in again."
            return@DisposableEffect onDispose {}
        }

        val accessListener = db.collection("adminAccess")
            .document(userId)
            .addSnapshotListener { document, error ->
                accessValid =
                    error == null &&
                            document?.getBoolean("isActive") == true &&
                            document.getString("clubId") == club.id

                owner = document?.getBoolean("canManageAdmins") == true
                notificationsEnabled =
                    document?.getBoolean("notificationsEnabled") ?: true

                if (!accessValid) {
                    errorMessage =
                        "Admin access is unavailable. Check your club assignment."
                }
            }

        val clubListener = db.collection("clubs")
            .document(club.id)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    errorMessage = error.message
                }

                if (document != null && document.exists()) {
                    clubName = document.getString("name") ?: club.name
                    clubDescription = document.getString("description").orEmpty()
                    clubHistory = document.getString("history").orEmpty()
                    clubCategory =
                        document.getString("category") ?: club.category.label
                    clubBannerUrl = document.getString("bannerUrl").orEmpty()
                    clubProfileUrl =
                        document.getString("profileImageUrl").orEmpty()
                }
            }

        val eventListener = db.collection("events")
            .whereEqualTo("clubId", club.id)
            .whereEqualTo("status", "PUBLISHED")
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    errorMessage = error.message
                } else {
                    events = snapshot?.documents?.map { document ->
                        val timestamp =
                            document.getTimestamp("eventDate")?.toDate()

                        AdminEvent(
                            id = document.id,
                            title = document.getString("title").orEmpty(),
                            category =
                                document.getString("category") ?: "Academic",
                            description =
                                document.getString("description").orEmpty(),
                            date = document.getString("date")
                                ?: timestamp?.let {
                                    formatAdminDate(it, "yyyy-MM-dd")
                                }.orEmpty(),
                            time = document.getString("time")
                                ?: timestamp?.let {
                                    formatAdminDate(it, "HH:mm")
                                }.orEmpty(),
                            endDate = document.getString("endDate")
                                ?: document.getTimestamp("eventEndDate")?.toDate()?.let { formatAdminDate(it, "yyyy-MM-dd") }.orEmpty(),
                            endTime = document.getString("endTime")
                                ?: document.getTimestamp("eventEndDate")?.toDate()?.let { formatAdminDate(it, "HH:mm") }.orEmpty(),
                            venue = document.getString("location").orEmpty(),
                            rsvpEnabled =
                                document.getBoolean("rsvpEnabled") ?: false,
                            link = document.getString("rsvpUrl").orEmpty(),
                            mediaType =
                                document.getString("mediaType") ?: "PHOTO",
                            mediaUrl =
                                document.getString("mediaUrl").orEmpty(),
                            status = "Upcoming"
                        )
                    }?.sortedBy { it.date + it.time } ?: emptyList()
                }
            }

        val postListener = db.collection("posts")
            .whereEqualTo("clubId", club.id)
            .whereEqualTo("status", "PUBLISHED")
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                loadingPosts = false

                if (error != null) {
                    errorMessage =
                        "Unable to load posts: ${error.message}"
                } else {
                    posts = snapshot?.documents
                        ?.sortedByDescending {
                            it.getTimestamp("createdAt")?.seconds ?: 0L
                        }
                        ?.map { document ->
                            val type = when (document.getString("type")) {
                                "PHOTO" -> AdminPostType.PHOTO
                                "VIDEO" -> AdminPostType.VIDEO
                                else -> AdminPostType.TEXT
                            }

                            AdminPost(
                                id = document.id,
                                author =
                                    document.getString("clubName") ?: clubName,
                                caption = document.getString("text").orEmpty(),
                                type = type,
                                mediaUrl =
                                    document.getString("mediaUrl")
                                        ?.takeIf { it.isNotBlank() }
                                        ?: if (type == AdminPostType.VIDEO) {
                                            document.getString("videoUrl").orEmpty()
                                        } else {
                                            document.getString("imageUrl").orEmpty()
                                        },
                                time =
                                    document.getTimestamp("createdAt")
                                        ?.toDate()?.let {
                                            formatAdminDate(
                                                it,
                                                "dd MMM yyyy, HH:mm"
                                            )
                                        } ?: "Publishing…"
                            )
                        } ?: emptyList()
                }
            }

        onDispose {
            accessListener.remove()
            clubListener.remove()
            eventListener.remove()
            postListener.remove()
        }
    }

    DisposableEffect(club.id, accessValid, retry) {
        if (!accessValid) {
            members = emptyList(); pendingMembers = emptyList(); loadingMembers = false
            return@DisposableEffect onDispose {}
        }
        var active = true
        var baseMembers = emptyList<AdminMember>()
        val profiles = mutableMapOf<String, com.google.firebase.firestore.DocumentSnapshot>()
        val profileListeners = mutableMapOf<String, com.google.firebase.firestore.ListenerRegistration>()
        fun publishMembers() {
            if (!active) return
            val loaded = baseMembers.map { member ->
                val profile = profiles[member.id]
                if (profile == null) member else member.copy(
                    name = profile.getString("name")?.takeIf { it.isNotBlank() } ?: "Name not provided",
                    studentNumber = profile.getString("studentNumber")?.takeIf { it.isNotBlank() } ?: "Not provided",
                    course = profile.getString("course")?.takeIf { it.isNotBlank() } ?: "Not provided",
                    year = profile.getString("year")?.takeIf { it.isNotBlank() } ?: "Not provided"
                )
            }
            members = loaded.filter { it.status == "Active" || it.status == "Suspended" }
            pendingMembers = loaded.filter { it.status == "Pending" }
        }
        loadingMembers = true
        val listener = db.collection("clubMembers").whereEqualTo("clubId", club.id)
            .addSnapshotListener { snapshot, error ->
                if (active) {
                    loadingMembers = false
                    if (error != null) errorMessage = "Unable to load memberships: ${error.message}"
                    else {
                        baseMembers = snapshot?.documents?.map { document ->
                            AdminMember(
                                id = document.id, userId = document.getString("userId").orEmpty(),
                                name = "Profile not synced", email = "",
                                studentNumber = "Not provided", course = "Not provided", year = "Not provided",
                                joined = document.getTimestamp("joinedAt")?.toDate()?.let { formatAdminDate(it, "dd MMM yyyy") } ?: "Not recorded",
                                role = "Member", status = when (document.getString("status")) {
                                    "ACTIVE" -> "Active"; "PENDING" -> "Pending"; "SUSPENDED" -> "Suspended"; else -> "Inactive"
                                }
                            )
                        } ?: emptyList()
                        val eligible = baseMembers.filter { it.status == "Active" || it.status == "Pending" }.map { it.id }.toSet()
                        profileListeners.keys.toList().filter { it !in eligible }.forEach {
                            profileListeners.remove(it)?.remove(); profiles.remove(it)
                        }
                        publishMembers()
                        eligible.filter { it !in profileListeners }.forEach { id ->
                            profileListeners[id] = db.collection("clubMembers").document(id)
                                .collection("profile").document("details").addSnapshotListener { profile, profileError ->
                                    if (active && id in baseMembers.map { it.id }) {
                                        if (profileError != null) {
                                            profiles.remove(id)
                                            errorMessage = "Unable to load member details. Check the published rules and retry."
                                        } else if (profile?.exists() == true) profiles[id] = profile
                                        else profiles.remove(id)
                                        publishMembers()
                                    }
                                }
                        }
                    }
                }
            }
        onDispose { active = false; listener.remove(); profileListeners.values.forEach { it.remove() } }
    }

    DisposableEffect(club.id, accessValid, retry) {
        if (!accessValid) {
            adminUserIds = emptySet()
            return@DisposableEffect onDispose {}
        }
        val listener = db.collection("adminAccess")
            .whereEqualTo("clubId", club.id)
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    errorMessage = "Unable to load admin roles: ${error.message}"
                } else {
                    adminUserIds = snapshot?.documents?.map { it.id }?.toSet() ?: emptySet()
                }
            }
        onDispose { listener.remove() }
    }

    fun updatePost(post: AdminPost, caption: String) {
        if (saving || !accessValid) return

        if (
            caption.length > 5000 ||
            (post.type == AdminPostType.TEXT && caption.isBlank())
        ) {
            errorMessage =
                "Text posts need a caption. Maximum length is 5,000 characters."
            return
        }

        saving = true
        errorMessage = null

        db.collection("posts").document(post.id)
            .update(
                mapOf(
                    "text" to caption,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            .addOnSuccessListener {
                saving = false
                infoMessage = "Post updated."
            }
            .addOnFailureListener {
                saving = false
                errorMessage = "Post was not saved: ${it.message}"
            }
    }

    BackHandler {
        when {
            saving -> Unit
            confirmation != null -> confirmation = null
            accountItems != null -> accountItems = null
            noticeItems != null -> noticeItems = null
            showEventEditor -> {
                showEventEditor = false
                editingEvent = null
            }
            showPostEditor -> showPostEditor = false
            selectedMember != null -> selectedMember = null
            showClubEditor -> showClubEditor = false
            selectedTab != 0 -> selectedTab = 0
            else -> showLogoutDialog = true
        }
    }

    Scaffold(
        containerColor = Bg,
        snackbarHost = {
            if (
                loadingPosts ||
                loadingMembers ||
                saving ||
                errorMessage != null ||
                infoMessage != null
            ) {
                Column(
                    Modifier.fillMaxWidth()
                        .background(CardBg)
                        .padding(12.dp)
                ) {
                    if (loadingPosts || loadingMembers || saving) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = Orange
                        )
                    }

                    errorMessage?.let { message ->
                        Text(message, color = PrimaryText)
                        TextButton(
                            enabled = !saving,
                            onClick = {
                                errorMessage = null
                                retry++
                                refreshMembers++
                            }
                        ) {
                            Text("Retry loading", color = Orange)
                        }
                    }

                    infoMessage?.let { message ->
                        Text(message, color = PrimaryText)
                        TextButton(onClick = { infoMessage = null }) {
                            Text("Dismiss", color = Orange)
                        }
                    }
                }
            }
        },
        topBar = {
            AdminTopBar(
                clubName = clubName,
                onLogout = {
                    if (!saving) showLogoutDialog = true
                }
            )
        },
        bottomBar = {
            AdminBottomNav(
                selectedTab = selectedTab,
                onSelected = {
                    if (!saving) {
                        initialMemberTab = 0
                        initialEventFilter = "All"
                        selectedTab = it
                    }
                }
            )
        },
        floatingActionButton = {
            when (selectedTab) {
                1 -> FloatingActionButton(
                    onClick = {
                        if (accessValid && !saving) {
                            editingEvent = null
                            eventDraftId = UUID.randomUUID().toString()
                            errorMessage = null
                            showEventEditor = true
                        }
                    },
                    containerColor = Orange
                ) {
                    Icon(
                        Icons.Default.Add,
                        "Create event",
                        tint = Color.Black
                    )
                }

                3 -> FloatingActionButton(
                    onClick = {
                        if (accessValid && !saving) {
                            publishId = UUID.randomUUID().toString()
                            errorMessage = null
                            showPostEditor = true
                        }
                    },
                    containerColor = Orange
                ) {
                    Icon(
                        Icons.Default.Add,
                        "Create post",
                        tint = Color.Black
                    )
                }
            }
        }
    ) { padding ->
        Box(
            Modifier.fillMaxSize().padding(padding)
        ) {
            when (selectedTab) {
                0 -> AdminDashboard(
                    clubName = clubName,
                    events = events,
                    members = members,
                    pendingCount = pendingMembers.size,
                    onEvents = { initialEventFilter = "Upcoming"; selectedTab = 1 },
                    onMembers = { initialMemberTab = 0; selectedTab = 2 },
                    onPending = { initialMemberTab = 1; selectedTab = 2 },
                    onRole = { selectedTab = 4 }
                )

                1 -> EventsPage(
                    events = events,
                    initialFilter = initialEventFilter,
                    onEdit = {
                        if (!saving) {
                            editingEvent = it
                            eventDraftId = it.id
                            errorMessage = null
                            showEventEditor = true
                        }
                    },
                    onDelete = { event ->
                        confirmation = "Delete this event?" to {
                            perform(
                                "eventDelete",
                                mapOf("id" to event.id)
                            )
                        }
                    }
                )

                2 -> MembersPage(
                    initialTab = initialMemberTab,
                    members = members.map {
                        it.copy(role = if (it.userId in adminUserIds) "Admin" else "Member")
                    },
                    pending = pendingMembers,
                    onView = { selectedMember = it },
                    onRemove = { member ->
                        confirmation =
                            "Remove ${member.name} from this club?" to {
                                changeMember(member, "remove")
                            }
                    },
                    onMakeAdmin = { member ->
                        if (!owner) {
                            errorMessage =
                                "Only the designated club owner can grant admin access."
                        } else {
                            confirmation =
                                "Give ${member.name} admin access?" to {
                                    perform(
                                        "adminGrant",
                                        mapOf("userId" to member.userId)
                                    )
                                }
                        }
                    },
                    onApprove = { changeMember(it, "approve") },
                    onReject = { changeMember(it, "reject") }
                )

                3 -> PostsPage(
                    posts = posts,
                    onEdit = { post, caption ->
                        updatePost(post, caption)
                    },
                    onDelete = {
                        if (accessValid && !saving) deletingPost = it
                    }
                )

                4 -> MorePage(
                    notificationsEnabled = notificationsEnabled,
                    memberCount = members.count { it.status == "Active" },
                    onNotificationChange = {
                        perform(
                            "notificationPreference",
                            mapOf("enabled" to it)
                        )
                    },
                    onEditClub = {
                        if (!saving) {
                            errorMessage = null
                            showClubEditor = true
                        }
                    },
                    onManageLeadership = { showLeadership = true },
                    onManageMembers = { selectedTab = 2 },
                    onManageAccount = {
                        perform("admins") {
                            accountItems = (it["items"] as? List<*>)
                                ?.filterIsInstance<Map<*, *>>()
                                ?: emptyList()
                        }
                    },
                    onSecurity = {
                        val email = auth.currentUser?.email

                        if (email == null) {
                            errorMessage =
                                "No email is linked to this account."
                        } else if (!saving) {
                            saving = true
                            auth.sendPasswordResetEmail(email)
                                .addOnSuccessListener {
                                    saving = false
                                    infoMessage =
                                        "Password reset email sent to $email."
                                }
                                .addOnFailureListener {
                                    saving = false
                                    errorMessage = it.message
                                }
                        }
                    },
                    onNotices = {
                        perform("notifications") {
                            noticeItems = (it["items"] as? List<*>)
                                ?.filterIsInstance<Map<*, *>>()
                                ?: emptyList()
                        }
                    }
                )
            }
        }
    }

    if (showLeadership) {
        LeadershipEditorSheet(club.id, members.filter { it.status == "Active" },
            onDismiss = { showLeadership = false })
    }

    if (showEventEditor) {
        EventEditorSheet(
            event = editingEvent,
            saving = saving,
            errorMessage = errorMessage,
            onDismiss = {
                if (!saving) {
                    showEventEditor = false
                    editingEvent = null
                }
            },
            onSave = { saved ->
                if (!saving && accessValid) {
                    saving = true
                    errorMessage = null

                    actions.upload(
                        context = context,
                        clubId = club.id,
                        uri = saved.mediaUri,
                        kind = saved.mediaType,
                        success = { uploaded ->
                            saving = false

                            perform(
                                action = "eventSave",
                                values = mapOf(
                                    "id" to saved.id.ifBlank { eventDraftId },
                                    "title" to saved.title,
                                    "category" to saved.category,
                                    "description" to saved.description,
                                    "date" to saved.date,
                                    "time" to saved.time,
                                    "endDate" to saved.endDate,
                                    "endTime" to saved.endTime,
                                    "venue" to saved.venue,
                                    "rsvpEnabled" to saved.rsvpEnabled,
                                    "link" to saved.link,
                                    "mediaType" to saved.mediaType,
                                    "mediaUrl" to uploaded.ifBlank {
                                        saved.mediaUrl
                                    }
                                )
                            ) {
                                showEventEditor = false
                                editingEvent = null
                                infoMessage = "Event saved."
                            }
                        },
                        failure = {
                            saving = false
                            errorMessage = it
                        }
                    )
                }
            }
        )
    }

    if (showPostEditor) {
        PostEditorSheet(
            saving = saving,
            errorMessage = errorMessage,
            canPublish = accessValid,
            onDismiss = {
                if (!saving) showPostEditor = false
            },
            onPublish = { caption, type, uri ->
                if (accessValid && !saving) {
                    saving = true
                    errorMessage = null

                    actions.upload(
                        context = context,
                        clubId = club.id,
                        uri = if (type == AdminPostType.TEXT) null else uri,
                        kind = type.name,
                        success = { url ->
                            saving = false

                            perform(
                                "postPublish",
                                mapOf(
                                    "id" to publishId,
                                    "type" to type.name,
                                    "text" to caption,
                                    "mediaUrl" to url
                                )
                            ) {
                                showPostEditor = false
                                selectedTab = 3
                                infoMessage = "Post published."
                            }
                        },
                        failure = {
                            saving = false
                            errorMessage = it
                        }
                    )
                }
            }
        )
    }

    deletingPost?.let { post ->
        AlertDialog(
            onDismissRequest = {
                if (!saving) deletingPost = null
            },
            title = { Text("Delete post?") },
            text = {
                Text("This removes the post from club and student feeds.")
            },
            confirmButton = {
                TextButton(
                    enabled = !saving && accessValid,
                    onClick = {
                        saving = true
                        errorMessage = null

                        db.collection("posts").document(post.id)
                            .delete()
                            .addOnSuccessListener {
                                saving = false
                                deletingPost = null
                                infoMessage = "Post deleted."
                            }
                            .addOnFailureListener {
                                saving = false
                                errorMessage =
                                    "Post was not deleted: ${it.message}"
                            }
                    }
                ) {
                    Text(if (saving) "Deleting…" else "Delete")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !saving,
                    onClick = { deletingPost = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    selectedMember?.let { selected ->
        val member = (members + pendingMembers).firstOrNull { it.id == selected.id }?.copy(
            role = if (selected.userId in adminUserIds) "Admin" else "Member"
        ) ?: selected
        MemberProfileSheet(
            member = member,
            onDismiss = { selectedMember = null }
        )
    }

    if (showClubEditor) {
        ClubEditorSheet(
            clubName = clubName,
            description = clubDescription,
            history = clubHistory,
            category = clubCategory,
            bannerUrl = clubBannerUrl,
            profileUrl = clubProfileUrl,
            saving = saving,
            errorMessage = errorMessage,
            onDismiss = {
                if (!saving) showClubEditor = false
            },
            onSave = { name, description, history, category, banner, photo ->
                if (!saving && accessValid) {
                    saving = true
                    errorMessage = null

                    actions.upload(
                        context = context,
                        clubId = club.id,
                        uri = banner,
                        kind = "PHOTO",
                        success = { bannerUrl ->
                            actions.upload(
                                context = context,
                                clubId = club.id,
                                uri = photo,
                                kind = "PHOTO",
                                success = { photoUrl ->
                                    saving = false

                                    val values = mutableMapOf<String, Any>(
                                        "name" to name,
                                        "description" to description,
                                        "history" to history,
                                        "category" to category
                                    )

                                    if (bannerUrl.isNotBlank()) {
                                        values["bannerUrl"] = bannerUrl
                                    }
                                    if (photoUrl.isNotBlank()) {
                                        values["profileImageUrl"] = photoUrl
                                    }

                                    perform("clubSave", values) {
                                        showClubEditor = false
                                        infoMessage = "Club updated."
                                    }
                                },
                                failure = {
                                    saving = false
                                    errorMessage = it
                                }
                            )
                        },
                        failure = {
                            saving = false
                            errorMessage = it
                        }
                    )
                }
            }
        )
    }

    confirmation?.let { (message, action) ->
        AlertDialog(
            onDismissRequest = {
                if (!saving) confirmation = null
            },
            title = { Text(message) },
            confirmButton = {
                TextButton(
                    enabled = !saving,
                    onClick = {
                        confirmation = null
                        action()
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !saving,
                    onClick = { confirmation = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    accountItems?.let { admins ->
        AlertDialog(
            onDismissRequest = {
                if (!saving) accountItems = null
            },
            containerColor = CardBg,
            title = { Text("Club admins", color = PrimaryText) },
            text = {
                LazyColumn {
                    items(admins) { admin ->
                        Column(Modifier.padding(vertical = 8.dp)) {
                            val uid = admin["uid"] as? String
                            val memberName = members.firstOrNull { it.userId == uid }?.name
                                ?.takeIf { it.isNotBlank() && it != "Profile not synced" && it != "Name not provided" }
                            val actualName = if (uid == userId) {
                                signedInAdminName.takeIf { it.isNotBlank() }
                                    ?: memberName
                                    ?: auth.currentUser?.displayName?.takeIf { it.isNotBlank() }
                            } else memberName
                            Text(actualName ?: "Name unavailable", color = PrimaryText, fontWeight = FontWeight.Bold)
                            Text(
                                (if (admin["owner"] == true) "Owner" else "Club admin") +
                                        (if (uid == userId) " • You" else ""),
                                color = SecondaryText, fontSize = 12.sp
                            )
                            if (actualName == null) {
                                Text("No synced name is available for this account.", color = SecondaryText, fontSize = 12.sp)
                            }

                            if (
                                owner &&
                                admin["owner"] != true &&
                                admin["self"] != true
                            ) {
                                TextButton(
                                    enabled = !saving,
                                    onClick = {
                                        confirmation =
                                            "Revoke this admin's access?" to {
                                                perform(
                                                    "adminRevoke",
                                                    mapOf(
                                                        "userId" to
                                                                admin["uid"].toString()
                                                    )
                                                ) {
                                                    accountItems = null
                                                }
                                            }
                                    }
                                ) {
                                    Text("Revoke access", color = Danger)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !saving,
                    onClick = { accountItems = null }
                ) {
                    Text("Close", color = Orange)
                }
            }
        )
    }

    noticeItems?.let { notices ->
        AlertDialog(
            onDismissRequest = {
                if (!saving) noticeItems = null
            },
            title = { Text("Platform announcements") },
            text = {
                LazyColumn {
                    if (notices.isEmpty()) {
                        item {
                            Text("No announcements for this club.")
                        }
                    }

                    items(notices) { notice ->
                        Column(Modifier.padding(vertical = 8.dp)) {
                            Text(
                                notice["title"].toString(),
                                fontWeight = FontWeight.Bold
                            )
                            Text(notice["message"].toString())

                            if (notice["read"] != true) {
                                TextButton(
                                    enabled = !saving,
                                    onClick = {
                                        perform(
                                            "readNotification",
                                            mapOf(
                                                "id" to notice["id"].toString()
                                            )
                                        ) {
                                            noticeItems = notices.map { item ->
                                                if (item["id"] == notice["id"]) {
                                                    item.entries.associate {
                                                        it.key to it.value
                                                    } + mapOf("read" to true)
                                                } else {
                                                    item
                                                }
                                            }
                                        }
                                    }
                                ) {
                                    Text("Mark as read")
                                }
                            } else {
                                Text("Read", color = Green)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !saving,
                    onClick = { noticeItems = null }
                ) {
                    Text("Close")
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!saving) showLogoutDialog = false
            },
            containerColor = CardBg,
            icon = {
                Icon(Icons.Default.Logout, null, tint = Danger)
            },
            title = {
                Text("Logout of Club Admin?", color = PrimaryText)
            },
            text = {
                Text(
                    "You will leave the club administration area.",
                    color = SecondaryText
                )
            },
            confirmButton = {
                Button(
                    enabled = !saving,
                    onClick = {
                        showLogoutDialog = false
                        auth.signOut()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Danger
                    )
                ) {
                    Text(
                        "Logout",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !saving,
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Cancel", color = PrimaryText)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminTopBar(
    clubName: String,
    onLogout: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Bg
        ),
        navigationIcon = {
            TextButton(
                onClick = onLogout,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Icon(
                    Icons.Default.Logout,
                    "Logout",
                    tint = Danger,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    "Logout",
                    color = Danger,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
        },
        title = {
            Column {
                Text(
                    clubName,
                    color = PrimaryText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text("Club Admin", color = Orange, fontSize = 11.sp)
            }
        },
        actions = {
            Box(
                modifier = Modifier.padding(end = 14.dp)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(CardSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AdminPanelSettings,
                    "Club administrator",
                    tint = Orange
                )
            }
        }
    )
}

@Composable
private fun AdminBottomNav(
    selectedTab: Int,
    onSelected: (Int) -> Unit
) {
    val tabs = listOf(
        Triple("Home", Icons.Default.Home, 0),
        Triple("Events", Icons.Default.Event, 1),
        Triple("Members", Icons.Default.People, 2),
        Triple("Posts", Icons.Default.Forum, 3),
        Triple("More", Icons.Default.MoreHoriz, 4)
    )

    NavigationBar(containerColor = CardBg) {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab.third,
                onClick = { onSelected(tab.third) },
                icon = { Icon(tab.second, tab.first) },
                label = { Text(tab.first, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Orange,
                    selectedTextColor = Orange,
                    indicatorColor = CardSoft,
                    unselectedIconColor = SecondaryText,
                    unselectedTextColor = SecondaryText
                )
            )
        }
    }
}

@Composable
private fun AdminDashboard(
    clubName: String,
    events: List<AdminEvent>,
    members: List<AdminMember>,
    pendingCount: Int,
    onEvents: () -> Unit,
    onMembers: () -> Unit,
    onPending: () -> Unit,
    onRole: () -> Unit
) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(30_000L)
        }
    }
    val upcomingEvents = events.filter { event ->
        val start = try {
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).apply {
                isLenient = false
                timeZone = TimeZone.getTimeZone("Africa/Johannesburg")
            }.parse("${event.date} ${event.time}")?.time
        } catch (_: Exception) {
            null
        }
        start != null && start > now
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Text(
                "Dashboard",
                color = PrimaryText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text("Manage $clubName", color = SecondaryText)
        }

        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    Modifier.weight(1f),
                    members.count { it.status == "Active" }.toString(),
                    "Members",
                    Icons.Default.People,
                    onMembers
                )
                StatCard(
                    Modifier.weight(1f),
                    upcomingEvents.size.toString(),
                    "Upcoming",
                    Icons.Default.Event,
                    onEvents
                )
            }
        }

        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    Modifier.weight(1f),
                    pendingCount.toString(),
                    "Pending",
                    Icons.Default.PersonAdd,
                    onPending
                )
                StatCard(
                    Modifier.weight(1f),
                    "Admin",
                    "Your role",
                    Icons.Default.AdminPanelSettings,
                    onRole
                )
            }
        }


        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Upcoming Events",
                    color = PrimaryText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                )
                Text(
                    "View all",
                    color = Orange,
                    modifier = Modifier.clickable { onEvents() }
                )
            }
        }

        if (upcomingEvents.isEmpty()) {
            item { EmptyState("No upcoming events", "Create a future event using the plus button in Events.") }
        }
        items(upcomingEvents.sortedBy { it.date + it.time }.take(3), key = { it.id }) { event ->
            Box(Modifier.fillMaxWidth().clickable { onEvents() }) {
                EventPreviewCard(event.copy(status = adminEventStatus(event, now)))
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    value: String,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(115.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, null, tint = Orange)
            Text(
                value,
                color = PrimaryText,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp
            )
            Text(label, color = SecondaryText, fontSize = 11.sp)
        }
    }
}

@Composable
private fun EventsPage(
    events: List<AdminEvent>,
    initialFilter: String = "All",
    onEdit: (AdminEvent) -> Unit,
    onDelete: (AdminEvent) -> Unit
) {
    var filter by remember(initialFilter) { mutableStateOf(initialFilter) }

    var statusNow by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) { statusNow = System.currentTimeMillis(); delay(1000L) }
    }
    val currentEvents = events.map { it.copy(status = adminEventStatus(it, statusNow)) }
    val visible = if (filter == "All") currentEvents else currentEvents.filter { it.status == filter }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "Events",
                color = PrimaryText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text("Create and manage club events", color = SecondaryText)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Upcoming", "Ongoing", "Passed").forEach { option ->
                    FilterChip(
                        selected = filter == option,
                        onClick = { filter = option },
                        label = { Text(option) }
                    )
                }
            }
        }

        if (visible.isEmpty()) {
            item {
                EmptyState(
                    "No events",
                    "No events are available in this category."
                )
            }
        } else {
            items(visible, key = { it.id }) { event ->
                AdminEventCard(
                    event,
                    onEdit = { onEdit(event) },
                    onDelete = { onDelete(event) }
                )
            }
        }
    }
}

@Composable
private fun AdminEventCard(
    event: AdminEvent,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(55.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Event, null, tint = Orange)
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    event.category,
                    color = Orange,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    event.title,
                    color = PrimaryText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    "${event.date} • ${event.time}",
                    color = SecondaryText,
                    fontSize = 11.sp
                )
                Text(event.venue, color = SecondaryText, fontSize = 11.sp)
                Spacer(Modifier.height(6.dp))
                StatusBadge(adminEventStatus(event))
            }

            Box {
                IconButton(onClick = { menu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        "Options",
                        tint = SecondaryText
                    )
                }

                DropdownMenu(
                    expanded = menu,
                    onDismissRequest = { menu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit event") },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, null)
                        },
                        onClick = {
                            menu = false
                            onEdit()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Delete event", color = Danger) },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, null, tint = Danger)
                        },
                        onClick = {
                            menu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventEditorSheet(
    event: AdminEvent?,
    saving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (AdminEvent) -> Unit
) {
    var title by remember { mutableStateOf(event?.title.orEmpty()) }
    var category by remember {
        mutableStateOf(event?.category ?: "Culture & Arts")
    }
    var description by remember {
        mutableStateOf(event?.description.orEmpty())
    }
    var date by remember { mutableStateOf(event?.date.orEmpty()) }
    var time by remember { mutableStateOf(event?.time.orEmpty()) }
    var endDate by remember { mutableStateOf(event?.endDate.orEmpty()) }
    var endTime by remember { mutableStateOf(event?.endTime.orEmpty()) }
    var venue by remember { mutableStateOf(event?.venue.orEmpty()) }
    var link by remember { mutableStateOf(event?.link.orEmpty()) }
    var rsvp by remember { mutableStateOf(event?.rsvpEnabled ?: true) }

    var mediaMode by remember {
        mutableStateOf(
            if (event?.mediaUrl.isNullOrBlank()) {
                "Text only"
            } else if (event?.mediaType == "VIDEO") {
                "Video"
            } else {
                "Photo"
            }
        )
    }

    var selectedMediaName by remember { mutableStateOf<String?>(null) }
    var selectedMediaUri by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedMediaUri = uri.toString()
            selectedMediaName =
                uri.lastPathSegment?.substringAfterLast("/")
                    ?: "Media selected"
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardBg
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp)
                .imePadding(),
            contentPadding = PaddingValues(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                if (saving) {
                    LinearProgressIndicator(
                        Modifier.fillMaxWidth(),
                        color = Orange
                    )
                }
                errorMessage?.let { Text(it, color = Danger) }
            }

            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (event == null) "New event" else "Edit event",
                        color = Orange,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        enabled = !saving,
                        onClick = onDismiss
                    ) {
                        Icon(
                            Icons.Default.Close,
                            "Close",
                            tint = SecondaryText
                        )
                    }
                }
            }

            item {
                DarkTextField(
                    title,
                    { title = it },
                    "Event title",
                    !saving
                )
            }

            item {
                DarkTextField(
                    category,
                    { category = it },
                    "Category",
                    !saving
                )
            }

            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(Modifier.weight(1f)) {
                        DarkTextField(
                            date,
                            { date = it },
                            "Date (YYYY-MM-DD)",
                            !saving
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        DarkTextField(
                            time,
                            { time = it },
                            "Time (HH:mm)",
                            !saving
                        )
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        DarkTextField(endDate, { endDate = it }, "End date (YYYY-MM-DD)", !saving)
                    }
                    Box(Modifier.weight(1f)) {
                        DarkTextField(endTime, { endTime = it }, "End time (HH:mm)", !saving)
                    }
                }
                Text("South African time · End must be after start. Use the next date for overnight events.", color = SecondaryText, fontSize = 11.sp)
            }

            item {
                DarkTextField(
                    venue,
                    { venue = it },
                    "Venue",
                    !saving
                )
            }

            item {
                Text(
                    "Event media",
                    color = PrimaryText,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Photo", "Video", "Text only").forEach { option ->
                        FilterChip(
                            enabled = !saving && option == "Text only",
                            selected = mediaMode == option,
                            onClick = {
                                mediaMode = option
                                selectedMediaUri = null
                                selectedMediaName = null
                            },
                            label = { Text(option) }
                        )
                    }
                }
            }

            item { Text("New photo and video uploads are unavailable for now. You can save event details without uploading media.", color = SecondaryText, fontSize = 12.sp) }
            if (mediaMode != "Text only") {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                            .height(110.dp)
                            .clickable(enabled = false) {
                                picker.launch(
                                    if (mediaMode == "Photo") {
                                        "image/*"
                                    } else {
                                        "video/*"
                                    }
                                )
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = CardSoft
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                if (mediaMode == "Photo") {
                                    Icons.Default.Image
                                } else {
                                    Icons.Default.PlayCircle
                                },
                                null,
                                tint = Orange
                            )
                            Spacer(Modifier.height(7.dp))
                            Text(
                                selectedMediaName
                                    ?: if (!event?.mediaUrl.isNullOrBlank()) {
                                        "Existing media • tap to replace"
                                    } else if (mediaMode == "Photo") {
                                        "Tap to upload a photo"
                                    } else {
                                        "Tap to upload a video"
                                    },
                                color = SecondaryText,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    enabled = !saving,
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth().height(110.dp),
                    placeholder = {
                        Text("Description — what's happening, who's it for...")
                    },
                    colors = darkFieldColors(),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            item {
                DarkTextField(
                    link,
                    { link = it },
                    "Add a link (RSVP form, WhatsApp group...)",
                    !saving
                )
            }

            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "RSVP Event",
                            color = PrimaryText,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Allow students to RSVP",
                            color = SecondaryText,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        enabled = !saving,
                        checked = rsvp,
                        onCheckedChange = { rsvp = it }
                    )
                }
            }

            item {
                Button(
                    enabled = !saving &&
                            title.isNotBlank() &&
                            date.isNotBlank() &&
                            time.isNotBlank() &&
                            endDate.isNotBlank() &&
                            endTime.isNotBlank() &&
                            venue.isNotBlank(),
                    onClick = {
                        val kind =
                            if (mediaMode == "Video") "VIDEO" else "PHOTO"

                        onSave(
                            AdminEvent(
                                id = event?.id.orEmpty(),
                                title = title.trim(),
                                category = category.trim(),
                                description = description.trim(),
                                date = date.trim(),
                                time = time.trim(),
                                endDate = endDate.trim(),
                                endTime = endTime.trim(),
                                venue = venue.trim(),
                                rsvpEnabled = rsvp,
                                link = link.trim(),
                                mediaUri = selectedMediaUri,
                                mediaType = kind,
                                mediaUrl =
                                    if (
                                        mediaMode == "Text only" ||
                                        event?.mediaType != kind
                                    ) {
                                        ""
                                    } else {
                                        event.mediaUrl
                                    }
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Orange
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        if (saving) {
                            "Saving…"
                        } else if (event == null) {
                            "Post event"
                        } else {
                            "Save changes"
                        },
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun MembersPage(
    initialTab: Int = 0,
    members: List<AdminMember>,
    pending: List<AdminMember>,
    onView: (AdminMember) -> Unit,
    onRemove: (AdminMember) -> Unit,
    onMakeAdmin: (AdminMember) -> Unit,
    onApprove: (AdminMember) -> Unit,
    onReject: (AdminMember) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var tab by remember(initialTab) { mutableIntStateOf(initialTab) }

    fun matches(member: AdminMember): Boolean {
        return member.name.contains(search, ignoreCase = true) ||
                member.email.contains(search, ignoreCase = true) ||
                member.studentNumber.contains(search, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Members",
                color = PrimaryText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text("${members.size} members", color = SecondaryText)
        }

        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search members") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                colors = darkFieldColors(),
                shape = RoundedCornerShape(18.dp)
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    label = { Text("Members (${members.size})") }
                )
                FilterChip(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    label = { Text("Pending (${pending.size})") }
                )
            }
        }

        if (tab == 0) {
            val filtered = members.filter { matches(it) }

            if (filtered.isEmpty()) {
                item {
                    EmptyState("No members", "No matching members.")
                }
            }

            items(filtered, key = { it.id }) { member ->
                MemberRow(
                    member,
                    onView,
                    onRemove,
                    onMakeAdmin
                )
            }
        } else {
            val filtered = pending.filter { matches(it) }

            if (filtered.isEmpty()) {
                item {
                    EmptyState(
                        "No requests",
                        "There are no matching pending requests."
                    )
                }
            }

            items(filtered, key = { it.id }) { member ->
                PendingMemberRow(member, onApprove, onReject)
            }
        }
    }
}

@Composable
private fun MemberRow(
    member: AdminMember,
    onView: (AdminMember) -> Unit,
    onRemove: (AdminMember) -> Unit,
    onMakeAdmin: (AdminMember) -> Unit
) {
    var menu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onView(member) },
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(member.name)
            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    member.name,
                    color = PrimaryText,
                    fontWeight = FontWeight.Bold
                )
                Text(member.email, color = SecondaryText, fontSize = 11.sp)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (member.role == "Admin") {
                        Text(
                            "ADMIN",
                            color = Green,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (member.status == "Suspended") {
                        Text(
                            "SUSPENDED",
                            color = Danger,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Box {
                IconButton(onClick = { menu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        "Options",
                        tint = SecondaryText
                    )
                }

                DropdownMenu(
                    expanded = menu,
                    onDismissRequest = { menu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("View profile") },
                        onClick = {
                            menu = false
                            onView(member)
                        }
                    )

                    if (member.role != "Admin") {
                        if (member.status == "Active") {
                            DropdownMenuItem(
                                text = { Text("Make admin") },
                                onClick = {
                                    menu = false
                                    onMakeAdmin(member)
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = {
                                Text("Remove member", color = Danger)
                            },
                            onClick = {
                                menu = false
                                onRemove(member)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemberProfileSheet(
    member: AdminMember,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardBg
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
            contentPadding = PaddingValues(bottom = 30.dp)
        ) {
            item {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Avatar(member.name, size = 82)
                    Spacer(Modifier.height(14.dp))
                    Text(
                        member.name,
                        color = PrimaryText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                    Text(member.email, color = SecondaryText)
                    Spacer(Modifier.height(22.dp))
                }
            }

            item { ProfileDetail("Student number", member.studentNumber) }
            item { ProfileDetail("Programme", member.course) }
            item { ProfileDetail("Year", member.year) }
            item { ProfileDetail("Club role", member.role) }
            item { ProfileDetail("Status", member.status) }
            item { ProfileDetail("Joined", member.joined) }
        }
    }
}

@Composable
private fun ProfileDetail(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = SecondaryText)
        Text(
            value,
            color = PrimaryText,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.widthIn(max = 210.dp)
        )
    }
    HorizontalDivider(color = Border)
}

@Composable
private fun PendingMemberRow(
    member: AdminMember,
    onApprove: (AdminMember) -> Unit,
    onReject: (AdminMember) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(member.name)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        member.name,
                        color = PrimaryText,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        member.email,
                        color = SecondaryText,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onReject(member) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reject", color = Danger)
                }

                Button(
                    onClick = { onApprove(member) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green
                    )
                ) {
                    Text("Approve", color = Color.Black)
                }
            }
        }
    }
}

@Composable
private fun PostsPage(
    posts: List<AdminPost>,
    onEdit: (AdminPost, String) -> Unit,
    onDelete: (AdminPost) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "Posts",
                color = PrimaryText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text("Share updates with club members", color = SecondaryText)
        }

        if (posts.isEmpty()) {
            item {
                EmptyState("No posts", "Create your first club post.")
            }
        } else {
            items(posts, key = { it.id }) {
                PostCard(it, onEdit, onDelete)
            }
        }
    }
}

@Composable
private fun PostCard(
    post: AdminPost,
    onEdit: (AdminPost, String) -> Unit,
    onDelete: (AdminPost) -> Unit
) {
    val context = LocalContext.current

    var menu by remember { mutableStateOf(false) }
    var editDialog by remember { mutableStateOf(false) }
    var editedCaption by remember(post.caption) {
        mutableStateOf(post.caption)
    }
    var playbackError by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(post.author)
                Spacer(Modifier.width(10.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        post.author,
                        color = PrimaryText,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        post.time,
                        color = SecondaryText,
                        fontSize = 10.sp
                    )
                }

                Box {
                    IconButton(onClick = { menu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            "Post options",
                            tint = SecondaryText
                        )
                    }

                    DropdownMenu(
                        expanded = menu,
                        onDismissRequest = { menu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit post") },
                            onClick = {
                                menu = false
                                editedCaption = post.caption
                                editDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete post") },
                            onClick = {
                                menu = false
                                onDelete(post)
                            }
                        )
                    }
                }
            }

            if (post.caption.isNotBlank()) {
                Spacer(Modifier.height(14.dp))
                Text(post.caption, color = PrimaryText)
            }

            if (post.type != AdminPostType.TEXT) {
                Spacer(Modifier.height(14.dp))

                Box(
                    modifier = Modifier.fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(CardSoft),
                    contentAlignment = Alignment.Center
                ) {
                    if (
                        post.type == AdminPostType.PHOTO &&
                        post.mediaUrl.isNotBlank()
                    ) {
                        AsyncImage(
                            model = post.mediaUrl,
                            contentDescription = "Club post photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            modifier = Modifier.clickable {
                                if (post.mediaUrl.isBlank()) {
                                    playbackError = "This post has no video URL."
                                } else {
                                    val uri = Uri.parse(post.mediaUrl)

                                    try {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW)
                                                .setDataAndType(uri, "video/*")
                                        )
                                    } catch (_: Exception) {
                                        try {
                                            context.startActivity(
                                                Intent(Intent.ACTION_VIEW, uri)
                                            )
                                        } catch (_: Exception) {
                                            playbackError =
                                                "No video player or browser is available."
                                        }
                                    }
                                }
                            },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.PlayCircle,
                                "Play video",
                                tint = Orange,
                                modifier = Modifier.size(36.dp)
                            )
                            Text("Play video", color = SecondaryText)
                        }
                    }
                }
            }

            playbackError?.let {
                Text(it, color = Danger, fontSize = 12.sp)
            }
        }
    }

    if (editDialog) {
        AlertDialog(
            onDismissRequest = { editDialog = false },
            title = { Text("Edit post") },
            text = {
                OutlinedTextField(
                    value = editedCaption,
                    onValueChange = { editedCaption = it },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    enabled = editedCaption.length <= 5000 &&
                            (
                                    post.type != AdminPostType.TEXT ||
                                            editedCaption.isNotBlank()
                                    ),
                    onClick = {
                        onEdit(post, editedCaption.trim())
                        editDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostEditorSheet(
    saving: Boolean,
    errorMessage: String?,
    canPublish: Boolean,
    onDismiss: () -> Unit,
    onPublish: (String, AdminPostType, String?) -> Unit
) {
    var caption by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(AdminPostType.TEXT) }
    var selectedMediaName by remember { mutableStateOf<String?>(null) }
    var selectedMediaUri by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedMediaName =
                uri.lastPathSegment?.substringAfterLast("/")
                    ?: "Media selected"
            selectedMediaUri = uri.toString()
        }
    }

    fun changeType(value: AdminPostType) {
        if (type != value) {
            type = value
            selectedMediaName = null
            selectedMediaUri = null
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardBg
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp)
                .imePadding(),
            contentPadding = PaddingValues(bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "New post",
                        color = Orange,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        enabled = !saving,
                        onClick = onDismiss
                    ) {
                        Icon(
                            Icons.Default.Close,
                            "Close",
                            tint = SecondaryText
                        )
                    }
                }
            }

            item {
                Text(
                    "Post as",
                    color = PrimaryText,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        enabled = !saving,
                        selected = type == AdminPostType.TEXT,
                        onClick = { changeType(AdminPostType.TEXT) },
                        label = { Text("Text") }
                    )
                    FilterChip(
                        enabled = false,
                        selected = type == AdminPostType.PHOTO,
                        onClick = { changeType(AdminPostType.PHOTO) },
                        label = { Text("Photo") }
                    )
                    FilterChip(
                        enabled = false,
                        selected = type == AdminPostType.VIDEO,
                        onClick = { changeType(AdminPostType.VIDEO) },
                        label = { Text("Video") }
                    )
                }
            }

            item { Text("Photo and video uploads are unavailable for now. Text posts work normally.", color = SecondaryText, fontSize = 12.sp) }
            if (type != AdminPostType.TEXT) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                            .height(130.dp)
                            .clickable(enabled = !saving) {
                                picker.launch(
                                    if (type == AdminPostType.PHOTO) {
                                        "image/*"
                                    } else {
                                        "video/*"
                                    }
                                )
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = CardSoft
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                if (type == AdminPostType.PHOTO) {
                                    Icons.Default.Image
                                } else {
                                    Icons.Default.PlayCircle
                                },
                                null,
                                tint = Orange
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                selectedMediaName
                                    ?: if (type == AdminPostType.PHOTO) {
                                        "Tap to import photo from your phone"
                                    } else {
                                        "Tap to import video from your phone"
                                    },
                                color = SecondaryText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                item {
                    Text(
                        "Images: up to 10 MB. Videos: up to 100 MB.",
                        color = Orange,
                        fontSize = 12.sp
                    )
                }
            }

            item {
                if (saving) {
                    LinearProgressIndicator(
                        Modifier.fillMaxWidth(),
                        color = Orange
                    )
                }
                errorMessage?.let { Text(it, color = Danger) }
            }

            item {
                OutlinedTextField(
                    enabled = !saving,
                    value = caption,
                    onValueChange = { caption = it },
                    modifier = Modifier.fillMaxWidth().height(130.dp),
                    placeholder = { Text("Write a caption...") },
                    colors = darkFieldColors(),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            item {
                Button(
                    enabled = canPublish &&
                            !saving &&
                            caption.trim().length <= 5000 &&
                            if (type == AdminPostType.TEXT) {
                                caption.isNotBlank()
                            } else {
                                selectedMediaUri != null
                            },
                    onClick = {
                        onPublish(caption.trim(), type, selectedMediaUri)
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Orange
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        if (saving) "Publishing…" else "Publish post",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun MorePage(
    notificationsEnabled: Boolean,
    memberCount: Int,
    onNotificationChange: (Boolean) -> Unit,
    onEditClub: () -> Unit,
    onManageLeadership: () -> Unit,
    onManageMembers: () -> Unit,
    onManageAccount: () -> Unit,
    onSecurity: () -> Unit,
    onNotices: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "More",
                color = PrimaryText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text("Club settings & administration", color = SecondaryText)
        }

        item { SectionTitle("CLUB PROFILE") }

        item {
            SettingsRow(
                Icons.Default.Edit,
                "Edit club",
                "Update banner, photo, name, description, history and category",
                onEditClub
            )
        }

        item {
            SettingsRow(Icons.Default.Groups, "Manage leadership",
                "Assign chairperson, secretary and other club positions", onManageLeadership)
        }
        item { SectionTitle("MEMBERSHIP") }


        item {
            SettingsRow(
                Icons.Default.People,
                "Manage members",
                "$memberCount current members",
                onManageMembers
            )
        }

        item { SectionTitle("ADMINISTRATION") }

        item {
            SettingsRow(
                Icons.Default.AdminPanelSettings,
                "Manage account",
                "Admins and account permissions",
                onManageAccount
            )
        }

        item {
            ToggleSettingsRow(
                Icons.Default.Notifications,
                "Super Admin notifications",
                "Important platform announcements",
                notificationsEnabled,
                onNotificationChange
            )
        }

        item {
            SettingsRow(
                Icons.Default.Notifications,
                "Announcements",
                "View platform messages",
                onNotices
            )
        }

        item {
            SettingsRow(
                Icons.Default.Security,
                "Account security",
                "Send a password reset email",
                onSecurity
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClubEditorSheet(
    clubName: String,
    description: String,
    history: String,
    category: String,
    bannerUrl: String,
    profileUrl: String,
    saving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String?, String?) -> Unit
) {
    var name by remember { mutableStateOf(clubName) }
    var desc by remember { mutableStateOf(description) }
    var hist by remember { mutableStateOf(history) }
    var cat by remember { mutableStateOf(category) }
    var bannerUri by remember { mutableStateOf<String?>(null) }
    var photoUri by remember { mutableStateOf<String?>(null) }

    val bannerPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) bannerUri = uri.toString()
    }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) photoUri = uri.toString()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardBg
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp)
                .imePadding(),
            contentPadding = PaddingValues(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                if (saving) {
                    LinearProgressIndicator(
                        Modifier.fillMaxWidth(),
                        color = Orange
                    )
                }
                errorMessage?.let { Text(it, color = Danger) }
            }

            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Edit club",
                        color = Orange,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        enabled = !saving,
                        onClick = onDismiss
                    ) {
                        Icon(
                            Icons.Default.Close,
                            "Close",
                            tint = SecondaryText
                        )
                    }
                }
            }

            item {
                Text(
                    "Club banner — uploads unavailable",
                    color = PrimaryText,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier.fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Orange)
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    val image = bannerUri ?: bannerUrl

                    if (image.isNotBlank()) {
                        AsyncImage(
                            model = image,
                            contentDescription = "Club banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Image,
                                null,
                                tint = Color.White
                            )
                            Text("Club banner", color = Color.White)
                            Text(
                                "Uploads unavailable for now",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(72.dp)
                            .clip(CircleShape)
                            .background(Orange)
                            .clickable(enabled = false) {},
                        contentAlignment = Alignment.Center
                    ) {
                        val image = photoUri ?: profileUrl

                        if (image.isNotBlank()) {
                            AsyncImage(
                                model = image,
                                contentDescription = "Club profile photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.PhotoCamera,
                                null,
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.width(14.dp))

                    Column {
                        Text(
                            "Profile photo",
                            color = PrimaryText,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (photoUri != null) {
                                "New photo selected"
                            } else {
                                "Uploads unavailable for now"
                            },
                            color = SecondaryText,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            item {
                DarkTextField(name, { name = it }, "Club name", !saving)
            }

            item {
                OutlinedTextField(
                    enabled = !saving,
                    value = desc,
                    onValueChange = { desc = it },
                    modifier = Modifier.fillMaxWidth().height(110.dp),
                    label = { Text("Description") },
                    colors = darkFieldColors(),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            item {
                OutlinedTextField(
                    enabled = !saving,
                    value = hist,
                    onValueChange = { hist = it },
                    modifier = Modifier.fillMaxWidth().height(130.dp),
                    label = { Text("Club history") },
                    colors = darkFieldColors(),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            item {
                DarkTextField(cat, { cat = it }, "Category", !saving)
                Text(
                    "SPORT, CULTURE, ADVOCACY, IDENTITY or ACADEMIC",
                    color = SecondaryText,
                    fontSize = 11.sp
                )
            }

            item {
                Button(
                    enabled = !saving && name.isNotBlank(),
                    onClick = {
                        onSave(
                            name.trim(),
                            desc.trim(),
                            hist.trim(),
                            cat.trim(),
                            bannerUri,
                            photoUri
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Orange
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        if (saving) "Saving…" else "Save changes",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        color = SecondaryText,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(45.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(CardSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Orange)
            }

            Spacer(Modifier.width(13.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    color = PrimaryText,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    subtitle,
                    color = SecondaryText,
                    fontSize = 11.sp,
                    maxLines = 2
                )
            }

            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                null,
                tint = SecondaryText
            )
        }
    }
}

@Composable
private fun ToggleSettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(45.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(CardSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Orange)
            }

            Spacer(Modifier.width(13.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    color = PrimaryText,
                    fontWeight = FontWeight.Bold
                )
                Text(subtitle, color = SecondaryText, fontSize = 11.sp)
            }

            Switch(
                checked = checked,
                onCheckedChange = onChecked
            )
        }
    }
}

@Composable
private fun Avatar(name: String, size: Int = 46) {
    Box(
        modifier = Modifier.size(size.dp)
            .clip(CircleShape)
            .background(Orange),
        contentAlignment = Alignment.Center
    ) {
        Text(
            name.take(1).uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = if (size > 60) 27.sp else 17.sp
        )
    }
}

@Composable
private fun EventPreviewCard(event: AdminEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Orange),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Event, null, tint = Color.White)
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    event.category,
                    color = Orange,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    event.title,
                    color = PrimaryText,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${event.date} • ${event.time}",
                    color = SecondaryText,
                    fontSize = 11.sp
                )
                Text(event.venue, color = SecondaryText, fontSize = 11.sp)
            }

            StatusBadge(adminEventStatus(event))
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val color = when (status) {
        "Upcoming" -> Orange
        "Ongoing" -> Green
        "End time not set" -> Orange
        "Draft" -> Orange
        else -> SecondaryText
    }

    Box(
        modifier = Modifier.clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(status, color = color, fontSize = 9.sp)
    }
}

@Composable
private fun DarkTextField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    enabled: Boolean = true
) {
    OutlinedTextField(
        enabled = enabled,
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        colors = darkFieldColors(),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun EmptyState(title: String, message: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 45.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Inbox,
            null,
            tint = SecondaryText,
            modifier = Modifier.size(45.dp)
        )
        Spacer(Modifier.height(10.dp))
        Text(
            title,
            color = PrimaryText,
            fontWeight = FontWeight.Bold
        )
        Text(
            message,
            color = SecondaryText,
            textAlign = TextAlign.Center,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun darkFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = CardSoft,
    unfocusedContainerColor = CardSoft,
    disabledContainerColor = CardSoft,
    focusedBorderColor = Orange,
    unfocusedBorderColor = Border,
    focusedTextColor = PrimaryText,
    unfocusedTextColor = PrimaryText,
    disabledTextColor = SecondaryText,
    focusedLabelColor = Orange,
    unfocusedLabelColor = SecondaryText,
    cursorColor = Orange
)
private data class AdminLeadership(
    val id: String, val name: String, val role: String,
    val membershipId: String = "", val userId: String = "",
    val photoUrl: String = "", val bio: String = "", val order: Long = 0L
)

@Composable
private fun LeadershipPhoto(name: String, photoUrl: String, size: androidx.compose.ui.unit.Dp = 56.dp) {
    var failed by remember(photoUrl) { mutableStateOf(false) }
    Box(Modifier.size(size).clip(CircleShape).background(CardSoft), contentAlignment = Alignment.Center) {
        Text(name.trim().split(Regex("\\s+")).take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString(""),
            color = PrimaryText, fontWeight = FontWeight.Bold)
        if (photoUrl.isNotBlank() && !failed) {
            AsyncImage(model = photoUrl, contentDescription = "$name profile photo",
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
                onError = { failed = true })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LeadershipEditorSheet(clubId: String, members: List<AdminMember>, onDismiss: () -> Unit) {
    val db = remember { FirebaseFirestore.getInstance() }
    val collection = remember(clubId) { db.collection("clubs").document(clubId).collection("leadership") }
    val suggestions = listOf("Chairperson", "Deputy Chairperson", "Secretary", "Deputy Secretary", "Treasurer", "Public Relations Officer", "Media")
    var leaders by remember(clubId) { mutableStateOf(emptyList<AdminLeadership>()) }
    var loading by remember(clubId) { mutableStateOf(true) }
    var busy by remember(clubId) { mutableStateOf(false) }
    var error by remember(clubId) { mutableStateOf<String?>(null) }
    var retry by remember { mutableIntStateOf(0) }
    var editing by remember { mutableStateOf(false) }
    var editingId by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf("") }
    var position by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var order by remember { mutableStateOf("0") }
    var membershipId by remember { mutableStateOf("") }
    var linkedUserId by remember { mutableStateOf("") }
    var memberMenu by remember { mutableStateOf(false) }
    var positionMenu by remember { mutableStateOf(false) }
    var removal by remember { mutableStateOf<AdminLeadership?>(null) }
    var confirmationMessage by remember { mutableStateOf<String?>(null) }
    var photoFailed by remember(photoUrl) { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    LaunchedEffect(editing, editingId) { listState.scrollToItem(0) }
    fun startEditor(leader: AdminLeadership?) {
        editingId = leader?.id; name = leader?.name.orEmpty(); position = leader?.role.orEmpty()
        photoUrl = leader?.photoUrl.orEmpty(); bio = leader?.bio.orEmpty()
        order = (leader?.order ?: ((leaders.maxOfOrNull { it.order } ?: -1L) + 1).coerceAtMost(999L)).toString()
        membershipId = leader?.membershipId.orEmpty(); linkedUserId = leader?.userId.orEmpty()
        error = null; confirmationMessage = null; editing = true
    }
    DisposableEffect(collection, retry) {
        var active = true
        loading = true
        val listener = collection.whereEqualTo("isActive", true).addSnapshotListener { snapshot, failure ->
            if (active) {
                loading = false
                if (failure != null) error = failure.message ?: "Unable to load leadership."
                else {
                    error = null
                    leaders = snapshot?.documents?.map {
                        AdminLeadership(it.id, it.getString("name").orEmpty(), it.getString("role").orEmpty(),
                            it.getString("membershipId").orEmpty(), it.getString("userId").orEmpty(),
                            it.getString("photoUrl").orEmpty(), it.getString("bio").orEmpty(), it.getLong("order") ?: 0L)
                    }?.sortedWith(compareBy<AdminLeadership> { it.order }.thenBy { it.name }) ?: emptyList()
                }
            }
        }
        onDispose { active = false; listener.remove() }
    }
    ModalBottomSheet(onDismissRequest = { if (!busy) onDismiss() }, sheetState = sheetState, containerColor = CardBg) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxWidth().imePadding(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Text(if (editing) { if (editingId == null) "Add leader" else "Edit leader" } else "Club leadership",
                    color = PrimaryText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(if (editing) "Public details shown on your club page." else "Manage the people and positions shown on your club page.", color = SecondaryText)
            }
            error?.let { message -> item {
                Text(message, color = Danger)
                if (!editing) TextButton(onClick = { retry++ }, enabled = !busy) { Text("Retry", color = Orange) }
            } }
            confirmationMessage?.let { message -> item { Text(message, color = Green) } }
            if (!editing) {
                item {
                    Button(onClick = { startEditor(null) }, enabled = !loading && !busy,
                        modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Orange)) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                        Spacer(Modifier.width(8.dp)); Text("Add leader", color = Color.Black)
                    }
                }
                if (loading) item { CircularProgressIndicator(color = Orange) }
                if (!loading && leaders.isEmpty()) item { EmptyState("No leaders added", "Tap Add leader to enter a name and position.") }
                items(leaders, key = { it.id }) { leader ->
                    Card(colors = CardDefaults.cardColors(containerColor = CardSoft)) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LeadershipPhoto(leader.name, leader.photoUrl)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(leader.name, color = PrimaryText, fontWeight = FontWeight.Bold)
                                    Text(leader.role, color = Orange)
                                }
                            }
                            if (leader.bio.isNotBlank()) { Spacer(Modifier.height(8.dp)); Text(leader.bio, color = SecondaryText, maxLines = 2) }
                            Row {
                                TextButton(enabled = !busy, onClick = { startEditor(leader) }) { Text("Edit details", color = Orange) }
                                TextButton(enabled = !busy, onClick = { removal = leader }) { Text("Remove", color = Danger) }
                            }
                        }
                    }
                }
                item { TextButton(enabled = !busy, onClick = onDismiss) { Text("Done", color = Orange) } }
            } else {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LeadershipPhoto(name.ifBlank { "Leader" }, photoUrl.trim(), 72.dp)
                        Spacer(Modifier.width(12.dp))
                        Text("Profile photo\nOptional", color = SecondaryText)
                    }
                }
                item {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full name *") },
                        enabled = !busy, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = darkFieldColors())
                }
                item {
                    Box {
                        TextButton(enabled = !busy, onClick = { memberMenu = true }) {
                            Icon(Icons.Default.People, contentDescription = null, tint = Orange)
                            Spacer(Modifier.width(8.dp)); Text("Choose a club member (optional)", color = Orange)
                        }
                        DropdownMenu(expanded = memberMenu, onDismissRequest = { memberMenu = false },
                            modifier = Modifier.heightIn(max = 280.dp).background(CardSoft)) {
                            DropdownMenuItem(text = { Text("Enter details manually", color = PrimaryText) }, onClick = {
                                membershipId = ""; linkedUserId = ""; memberMenu = false
                            })
                            members.filter { it.name != "Profile not synced" }.sortedBy { it.name.lowercase() }.forEach { member ->
                                DropdownMenuItem(text = { Text(member.name, color = PrimaryText) }, onClick = {
                                    name = member.name; membershipId = member.id; linkedUserId = member.userId; memberMenu = false
                                })
                            }
                        }
                    }
                    Text(if (membershipId.isBlank()) "You can add a leader without a CampusPulse account." else "Linked to a club member. These public details do not change their student account.",
                        color = SecondaryText, fontSize = 12.sp)
                }
                item {
                    OutlinedTextField(value = position, onValueChange = { position = it }, label = { Text("Position *") },
                        enabled = !busy, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = darkFieldColors())
                    Box {
                        TextButton(enabled = !busy, onClick = { positionMenu = true }) { Text("Choose a common position", color = Orange) }
                        DropdownMenu(expanded = positionMenu, onDismissRequest = { positionMenu = false }, modifier = Modifier.background(CardSoft)) {
                            suggestions.forEach { role -> DropdownMenuItem(text = { Text(role, color = PrimaryText) }, onClick = { position = role; positionMenu = false }) }
                        }
                    }
                }
                item {
                    OutlinedTextField(value = photoUrl, onValueChange = { photoUrl = it }, label = { Text("Photo link (optional)") },
                        supportingText = { Text("Paste a direct HTTPS image link. Leave blank for initials.", color = SecondaryText) },
                        enabled = !busy, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = darkFieldColors())
                    if (photoUrl.trim().startsWith("https://")) {
                        AsyncImage(model = photoUrl.trim(), contentDescription = "Photo preview", contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(16.dp)),
                            onError = { photoFailed = true }, onSuccess = { photoFailed = false })
                        if (photoFailed) Text("The photo could not load. Check that this link opens an image without signing in.", color = Danger, fontSize = 12.sp)
                    }
                }
                item {
                    OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Short introduction (optional)") },
                        supportingText = { Text("${bio.length}/500 characters", color = SecondaryText) },
                        enabled = !busy, minLines = 2, maxLines = 4, modifier = Modifier.fillMaxWidth(), colors = darkFieldColors())
                }
                item {
                    OutlinedTextField(value = order, onValueChange = { order = it }, label = { Text("Display order") },
                        supportingText = { Text("0–999. Lower numbers appear first.", color = SecondaryText) },
                        enabled = !busy, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = darkFieldColors())
                }
                item {
                    val photo = photoUrl.trim()
                    val photoValid = photo.isBlank() || (photo.length <= 2048 && photo.startsWith("https://") && !Uri.parse(photo).host.isNullOrBlank() && !photo.contains(Regex("\\s")))
                    val valid = name.trim().length in 2..80 && position.trim().length in 2..100 && bio.trim().length <= 500 && (order.toIntOrNull() ?: -1) in 0..999 && photoValid
                    Button(enabled = !busy && !loading && valid, modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Orange, disabledContainerColor = Border, disabledContentColor = SecondaryText), onClick = {
                            val member = members.firstOrNull { it.id == membershipId && it.userId == linkedUserId }
                            if (membershipId.isNotBlank() && member == null) {
                                error = "The linked member is no longer active. Choose another member or select Enter details manually."
                                return@Button
                            }
                            if (leaders.any { it.role.equals(position.trim(), ignoreCase = true) && it.id != editingId }) {
                                error = "This position is already assigned. Edit that leader or use a different position."
                                return@Button
                            }
                            busy = true; error = null
                            val id = editingId ?: UUID.randomUUID().toString()
                            val data = mapOf("name" to name.trim(), "role" to position.trim(), "photoUrl" to photo,
                                "bio" to bio.trim(), "order" to (order.toIntOrNull() ?: 0), "isActive" to true,
                                "membershipId" to membershipId, "userId" to linkedUserId, "updatedAt" to FieldValue.serverTimestamp())
                            collection.document(id).set(data).addOnSuccessListener {
                                busy = false; editing = false; editingId = null; confirmationMessage = "Leadership details saved."
                            }.addOnFailureListener { busy = false; error = it.message ?: "Unable to save leadership details." }
                        }) { Text(if (busy) "Saving…" else "Save leader") }
                    if (!valid) Text("Enter a name, position and display order from 0 to 999. Photo links must use HTTPS; introductions can have up to 500 characters.", color = SecondaryText, fontSize = 12.sp)
                    TextButton(enabled = !busy, onClick = { editing = false; editingId = null; error = null }) { Text("Cancel", color = Orange) }
                }
            }
        }
    }
    removal?.let { leader ->
        AlertDialog(onDismissRequest = { if (!busy) removal = null }, containerColor = CardBg,
            title = { Text("Remove leader?", color = PrimaryText) },
            text = { Text("Remove ${leader.name} as ${leader.role} from the club page? Their membership and admin access stay unchanged.", color = SecondaryText) },
            confirmButton = { TextButton(enabled = !busy, onClick = {
                busy = true; error = null
                collection.document(leader.id).delete().addOnSuccessListener { busy = false; removal = null; confirmationMessage = "Leader removed." }
                    .addOnFailureListener { busy = false; error = it.message; removal = null }
            }) { Text("Remove", color = Danger) } },
            dismissButton = { TextButton(enabled = !busy, onClick = { removal = null }) { Text("Cancel", color = Orange) } })
    }
}
