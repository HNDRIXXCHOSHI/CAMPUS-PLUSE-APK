package com.campuspulse.app.ui.screens

import android.widget.Toast
import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import coil.compose.AsyncImage
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn

import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.campuspulse.app.R
import com.campuspulse.app.data.FirestoreClub
import com.campuspulse.app.data.FirestoreClubLeader
import com.campuspulse.app.data.FirestoreEvent
import com.campuspulse.app.data.FirestorePost
import com.campuspulse.app.data.HomeRepository
import com.campuspulse.app.data.SampleData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.campuspulse.app.model.Club
import com.campuspulse.app.model.Category
import com.campuspulse.app.model.EventItem
import com.campuspulse.app.ui.components.NavDestination
import com.campuspulse.app.ui.components.CategoryChip
import com.campuspulse.app.ui.components.shareEvent
import com.campuspulse.app.ui.components.shareClub
import com.campuspulse.app.ui.theme.CampusPulseTheme
import java.text.SimpleDateFormat
import java.util.Locale


// =============================================================================
// COLOURS
// =============================================================================

private val DarkBackground = Color(0xFF0A0A0A)

private val AccentPink = Color(0xFFD81B60)

private val TextWhite = Color(0xFFFFFFFF)

private val TextMuted = Color(0xFF9E9E9E)

private val DividerColor = Color(0xFF222222)

private val SurfaceCardColor = Color(0xFF161616)

// Home feed post-card colours reused on the Club Posts tab.
private val HomePostSurface = Color(0xFF05294B)
private val HomePostBorder = Color(0xFF315574)
private val HomePostMuted = Color(0xFFFFFFFF)
private val HomePostAccent = Color(0xFFFF5722)
private val HomePostReportAccent = Color(0xFFF7941D)
private val HomePostSoft = Color(0xFF0D426F)


// =============================================================================
// CLUB ACCOUNT SCREEN
// =============================================================================

@Composable
fun ClubAccountScreen(
    club: Club,

    // NEW:
    // This performs a real back-stack action from your navigation file.
    onBack: () -> Unit,

    onManage: () -> Unit,

    onNavigate: (NavDestination) -> Unit,
    onOpenEvent: (EventItem) -> Unit = {}
) {

    var contentNow by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) { contentNow = System.currentTimeMillis(); delay(1000) }
    }

    var selectedTab by remember(club.id) {
        mutableIntStateOf(0)
    }

    // Local UI filters. These only change what is displayed; Firestore data is untouched.
    var postFilter by remember(club.id) { mutableStateOf("All") }
    var eventFilter by remember(club.id) { mutableStateOf("All") }
    var showPostFilter by remember(club.id) { mutableStateOf(false) }
    var showEventFilter by remember(club.id) { mutableStateOf(false) }

    var isJoined by remember(club.id) {
        mutableStateOf(false)
    }

    var membershipId by remember(club.id) {
        mutableStateOf<String?>(null)
    }

    val membershipContext = LocalContext.current
    var membershipError by remember(club.id) { mutableStateOf<String?>(null) }
    var membershipLoading by remember(club.id) {
        mutableStateOf(true)
    }

    val currentUserId = if (LocalInspectionMode.current) null else FirebaseAuth.getInstance().currentUser?.uid
    val firestore = if (LocalInspectionMode.current) null else FirebaseFirestore.getInstance()

    // Skip HomeRepository initialization during Compose Preview inspection mode
    // to prevent FirebaseApp uninitialized exception.
    val isPreview = LocalInspectionMode.current
    val homeRepository = if (isPreview) null else remember {
        HomeRepository()
    }

    var profileLoading by remember(club.id) { mutableStateOf(!isPreview) }
    var profileError by remember(club.id) { mutableStateOf<String?>(null) }
    var profileRetry by remember(club.id) { mutableIntStateOf(0) }

    var liveClub by remember(club.id) {
        mutableStateOf<FirestoreClub?>(null)
    }

    var clubPosts by remember(club.id) {
        mutableStateOf<List<FirestorePost>>(emptyList())
    }

    var clubEvents by remember(club.id) {
        mutableStateOf<List<FirestoreEvent>>(emptyList())
    }

    var clubLeaders by remember(club.id) {
        mutableStateOf<List<FirestoreClubLeader>>(emptyList())
    }
    var selectedLeaderId by remember(club.id) { mutableStateOf<String?>(null) }
    clubLeaders.firstOrNull { it.id == selectedLeaderId }?.let { leader ->
        LeadershipProfileDialog(
            leader = leader,
            onDismiss = { selectedLeaderId = null }
        )
    }


    DisposableEffect(homeRepository, club.id, currentUserId, profileRetry) {
        if (homeRepository == null) {
            return@DisposableEffect onDispose {}
        }

        val membershipListener = if (currentUserId != null && firestore != null) {
            firestore.collection("clubMembers")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("clubId", club.id)
                .whereEqualTo("status", "ACTIVE")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        membershipError = "Unable to check membership. Check your connection and tap Retry membership."
                        Toast.makeText(membershipContext, membershipError, Toast.LENGTH_LONG).show()
                        membershipLoading = false
                        return@addSnapshotListener
                    }

                    membershipError = null
                    val membership = snapshot?.documents?.firstOrNull()
                    membershipId = membership?.id
                    isJoined = membership != null
                    membershipLoading = false
                }
        } else {
            membershipLoading = false
            null
        }

        profileLoading = true
        profileError = null
        val clubsListener =
            homeRepository.listenToClubs(
                onSuccess = { clubs ->
                    profileLoading = false
                    liveClub = clubs.firstOrNull { firestoreClub ->
                        firestoreClub.id == club.id
                    }
                    profileError = if (liveClub == null) "This club is no longer available." else null
                },
                onError = {
                    profileLoading = false
                    profileError = "Unable to load this club. Check your connection."
                }
            )

        val postsListener =
            homeRepository.listenToPosts(
                onSuccess = { posts ->
                    clubPosts = posts.filter { post ->
                        post.clubId == club.id
                    }
                },
                onError = {
                    clubPosts = emptyList()
                }
            )

        val eventsListener =
            homeRepository.listenToEvents(
                onSuccess = { events ->
                    clubEvents = events.filter { event ->
                        event.clubId == club.id
                    }
                },
                onError = {
                    clubEvents = emptyList()
                }
            )

        val leadershipListener =
            homeRepository.listenToClubLeadership(
                clubId = club.id,
                onSuccess = { leaders ->
                    clubLeaders = leaders
                },
                onError = {
                    clubLeaders = emptyList()
                }
            )

        onDispose {
            clubsListener.remove()
            postsListener.remove()
            eventsListener.remove()
            leadershipListener.remove()
            membershipListener?.remove()
        }
    }

    val displayCategory = Category.values().firstOrNull {
        it.name.equals(liveClub?.category, ignoreCase = true)
    } ?: club.category

    val displayClubName =
        liveClub?.name
            ?.takeIf { it.isNotBlank() }
            ?: club.name

    val displayDescription =
        liveClub?.description
            ?.takeIf { it.isNotBlank() }
            ?: ""

    val displayHistory =
        liveClub?.history
            ?.takeIf { it.isNotBlank() }
            ?: ""

    val displayLocation =
        liveClub?.location
            ?.takeIf { it.isNotBlank() }
            ?: ""

    val displayBannerImageUrl =
        liveClub?.bannerImageUrl
            ?.takeIf { it.isNotBlank() }
            ?: ""

    val displayProfileImageUrl =
        liveClub?.profileImageUrl
            ?.takeIf { it.isNotBlank() }
            ?: ""

    val context = LocalContext.current


    Scaffold(
        containerColor = DarkBackground
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                bottom = 24.dp
            )
        ) {


            if (profileLoading || profileError != null) {
                item {
                    Column(Modifier.fillMaxWidth().padding(20.dp)) {
                        if (profileLoading) Text("Loading club…", color = TextMuted)
                        profileError?.let { message ->
                            Text(message, color = TextMuted)
                            TextButton(onClick = { profileRetry++ }) { Text("Retry", color = AccentPink) }
                        }
                    }
                }
            }

            // =================================================================
            // HERO / PROFILE HEADER
            // =================================================================

            item {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {

                    if (displayBannerImageUrl.isNotBlank()) {
                        AsyncImage(
                            model = displayBannerImageUrl,
                            contentDescription = "$displayClubName banner",
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop
                        )

                        Box(
                            modifier =
                                Modifier
                                    .matchParentSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colorStops =
                                                arrayOf(
                                                    0.00f to Color.Black.copy(alpha = 0.10f),
                                                    0.15f to Color.Black.copy(alpha = 0.25f),
                                                    0.28f to Color.Black.copy(alpha = 0.50f),
                                                    0.40f to Color.Black.copy(alpha = 0.70f),
                                                    0.55f to Color.Black.copy(alpha = 0.85f),
                                                    1.00f to DarkBackground.copy(alpha = 0.97f)
                                                )
                                        )
                                    )
                        )
                    } else if (
                        club.id.equals("Coding_club", ignoreCase = true) ||
                        club.name.equals("Coding Club", ignoreCase = true)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.cclub),
                            contentDescription = "$displayClubName banner",
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop
                        )

                        Box(
                            modifier =
                                Modifier
                                    .matchParentSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colorStops =
                                                arrayOf(
                                                    0.00f to Color.Black.copy(alpha = 0.10f),
                                                    0.15f to Color.Black.copy(alpha = 0.25f),
                                                    0.28f to Color.Black.copy(alpha = 0.50f),
                                                    0.40f to Color.Black.copy(alpha = 0.70f),
                                                    0.55f to Color.Black.copy(alpha = 0.85f),
                                                    1.00f to DarkBackground.copy(alpha = 0.97f)
                                                )
                                        )
                                    )
                        )
                    } else {
                        Box(
                            modifier =
                                Modifier
                                    .matchParentSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors =
                                                listOf(
                                                    displayCategory.color.copy(alpha = 0.85f),
                                                    displayCategory.color.copy(alpha = 0.40f),
                                                    DarkBackground
                                                )
                                        )
                                    )
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                bottom = 12.dp
                            ),
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {


                        // =====================================================
                        // TOP BAR
                        // =====================================================

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = 16.dp,
                                    start = 16.dp,
                                    end = 16.dp,
                                    bottom = 12.dp
                                ),
                            verticalAlignment =
                                Alignment.CenterVertically,
                            horizontalArrangement =
                                Arrangement.SpaceBetween
                        ) {


                            // =================================================
                            // BACK BUTTON
                            //
                            // FIXED:
                            // No longer navigates directly to HOME.
                            // It calls onBack() instead.
                            // =================================================

                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Color.Black.copy(
                                            alpha = 0.40f
                                        )
                                    )
                            ) {

                                Icon(
                                    imageVector =
                                        Icons.AutoMirrored.Filled.ArrowBack,

                                    contentDescription =
                                        "Back",

                                    tint = TextWhite,

                                    modifier =
                                        Modifier.size(
                                            22.dp
                                        )
                                )
                            }


                            // =================================================
                            // EMPTY SPACE
                            //
                            // Keeps the back button on the left while
                            // maintaining the current clean profile layout.
                            // =================================================

                            Spacer(
                                modifier =
                                    Modifier.size(
                                        42.dp
                                    )
                            )
                        }


                        // =====================================================
                        // CLUB PROFILE IMAGE / PLACEHOLDER
                        // =====================================================

                        Box(
                            contentAlignment =
                                Alignment.BottomEnd,

                            modifier =
                                Modifier.padding(
                                    bottom = 16.dp
                                )
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(
                                        displayCategory.color
                                    )
                                    .border(
                                        width = 2.dp,
                                        color =
                                            TextWhite.copy(
                                                alpha = 0.20f
                                            ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {

                                if (displayProfileImageUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = displayProfileImageUrl,
                                        contentDescription = "$displayClubName profile photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else if (
                                    club.id.equals("Coding_club", ignoreCase = true) ||
                                    club.name.equals("Coding Club", ignoreCase = true)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.code),
                                        contentDescription = "$displayClubName profile photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }


                        // =====================================================
                        // CLUB NAME
                        // =====================================================

                        Text(
                            text = displayClubName,

                            style =
                                MaterialTheme.typography.headlineMedium,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                TextWhite,

                            textAlign =
                                TextAlign.Center,

                            modifier =
                                Modifier.padding(
                                    horizontal = 24.dp
                                )
                        )


                        Spacer(
                            modifier =
                                Modifier.height(
                                    8.dp
                                )
                        )


                        // =====================================================
                        // CLUB DESCRIPTION
                        // =====================================================

                        Text(
                            text = displayDescription,

                            style =
                                MaterialTheme.typography.bodyMedium,

                            color =
                                TextMuted,

                            textAlign =
                                TextAlign.Center,

                            modifier =
                                Modifier.padding(
                                    horizontal = 30.dp
                                )
                        )


                        // =====================================================
                        // LOCATION
                        //
                        // NEW:
                        // Uses the location stored in the Club object.
                        // =====================================================

                        Spacer(
                            modifier =
                                Modifier.height(
                                    12.dp
                                )
                        )


                        Surface(
                            color =
                                Color.Black.copy(
                                    alpha = 0.25f
                                ),

                            shape =
                                RoundedCornerShape(
                                    100.dp
                                )
                        ) {

                            Row(
                                modifier =
                                    Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 7.dp
                                    ),

                                verticalAlignment =
                                    Alignment.CenterVertically,

                                horizontalArrangement =
                                    Arrangement.Center
                            ) {

                                Icon(
                                    imageVector =
                                        Icons.Outlined.LocationOn,

                                    contentDescription =
                                        null,

                                    tint =
                                        AccentPink,

                                    modifier =
                                        Modifier.size(
                                            15.dp
                                        )
                                )


                                Spacer(
                                    modifier =
                                        Modifier.width(
                                            5.dp
                                        )
                                )


                                Text(
                                    text =
                                        displayLocation,

                                    color =
                                        TextWhite.copy(
                                            alpha = 0.85f
                                        ),

                                    fontSize =
                                        11.sp,

                                    fontWeight =
                                        FontWeight.Medium,

                                    maxLines = 1,

                                    overflow =
                                        TextOverflow.Ellipsis
                                )
                            }
                        }


                        Spacer(
                            modifier =
                                Modifier.height(
                                    24.dp
                                )
                        )


                        // =====================================================
                        // CLUB STATS
                        // =====================================================

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 24.dp
                                ),

                            horizontalArrangement =
                                Arrangement.SpaceEvenly
                        ) {

                            StatCol(
                                value =
                                    if (isPreview) club.members.toString() else liveClub?.memberCount?.toString() ?: "—",
                                label =
                                    "Members"
                            )


                            StatCol(
                                value =
                                    "${clubPosts.size}",
                                label =
                                    "Posts"
                            )


                            StatCol(
                                value =
                                    "${clubEvents.size}",
                                label =
                                    "Events"
                            )
                        }


                        Spacer(
                            modifier =
                                Modifier.height(
                                    24.dp
                                )
                        )


                        // =====================================================
                        // CLUB ACTIONS
                        // =====================================================

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 24.dp
                                ),

                            verticalAlignment =
                                Alignment.CenterVertically,

                            horizontalArrangement =
                                Arrangement.SpaceBetween
                        ) {





                            // =================================================
                            // JOIN CLUB
                            // =================================================

                            if (membershipError != null) {
                                TextButton(onClick = { membershipLoading = true; membershipError = null; profileRetry++ }) {
                                    Text("Retry membership", color = Color(0xFFFF7A00))
                                }
                            }
                            Button(
                                enabled = !membershipLoading && membershipError == null && currentUserId != null,
                                onClick = {
                                    val uid = currentUserId
                                    val db = firestore

                                    if (uid != null && db != null && !membershipLoading) {
                                        membershipLoading = true

                                        if (isJoined) {
                                            val id = membershipId
                                            if (id != null) {
                                                db.collection("clubMembers")
                                                    .document(id)
                                                    .delete()
                                                    .addOnFailureListener {
                                                        membershipLoading = false
                                                        Toast.makeText(membershipContext, "Could not leave this club. Check your connection and try again.", Toast.LENGTH_LONG).show()
                                                    }
                                            } else {
                                                membershipLoading = false
                                            }
                                        } else {
                                            val membership = hashMapOf(
                                                "userId" to uid,
                                                "clubId" to club.id,
                                                "status" to "ACTIVE",
                                                "joinedAt" to FieldValue.serverTimestamp()
                                            )

                                            db.collection("clubMembers")
                                                .add(membership)
                                                .addOnFailureListener { failure ->
                                                    membershipLoading = false
                                                    val denied = (failure as? com.google.firebase.firestore.FirebaseFirestoreException)?.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED
                                                    val message = if (denied) "Joining was denied. You may have been removed from this club, or the club is no longer available."
                                                    else "Could not join this club. Check your connection and try again."
                                                    Toast.makeText(membershipContext, message, Toast.LENGTH_LONG).show()
                                                }
                                        }
                                    }
                                },

                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor =
                                            if (isJoined) {

                                                Color(
                                                    0xFF2A2A2A
                                                )

                                            } else {

                                                AccentPink
                                            }
                                    ),

                                shape =
                                    RoundedCornerShape(
                                        24.dp
                                    ),

                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .padding(
                                            horizontal =
                                                24.dp
                                        )
                                        .height(
                                            44.dp
                                        )
                            ) {

                                if (isJoined) {

                                    Icon(
                                        imageVector =
                                            Icons.Filled.Check,

                                        contentDescription =
                                            null,

                                        tint =
                                            TextWhite,

                                        modifier =
                                            Modifier.size(
                                                16.dp
                                            )
                                    )


                                    Spacer(
                                        modifier =
                                            Modifier.width(
                                                6.dp
                                            )
                                    )
                                }


                                Text(
                                    text =
                                        if (isJoined) {
                                            "Joined"
                                        } else {
                                            "Join Club"
                                        },

                                    color =
                                        TextWhite,

                                    fontWeight =
                                        FontWeight.Bold,

                                    fontSize =
                                        15.sp
                                )
                            }


                            // =================================================
                            // SHARE
                            // =================================================

                            Icon(
                                imageVector =
                                    Icons.Outlined.Share,

                                contentDescription =
                                    "Share",

                                tint =
                                    AccentPink,

                                modifier =
                                    Modifier
                                        .size(
                                            28.dp
                                        )
                                        .clickable {

                                            shareClub(
                                                context,
                                                club
                                            )
                                        }
                            )
                        }


                        Spacer(
                            modifier =
                                Modifier.height(
                                    20.dp
                                )
                        )
                    }
                }


                HorizontalDivider(
                    color =
                        DividerColor,

                    thickness =
                        1.dp
                )
            }


            // =================================================================
            // TAB NAVIGATION
            // =================================================================

            item {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        ),

                    horizontalArrangement =
                        Arrangement.SpaceAround,

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {


                    // =========================================================
                    // LEADERSHIP
                    // =========================================================

                    TabIconButton(
                        icon =
                            Icons.Outlined.GridView,

                        isSelected =
                            selectedTab == 0,

                        onClick = {
                            selectedTab = 0
                        }
                    )


                    // =========================================================
                    // POSTS
                    // =========================================================

                    TabIconButton(
                        icon =
                            Icons.Outlined.Explore,

                        isSelected =
                            selectedTab == 1,

                        onClick = {
                            selectedTab = 1
                        }
                    )


                    // =========================================================
                    // EVENTS
                    // =========================================================

                    TabIconButton(
                        icon =
                            Icons.Outlined.DateRange,

                        isSelected =
                            selectedTab == 2,

                        onClick = {
                            selectedTab = 2
                        }
                    )


                    // =========================================================
                    // ABOUT
                    // =========================================================

                    TabIconButton(
                        icon =
                            Icons.Outlined.Info,

                        isSelected =
                            selectedTab == 3,

                        onClick = {
                            selectedTab = 3
                        }
                    )
                }


                HorizontalDivider(
                    color =
                        DividerColor,

                    thickness =
                        1.dp
                )
            }


            // =================================================================
            // TAB CONTENT
            // =================================================================

            when (selectedTab) {


                // =============================================================
                // LEADERSHIP
                // =============================================================

                0 -> {

                    item {

                        Column(
                            modifier =
                                Modifier.padding(
                                    vertical = 16.dp
                                )
                        ) {

                            Text(
                                text =
                                    "Club Leadership",

                                style =
                                    MaterialTheme.typography.titleMedium,

                                fontWeight =
                                    FontWeight.Bold,

                                color =
                                    TextWhite,

                                modifier =
                                    Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 17.dp
                                    )
                            )


                            LazyRow(
                                contentPadding =
                                    PaddingValues(
                                        horizontal = 16.dp
                                    ),

                                horizontalArrangement =
                                    Arrangement.spacedBy(
                                        16.dp
                                    )
                            ) {

                                items(
                                    items = clubLeaders,
                                    key = { leader ->
                                        leader.id
                                    }
                                ) { leader ->


                                    // =========================================
                                    // LEADER CARD
                                    // =========================================

                                    Column(
                                        horizontalAlignment =
                                            Alignment.CenterHorizontally,

                                        modifier =
                                            Modifier
                                                .clickable { selectedLeaderId = leader.id }
                                                .width(
                                                    96.dp
                                                )
                                                .border(
                                                    width =
                                                        1.dp,

                                                    color =
                                                        Color(
                                                            0xFF2C2C2E
                                                        ),

                                                    shape =
                                                        RoundedCornerShape(
                                                            15.dp
                                                        )
                                                )
                                                .padding(
                                                    8.dp
                                                )
                                    ) {


                                        // =====================================
                                        // AVATAR
                                        // =====================================

                                        ClubLeaderPhoto(leader, Modifier.size(60.dp))


                                        Spacer(
                                            modifier =
                                                Modifier.height(
                                                    6.dp
                                                )
                                        )


                                        // =====================================
                                        // NAME
                                        // =====================================

                                        Text(
                                            text =
                                                leader.name,
                                            maxLines = 2,

                                            style =
                                                MaterialTheme.typography.bodyMedium,

                                            fontWeight =
                                                FontWeight.SemiBold,

                                            color =
                                                TextWhite,

                                            textAlign =
                                                TextAlign.Center
                                        )


                                        // =====================================
                                        // ROLE
                                        // =====================================

                                        Text(
                                            text =
                                                leader.role,

                                            style =
                                                MaterialTheme.typography.labelSmall,

                                            color =
                                                TextMuted,

                                            textAlign =
                                                TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }


                // =============================================================
                // POSTS
                // =============================================================

                1 -> {

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 16.dp,
                                    bottom = 10.dp
                                )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Posts",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )

                                ClubSectionFilterMenu(
                                    options = listOf("All", "Latest", "Oldest"),
                                    selected = postFilter,
                                    expanded = showPostFilter,
                                    onExpandedChange = { showPostFilter = it },
                                    onSelected = {
                                        postFilter = it
                                        showPostFilter = false
                                    }
                                )
                            }
                        }
                    }

                    val filteredPosts = when (postFilter) {
                        "Oldest" -> clubPosts.sortedBy { post ->
                            post.createdAt?.toDate()?.time ?: Long.MAX_VALUE
                        }
                        else -> clubPosts.sortedByDescending { post ->
                            post.createdAt?.toDate()?.time ?: 0L
                        }
                    }

                    if (filteredPosts.isEmpty()) {
                        item {
                            Text(
                                text = "No published posts yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        items(
                            items = filteredPosts,
                            key = { post -> post.id }
                        ) { post ->
                            ClubHomeStyleTextPostCard(
                                post = post,
                                fallbackClubName = displayClubName,
                                profileImageUrl = displayProfileImageUrl,
                                clubId = club.id
                            )
                        }
                    }
                }


                // =============================================================
                // EVENTS — SAME CARD DESIGN AS HOME
                // =============================================================

                2 -> {

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 16.dp,
                                    bottom = 10.dp
                                )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Events",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )

                                ClubSectionFilterMenu(
                                    options = listOf("All", "Upcoming", "Ongoing", "Passed"),
                                    selected = eventFilter,
                                    expanded = showEventFilter,
                                    onExpandedChange = { showEventFilter = it },
                                    onSelected = {
                                        eventFilter = it
                                        showEventFilter = false
                                    }
                                )
                            }
                        }
                    }

                    val filteredEvents = clubEvents.filter { event ->
                        eventFilter == "All" || clubContentEventStatus(event, contentNow) == eventFilter
                    }.let { events ->
                        if (eventFilter == "Passed") events.sortedByDescending { it.eventDate?.toDate()?.time ?: 0L }
                        else events.sortedBy { it.eventDate?.toDate()?.time ?: Long.MAX_VALUE }
                    }

                    if (filteredEvents.isEmpty()) {
                        item {
                            Text(
                                text = when (eventFilter) {
                                    "Upcoming" -> "No upcoming events."
                                    "Ongoing" -> "No ongoing events."
                                    "Passed" -> "No passed events."
                                    else -> "No published events yet."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        items(
                            items = filteredEvents,
                            key = { event -> event.id }
                        ) { event ->
                            ClubHomeStyleEventCard(
                                event = event.toClubEventItem().copy(category = displayCategory),
                                sourceEvent = event,
                                now = contentNow,
                                onOpenEvent = { onOpenEvent(event.toClubEventItem().copy(category = displayCategory)) }
                            )
                        }
                    }
                }


                // =============================================================
                // ABOUT
                // =============================================================

                3 -> {

                    item {

                        Column(
                            modifier =
                                Modifier.padding(
                                    16.dp
                                )
                        ) {

                            Text(
                                text =
                                    "About",

                                style =
                                    MaterialTheme.typography.titleMedium,

                                fontWeight =
                                    FontWeight.Bold,

                                color =
                                    TextWhite,

                                modifier =
                                    Modifier.padding(
                                        bottom = 8.dp
                                    )
                            )


                            Text(
                                text =
                                    if (displayDescription.isBlank()) {
                                        "No club description has been added yet."
                                    } else {
                                        displayDescription
                                    },

                                style =
                                    MaterialTheme.typography.bodyLarge,

                                color =
                                    TextMuted
                            )


                            Spacer(
                                modifier =
                                    Modifier.height(
                                        24.dp
                                    )
                            )

                            Text(
                                text = "History",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                modifier = Modifier.padding(
                                    bottom = 8.dp
                                )
                            )

                            Text(
                                text =
                                    if (displayHistory.isBlank()) {
                                        "No club history has been added yet."
                                    } else {
                                        displayHistory
                                    },
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextMuted
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        24.dp
                                    )
                            )


                            // =================================================
                            // LOCATION IN ABOUT SECTION
                            // =================================================

                            Surface(
                                modifier =
                                    Modifier.fillMaxWidth(),

                                color =
                                    SurfaceCardColor,

                                shape =
                                    RoundedCornerShape(
                                        14.dp
                                    )
                            ) {

                                Row(
                                    modifier =
                                        Modifier.padding(
                                            16.dp
                                        ),

                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    Box(
                                        modifier =
                                            Modifier
                                                .size(
                                                    42.dp
                                                )
                                                .clip(
                                                    CircleShape
                                                )
                                                .background(
                                                    AccentPink.copy(
                                                        alpha = 0.15f
                                                    )
                                                ),

                                        contentAlignment =
                                            Alignment.Center
                                    ) {

                                        Icon(
                                            imageVector =
                                                Icons.Outlined.LocationOn,

                                            contentDescription =
                                                null,

                                            tint =
                                                AccentPink,

                                            modifier =
                                                Modifier.size(
                                                    20.dp
                                                )
                                        )
                                    }


                                    Spacer(
                                        modifier =
                                            Modifier.width(
                                                12.dp
                                            )
                                    )


                                    Column {

                                        Text(
                                            text =
                                                "Club location",

                                            color =
                                                TextMuted,

                                            fontSize =
                                                11.sp
                                        )


                                        Spacer(
                                            modifier =
                                                Modifier.height(
                                                    2.dp
                                                )
                                        )


                                        Text(
                                            text =
                                                displayLocation,

                                            color =
                                                TextWhite,

                                            fontSize =
                                                14.sp,

                                            fontWeight =
                                                FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ============================================================================
// COMPACT POP-UP FILTER
// Inspired by the supplied filter modal: one small trigger, options in a popover.
// ============================================================================

@Composable
private fun ClubSectionFilterMenu(
    options: List<String>,
    selected: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelected: (String) -> Unit
) {
    Box {
        Surface(
            modifier = Modifier
                .height(38.dp)
                .clickable { onExpandedChange(true) },
            shape = RoundedCornerShape(19.dp),
            color = HomePostSurface,
            border = BorderStroke(1.dp, HomePostBorder)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.FilterList,
                    contentDescription = "Filter",
                    tint = HomePostMuted,
                    modifier = Modifier.size(17.dp)
                )
                Text(
                    text = if (selected == "All") "Filter" else selected,
                    color = if (selected == "All") HomePostMuted else HomePostAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier
                .background(HomePostSurface, RoundedCornerShape(18.dp))
                .border(1.dp, HomePostBorder, RoundedCornerShape(18.dp))
        ) {
            Text(
                text = "Filter",
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 6.dp)
            )

            options.forEach { option ->
                val isSelected = option == selected
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            color = if (isSelected) HomePostAccent else TextWhite,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    trailingIcon = {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = HomePostAccent,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    },
                    onClick = { onSelected(option) }
                )
            }
        }
    }
}


@Composable
private fun ClubHomeStylePostHeader(
    post: FirestorePost,
    fallbackClubName: String,
    profileImageUrl: String,
    clubId: String
) {
    val clubName = post.clubName.ifBlank { fallbackClubName }
    val initials = post.clubShortName.ifBlank {
        clubName.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.take(1).uppercase() }
    }
    val time = post.createdAt?.let {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(it.toDate())
    } ?: ""

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        listOf(HomePostAccent, Color(0xFFE11111))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (profileImageUrl.isNotBlank()) {
                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = "$clubName profile photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (
                clubId.equals("Coding_club", ignoreCase = true) ||
                clubName.equals("Coding Club", ignoreCase = true)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.code),
                    contentDescription = "$clubName profile photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = initials.take(2).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = clubName,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$time · Club post",
                color = HomePostMuted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ClubHomeStyleTextPostCard(
    post: FirestorePost,
    fallbackClubName: String,
    profileImageUrl: String,
    clubId: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = HomePostSurface),
        border = BorderStroke(1.dp, HomePostBorder)
    ) {
        Column(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp
            )
        ) {
            ClubHomeStylePostHeader(
                post = post,
                fallbackClubName = fallbackClubName,
                profileImageUrl = profileImageUrl,
                clubId = clubId
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (post.text.isNotBlank()) {
                Text(
                    text = post.text,
                    color = Color.White,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            val isVideo = post.type.uppercase().contains("VIDEO") || post.videoUrl.isNotBlank()
            if (isVideo || post.imageUrl.isNotBlank() || post.type.uppercase().contains("PHOTO") || post.type.uppercase().contains("IMAGE")) {
                Spacer(modifier = Modifier.height(14.dp))
                HomePostMedia(url = if (isVideo) post.videoUrl else post.imageUrl,
                    video = isVideo, thumbnailUrl = post.thumbnailUrl,
                    description = "${post.clubName.ifBlank { fallbackClubName }} post",
                    modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(18.dp)))
            }

            Spacer(modifier = Modifier.height(18.dp))

            HorizontalDivider(color = HomePostBorder.copy(alpha = 0.8f))
            ClubHomeStyleShareReportRow(post.id, post.clubName.ifBlank { fallbackClubName })
        }
    }
}

@Composable
private fun ClubHomeStyleShareReportRow(postId: String, clubName: String) {
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
                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, "Check out this post on Campus Pulse")
                    }
                    context.startActivity(android.content.Intent.createChooser(intent, "Share via"))
                }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = "Share",
                tint = HomePostMuted,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Share",
                color = HomePostMuted,
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
                tint = HomePostMuted,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Report",
                color = HomePostMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    if (showReportDialog) {
        ReportContentDialog(
            contentLabel = "this post", targetType = "POST", targetId = postId, societyName = clubName,
            onDismiss = { showReportDialog = false },
            onSubmit = {
                showReportDialog = false
                reportSubmitted = true
            }
        )
    }

    if (reportSubmitted) {
        AlertDialog(
            onDismissRequest = { reportSubmitted = false },
            containerColor = HomePostSurface,
            titleContentColor = Color.White,
            textContentColor = HomePostMuted,
            icon = {
                Icon(Icons.Outlined.Flag, null, tint = HomePostAccent)
            },
            title = { Text("Report submitted", fontWeight = FontWeight.Bold) },
            text = { Text("Thank you. Your report has been submitted for review.") },
            confirmButton = {
                Button(
                    onClick = { reportSubmitted = false },
                    colors = ButtonDefaults.buttonColors(containerColor = HomePostAccent),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Done", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun ClubPostReportDialog(
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

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HomePostSurface,
        titleContentColor = Color.White,
        textContentColor = HomePostMuted,
        icon = {
            Icon(Icons.Outlined.Flag, null, tint = HomePostReportAccent)
        },
        title = { Text("Report this post", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Why are you reporting this?", color = HomePostMuted, fontSize = 13.sp)
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
                                selectedColor = HomePostAccent,
                                unselectedColor = HomePostMuted
                            )
                        )
                        Text(reason, color = Color.White, fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Reports are reviewed by Campus Pulse moderators. Reporting does not automatically remove the content.",
                    color = HomePostMuted,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedReason?.let(onSubmit) },
                enabled = selectedReason != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HomePostAccent,
                    disabledContainerColor = HomePostSoft
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    "Submit report",
                    color = if (selectedReason != null) Color.White else HomePostMuted,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = HomePostMuted)
            }
        }
    )
}



private fun FirestoreEvent.toClubEventItem(): EventItem {
    val schedule = eventDate?.let { timestamp ->
        SimpleDateFormat("EEE, d MMM · HH:mm", Locale.getDefault())
            .format(timestamp.toDate())
    } ?: "Date to be confirmed · Time to be confirmed"

    return EventItem(
        id = id,
        title = title,
        category = Category.ACADEMIC,
        hostClub = clubName.ifBlank { "Campus Club" },
        schedule = schedule,
        venue = location,
        description = description,
        rating = 0.0,
        going = 0,
        isFree = true
    )
}

@Composable
private fun ClubHomeStyleEventCard(
    event: EventItem,
    sourceEvent: FirestoreEvent,
    now: Long,
    onOpenEvent: () -> Unit
) {
    val context = LocalContext.current
    var showReportDialog by remember { mutableStateOf(false) }
    var reportSubmitted by remember { mutableStateOf(false) }

    Card(
        onClick = onOpenEvent,
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .border(
                width = 1.5.dp,
                color = HomePostBorder,
                shape = RoundedCornerShape(26.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            event.category.color,
                            event.category.color.copy(alpha = 0.45f),
                            Color(0xFF021D38).copy(alpha = 0.96f)
                        )
                    )
                )
        ) {
            HomeMediaImage(sourceEvent.posterImageUrl, "${event.title} poster", Modifier.fillMaxSize(), ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(
                Color.Black.copy(alpha = 0.12f), Color.Black.copy(alpha = 0.35f), Color(0xFF021D38).copy(alpha = 0.95f)
            ))))
            Box(modifier = Modifier.padding(16.dp)) {
                CategoryChip(category = event.category)
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                val status = clubContentEventStatus(sourceEvent, now)
                Text(status, color = if (status == "Ongoing") Color(0xFF66BB6A) else HomePostAccent,
                    fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = event.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = event.description,
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ClubEventStatColumn(label = "Date") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.DateRange,
                                contentDescription = null,
                                tint = Color(0xFFF7941D),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = event.schedule.substringBefore("·").trim(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    ClubEventStatColumn(label = "Time") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.AccessTime,
                                contentDescription = null,
                                tint = Color(0xFF38A9E0),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = event.schedule.substringAfter("·", "Time to be confirmed").trim(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    ClubEventStatColumn(label = "Share") {
                        Row(
                            modifier = Modifier.clickable { shareEvent(context, event) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share event",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Send",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onOpenEvent,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = "View Event",
                            color = Color(0xFF021D38),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "Current Club",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.20f))
                            .clickable { showReportDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Flag,
                            contentDescription = "Report event",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }

    if (showReportDialog) {
        ReportContentDialog(
            contentLabel = "this event", targetType = "EVENT", targetId = event.id, societyName = event.hostClub,
            onDismiss = { showReportDialog = false },
            onSubmit = {
                showReportDialog = false
                reportSubmitted = true
            }
        )
    }

    if (reportSubmitted) {
        AlertDialog(
            onDismissRequest = { reportSubmitted = false },
            containerColor = HomePostSurface,
            titleContentColor = Color.White,
            textContentColor = HomePostMuted,
            icon = { Icon(Icons.Outlined.Flag, null, tint = HomePostAccent) },
            title = { Text("Report submitted", fontWeight = FontWeight.Bold) },
            text = { Text("Thank you. Your report has been submitted for review.") },
            confirmButton = {
                Button(
                    onClick = { reportSubmitted = false },
                    colors = ButtonDefaults.buttonColors(containerColor = HomePostAccent),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Done", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun RowScope.ClubEventStatColumn(
    label: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.Start
    ) {
        content()
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = HomePostMuted,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun ClubEventReportDialog(
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

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HomePostSurface,
        titleContentColor = Color.White,
        textContentColor = HomePostMuted,
        icon = { Icon(Icons.Outlined.Flag, null, tint = HomePostReportAccent) },
        title = { Text("Report this event", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Why are you reporting this?", color = HomePostMuted, fontSize = 13.sp)
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
                                selectedColor = HomePostAccent,
                                unselectedColor = HomePostMuted
                            )
                        )
                        Text(reason, color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedReason?.let(onSubmit) },
                enabled = selectedReason != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HomePostAccent,
                    disabledContainerColor = HomePostSoft
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    "Submit report",
                    color = if (selectedReason != null) Color.White else HomePostMuted,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = HomePostMuted)
            }
        }
    )
}

private fun formatClubEventDate(
    event: FirestoreEvent
): String {

    val timestamp =
        event.eventDate
            ?: return "Date to be confirmed"

    val formatter =
        SimpleDateFormat(
            "EEE, d MMM yyyy • HH:mm",
            Locale.getDefault()
        )

    return formatter.format(
        timestamp.toDate()
    )
}


// =============================================================================
// TAB ICON BUTTON
// =============================================================================

@Composable
private fun TabIconButton(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {

    Surface(
        color =
            if (isSelected) {

                AccentPink.copy(
                    alpha = 0.15f
                )

            } else {

                Color.Transparent
            },

        shape =
            RoundedCornerShape(
                12.dp
            ),

        modifier =
            Modifier
                .size(
                    44.dp
                )
                .clickable(
                    onClick = onClick
                )
    ) {

        Box(
            contentAlignment =
                Alignment.Center
        ) {

            Icon(
                imageVector =
                    icon,

                contentDescription =
                    null,

                tint =
                    if (isSelected) {
                        AccentPink
                    } else {
                        TextMuted
                    },

                modifier =
                    Modifier.size(
                        22.dp
                    )
            )
        }
    }
}


// =============================================================================
// STAT COLUMN
// =============================================================================

@Composable
private fun RowScope.StatCol(
    value: String,
    label: String
) {

    Column(
        horizontalAlignment =
            Alignment.CenterHorizontally,

        modifier =
            Modifier.weight(
                1f
            )
    ) {

        Text(
            text =
                value,

            fontWeight =
                FontWeight.Bold,

            style =
                MaterialTheme.typography.headlineSmall,

            color =
                TextWhite
        )


        Spacer(
            modifier =
                Modifier.height(
                    2.dp
                )
        )


        Text(
            text =
                label,

            style =
                MaterialTheme.typography.bodySmall,

            color =
                TextMuted
        )
    }
}


// =============================================================================
// PREVIEW
// =============================================================================

@Preview(
    name = "Club Account - Back + Location",
    uiMode =
        Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun ClubAccountScreenPreview() {

    CampusPulseTheme {

        ClubAccountScreen(
            club =
                SampleData.clubs.first(),

            onBack = {},

            onManage = {},

            onNavigate = {}
        )
    }
}

private fun clubContentEventStatus(event: FirestoreEvent, now: Long): String {
    val start = event.eventDate?.toDate()?.time
    val end = event.eventEndDate?.toDate()?.time
    return when {
        start == null -> "Schedule not set"
        now < start -> "Upcoming"
        end == null || end <= start -> "End time not set"
        now < end -> "Ongoing"
        else -> "Passed"
    }
}

@Composable
private fun ClubLeaderPhoto(leader: FirestoreClubLeader, modifier: Modifier = Modifier) {
    var failed by remember(leader.photoUrl) { mutableStateOf(false) }
    Box(modifier.clip(CircleShape).background(Color(0xFF0B3B68)), contentAlignment = Alignment.Center) {
        Text(leader.name.trim().split(Regex("\\s+")).take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString(""),
            color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        if (leader.photoUrl.isNotBlank() && !failed) {
            AsyncImage(model = leader.photoUrl, contentDescription = "${leader.name} profile photo",
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, onError = { failed = true })
        }
    }
}

/** Profile card: dismiss with Android Back or a tap outside the card. */
@Composable
private fun LeadershipProfileDialog(
    leader: FirestoreClubLeader,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = Color(0xFF062F59),
            border = BorderStroke(1.dp, Color(0xFF315574)),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(Modifier.fillMaxWidth()) {
                    Box(
                        Modifier.fillMaxWidth().height(126.dp).background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0B3B68), Color(0xFF315574), Color(0xFFF7941D))
                            )
                        )
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(top = 88.dp, start = 10.dp, end = 10.dp),
                        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                        color = Color(0xFF062F59)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, bottom = 22.dp)) {
                            // Reserve space for the portrait overlapping the header.
                            Spacer(Modifier.height(70.dp))
                            Text(
                                text = leader.name,
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (leader.role.isNotBlank()) {
                                Spacer(Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(50.dp),
                                    color = Color(0xFFF7941D).copy(alpha = 0.16f),
                                    border = BorderStroke(1.dp, Color(0xFFF7941D).copy(alpha = 0.45f))
                                ) {
                                    Text(
                                        text = leader.role,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                        color = Color(0xFFF7941D),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            if (leader.bio.isNotBlank()) {
                                Spacer(Modifier.height(20.dp))
                                Text(
                                    text = leader.bio,
                                    color = Color(0xFFD5E2EF),
                                    fontSize = 15.sp,
                                    lineHeight = 23.sp
                                )
                            }
                        }
                    }
                    Surface(
                        modifier = Modifier.padding(start = 28.dp, top = 42.dp).size(106.dp),
                        shape = CircleShape,
                        color = Color(0xFF062F59),
                        border = BorderStroke(4.dp, Color(0xFF062F59)),
                        shadowElevation = 6.dp
                    ) {
                        ClubLeaderPhoto(leader, Modifier.fillMaxSize().padding(4.dp))
                    }
                }
            }
        }
    }
}
