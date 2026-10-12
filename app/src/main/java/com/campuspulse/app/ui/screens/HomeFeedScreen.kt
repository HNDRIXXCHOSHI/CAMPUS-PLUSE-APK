package com.campuspulse.app.ui.screens

import android.content.res.Configuration
import android.net.Uri
import android.widget.VideoView
import android.widget.MediaController
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.CircularProgressIndicator
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Source
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.delay
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.campuspulse.app.R
import com.campuspulse.app.data.FirestoreClub
import com.campuspulse.app.data.FirestoreEvent
import com.campuspulse.app.data.FirestorePost
import com.campuspulse.app.data.HomeRepository
import com.campuspulse.app.model.Category
import com.campuspulse.app.model.EventItem
import com.campuspulse.app.ui.components.CategoryChip
import com.campuspulse.app.ui.components.NavDestination
import com.campuspulse.app.ui.components.shareEvent
import com.campuspulse.app.ui.theme.CampusPulseTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ============================================================================
// COLOURS
// ============================================================================

// MAIN DARK MODE COLOUR — UMP NAVY
private val DarkBackground = Color(0xFF021D38)

// NAVY SURFACES — lighter than the page background for separation
private val SurfaceDark = Color(0xFF05294B)
private val SurfaceSoft = Color(0xFF0D426F)

private val TextWhite = Color.White
private val TextMuted = Color(0xFFFFFFFF)

// UMP-INSPIRED CAMPUS PULSE ACCENTS
private val AccentPurple = Color(0xFFFF5722)
private val AccentPink = Color(0xFFE11111)
private val AccentRed = Color(0xFFF7941D)
private val AccentBlue = Color(0xFF38A9E0)

private val GradientPurple = Color(0xFF00254A)
private val GradientRed = Color(0xFFF7941D)

private val BorderColor = Color(0xFF315574)

// ============================================================================
// WEEK DATA
// ============================================================================

private val weekDays = listOf(
    "Mon",
    "Tue",
    "Wed",
    "Thu",
    "Fri",
    "Sat",
    "Sun"
)

private fun currentWeekDates(): List<Date> {
    val calendar = Calendar.getInstance()

    val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
    val daysFromMonday = when (currentDay) {
        Calendar.SUNDAY -> 6
        else -> currentDay - Calendar.MONDAY
    }

    calendar.add(Calendar.DAY_OF_MONTH, -daysFromMonday)

    return List(7) { index ->
        (calendar.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, index)
        }.time
    }
}

private fun currentDayIndex(): Int {
    return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 0
        Calendar.TUESDAY -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3
        Calendar.FRIDAY -> 4
        Calendar.SATURDAY -> 5
        Calendar.SUNDAY -> 6
        else -> 0
    }
}

// ============================================================================
// FEED MODEL
// ============================================================================

private sealed class FeedItem {

    abstract val id: String
    abstract val dayIndex: Int
    open val dateKey: String = ""
    open val sortTime: Long = Long.MIN_VALUE

    data class Event(
        val event: EventItem,
        override val dayIndex: Int,
        val clubId: String = "",
        val endTimeMillis: Long? = null,
        val posterImageUrl: String = "",
        override val dateKey: String = "",
        override val sortTime: Long = Long.MIN_VALUE
    ) : FeedItem() {
        override val id: String
            get() = "event_${event.id}"
    }

    data class TextPost(
        override val id: String,
        override val dayIndex: Int,
        val clubName: String,
        val initials: String,
        val time: String,
        val date: String = "",
        val text: String,
        val clubId: String = "",
        val imageUrl: String = "",
        override val dateKey: String = "",
        override val sortTime: Long = Long.MIN_VALUE
    ) : FeedItem()

    data class PhotoPost(
        override val id: String,
        override val dayIndex: Int,
        val clubName: String,
        val initials: String,
        val time: String,
        val date: String = "",
        val caption: String,
        val clubId: String = "",
        val mediaUrl: String = "",
        val thumbnailUrl: String = "",
        override val dateKey: String = "",
        override val sortTime: Long = Long.MIN_VALUE
    ) : FeedItem()

    data class VideoPost(
        override val id: String,
        override val dayIndex: Int,
        val clubName: String,
        val initials: String,
        val time: String,
        val date: String = "",
        val caption: String,
        val clubId: String = "",
        val mediaUrl: String = "",
        val thumbnailUrl: String = "",
        override val dateKey: String = "",
        override val sortTime: Long = Long.MIN_VALUE
    ) : FeedItem()
}

// ============================================================================
// EVENT DAY HELPER
// ============================================================================

private fun EventItem.dayIndex(): Int {

    return weekDays.indexOfFirst { day ->

        schedule.startsWith(
            day,
            ignoreCase = true
        )
    }
}

// ============================================================================
// FIRESTORE FEED HELPERS
// ============================================================================

private fun firestoreDayIndex(timestamp: com.google.firebase.Timestamp?): Int {
    if (timestamp == null) return 2

    val calendar = Calendar.getInstance().apply {
        time = timestamp.toDate()
    }

    return when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 0
        Calendar.TUESDAY -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3
        Calendar.FRIDAY -> 4
        Calendar.SATURDAY -> 5
        Calendar.SUNDAY -> 6
        else -> 2
    }
}

private fun feedDateKey(date: Date): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)

private fun firestoreTime(timestamp: com.google.firebase.Timestamp?): String {
    if (timestamp == null) return ""
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp.toDate())
}

private fun firestoreEventSchedule(timestamp: com.google.firebase.Timestamp?): String {
    if (timestamp == null) return "Date to be confirmed · Time to be confirmed"
    return SimpleDateFormat("EEE, d MMM · HH:mm", Locale.getDefault()).format(timestamp.toDate())
}

private fun FirestorePost.toFeedItem(): FeedItem {
    val label = clubName.ifBlank { "Campus Club" }
    val short = clubShortName.ifBlank {
        label.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1).uppercase() }
    }
    val day = firestoreDayIndex(createdAt)
    val key = createdAt?.toDate()?.let(::feedDateKey).orEmpty()
    val sort = createdAt?.toDate()?.time ?: Long.MIN_VALUE
    val clock = firestoreTime(createdAt)
    val dateLabel = createdAt?.toDate()?.let {
        SimpleDateFormat("EEEE, d MMM yyyy", Locale.getDefault()).format(it)
    }.orEmpty()
    return when {
        type.uppercase().contains("VIDEO") || videoUrl.isNotBlank() -> FeedItem.VideoPost(
            id = "firestore_post_$id", dayIndex = day, clubName = label, initials = short,
            time = clock, date = dateLabel, caption = text, clubId = clubId,
            mediaUrl = videoUrl, thumbnailUrl = thumbnailUrl, dateKey = key, sortTime = sort)
        imageUrl.isNotBlank() || type.uppercase().contains("PHOTO") || type.uppercase().contains("IMAGE") -> FeedItem.PhotoPost(
            id = "firestore_post_$id", dayIndex = day, clubName = label, initials = short,
            time = clock, date = dateLabel, caption = text, clubId = clubId,
            mediaUrl = imageUrl, dateKey = key, sortTime = sort)
        else -> FeedItem.TextPost(
            id = "firestore_post_$id", dayIndex = day, clubName = label, initials = short,
            time = clock, date = dateLabel, text = text, clubId = clubId, dateKey = key, sortTime = sort)
    }
}

private fun FirestoreEvent.toFeedItem(): FeedItem.Event {
    val eventItem = EventItem(
        id = id,
        title = title,
        category = Category.ACADEMIC,
        hostClub = clubName.ifBlank { "Campus Club" },
        schedule = firestoreEventSchedule(eventDate),
        venue = location,
        description = description,
        rating = 0.0,
        going = 0,
        isFree = true
    )

    return FeedItem.Event(
        event = eventItem,
        clubId = clubId,
        endTimeMillis = eventEndDate?.toDate()?.time,
        posterImageUrl = posterImageUrl,
        dayIndex = firestoreDayIndex(eventDate),
        dateKey = eventDate?.toDate()?.let(::feedDateKey).orEmpty(),
        sortTime = eventDate?.toDate()?.time ?: Long.MIN_VALUE
    )
}

// ============================================================================
// HOME SCREEN
// ============================================================================

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CampusPulseHomeScreen(
    onOpenEvent: (EventItem) -> Unit,
    onNavigateToClubHome: (String) -> Unit,
    onNavigate: (NavDestination) -> Unit,
    scrollToTopSignal: Int = 0,
    onOpenCalendar: () -> Unit = {}
) {

    var selectedDayIndex by remember {
        mutableIntStateOf(currentDayIndex())
    }

    var todayKey by remember { mutableStateOf(feedDateKey(Date())) }
    LaunchedEffect(Unit) {
        while (true) {
            val newKey = feedDateKey(Date())
            if (newKey != todayKey) {
                todayKey = newKey
                selectedDayIndex = currentDayIndex()
            }
            delay(1000)
        }
    }
    val weekDates = remember(todayKey) { currentWeekDates() }

    var clubsExpanded by remember { mutableStateOf(false) }
    val feedListState = rememberLazyListState()
    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > 0) feedListState.animateScrollToItem(0)
    }

    // ------------------------------------------------------------------------
    // FIRESTORE HOME DATA
    // ------------------------------------------------------------------------

    val isPreview = LocalInspectionMode.current
    val homeRepository = if (isPreview) null else remember { HomeRepository() }

    val auth = remember(isPreview) {
        if (isPreview) null else FirebaseAuth.getInstance()
    }
    val membershipDb = remember(isPreview) {
        if (isPreview) null else FirebaseFirestore.getInstance()
    }
    var currentUserId by remember(auth) { mutableStateOf(auth?.currentUser?.uid) }
    var joinedClubIds by remember(currentUserId) { mutableStateOf(emptySet<String>()) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { currentUserId = it.currentUser?.uid }
        auth?.addAuthStateListener(listener)
        onDispose { auth?.removeAuthStateListener(listener) }
    }
    DisposableEffect(membershipDb, currentUserId) {
        joinedClubIds = emptySet()
        val uid = currentUserId
        val listener = if (membershipDb != null && uid != null) {
            membershipDb.collection("clubMembers")
                .whereEqualTo("userId", uid)
                .whereEqualTo("status", "ACTIVE")
                .addSnapshotListener { snapshot, error ->
                    joinedClubIds = if (error == null) {
                        snapshot?.documents?.mapNotNull {
                            it.getString("clubId")?.takeIf(String::isNotBlank)
                        }?.toSet() ?: emptySet()
                    } else emptySet()
                }
        } else null
        onDispose { listener?.remove() }
    }

    var firestorePosts by remember {
        mutableStateOf<List<FirestorePost>>(emptyList())
    }

    var firestoreEvents by remember {
        mutableStateOf<List<FirestoreEvent>>(emptyList())
    }

    var firestoreClubs by remember {
        mutableStateOf<List<FirestoreClub>>(emptyList())
    }

    var postsLoading by remember { mutableStateOf(!isPreview) }
    var eventsLoading by remember { mutableStateOf(!isPreview) }
    var clubsLoading by remember { mutableStateOf(!isPreview) }
    var postsError by remember { mutableStateOf(false) }
    var eventsError by remember { mutableStateOf(false) }
    var clubsError by remember { mutableStateOf(false) }
    var retryGeneration by remember { mutableIntStateOf(0) }

    var cachedSources by remember { mutableStateOf(emptySet<String>()) }
    DisposableEffect(homeRepository, retryGeneration) {
        if (homeRepository == null) {
            return@DisposableEffect onDispose {}
        }

        postsLoading = true
        eventsLoading = true
        clubsLoading = true
        postsError = false
        eventsError = false
        clubsError = false
        val postsListener = homeRepository.listenToPosts(
            onCacheState = { cached -> cachedSources = if (cached) cachedSources + "posts" else cachedSources - "posts" },
            onSuccess = { posts ->
                firestorePosts = posts
                postsLoading = false
                postsError = false
            },
            onError = {
                postsLoading = false
                postsError = true
            }
        )

        val eventsListener = homeRepository.listenToEvents(
            onCacheState = { cached -> cachedSources = if (cached) cachedSources + "events" else cachedSources - "events" },
            onSuccess = { events ->
                firestoreEvents = events
                eventsLoading = false
                eventsError = false
            },
            onError = {
                eventsLoading = false
                eventsError = true
            }
        )

        val clubsListener = homeRepository.listenToClubs(
            onCacheState = { cached -> cachedSources = if (cached) cachedSources + "clubs" else cachedSources - "clubs" },
            onSuccess = { clubs ->
                firestoreClubs = clubs
                clubsLoading = false
                clubsError = false
            },
            onError = {
                clubsLoading = false
                clubsError = true
            }
        )

        onDispose {
            postsListener.remove()
            eventsListener.remove()
            clubsListener.remove()
        }
    }

    var refreshing by remember { mutableStateOf(false) }
    var refreshError by remember { mutableStateOf<String?>(null) }
    var refreshGeneration by remember { mutableIntStateOf(0) }
    val refresh: () -> Unit = {
        val db = membershipDb
        if (!refreshing && db != null) {
            refreshing = true
            refreshError = null
            refreshGeneration += 1
            val generation = refreshGeneration
            val requests = mutableListOf(
                db.collection("posts").whereEqualTo("status", "PUBLISHED").whereEqualTo("isActive", true).get(Source.SERVER),
                db.collection("events").whereEqualTo("status", "PUBLISHED").whereEqualTo("isActive", true).get(Source.SERVER),
                db.collection("clubs").whereEqualTo("status", "APPROVED").whereEqualTo("isActive", true).get(Source.SERVER)
            )
            currentUserId?.let { uid ->
                requests.add(db.collection("clubMembers").whereEqualTo("userId", uid).whereEqualTo("status", "ACTIVE").get(Source.SERVER))
            }
            Tasks.whenAllComplete(requests).addOnCompleteListener {
                if (generation == refreshGeneration) {
                    refreshing = false
                    if (requests.any { !it.isSuccessful }) refreshError = "Could not refresh all content. Check your connection and try again."
                }
            }
        }
    }
    LaunchedEffect(refreshing, refreshGeneration) {
        if (refreshing) {
            delay(15000)
            if (refreshing) {
                refreshGeneration += 1
                refreshing = false
                refreshError = "Refresh timed out. Check your connection and try again."
            }
        }
    }
    var pullDistance by remember { mutableStateOf(0f) }
    val pullThreshold = with(LocalDensity.current) { 80.dp.toPx() }
    val pullConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (source == NestedScrollSource.Drag && available.y < 0 && pullDistance > 0) {
                val consumed = maxOf(available.y, -pullDistance)
                pullDistance += consumed
                return Offset(0f, consumed)
            }
            return Offset.Zero
        }
        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            if (source == NestedScrollSource.Drag && available.y > 0 && !feedListState.canScrollBackward && !refreshing) {
                pullDistance = (pullDistance + available.y * 0.5f).coerceAtMost(pullThreshold * 1.5f)
                return Offset(0f, available.y)
            }
            return Offset.Zero
        }
        override suspend fun onPreFling(available: Velocity): Velocity {
            val pulled = pullDistance > 0
            if (pullDistance >= pullThreshold) refresh()
            pullDistance = 0f
            return if (pulled) available else Velocity.Zero
        }
    }

    // ------------------------------------------------------------------------
    // LIVE FIRESTORE FEED ONLY
    // ------------------------------------------------------------------------

    val allFeedItems =
        firestoreEvents.map { it.toFeedItem() } +
                firestorePosts.map { it.toFeedItem() }

    val selectedDateKey = feedDateKey(weekDates[selectedDayIndex])
    val selectedDayStart = Calendar.getInstance().apply {
        time = weekDates[selectedDayIndex]
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val selectedDayEnd = (selectedDayStart.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }.timeInMillis
    val feedForSelectedDay = allFeedItems.filter {
        if (it is FeedItem.Event && it.endTimeMillis != null && it.endTimeMillis > it.sortTime) {
            it.sortTime < selectedDayEnd && it.endTimeMillis > selectedDayStart.timeInMillis
        } else it.dateKey.isNotBlank() && it.dateKey == selectedDateKey
    }.sortedWith(compareByDescending<FeedItem> { it.sortTime }.thenBy { it.id })

    // ------------------------------------------------------------------------
    // SCREEN
    // ------------------------------------------------------------------------

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {

        LazyColumn(
            state = feedListState,
            modifier = Modifier.fillMaxSize().nestedScroll(pullConnection),
            contentPadding = PaddingValues(
                bottom = 110.dp
            )
        ) {

            // =================================================================
            // THIS WEEK ON CAMPUS
            // =================================================================

            item {

                ThisWeekHeader()
            }
            if (cachedSources.isNotEmpty()) {
                item {
                    Text("Showing saved content. Updates may be delayed until a connection is available.",
                        color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                }
            }

            // =================================================================
            // MY CLUBS
            // This scrolls away with the feed.
            // =================================================================

            item {

                MyClubsSection(
                    clubs = firestoreClubs.filter { it.id in joinedClubIds },
                    expanded = clubsExpanded,
                    onToggle = {
                        clubsExpanded = !clubsExpanded
                    },
                    onClubClick = onNavigateToClubHome
                )
            }

            item {
                Spacer(
                    modifier = Modifier.height(12.dp)
                )
            }

            // =================================================================
            // STICKY WEEK SELECTOR
            //
            // This stays at the top when the user scrolls through many posts.
            // =================================================================

            stickyHeader {

                WeekDateSelector(
                    weekDates = weekDates,
                    selectedDayIndex = selectedDayIndex,
                    onDaySelected = { index ->
                        selectedDayIndex = index
                    }
                )
            }

            // =================================================================
            // SELECTED DAY INFORMATION
            // =================================================================

            item {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkBackground)
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 14.dp,
                            bottom = 6.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Column {

                        Text(
                            text = weekDays[selectedDayIndex],
                            color = TextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "${feedForSelectedDay.size} updates",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        text = SimpleDateFormat(
                            "d MMMM yyyy",
                            Locale.getDefault()
                        ).format(weekDates[selectedDayIndex]),
                        color = AccentPurple,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // =================================================================
            // EMPTY STATE
            // =================================================================

            if (postsError || eventsError || clubsError) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(18.dp)).background(SurfaceDark).padding(16.dp)) {
                        Text("Some content could not load", color = TextWhite, fontWeight = FontWeight.Bold)
                        Text(
                            "Could not load " + listOfNotNull(
                                "posts".takeIf { postsError }, "events".takeIf { eventsError }, "club profiles".takeIf { clubsError }
                            ).joinToString(", ") + ". Previously loaded content stays visible.",
                            color = TextMuted, fontSize = 12.sp
                        )
                        TextButton(onClick = { retryGeneration += 1 }) { Text("Retry", color = AccentPurple) }
                    }
                }
            }
            if (postsLoading || eventsLoading || clubsLoading) {
                item {
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = AccentPurple, strokeWidth = 2.dp)
                    }
                }
            }
            if (feedForSelectedDay.isEmpty() && !postsLoading && !eventsLoading && !postsError && !eventsError) {

                item {

                    EmptyDayState(
                        day = weekDays[selectedDayIndex]
                    )
                }

            } else {

                // =============================================================
                // FEED
                // =============================================================

                items(
                    items = feedForSelectedDay,
                    key = { feedItem ->
                        feedItem.id
                    }
                ) { feedItem ->

                    Box(
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 8.dp
                        )
                    ) {

                        when (feedItem) {

                            is FeedItem.Event -> {

                                EventCard(
                                    event = feedItem.event,
                                    startTimeMillis = feedItem.sortTime.takeIf { it != Long.MIN_VALUE },
                                    endTimeMillis = feedItem.endTimeMillis,
                                    posterImageUrl = feedItem.posterImageUrl,
                                    onClick = {
                                        onOpenEvent(feedItem.event)
                                    },
                                    onVisitClubHome = { onNavigateToClubHome(feedItem.clubId) }
                                )
                            }

                            is FeedItem.TextPost -> {

                                TextPostCard(
                                    post = feedItem,
                                    profileImageUrl = firestoreClubs.firstOrNull {
                                        it.id == feedItem.clubId
                                    }?.profileImageUrl.orEmpty(),
                                    onVisitClub = { onNavigateToClubHome(feedItem.clubId) }
                                )
                            }

                            is FeedItem.PhotoPost -> {

                                PhotoPostCard(
                                    post = feedItem,
                                    profileImageUrl = firestoreClubs.firstOrNull {
                                        it.id == feedItem.clubId
                                    }?.profileImageUrl.orEmpty(),
                                    onVisitClub = { onNavigateToClubHome(feedItem.clubId) }
                                )
                            }

                            is FeedItem.VideoPost -> {

                                VideoPostCard(
                                    post = feedItem,
                                    profileImageUrl = firestoreClubs.firstOrNull {
                                        it.id == feedItem.clubId
                                    }?.profileImageUrl.orEmpty(),
                                    onVisitClub = { onNavigateToClubHome(feedItem.clubId) }
                                )
                            }
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = onOpenCalendar,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp),
            containerColor = AccentPurple,
            contentColor = DarkBackground
        ) {
            Icon(Icons.Filled.DateRange, contentDescription = "Calendar")
        }
        AnimatedVisibility(
            visible = refreshing || pullDistance > 0f,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp),
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(150))
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(SurfaceDark),
                contentAlignment = Alignment.Center
            ) {
                if (refreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = AccentPurple,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = if (pullDistance >= pullThreshold) "Release to refresh" else "Pull to refresh",
                        tint = AccentPurple,
                        modifier = Modifier.size(24.dp).rotate((pullDistance / pullThreshold).coerceIn(0f, 1.5f) * 180f)
                    )
                }
            }
        }
    }
    refreshError?.let { message ->
        AlertDialog(onDismissRequest = { refreshError = null }, title = { Text("Refresh") },
            text = { Text(message) }, confirmButton = { TextButton(onClick = { refreshError = null }) { Text("OK") } })
    }
}

// ============================================================================
// THIS WEEK HEADER
// ============================================================================

@Composable
private fun ThisWeekHeader() {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(
            start = 16.dp,
            end = 16.dp,
            top = 28.dp,
            bottom = 14.dp
        )
    ) {

        // ====================================================================
        // ACTUAL CAMPUS PULSE LOGO — TOP LEFT
        //
        // Image location:
        // app/src/main/res/drawable/campus_pulse_logo.jpeg
        //
        // CHANGE .size(42.dp) TO INCREASE/DECREASE LOGO SIZE.
        // Change start/top padding above to adjust its alignment.
        // ====================================================================

        Image(
            painter = painterResource(
                id = R.drawable.campus_pulse_logo
            ),
            contentDescription = "Campus Pulse Logo",
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(
            modifier = Modifier.width(12.dp)
        )

        Text(
            text = "This week on campus",
            color = TextWhite,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

// ============================================================================
// COLLAPSIBLE MY CLUBS
// ============================================================================

@Composable
private fun MyClubsSection(
    clubs: List<FirestoreClub>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onClubClick: (String) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(
                RoundedCornerShape(18.dp)
            )
            .border(
                width = 1.dp,
                color = BorderColor,
                shape = RoundedCornerShape(18.dp)
            )
            .background(SurfaceDark)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = onToggle
                )
                .padding(
                    horizontal = 16.dp,
                    vertical = 13.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(
                        RoundedCornerShape(10.dp)
                    )
                    .background(
                        AccentPurple.copy(
                            alpha = 0.18f
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = Icons.Filled.Groups,
                    contentDescription = null,
                    tint = AccentPurple,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(
                modifier = Modifier.width(11.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "My Clubs",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Text(
                    text = if (clubs.size == 1) "1 joined club" else "${clubs.size} joined clubs",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }

            Text(
                text = if (expanded) {
                    "Hide"
                } else {
                    "Show"
                },
                color = AccentPurple,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(
                modifier = Modifier.width(4.dp)
            )

            Icon(
                imageVector = if (expanded) {
                    Icons.Filled.ExpandLess
                } else {
                    Icons.Filled.ExpandMore
                },
                contentDescription = if (expanded) {
                    "Hide clubs"
                } else {
                    "Show clubs"
                },
                tint = TextMuted
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(320, easing = FastOutSlowInEasing), expandFrom = Alignment.Top) + fadeIn(tween(220)),
            exit = shrinkVertically(animationSpec = tween(280, easing = FastOutSlowInEasing), shrinkTowards = Alignment.Top) + fadeOut(tween(180))
        ) {
            Column {

                HorizontalDivider(
                    color = BorderColor
                )

                LazyRow(
                    contentPadding = PaddingValues(
                        start = 14.dp,
                        end = 14.dp,
                        top = 14.dp,
                        bottom = 16.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(
                        12.dp
                    )
                ) {

                    items(
                        items = clubs,
                        key = { club -> club.id }
                    ) { club ->

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(75.dp)
                                .clickable {
                                    onClubClick(club.id)
                                }
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(
                                        RoundedCornerShape(18.dp)
                                    )
                                    .background(
                                        when (club.category.uppercase()) {
                                            "SPORT" -> Category.SPORT.color
                                            "CULTURE" -> Category.CULTURE.color
                                            "ADVOCACY" -> Category.ADVOCACY.color
                                            "IDENTITY" -> Category.IDENTITY.color
                                            "ACADEMIC" -> Category.ACADEMIC.color
                                            else -> AccentPurple
                                        }
                                    )
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(
                                            RoundedCornerShape(16.dp)
                                        )
                                        .background(SurfaceSoft),
                                    contentAlignment = Alignment.Center
                                ) {

                                    ClubProfilePhoto(
                                        url = club.profileImageUrl,
                                        clubName = club.name,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(
                                text = club.name,
                                color = TextMuted,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// STICKY DATE SELECTOR
// ============================================================================

@Composable
private fun WeekDateSelector(
    weekDates: List<Date>,
    selectedDayIndex: Int,
    onDaySelected: (Int) -> Unit
) {

    /*
     * The outer background is deliberately opaque.
     *
     * Because this composable is inside stickyHeader(), feed content will
     * scroll BEHIND it instead of being visible through it.
     */
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBackground)
            .padding(
                start = 10.dp,
                end = 10.dp,
                top = 6.dp,
                bottom = 8.dp
            )
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(20.dp)
                )
                .border(
                    width = 1.dp,
                    color = BorderColor,
                    shape = RoundedCornerShape(20.dp)
                )
                .background(SurfaceDark)
                .padding(
                    start = 6.dp,
                    end = 6.dp,
                    top = 9.dp,
                    bottom = 11.dp
                )
        ) {

            Column {

                // ------------------------------------------------------------
                // MONTH
                // ------------------------------------------------------------

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 8.dp,
                            top = 8.dp,
                            end = 8.dp,
                            bottom = 9.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = run {
                            val first = weekDates.first()
                            val last = weekDates.last()
                            val firstMonth = SimpleDateFormat("MMMM", Locale.getDefault()).format(first)
                            val lastMonth = SimpleDateFormat("MMMM", Locale.getDefault()).format(last)
                            val firstYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(first)
                            val lastYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(last)

                            when {
                                firstYear != lastYear ->
                                    "$firstMonth $firstYear – $lastMonth $lastYear".uppercase()
                                firstMonth != lastMonth ->
                                    "$firstMonth – $lastMonth $lastYear".uppercase()
                                else ->
                                    "$firstMonth $lastYear".uppercase()
                            }
                        },
                        color = TextMuted.copy(
                            alpha = 0.72f
                        ),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "THIS WEEK",
                        color = AccentPurple,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // ------------------------------------------------------------
                // ALL 7 DAYS
                //
                // This is NOT horizontally scrollable.
                // All days remain visible.
                // ------------------------------------------------------------

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    weekDays.forEachIndexed { index, day ->

                        val selected =
                            selectedDayIndex == index

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    onDaySelected(index)
                                }
                                .padding(
                                    vertical = 3.dp
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .then(
                                        if (selected) {

                                            Modifier.background(
                                                Brush.linearGradient(
                                                    listOf(
                                                        AccentPurple,
                                                        AccentPink,
                                                        AccentRed
                                                    )
                                                )
                                            )

                                        } else {

                                            Modifier.background(
                                                SurfaceSoft
                                            )
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {

                                Text(
                                    text = SimpleDateFormat(
                                        "d",
                                        Locale.getDefault()
                                    ).format(weekDates[index]),
                                    color = if (selected) {
                                        TextWhite
                                    } else {
                                        TextMuted
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )

                            Text(
                                text = day,
                                color = if (selected) {
                                    TextWhite
                                } else {
                                    TextMuted
                                },
                                fontSize = 9.sp,
                                fontWeight = if (selected) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// POST HEADER
//
// NO THREE-DOT MENU.
// ============================================================================

@Composable
private fun ClubProfilePhoto(url: String, clubName: String, modifier: Modifier = Modifier) {
    var failed by remember(url) { mutableStateOf(false) }
    Box(modifier = modifier.background(SurfaceSoft), contentAlignment = Alignment.Center) {
        if (url.isNotBlank() && !failed) {
            AsyncImage(
                model = url,
                contentDescription = "$clubName profile photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onError = { failed = true }
            )
        } else if (clubName.trim().equals("Coding Club", ignoreCase = true)) {
            Image(
                painter = painterResource(id = R.drawable.code),
                contentDescription = "$clubName profile photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Groups,
                contentDescription = "$clubName has no available profile photo",
                tint = TextMuted,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun PostHeader(
    clubName: String,
    initials: String,
    profileImageUrl: String = "",
    time: String,
    date: String = "",
    onVisitClub: () -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(
                    RoundedCornerShape(14.dp)
                )
                .background(
                    Brush.linearGradient(
                        listOf(
                            AccentPurple,
                            AccentPink
                        )
                    )
                )
                .clickable(
                    onClick = onVisitClub
                ),
            contentAlignment = Alignment.Center
        ) {

            ClubProfilePhoto(
                url = profileImageUrl,
                clubName = clubName,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(
            modifier = Modifier.width(12.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = clubName,
                color = TextWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(
                    onClick = onVisitClub
                )
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = listOf(date, time, "Club post").filter { it.isNotBlank() }.joinToString(" · "),
                color = TextMuted,
                fontSize = 11.sp
            )
        }
    }
}

// ============================================================================
// TEXT POST
// ============================================================================

@Composable
private fun TextPostCard(
    post: FeedItem.TextPost,
    profileImageUrl: String = "",
    onVisitClub: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        ),
        border = BorderStroke(
            1.dp,
            BorderColor
        )
    ) {

        Column(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp
            )
        ) {

            PostHeader(
                clubName = post.clubName,
                initials = post.initials,
                profileImageUrl = profileImageUrl,
                time = post.time,
                date = post.date,
                onVisitClub = onVisitClub
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(
                text = post.text,
                color = TextWhite,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium
            )

            if (post.imageUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))

                HomePostMedia(url = post.imageUrl, video = false, description = "Post from ${post.clubName}",
                    modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(18.dp)))
            }

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            HorizontalDivider(
                color = BorderColor.copy(
                    alpha = 0.8f
                )
            )

            ShareReportRow(contentLabel = "this post", targetId = post.id.removePrefix("firestore_post_"), clubName = post.clubName)
        }
    }
}

// ============================================================================
// PHOTO POST
// ============================================================================

@Composable
private fun PhotoPostCard(
    post: FeedItem.PhotoPost,
    profileImageUrl: String = "",
    onVisitClub: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        ),
        border = BorderStroke(
            1.dp,
            BorderColor
        )
    ) {

        Column {

            Column(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 14.dp
                )
            ) {

                PostHeader(
                    clubName = post.clubName,
                    initials = post.initials,
                    profileImageUrl = profileImageUrl,
                    time = post.time,
                    date = post.date,
                    onVisitClub = onVisitClub
                )

                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                Text(
                    text = post.caption,
                    color = TextWhite,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }

            HomePostMedia(url = post.mediaUrl, video = false, thumbnailUrl = post.thumbnailUrl,
                description = "Post from ${post.clubName}",
                modifier = Modifier.fillMaxWidth().height(260.dp))

            Column(
                modifier = Modifier.padding(
                    horizontal = 16.dp
                )
            ) {

                ShareReportRow(contentLabel = "this post", targetId = post.id.removePrefix("firestore_post_"), clubName = post.clubName)
            }
        }
    }
}

// ============================================================================
// VIDEO POST
// ============================================================================

@Composable
private fun VideoPostCard(
    post: FeedItem.VideoPost,
    profileImageUrl: String = "",
    onVisitClub: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        ),
        border = BorderStroke(
            1.dp,
            BorderColor
        )
    ) {

        Column {

            Column(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 14.dp
                )
            ) {

                PostHeader(
                    clubName = post.clubName,
                    initials = post.initials,
                    profileImageUrl = profileImageUrl,
                    time = post.time,
                    date = post.date,
                    onVisitClub = onVisitClub
                )

                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                Text(
                    text = post.caption,
                    color = TextWhite,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }

            HomePostMedia(url = post.mediaUrl, video = true, thumbnailUrl = post.thumbnailUrl,
                description = "Post from ${post.clubName}",
                modifier = Modifier.fillMaxWidth().height(270.dp))

            Column(
                modifier = Modifier.padding(
                    horizontal = 16.dp
                )
            ) {

                ShareReportRow(contentLabel = "this post", targetId = post.id.removePrefix("firestore_post_"), clubName = post.clubName)
            }
        }
    }
}

// ============================================================================
// SHARE ONLY
//
// NO LIKE
// NO COMMENT
// NO COUNTERS
// ============================================================================

@Composable
private fun ShareReportRow(
    contentLabel: String,
    targetId: String = "",
    clubName: String = "",
    onShare: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var showReportDialog by remember { mutableStateOf(false) }
    var reportSubmitted by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable {
                    if (onShare != null) onShare() else {
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, "Check out $contentLabel on Campus Pulse")
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, "Share via"))
                    }
                }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = "Share",
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Share",
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable { showReportDialog = true }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Flag,
                contentDescription = "Report",
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Report",
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    if (showReportDialog) {
        ReportContentDialog(
            contentLabel = contentLabel,
            targetType = "POST",
            targetId = targetId,
            societyName = clubName,
            onDismiss = { showReportDialog = false },
            onSubmit = {
                showReportDialog = false
                reportSubmitted = true
            }
        )
    }

    if (reportSubmitted) {
        ReportSubmittedDialog(
            onDismiss = { reportSubmitted = false }
        )
    }
}

@Composable
internal fun ReportContentDialog(
    contentLabel: String,
    targetType: String,
    targetId: String,
    societyName: String,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    val reasons = listOf(
        "Spam or misleading",
        "Harassment or bullying",
        "Hate speech",
        "Inappropriate content",
        "False information",
        "Violence or threats",
        "Other"
    )

    var selectedReason by remember { mutableStateOf<String?>(null) }

    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val reportId = remember { java.util.UUID.randomUUID().toString() }
    AlertDialog(
        onDismissRequest = { if (!submitting) onDismiss() },
        containerColor = SurfaceDark,
        titleContentColor = TextWhite,
        textContentColor = TextMuted,
        icon = {
            Icon(
                imageVector = Icons.Outlined.Flag,
                contentDescription = null,
                tint = AccentRed
            )
        },
        title = {
            Text(
                text = "Report $contentLabel",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                error?.let { Text(it, color = AccentRed, fontSize = 12.sp) }
                Text(
                    text = "Why are you reporting this?",
                    color = TextMuted,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                reasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedReason = reason }
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = AccentPurple,
                                unselectedColor = TextMuted
                            )
                        )

                        Text(
                            text = reason,
                            color = TextWhite,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Reports are reviewed by Campus Pulse moderators. Reporting does not automatically remove the content.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val reason = selectedReason
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid == null) error = "Sign in before reporting content."
                    else if (targetId.isBlank()) error = "This content cannot be reported."
                    else if (reason != null && !submitting) {
                        submitting = true
                        error = null
                        FirebaseFirestore.getInstance().collection("reports").document(reportId).set(
                            mapOf("reporterId" to uid, "targetType" to targetType, "targetId" to targetId,
                                "societyName" to societyName, "reason" to reason, "description" to reason,
                                "attachment" to "", "category" to targetType, "priority" to "NORMAL",
                                "status" to "OPEN", "assignedTo" to "", "createdAt" to FieldValue.serverTimestamp())
                        ).addOnSuccessListener { submitting = false; onSubmit(reason) }
                            .addOnFailureListener { submitting = false; error = "Report was not saved. Check your connection and try again." }
                    }
                },
                enabled = selectedReason != null && !submitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentPurple,
                    disabledContainerColor = SurfaceSoft
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = if (submitting) "Saving…" else "Submit report",
                    color = if (selectedReason != null) TextWhite else TextMuted,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !submitting) {
                Text(
                    text = "Cancel",
                    color = TextMuted
                )
            }
        }
    )
}

@Composable
private fun ReportSubmittedDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        titleContentColor = TextWhite,
        textContentColor = TextMuted,
        icon = {
            Icon(
                imageVector = Icons.Outlined.Flag,
                contentDescription = null,
                tint = AccentPurple
            )
        },
        title = {
            Text(
                text = "Report submitted",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Thank you. Your report has been submitted for review.",
                color = TextMuted
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentPurple
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Done",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

// ============================================================================
// EVENT CARD
// ============================================================================

@Composable
private fun EventCard(
    event: EventItem,
    startTimeMillis: Long? = null,
    endTimeMillis: Long? = null,
    posterImageUrl: String = "",
    onClick: () -> Unit,
    onVisitClubHome: () -> Unit
) {

    var posterFailed by remember(event.id, posterImageUrl) { mutableStateOf(false) }
    var posterLoading by remember(event.id, posterImageUrl) { mutableStateOf(posterImageUrl.isNotBlank()) }
    var statusNow by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(event.id) {
        while (true) { statusNow = System.currentTimeMillis(); delay(1000L) }
    }
    val eventStatus = when {
        startTimeMillis == null -> "Schedule not set"
        statusNow < startTimeMillis -> "Upcoming"
        endTimeMillis == null || endTimeMillis <= startTimeMillis -> "End time not set"
        statusNow < endTimeMillis -> "Ongoing"
        else -> "Passed"
    }
    val statusColor = when (eventStatus) {
        "Ongoing" -> Color(0xFF27D79C)
        "Passed" -> TextMuted
        else -> AccentPurple
    }
    var showRsvp by remember { mutableStateOf(false) }
    var rsvpEnabled by remember(event.id) { mutableStateOf(false) }
    val preview = LocalInspectionMode.current
    DisposableEffect(event.id, preview) {
        val listener = if (preview) null else FirebaseFirestore.getInstance().collection("events").document(event.id)
            .addSnapshotListener { snapshot, exception ->
                rsvpEnabled = exception == null && snapshot != null && snapshot.getBoolean("rsvpEnabled") == true
                        && snapshot.getBoolean("isActive") == true && snapshot.getString("status") == "PUBLISHED"
            }
        onDispose { listener?.remove() }
    }
    val context = LocalContext.current
    var showReportDialog by remember { mutableStateOf(false) }
    var reportSubmitted by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = BorderColor,
                shape = RoundedCornerShape(26.dp)
            )
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .background(DarkBackground)
        ) {

            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(SurfaceSoft, DarkBackground))
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = if (posterImageUrl.isBlank() || posterFailed) "No available poster for ${event.title}" else null,
                    tint = TextMuted.copy(alpha = 0.25f),
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 80.dp).size(52.dp)
                )
            }
            if (posterImageUrl.isNotBlank() && !posterFailed) {
                AsyncImage(
                    model = posterImageUrl,
                    contentDescription = "${event.title} event poster",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onLoading = { posterLoading = true },
                    onSuccess = { posterLoading = false },
                    onError = { posterFailed = true; posterLoading = false }
                )
            }
            if (posterLoading && !posterFailed) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 92.dp).size(24.dp),
                    color = AccentPurple,
                    strokeWidth = 2.dp
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                DarkBackground.copy(alpha = 0.12f),
                                DarkBackground.copy(alpha = 0.72f),
                                DarkBackground.copy(alpha = 0.98f)
                            )
                        )
                    )
            )

            // ----------------------------------------------------------------
            // CATEGORY
            // ----------------------------------------------------------------

            Box(
                modifier = Modifier.padding(16.dp)
            ) {

                CategoryChip(
                    category = event.category
                )
            }

            // ----------------------------------------------------------------
            // EVENT CONTENT
            // ----------------------------------------------------------------

            Column(
                modifier = Modifier
                    .align(
                        Alignment.BottomStart
                    )
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                Text(
                    text = eventStatus,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                        .background(DarkBackground.copy(alpha = 0.8f)).padding(horizontal = 10.dp, vertical = 5.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = event.title,
                    color = TextWhite,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = event.description,
                    color = TextWhite.copy(
                        alpha = 0.85f
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    StatColumn(
                        label = "Date"
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Icon(
                                imageVector = Icons.Filled.DateRange,
                                contentDescription = null,
                                tint = AccentRed,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(
                                modifier = Modifier.width(4.dp)
                            )

                            Text(
                                text = event.schedule
                                    .substringBefore("·")
                                    .trim(),
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    StatColumn(
                        label = "Time"
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Icon(
                                imageVector = Icons.Filled.AccessTime,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(
                                modifier = Modifier.width(4.dp)
                            )

                            Text(
                                text = event.schedule
                                    .substringAfter(
                                        "·",
                                        "17:00"
                                    )
                                    .trim(),
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    StatColumn(
                        label = "Share"
                    ) {

                        Row(
                            modifier = Modifier.clickable {

                                shareEvent(
                                    context,
                                    event
                                )
                            },
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share event",
                                tint = TextWhite,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(
                                modifier = Modifier.width(4.dp)
                            )

                            Text(
                                text = "Send",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = AccentRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = event.venue.ifBlank { "Location to be confirmed" },
                        color = TextWhite,
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // ----------------------------------------------------------------
                // EVENT ACTIONS
                // ----------------------------------------------------------------

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        12.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Button(
                        onClick = { if (rsvpEnabled) showRsvp = true else onClick() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(
                            25.dp
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TextWhite
                        )
                    ) {

                        Text(
                            text = if (rsvpEnabled) "RSVP Event" else "View Event",
                            color = DarkBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(
                                TextWhite.copy(
                                    alpha = 0.20f
                                )
                            )
                            .clickable(
                                onClick = onVisitClubHome
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "Visit Club",
                            tint = TextWhite,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(
                                TextWhite.copy(alpha = 0.20f)
                            )
                            .clickable {
                                showReportDialog = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Flag,
                            contentDescription = "Report event",
                            tint = TextWhite,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }

    if (showRsvp) {
        HomeRsvpDialog(eventId = event.id, title = event.title, onDismiss = { showRsvp = false })
    }
    if (showReportDialog) {
        ReportContentDialog(
            contentLabel = "this event",
            targetType = "EVENT",
            targetId = event.id,
            societyName = event.hostClub,
            onDismiss = { showReportDialog = false },
            onSubmit = {
                showReportDialog = false
                reportSubmitted = true
            }
        )
    }

    if (reportSubmitted) {
        ReportSubmittedDialog(
            onDismiss = { reportSubmitted = false }
        )
    }
}

// ============================================================================
// STAT COLUMN
// ============================================================================

@Composable
private fun RowScope.StatColumn(
    label: String,
    content: @Composable () -> Unit
) {

    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.Start
    ) {

        content()

        Spacer(
            modifier = Modifier.height(2.dp)
        )

        Text(
            text = label,
            color = TextMuted,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

// ============================================================================
// EMPTY STATE
// ============================================================================

@Composable
private fun EmptyDayState(
    day: String
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 50.dp,
                bottom = 30.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector = Icons.Outlined.EventBusy,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(36.dp)
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(
            text = "Nothing happening on $day yet",
            color = TextWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = "Choose another day to continue browsing.",
            color = TextMuted,
            fontSize = 11.sp
        )
    }
}

// ============================================================================
// PREVIEW
// ============================================================================

@Preview(
    name = "Campus Pulse Home Feed",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun HomeFeedScreenPreview() {

    CampusPulseTheme {

        CampusPulseHomeScreen(
            onOpenEvent = {},
            onNavigateToClubHome = {},
            onNavigate = {}
        )
    }
}


@Composable
private fun HomeRsvpDialog(eventId: String, title: String, onDismiss: () -> Unit) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val db = remember { FirebaseFirestore.getInstance() }
    var response by remember { mutableStateOf<String?>(null) }
    var loaded by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    DisposableEffect(uid, eventId) {
        val listener = uid?.let {
            db.collection("events").document(eventId).collection("rsvps").document(it)
                .addSnapshotListener { snapshot, exception ->
                    loaded = exception == null
                    if (exception != null) error = "Could not load your RSVP. Check your connection."
                    else { response = snapshot?.getString("response"); error = null }
                }
        }
        if (uid == null) { loaded = false; error = "Sign in to RSVP." }
        onDispose { listener?.remove() }
    }
    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        containerColor = SurfaceDark,
        title = { Text("RSVP · $title", color = TextWhite) },
        text = {
            Column {
                Text("Your choice: ${response ?: "No response"}", color = TextMuted)
                error?.let { Text(it, color = AccentRed) }
                if (!loaded && error == null) CircularProgressIndicator(color = AccentPurple)
                listOf("GOING" to "Going", "INTERESTED" to "Interested", "NOT_GOING" to "Not going").forEach { (value, label) ->
                    TextButton(enabled = loaded && !saving && uid != null, onClick = {
                        if (uid != null) {
                            saving = true
                            error = null
                            db.collection("events").document(eventId).collection("rsvps").document(uid)
                                .set(mapOf("response" to value, "updatedAt" to FieldValue.serverTimestamp()))
                                .addOnSuccessListener { saving = false; response = value; onDismiss() }
                                .addOnFailureListener { saving = false; error = "RSVP was not saved. Check your connection and try again." }
                        }
                    }) { Text(if (response == value) "✓ $label" else label, color = TextWhite) }
                }
                if (saving) Text("Saving…", color = TextMuted)
            }
        },
        confirmButton = { TextButton(enabled = !saving, onClick = onDismiss) { Text("Close", color = TextMuted) } }
    )
}

// Media opens only after a tap, so scrolling never starts video or audio.
@Composable
internal fun HomePostMedia(
    url: String,
    video: Boolean,
    description: String,
    modifier: Modifier,
    thumbnailUrl: String = ""
) {
    var open by remember(url) { mutableStateOf(false) }
    Box(modifier.background(Brush.verticalGradient(listOf(GradientPurple, SurfaceDark)))
        .clickable(enabled = url.isNotBlank()) { open = true }, contentAlignment = Alignment.Center) {
        if (video) {
            if (thumbnailUrl.isNotBlank()) HomeMediaImage(thumbnailUrl, description, Modifier.fillMaxSize(), ContentScale.Crop)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.PlayArrow, "Play video", tint = TextWhite, modifier = Modifier.size(64.dp))
                Text(if (url.isBlank()) "Video unavailable" else "Tap to play", color = TextWhite)
            }
        } else HomeMediaImage(url, description, Modifier.fillMaxSize(), ContentScale.Crop)
    }
    if (open) Dialog(onDismissRequest = { open = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(Modifier.fillMaxSize().background(Color.Black).statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { open = false }) { Text("Close", color = TextWhite) }
            }
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (video) HomeVideoPlayer(url) else HomeMediaImage(url, description, Modifier.fillMaxSize(), ContentScale.Fit)
            }
        }
    }
}

@Composable
internal fun HomeMediaImage(url: String, description: String, modifier: Modifier, scale: ContentScale) {
    var loading by remember(url) { mutableStateOf(url.isNotBlank()) }
    var failed by remember(url) { mutableStateOf(false) }
    var retry by remember(url) { mutableIntStateOf(0) }
    Box(modifier, contentAlignment = Alignment.Center) {
        if (url.isNotBlank() && !failed) androidx.compose.runtime.key(url, retry) {
            AsyncImage(model = url, contentDescription = description, modifier = Modifier.fillMaxSize(), contentScale = scale,
                onLoading = { loading = true }, onSuccess = { loading = false },
                onError = { loading = false; failed = true })
        }
        if (loading) CircularProgressIndicator(color = AccentPurple, modifier = Modifier.size(28.dp))
        if (url.isBlank() || failed) Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.Image, null, tint = TextMuted, modifier = Modifier.size(40.dp))
            Text(if (url.isBlank()) "No image available" else "Image could not load", color = TextMuted)
            if (failed) TextButton(onClick = { retry++; failed = false; loading = true }) { Text("Retry", color = AccentPurple) }
        }
    }
}

@Composable
private fun HomeVideoPlayer(url: String) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    var attempt by remember(url) { mutableIntStateOf(0) }
    var failed by remember(url) { mutableStateOf(false) }
    var loading by remember(url) { mutableStateOf(true) }
    var aspect by remember(url) { mutableStateOf(16f / 9f) }
    val player = remember(url, attempt) { VideoView(context) }
    DisposableEffect(player, owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) player.pause()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); player.stopPlayback() }
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (!failed) androidx.compose.runtime.key(url, attempt) { AndroidView(
            factory = {
                player.apply {
                    val controls = MediaController(context)
                    controls.setAnchorView(this)
                    setMediaController(controls)
                    setOnPreparedListener { media ->
                        if (media.videoWidth > 0 && media.videoHeight > 0) aspect = media.videoWidth.toFloat() / media.videoHeight
                        loading = false
                        if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) start()
                        controls.show(3000)
                    }
                    setOnErrorListener { _, _, _ -> loading = false; failed = true; true }
                    setOnCompletionListener { loading = false; controls.show(0) }
                    setVideoURI(Uri.parse(url))
                }
            },
            modifier = Modifier.fillMaxWidth().aspectRatio(aspect)
        ) }
        if (loading && !failed) CircularProgressIndicator(color = AccentPurple)
        if (failed) Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Video could not play. Check your connection or the video link.", color = TextWhite,
                textAlign = TextAlign.Center, modifier = Modifier.padding(20.dp))
            TextButton(onClick = { player.stopPlayback(); attempt++; failed = false; loading = true }) { Text("Retry", color = AccentPurple) }
        }
    }
}
