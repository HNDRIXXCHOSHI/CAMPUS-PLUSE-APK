package com.campuspulse.app.ui.screens

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.delay
import com.campuspulse.app.model.EventItem
import com.campuspulse.app.model.Category
import android.widget.Toast
import android.content.Intent
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.SubcomposeAsyncImage
import com.campuspulse.app.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val NoticeBg = Color(0xFF00254A)
private val NoticeCard = Color(0xFF052F55)
private val NoticeMuted = Color(0xFFC1D0DE)
private val NoticeOrange = Color(0xFFF7941D)
private val NoticeBlue = Color(0xFF38A9E0)

private data class NotificationContent(
    val id: String,
    val clubId: String,
    val clubName: String,
    val text: String,
    val published: Long?,
    val event: Boolean,
    val title: String = "",
    val eventDate: Long? = null,
    val eventEndDate: Long? = null,
    val venue: String = "",
    val image: String = "",
    val video: String = "",
    val thumbnail: String = ""
)

private fun DocumentSnapshot.stringValue(vararg names: String): String =
    names.firstNotNullOfOrNull {
        (get(it) as? String)?.trim()?.takeIf(String::isNotEmpty)
    } ?: ""

private fun DocumentSnapshot.notification(event: Boolean): NotificationContent {
    val kind = (stringValue("type") + " " + stringValue("mediaType")).uppercase()
    val video = stringValue("videoUrl", "postVideoUrl", "eventVideoUrl")
    val isVideo = kind.contains("VIDEO") || video.isNotBlank()
    val media = stringValue("mediaUrl")
    return NotificationContent(
        id = "${if (event) "event" else "post"}:$id",
        clubId = stringValue("clubId"),
        clubName = stringValue("clubName"),
        text = if (event) stringValue("description", "text")
        else stringValue("text", "caption"),
        published = getTimestamp("createdAt")?.toDate()?.time,
        event = event,
        title = if (event) stringValue("title") else "",
        eventDate = if (event) getTimestamp("eventDate")?.toDate()?.time else null,
        eventEndDate = if (event) getTimestamp("eventEndDate")?.toDate()?.time else null,
        venue = if (event) stringValue("location", "venue") else "",
        image = stringValue(
            "posterUrl", "posterImageUrl", "eventPosterUrl", "imageUrl", "postImageUrl", "eventImageUrl", "photoUrl"
        ).ifBlank { if (isVideo) "" else media },
        video = video.ifBlank { if (isVideo) media else "" },
        thumbnail = stringValue("thumbnailUrl", "videoThumbnailUrl", "posterUrl")
    )
}

@Composable
fun NotificationsScreen(
    scrollToTopSignal: Int = 0,
    onOpenClub: (String) -> Unit,
    onOpenEvent: (EventItem) -> Unit
) {
    val context = LocalContext.current
    val preview = LocalInspectionMode.current
    val auth = remember(preview) { if (preview) null else FirebaseAuth.getInstance() }
    val db = remember(preview) { if (preview) null else FirebaseFirestore.getInstance() }
    val prefs = remember(context) {
        context.getSharedPreferences("campuspulse_notification_state", 0)
    }
    var uid by remember(auth) { mutableStateOf(auth?.currentUser?.uid) }
    var clubNames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var followed by remember { mutableStateOf<Set<String>>(emptySet()) }
    var posts by remember { mutableStateOf<List<NotificationContent>>(emptyList()) }
    var events by remember { mutableStateOf<List<NotificationContent>>(emptyList()) }
    var pending by remember { mutableStateOf<Set<String>>(emptySet()) }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var retry by remember { mutableStateOf(0) }
    var menu by remember { mutableStateOf(false) }
    var clear by remember { mutableStateOf(false) }
    var unreadOnly by rememberSaveable { mutableStateOf(false) }
    var selectedId by remember(uid) { mutableStateOf<String?>(null) }
    var read by remember(uid) {
        mutableStateOf(if (uid == null) emptySet<String>() else
            prefs.getStringSet("${uid}_read", emptySet())?.toSet() ?: emptySet())
    }
    var hidden by remember(uid) {
        mutableStateOf(if (uid == null) emptySet<String>() else
            prefs.getStringSet("${uid}_hidden", emptySet())?.toSet() ?: emptySet())
    }
    var syncError by remember(uid) { mutableStateOf<String?>(null) }
    var stateCached by remember(uid) { mutableStateOf(true) }
    var pendingState by remember(uid) { mutableStateOf(false) }
    var syncRetry by remember { mutableStateOf(0) }
    fun saveNotificationState(user: String, readIds: Set<String>, hiddenIds: Set<String>) {
        val database = db ?: return
        val ids = (readIds + hiddenIds).filter { it.isNotBlank() && !it.contains("/") }
        ids.chunked(400).forEach { chunk ->
            val batch = database.batch()
            chunk.forEach { id ->
                val fields = mutableMapOf<String, Any>("updatedAt" to FieldValue.serverTimestamp())
                if (id in readIds) fields["read"] = true
                if (id in hiddenIds) fields["hidden"] = true
                batch.set(database.collection("users").document(user).collection("notificationState").document(id), fields, SetOptions.merge())
            }
            batch.commit().addOnFailureListener {
                if (auth?.currentUser?.uid == user) syncError = "Your changes are saved on this device, but could not sync. Check your connection and retry."
            }
        }
    }
    DisposableEffect(db, uid, syncRetry) {
        var active = true
        var migrated = false
        stateCached = true; pendingState = false; syncError = null
        val user = uid
        val listener = if (db != null && user != null) {
            db.collection("users").document(user).collection("notificationState")
                .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, failure ->
                    if (active) {
                        if (failure != null) syncError = "Unable to sync notification preferences. Check your connection, publish the updated rules and retry."
                        else if (snapshot != null) {
                            stateCached = snapshot.metadata.isFromCache
                            pendingState = snapshot.metadata.hasPendingWrites()
                            val remoteRead = snapshot.documents.filter { it.getBoolean("read") == true }.map { it.id }.toSet()
                            val remoteHidden = snapshot.documents.filter { it.getBoolean("hidden") == true }.map { it.id }.toSet()
                            read = read + remoteRead; hidden = hidden + remoteHidden
                            prefs.edit().putStringSet("${user}_read", read.toSet()).putStringSet("${user}_hidden", hidden.toSet()).apply()
                            if (!snapshot.metadata.isFromCache && !migrated) {
                                migrated = true
                                saveNotificationState(user, read - remoteRead, hidden - remoteHidden)
                            }
                        }
                    }
                }
        } else null
        onDispose { active = false; listener?.remove() }
    }
    val listState = rememberLazyListState()

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { uid = it.currentUser?.uid }
        auth?.addAuthStateListener(listener)
        onDispose { auth?.removeAuthStateListener(listener) }
    }
    DisposableEffect(db, uid, retry) {
        clubNames = emptyMap(); followed = emptySet()
        posts = emptyList(); events = emptyList()
        pending = emptySet(); errors = emptyMap()
        val user = uid
        if (db == null || user == null) return@DisposableEffect onDispose {}
        pending = setOf("clubs", "memberships", "posts", "events")
        val listeners = mutableListOf<ListenerRegistration>()
        fun complete(source: String, error: Exception?) {
            pending = pending - source
            errors = if (error == null) errors - source else
                errors + (source to (error.message ?: "Unable to load $source."))
        }
        listeners += db.collection("clubs").whereEqualTo("status", "APPROVED")
            .whereEqualTo("isActive", true).addSnapshotListener { snapshot, error ->
                clubNames = if (error == null) snapshot?.documents?.associate {
                    it.id to it.stringValue("name")
                } ?: emptyMap() else emptyMap()
                complete("clubs", error)
            }
        listeners += db.collection("clubMembers").whereEqualTo("userId", user)
            .whereEqualTo("status", "ACTIVE").addSnapshotListener { snapshot, error ->
                followed = if (error == null) snapshot?.documents?.mapNotNull {
                    it.stringValue("clubId").takeIf(String::isNotEmpty)
                }?.toSet() ?: emptySet() else emptySet()
                complete("memberships", error)
            }
        listeners += db.collection("posts").whereEqualTo("status", "PUBLISHED")
            .whereEqualTo("isActive", true).addSnapshotListener { snapshot, error ->
                posts = if (error == null) snapshot?.documents?.map {
                    it.notification(false)
                } ?: emptyList() else emptyList()
                complete("posts", error)
            }
        listeners += db.collection("events").whereEqualTo("status", "PUBLISHED")
            .whereEqualTo("isActive", true).addSnapshotListener { snapshot, error ->
                events = if (error == null) snapshot?.documents?.map {
                    it.notification(true)
                } ?: emptyList() else emptyList()
                complete("events", error)
            }
        onDispose { listeners.forEach { it.remove() } }
    }
    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > 0) { selectedId = null; listState.animateScrollToItem(0) }
    }
    val notices = remember(posts, events, followed, clubNames) {
        (posts + events).filter { it.clubId in followed && it.clubId in clubNames }
            .map { it.copy(clubName = clubNames[it.clubId].orEmpty().ifBlank { it.clubName }) }
            .distinctBy { it.id }.sortedByDescending { it.published ?: Long.MIN_VALUE }
    }
    val available = notices.filter { it.id !in hidden }
    val visible = available.filter { !unreadOnly || it.id !in read }
    val selected = available.firstOrNull { it.id == selectedId }
    fun markRead(ids: Set<String>) {
        val user = uid ?: return
        val added = ids - read
        read = read + ids
        prefs.edit().putStringSet("${user}_read", read.toSet()).apply()
        syncError = null
        saveNotificationState(user, added, emptySet())
    }
    fun share(item: NotificationContent) {
        val message = listOf(item.clubName, item.title, item.text,
            item.eventDate?.let { formatNoticeDate(it, "EEE, dd MMM yyyy • HH:mm") }.orEmpty(),
            item.venue, item.image, item.video).filter(String::isNotBlank).joinToString("\n\n")
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }, "Share update"))
    }
    BackHandler(selected != null) { selectedId = null }
    Column(Modifier.fillMaxSize().background(NoticeBg).safeDrawingPadding()) {
        Row(Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            if (selected != null) IconButton(onClick = { selectedId = null }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Text(if (selected == null) "Notifications" else if (selected.event) "Event" else "Post",
                color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f))
            if (selected != null) IconButton(onClick = { share(selected) }) {
                Icon(Icons.Default.Share, "Share update", tint = Color.White)
            } else Box {
                IconButton(onClick = { menu = true }) {
                    Icon(Icons.Default.MoreVert, "Notification options", tint = Color.White)
                }
                DropdownMenu(menu, { menu = false }, Modifier.background(NoticeCard)) {
                    DropdownMenuItem(text = { Text("Mark all as read", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.DoneAll, null, tint = NoticeOrange) },
                        onClick = {
                            val count = available.count { it.id !in read }
                            markRead(read + available.map { it.id }); menu = false
                            Toast.makeText(context, if (count == 0) "All notifications are already read." else "$count notifications marked as read.", Toast.LENGTH_SHORT).show()
                        })
                    DropdownMenuItem(text = { Text(if (unreadOnly) "Show all" else "Unread only (${available.count { it.id !in read }})", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.FilterList, null, tint = NoticeBlue) },
                        onClick = {
                            unreadOnly = !unreadOnly; selectedId = null; menu = false
                            Toast.makeText(context, if (unreadOnly) "Showing unread notifications only." else "Showing all notifications.", Toast.LENGTH_SHORT).show()
                        })
                    DropdownMenuItem(text = { Text("Clear notifications", color = NoticeOrange) },
                        leadingIcon = { Icon(Icons.Default.DeleteOutline, null, tint = NoticeOrange) },
                        onClick = { clear = true; menu = false })
                }
            }
        }
        HorizontalDivider(color = NoticeMuted.copy(alpha = 0.18f))
        if (uid != null && (syncError != null || stateCached || pendingState)) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(syncError ?: if (pendingState) "Changes are waiting to sync across devices."
                else "Showing saved preferences. Server confirmation is pending.", color = NoticeMuted, fontSize = 12.sp)
                if (syncError != null) TextButton(onClick = { syncRetry++ }) { Text("Retry sync", color = NoticeOrange) }
            }
        }
        when {
            selected != null -> key(selected.id) {
                NoticeDetail(selected,
                    onOpenClub = { onOpenClub(selected.clubId) },
                    onOpenEvent = { onOpenEvent(selected.toNoticeEvent()) },
                    onShare = { share(selected) })
            }
            uid == null -> NoticeEmpty("Sign in to see updates from clubs you follow.")
            pending.isNotEmpty() -> Box(Modifier.fillMaxWidth().weight(1f), Alignment.Center) {
                CircularProgressIndicator(color = NoticeOrange)
            }
            errors.isNotEmpty() -> Column(Modifier.fillMaxWidth().padding(24.dp)) {
                Text(errors.values.joinToString("\n"), color = NoticeMuted)
                TextButton(onClick = { retry++ }) { Text("Retry", color = NoticeOrange) }
            }
            visible.isEmpty() -> NoticeEmpty(if (unreadOnly) "You're all caught up."
            else "No updates yet.\nPosts and events from clubs you follow will appear here.")
            else -> LazyColumn(state = listState, contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)) {
                listOf("Today", "Yesterday", "Earlier", "Date unavailable").forEach { group ->
                    val grouped = visible.filter { noticeGroup(it.published) == group }
                    if (grouped.isNotEmpty()) {
                        item(key = "group:$group") {
                            Text(
                                group,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth()
                                    .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 12.dp)
                            )
                        }
                        items(grouped, key = { it.id }) { item ->
                            NoticePreview(item, item.id !in read) {
                                markRead(read + item.id); selectedId = item.id
                            }
                        }
                    }
                }
            }
        }
    }
    if (clear) AlertDialog(onDismissRequest = { clear = false }, containerColor = NoticeCard,
        title = { Text("Clear notifications?", color = Color.White) },
        text = { Text("Hide current updates across your signed-in devices? New posts and events will still appear. Offline changes sync when you reconnect.", color = NoticeMuted) },
        confirmButton = { TextButton(onClick = {
            uid?.let { user ->
                val count = available.size
                val clearedIds = available.map { it.id }.toSet()
                hidden = hidden + clearedIds
                syncError = null
                saveNotificationState(user, emptySet(), clearedIds)
                selectedId = null
                prefs.edit().putStringSet("${user}_hidden", hidden.toSet()).apply()
                Toast.makeText(context, "$count notifications hidden. Changes will sync when connected.", Toast.LENGTH_SHORT).show()
            }; clear = false
        }) { Text("Clear", color = NoticeOrange) } },
        dismissButton = { TextButton(onClick = { clear = false }) { Text("Cancel", color = Color.White) } })
}

@Composable
private fun NoticePreview(item: NotificationContent, unread: Boolean, onClick: () -> Unit) {
    // Full-width notification rows. Existing detail navigation is kept on the whole row.
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = if (unread) NoticeCard else NoticeBg
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 15.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                NoticeTypeIcon(item.event)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.clubName,
                    color = Color.White,
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    fontWeight = if (unread) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    if (item.event) item.title.ifBlank { "New event" }
                    else item.text.ifBlank {
                        if (item.video.isNotBlank()) "New video shared"
                        else if (item.image.isNotBlank()) "New image shared"
                        else "New post shared"
                    },
                    color = NoticeMuted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (unread) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(NoticeOrange))
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        "${if (item.event) "Event" else if (item.video.isNotBlank()) "Video post" else "Post"} • ${item.published?.let { formatNoticeDate(it, "dd MMM • HH:mm") } ?: "Date unavailable"}",
                        color = NoticeMuted,
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (item.image.isNotBlank() || item.video.isNotBlank()) {
                Spacer(Modifier.width(10.dp))
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    NoticeImage(item, preview = true, modifier = Modifier.fillMaxSize())
                    if (item.video.isNotBlank()) {
                        Surface(color = NoticeBg.copy(alpha = 0.8f), shape = CircleShape) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Video preview",
                                tint = Color.White,
                                modifier = Modifier.padding(3.dp).size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoticeDetail(item: NotificationContent, onOpenClub: () -> Unit, onOpenEvent: () -> Unit, onShare: () -> Unit) {
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(item.id) { while (true) { now = System.currentTimeMillis(); delay(1000) } }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        .padding(16.dp).padding(bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            NoticeTypeIcon(item.event)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(item.clubName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(item.published?.let { formatNoticeDate(it, "dd MMM yyyy • HH:mm") }
                    ?: "Publication date unavailable", color = NoticeMuted, fontSize = 12.sp)
            }
        }
        if (item.video.isNotBlank()) NoticeVideo(item.video)
        else if (item.event || item.image.isNotBlank()) {
            NoticeImage(item, false, Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(20.dp)))
        }
        if (item.event) {
            Text(item.title.ifBlank { "Event" }, color = Color.White, fontSize = 26.sp,
                lineHeight = 32.sp, fontWeight = FontWeight.Bold)
            Surface(color = NoticeCard, shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    NoticeMeta(Icons.Default.Event, "Status", noticeEventStatus(item, now))
                    NoticeMeta(Icons.Default.Event, "Date", item.eventDate?.let {
                        formatNoticeDate(it, "EEEE, dd MMM yyyy") } ?: "To be confirmed")
                    NoticeMeta(Icons.Default.Schedule, "Time", item.eventDate?.let {
                        formatNoticeDate(it, "HH:mm") } ?: "To be confirmed")
                    NoticeMeta(Icons.Default.Schedule, "Ends", item.eventEndDate?.let {
                        formatNoticeDate(it, "EEEE, dd MMM yyyy • HH:mm") } ?: "End time not set")
                    if (item.venue.isNotBlank()) NoticeMeta(Icons.Default.LocationOn, "Venue", item.venue)
                }
            }
        }
        if (item.text.isNotBlank()) {
            Text(if (item.event) "About this event" else "Post", color = Color.White,
                fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(item.text, color = Color.White, fontSize = 16.sp, lineHeight = 24.sp)
        }
        if (item.event) Button(onClick = onOpenEvent, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NoticeOrange)) {
            Text("View Event", color = NoticeBg)
        }
        OutlinedButton(onClick = onOpenClub, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text("View Club", color = NoticeOrange)
        }
        OutlinedButton(onClick = onShare, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Icon(Icons.Default.Share, null, tint = NoticeOrange)
            Spacer(Modifier.width(8.dp))
            Text("Share ${if (item.event) "event" else "post"}", color = NoticeOrange)
        }
    }
}

@Composable
private fun NoticeTypeIcon(event: Boolean) {
    val accent = if (event) NoticeOrange else NoticeBlue
    Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(accent.copy(alpha = 0.15f)), Alignment.Center) {
        Icon(if (event) Icons.Default.Event else Icons.Default.Article, null, tint = accent)
    }
}

@Composable
private fun NoticeMeta(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = NoticeOrange, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, color = NoticeMuted, fontSize = 11.sp)
            Text(value, color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
private fun NoticeImage(item: NotificationContent, preview: Boolean, modifier: Modifier) {
    val url = if (item.video.isNotBlank()) item.thumbnail.ifBlank { item.image } else item.image
    Box(modifier.background(NoticeCard), Alignment.Center) {
        if (url.isBlank()) {
            Text(if (item.event) "No poster saved for this event" else "No image saved for this post", color = NoticeMuted)
        }
        else SubcomposeAsyncImage(model = url, contentDescription = if (item.event) "Event poster" else "Post image",
            modifier = Modifier.fillMaxSize(), contentScale = if (preview) ContentScale.Crop else ContentScale.Fit,
            loading = { Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(Modifier.size(24.dp), color = NoticeOrange, strokeWidth = 2.dp)
            } }, error = { NoticeImageFallback(item) })
    }
}

@Composable
private fun NoticeImageFallback(item: NotificationContent) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Text(if (item.video.isNotBlank()) "Video" else if (item.event) "Event poster unavailable" else "Image unavailable", color = NoticeMuted)
    }
}

@Suppress("DEPRECATION")
@Composable
private fun NoticeVideo(url: String) {
    val owner = LocalLifecycleOwner.current
    var player by remember(url) { mutableStateOf<VideoView?>(null) }
    var play by remember(url) { mutableStateOf(false) }
    var error by remember(url) { mutableStateOf(false) }
    DisposableEffect(player, owner) {
        val current = player
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) current?.pause()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); current?.stopPlayback() }
    }
    Box(Modifier.fillMaxWidth().height(270.dp).clip(RoundedCornerShape(20.dp)).background(Color.Black), Alignment.Center) {
        when {
            error -> Text("Video unavailable. Check its URL and format.", color = Color.White,
                textAlign = TextAlign.Center, modifier = Modifier.padding(20.dp))
            play -> AndroidView(modifier = Modifier.fillMaxSize(), factory = { context ->
                VideoView(context).apply {
                    val controls = MediaController(context)
                    controls.setAnchorView(this); setMediaController(controls)
                    setOnPreparedListener { start(); controls.show() }
                    setOnErrorListener { _, _, _ -> error = true; true }
                    setVideoURI(Uri.parse(url)); player = this
                }
            })
            else -> Button(onClick = { play = true }, colors = ButtonDefaults.buttonColors(containerColor = NoticeOrange)) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.Black)
                Text("Play video", color = Color.Black)
            }
        }
    }
}

@Composable
private fun NoticeEmpty(message: String) {
    Column(Modifier.fillMaxSize().padding(30.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        Icon(Icons.Default.Notifications, null, tint = NoticeBlue, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(16.dp))
        Text(message, color = NoticeMuted, textAlign = TextAlign.Center, fontSize = 15.sp)
    }
}

private fun formatNoticeDate(value: Long, pattern: String): String =
    SimpleDateFormat(pattern, Locale.getDefault()).format(Date(value))

private fun noticeGroup(value: Long?): String {
    if (value == null) return "Date unavailable"
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val yesterday = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
    return when {
        value >= today.timeInMillis -> "Today"
        value >= yesterday.timeInMillis -> "Yesterday"
        else -> "Earlier"
    }
}

private fun noticeEventStatus(item: NotificationContent, now: Long): String {
    val start = item.eventDate
    val end = item.eventEndDate
    return when {
        start == null -> "Schedule not set"
        now < start -> "Upcoming"
        end == null || end <= start -> "End time not set"
        now < end -> "Ongoing"
        else -> "Passed"
    }
}

private fun NotificationContent.toNoticeEvent(): EventItem = EventItem(
    id = id.removePrefix("event:"), title = title,
    category = Category.ACADEMIC, hostClub = clubName,
    schedule = eventDate?.let { formatNoticeDate(it, "EEE, dd MMM yyyy • HH:mm") }.orEmpty(),
    venue = venue, description = text, rating = 0.0, going = 0, isFree = true
)
