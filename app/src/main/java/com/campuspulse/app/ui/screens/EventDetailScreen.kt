@file:OptIn(ExperimentalMaterial3Api::class)

package com.campuspulse.app.ui.screens

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.Date

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import com.campuspulse.app.model.Category
import com.campuspulse.app.model.EventItem
import com.campuspulse.app.ui.components.CategoryChip
import com.campuspulse.app.ui.components.NavDestination
import com.campuspulse.app.ui.components.shareEvent
import com.campuspulse.app.ui.theme.CampusPulseTheme


// =============================================================================
// COLORS
// =============================================================================

private val DarkBackground = Color(0xFF0A0A0A)

private val ChipBackground = Color(0xFF1E1E1E)

private val TextWhite = Color(0xFFFFFFFF)

private val TextMuted = Color(0xFFB0B0B0)

private val AccentGradientStart = Color(0xFF4A148C)

private val AccentGradientEnd = Color(0xFFE53935)

private val VerifiedBlue = Color(0xFF2196F3)


// =============================================================================
// EVENT DETAIL SCREEN
// =============================================================================

@Composable
fun EventDetailScreen(
    event: EventItem,
    onNavigate: (NavDestination) -> Unit,
    onNavigateToClubHome: () -> Unit = {},
    rsvpUrl: String = "https://campuspulse.app/rsvp",
    onBack: () -> Unit
) {

    val context = LocalContext.current

    val database = remember { FirebaseFirestore.getInstance() }
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var savedEvent by remember(event.id) { mutableStateOf<DocumentSnapshot?>(null) }
    var eventLoading by remember(event.id) { mutableStateOf(true) }
    var eventError by remember(event.id) { mutableStateOf<String?>(null) }
    var retry by remember(event.id) { mutableStateOf(0) }
    var clubPhoto by remember(event.id) { mutableStateOf("") }
    var clubCategory by remember(event.id) { mutableStateOf(event.category) }
    var rsvpStatus by remember(event.id, userId) { mutableStateOf("") }
    var responseLoading by remember(event.id, userId) { mutableStateOf(userId != null) }
    var responseError by remember(event.id, userId) { mutableStateOf<String?>(null) }
    var saving by remember(event.id, userId) { mutableStateOf(false) }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(1000) } }
    DisposableEffect(event.id, retry) {
        eventLoading = true
        eventError = null
        val listener = database.collection("events").document(event.id).addSnapshotListener { snapshot, error ->
            eventLoading = false
            if (error != null) { eventError = "Unable to load this event. Check your connection." }
            else if (snapshot == null || !snapshot.exists()) { savedEvent = null; eventError = "This event is no longer available." }
            else { savedEvent = snapshot; eventError = null }
        }
        onDispose { listener.remove() }
    }
    val clubId = savedEvent?.getString("clubId").orEmpty()
    DisposableEffect(clubId, retry) {
        clubPhoto = ""
        clubCategory = event.category
        val listener = if (clubId.isNotBlank()) database.collection("clubs").document(clubId)
            .addSnapshotListener { snapshot, _ ->
                clubPhoto = snapshot?.getString("profileImageUrl").orEmpty()
                val categoryName = snapshot?.getString("category")
                clubCategory = Category.values().firstOrNull { it.name.equals(categoryName, true) } ?: event.category
            } else null
        onDispose { listener?.remove() }
    }
    DisposableEffect(event.id, userId, retry) {
        responseLoading = userId != null
        responseError = null
        val listener = if (userId != null) database.collection("events").document(event.id)
            .collection("rsvps").document(userId).addSnapshotListener { snapshot, error ->
                responseLoading = false
                if (error != null) responseError = "Unable to load your response. Try again."
                else { rsvpStatus = when (snapshot?.getString("response")) {
                    "GOING" -> "Going"; "INTERESTED" -> "Interested"; "NOT_GOING" -> "Can't go"; else -> ""
                }; responseError = null }
            } else null
        onDispose { listener?.remove() }
    }
    val start = savedEvent?.getTimestamp("eventDate")?.toDate()?.time
    val end = savedEvent?.getTimestamp("eventEndDate")?.toDate()?.time
    val eventStatus = when {
        start == null -> "Schedule not set"
        now < start -> "Upcoming"
        end == null || end <= start -> "End time not set"
        now < end -> "Ongoing"
        else -> "Passed"
    }
    val displayEvent = event.copy(
        title = savedEvent?.getString("title") ?: event.title,
        hostClub = savedEvent?.getString("clubName") ?: event.hostClub,
        description = savedEvent?.getString("description") ?: event.description,
        venue = savedEvent?.getString("location") ?: event.venue,
        category = clubCategory,
        schedule = start?.let { detailDate(it) } ?: event.schedule
    )
    val posterUrl = savedEvent?.getString("posterUrl")?.takeIf { it.isNotBlank() }
        ?: savedEvent?.getString("imageUrl")?.takeIf { it.isNotBlank() }
        ?: if (savedEvent?.getString("mediaType")?.uppercase() != "VIDEO") savedEvent?.getString("mediaUrl").orEmpty() else ""
    val canRespond = !eventLoading && eventError == null && savedEvent?.getBoolean("rsvpEnabled") == true
            && savedEvent?.getBoolean("isActive") == true && savedEvent?.getString("status") == "PUBLISHED"
            && userId != null && !responseLoading && responseError == null && !saving
    fun saveResponse(option: String) {
        if (!canRespond || userId == null) return
        saving = true
        responseError = null
        val value = when(option) { "Going" -> "GOING"; "Interested" -> "INTERESTED"; else -> "NOT_GOING" }
        database.collection("events").document(event.id).collection("rsvps").document(userId)
            .set(mapOf("response" to value, "updatedAt" to FieldValue.serverTimestamp()))
            .addOnSuccessListener { rsvpStatus = option; saving = false }
            .addOnFailureListener { saving = false; responseError = "Your response could not be saved. Tap Retry and try again." }
    }

    var heroFullScreen by remember {
        mutableStateOf(false)
    }


    // =========================================================================
    // ROOT
    //
    // IMPORTANT:
    // We deliberately do NOT use safeDrawingPadding() here.
    //
    // The hero needs to extend behind the status bar.
    // Only the controls inside the hero use statusBarsPadding().
    // =========================================================================

    Scaffold(
        containerColor = DarkBackground
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = padding.calculateBottomPadding()
                )
                .verticalScroll(
                    rememberScrollState()
                )
        ) {


            // =================================================================
            // HERO
            // =================================================================

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .clickable {
                        heroFullScreen = true
                    }
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(

                                displayEvent.category.color,

                                displayEvent.category.color.copy(
                                    alpha = 0.82f
                                ),

                                displayEvent.category.color.copy(
                                    alpha = 0.45f
                                ),

                                DarkBackground
                            )
                        )
                    )
            ) {


                DetailPoster(posterUrl, Modifier.fillMaxSize(), ContentScale.Crop)
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
                    listOf(Color.Black.copy(alpha = 0.15f), Color.Transparent, DarkBackground)
                )))

                // =============================================================
                // BACK BUTTON
                //
                // statusBarsPadding() protects the button,
                // NOT the hero background.
                // =============================================================

                Box(
                    modifier = Modifier
                        .align(
                            Alignment.TopStart
                        )
                        .statusBarsPadding()
                        .padding(
                            top = 16.dp,
                            start = 20.dp
                        )
                ) {

                    IconButtonGlass(
                        icon =
                            Icons.AutoMirrored.Filled.ArrowBack,

                        contentDescription =
                            "Back",

                        onClick =
                            onBack
                    )
                }


                // =============================================================
                // SHARE BUTTON
                // =============================================================

                Box(
                    modifier = Modifier
                        .align(
                            Alignment.TopEnd
                        )
                        .statusBarsPadding()
                        .padding(
                            top = 16.dp,
                            end = 20.dp
                        )
                ) {

                    IconButtonGlass(
                        icon =
                            Icons.Filled.Share,

                        contentDescription =
                            "Share",

                        onClick = {

                            shareEvent(
                                context,
                                displayEvent
                            )
                        }
                    )
                }


                // =============================================================
                // EVENT INFORMATION
                // =============================================================

                Column(
                    modifier = Modifier
                        .align(
                            Alignment.BottomStart
                        )
                        .fillMaxWidth()
                        .padding(
                            start = 20.dp,
                            end = 20.dp,
                            bottom = 20.dp
                        )
                ) {


                    // =========================================================
                    // CATEGORY
                    // =========================================================

                    CategoryChip(
                        category =
                            displayEvent.category
                    )


                    Spacer(
                        modifier =
                            Modifier.height(
                                10.dp
                            )
                    )


                    // =========================================================
                    // TITLE + VERIFIED
                    // =========================================================

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Text(
                            text =
                                displayEvent.title,

                            style =
                                MaterialTheme.typography.headlineSmall,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                TextWhite,

                            modifier =
                                Modifier.weight(
                                    1f
                                )
                        )


                        Spacer(
                            modifier =
                                Modifier.width(
                                    8.dp
                                )
                        )


                        Box(
                            modifier = Modifier
                                .size(
                                    19.dp
                                )
                                .clip(
                                    CircleShape
                                )
                                .background(
                                    VerifiedBlue
                                ),

                            contentAlignment =
                                Alignment.Center
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Filled.Check,

                                contentDescription =
                                    "Verified Host",

                                tint =
                                    TextWhite,

                                modifier =
                                    Modifier.size(
                                        12.dp
                                    )
                            )
                        }
                    }


                    // =========================================================
                    // HOST
                    // =========================================================

                    Text(
                        text =
                            "Hosted by ${displayEvent.hostClub}",

                        style =
                            MaterialTheme.typography.bodyMedium,

                        color =
                            TextMuted,

                        modifier =
                            Modifier.padding(
                                top = 3.dp
                            )
                    )


                    Spacer(
                        modifier =
                            Modifier.height(
                                16.dp
                            )
                    )


                    // =========================================================
                    // DECORATIVE INDICATOR
                    // =========================================================

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                vertical = 4.dp
                            ),

                        horizontalArrangement =
                            Arrangement.spacedBy(
                                6.dp
                            )
                    ) {

                        Box(
                            modifier = Modifier
                                .weight(
                                    1f
                                )
                                .height(
                                    3.dp
                                )
                                .clip(
                                    RoundedCornerShape(
                                        2.dp
                                    )
                                )
                                .background(
                                    AccentGradientEnd
                                )
                        )


                        Box(
                            modifier = Modifier
                                .weight(
                                    1f
                                )
                                .height(
                                    3.dp
                                )
                                .clip(
                                    RoundedCornerShape(
                                        2.dp
                                    )
                                )
                                .background(
                                    TextWhite.copy(
                                        alpha = 0.20f
                                    )
                                )
                        )
                    }
                }
            }


            // =================================================================
            // LOWER CONTENT
            // =================================================================

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        DarkBackground
                    )
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = 18.dp,
                        bottom = 28.dp
                    )
            ) {


                if (eventLoading) {
                    CircularProgressIndicator(color = AccentGradientEnd, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.height(12.dp))
                }
                eventError?.let { message ->
                    Text(message, color = TextMuted)
                    Button(onClick = { retry++ }) { Text("Retry") }
                }
                Text(eventStatus, color = if (eventStatus == "Ongoing") Color(0xFF66BB6A) else AccentGradientEnd,
                    fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))

                // =============================================================
                // RSVP + CLUB HOME
                // =============================================================

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            12.dp
                        ),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {


                    // =========================================================
                    // RSVP
                    // =========================================================

                    Button(
                        onClick = { saveResponse("Going") },
                        enabled = canRespond,

                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    TextWhite
                            ),

                        shape =
                            RoundedCornerShape(
                                26.dp
                            ),

                        modifier = Modifier
                            .weight(
                                1f
                            )
                            .height(
                                52.dp
                            )
                    ) {

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Text(
                                text =
                                    if (saving) "Saving…" else if (rsvpStatus.isNotBlank()) rsvpStatus else "RSVP",

                                color =
                                    Color.Black,

                                fontWeight =
                                    FontWeight.Bold,

                                fontSize =
                                    15.sp
                            )


                            Spacer(
                                modifier =
                                    Modifier.width(
                                        7.dp
                                    )
                            )


                            Icon(
                                imageVector =
                                    Icons.Filled.Check,

                                contentDescription =
                                    "Save RSVP",

                                tint =
                                    Color.Black,

                                modifier =
                                    Modifier.size(
                                        16.dp
                                    )
                            )
                        }
                    }


                    // =========================================================
                    // CLUB HOME
                    // =========================================================

                    Box(
                        modifier = Modifier
                            .size(
                                52.dp
                            )
                            .clip(
                                CircleShape
                            )
                            .background(
                                ChipBackground
                            )
                            .border(
                                width =
                                    0.5.dp,

                                color =
                                    Color(
                                        0xFF2C2C2E
                                    ),

                                shape =
                                    CircleShape
                            )
                            .clickable {

                                onNavigateToClubHome()
                            },

                        contentAlignment =
                            Alignment.Center
                    ) {

                        if (clubPhoto.isNotBlank()) {
                            AsyncImage(model = clubPhoto, contentDescription = "${displayEvent.hostClub} profile photo",
                                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        } else {
                            Icon(
                                imageVector =
                                    Icons.Filled.Home,

                                contentDescription =
                                    "Go to Club Home",

                                tint =
                                    TextWhite,

                                modifier =
                                    Modifier.size(
                                        21.dp
                                    )
                            )
                        }
                    }
                }


                Spacer(
                    modifier =
                        Modifier.height(
                            20.dp
                        )
                )


                HorizontalDivider(
                    color =
                        Color(
                            0xFF2C2C2E
                        ),

                    thickness =
                        1.dp
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            22.dp
                        )
                )


                // =============================================================
                // ABOUT EVENT
                // =============================================================

                Text(
                    text =
                        "About Event",

                    style =
                        MaterialTheme.typography.titleMedium,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        TextWhite
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            7.dp
                        )
                )


                Text(
                    text =
                        displayEvent.description,

                    style =
                        MaterialTheme.typography.bodyMedium,

                    color =
                        TextMuted,

                    lineHeight =
                        20.sp
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            24.dp
                        )
                )


                // =============================================================
                // EVENT BASICS
                // =============================================================

                Text(
                    text =
                        "Event Basics",

                    style =
                        MaterialTheme.typography.titleMedium,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        TextWhite
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            12.dp
                        )
                )


                // =============================================================
                // DATE
                // =============================================================

                MetaInformationRow(
                    icon =
                        Icons.Outlined.DateRange,

                    title =
                        "Starts",

                    value =
                        displayEvent.schedule
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            10.dp
                        )
                )


                MetaInformationRow(Icons.Outlined.DateRange, "Ends",
                    end?.let { detailDate(it) } ?: "End time not set")
                Spacer(Modifier.height(10.dp))

                // =============================================================
                // LOCATION
                // =============================================================

                MetaInformationRow(
                    icon =
                        Icons.Outlined.LocationOn,

                    title =
                        "Location",

                    value =
                        displayEvent.venue
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            26.dp
                        )
                )


                HorizontalDivider(
                    color =
                        Color(
                            0xFF2C2C2E
                        ),

                    thickness =
                        1.dp
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            22.dp
                        )
                )


                // =============================================================
                // YOUR RESPONSE
                // =============================================================

                Text(
                    text =
                        "Your Response",

                    style =
                        MaterialTheme.typography.titleMedium,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        TextWhite
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            12.dp
                        )
                )


                val responseNotice = when {
                    saving -> "Saving your response…"
                    responseLoading -> "Loading your response…"
                    userId == null -> "Sign in to RSVP."
                    savedEvent?.getBoolean("rsvpEnabled") != true -> "RSVP is not enabled for this event."
                    rsvpStatus.isBlank() -> "You have not responded yet."
                    else -> "Your saved response: $rsvpStatus"
                }
                Text(responseNotice, color = TextMuted, style = MaterialTheme.typography.bodySmall)
                responseError?.let { Text(it, color = AccentGradientEnd); Button(onClick = { retry++ }) { Text("Retry") } }
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            8.dp
                        )
                ) {

                    listOf(
                        "Going",
                        "Interested",
                        "Can't go"
                    ).forEach { option ->

                        val isSelected =
                            option == rsvpStatus


                        Surface(
                            onClick = {

                                saveResponse(option)
                            },

                            enabled = canRespond,
                            color =
                                if (isSelected) {

                                    AccentGradientStart.copy(
                                        alpha = 0.45f
                                    )

                                } else {

                                    ChipBackground
                                },

                            border =
                                BorderStroke(
                                    width =
                                        1.dp,

                                    color =
                                        if (isSelected) {

                                            AccentGradientEnd

                                        } else {

                                            Color(
                                                0xFF2C2C2E
                                            )
                                        }
                                ),

                            shape =
                                RoundedCornerShape(
                                    16.dp
                                ),

                            modifier =
                                Modifier.weight(
                                    1f
                                )
                        ) {

                            Text(
                                text =
                                    option,

                                color =
                                    if (isSelected) {
                                        TextWhite
                                    } else {
                                        TextMuted
                                    },

                                fontWeight =
                                    FontWeight.Bold,

                                style =
                                    MaterialTheme.typography.bodyMedium,

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        vertical =
                                            13.dp
                                    ),

                                textAlign =
                                    TextAlign.Center
                            )
                        }
                    }
                }


                Spacer(
                    modifier =
                        Modifier.height(
                            36.dp
                        )
                )
            }
        }
    }


    // =========================================================================
    // FULL SCREEN HERO PREVIEW
    // =========================================================================

    if (heroFullScreen) {

        Dialog(
            onDismissRequest = {

                heroFullScreen =
                    false
            },

            properties =
                DialogProperties(
                    usePlatformDefaultWidth =
                        false
                )
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(

                                    displayEvent.category.color,

                                    displayEvent.category.color.copy(
                                        alpha = 0.70f
                                    ),

                                    DarkBackground
                                )
                        )
                    )
                    .clickable {

                        heroFullScreen =
                            false
                    },

                contentAlignment =
                    Alignment.Center
            ) {

                DetailPoster(posterUrl, Modifier.fillMaxSize(), ContentScale.Fit)
                if (posterUrl.isBlank())
                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally,

                        modifier =
                            Modifier.padding(
                                24.dp
                            )
                    ) {

                        Text(
                            text =
                                displayEvent.title,

                            color =
                                TextWhite,

                            fontSize =
                                28.sp,

                            fontWeight =
                                FontWeight.Bold,

                            textAlign =
                                TextAlign.Center
                        )


                        Spacer(
                            modifier =
                                Modifier.height(
                                    10.dp
                                )
                        )


                        Text(
                            text =
                                "No event poster available",

                            color =
                                TextMuted
                        )


                        Spacer(
                            modifier =
                                Modifier.height(
                                    20.dp
                                )
                        )


                        Text(
                            text =
                                "Tap anywhere to close",

                            color =
                                TextWhite,

                            fontSize =
                                12.sp
                        )
                    }
            }
        }
    }
}


// =============================================================================
// GLASS ICON BUTTON
// =============================================================================

@Composable
private fun IconButtonGlass(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .size(
                42.dp
            )
            .clip(
                CircleShape
            )
            .background(
                Color.Black.copy(
                    alpha = 0.38f
                )
            )
            .border(
                width =
                    0.5.dp,

                color =
                    TextWhite.copy(
                        alpha = 0.12f
                    ),

                shape =
                    CircleShape
            )
            .clickable(
                onClick =
                    onClick
            ),

        contentAlignment =
            Alignment.Center
    ) {

        Icon(
            imageVector =
                icon,

            contentDescription =
                contentDescription,

            tint =
                TextWhite,

            modifier =
                Modifier.size(
                    19.dp
                )
        )
    }
}


// =============================================================================
// EVENT META INFORMATION
// =============================================================================

@Composable
private fun MetaInformationRow(
    icon: ImageVector,
    title: String,
    value: String
) {

    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        color =
            ChipBackground,

        shape =
            RoundedCornerShape(
                16.dp
            ),

        border =
            BorderStroke(
                width =
                    0.5.dp,

                color =
                    Color(
                        0xFF2C2C2E
                    )
            )
    ) {

        Row(
            modifier =
                Modifier.padding(
                    horizontal =
                        15.dp,

                    vertical =
                        13.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(
                        38.dp
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        AccentGradientStart.copy(
                            alpha = 0.35f
                        )
                    ),

                contentAlignment =
                    Alignment.Center
            ) {

                Icon(
                    imageVector =
                        icon,

                    contentDescription =
                        null,

                    tint =
                        TextWhite,

                    modifier =
                        Modifier.size(
                            18.dp
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
                        title,

                    color =
                        TextMuted,

                    fontSize =
                        10.sp
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            2.dp
                        )
                )


                Text(
                    text =
                        value,

                    color =
                        TextWhite,

                    fontWeight =
                        FontWeight.SemiBold,

                    fontSize =
                        13.sp
                )
            }
        }
    }
}


// =============================================================================
// ORIGINAL META CHIP
//
// Kept in the file in case another section of the project still uses it.
// =============================================================================

@Composable
private fun MetaChip(
    icon: ImageVector,
    label: String
) {

    Surface(
        color =
            ChipBackground,

        shape =
            RoundedCornerShape(
                18.dp
            ),

        modifier =
            Modifier.border(
                width =
                    0.5.dp,

                color =
                    Color(
                        0xFF2C2C2E
                    ),

                shape =
                    RoundedCornerShape(
                        18.dp
                    )
            )
    ) {

        Row(
            verticalAlignment =
                Alignment.CenterVertically,

            modifier =
                Modifier.padding(
                    horizontal =
                        14.dp,

                    vertical =
                        10.dp
                )
        ) {

            Icon(
                imageVector =
                    icon,

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


            Text(
                text =
                    label,

                color =
                    TextWhite,

                fontWeight =
                    FontWeight.Medium,

                fontSize =
                    12.sp
            )
        }
    }
}


// =============================================================================
// PREVIEW
// =============================================================================

@Preview(
    name = "Event Detail - Edge To Edge",
    uiMode =
        Configuration.UI_MODE_NIGHT_YES,
    showBackground =
        true
)
@Composable
private fun EventDetailScreenPreview() {

    val sampleEvent =
        EventItem(
            id =
                "1",

            title =
                "Zulu Pride Heritage Night",

            hostClub =
                "Zulu Pride Cultural Society",

            schedule =
                "Wed · 18:00",

            venue =
                "Great Hall",

            description =
                "Choir, dance & food, open to everyone.",

            category =
                Category.CULTURE,

            rating =
                4.5,

            going =
                120
        )


    CampusPulseTheme {

        EventDetailScreen(
            event =
                sampleEvent,

            onNavigate = {
                // Preview only
            },

            onNavigateToClubHome = {
                // Preview only
            },

            rsvpUrl =
                "https://campuspulse.app/rsvp",

            onBack = {
                // Preview only
            }
        )
    }
}

private fun detailDate(millis: Long): String =
    SimpleDateFormat("EEEE, d MMM yyyy • HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Africa/Johannesburg")
    }.format(Date(millis))

@Composable
private fun DetailPoster(url: String, modifier: Modifier, scale: ContentScale) {
    var loading by remember(url) { mutableStateOf(url.isNotBlank()) }
    var failed by remember(url) { mutableStateOf(false) }
    Box(modifier, contentAlignment = Alignment.Center) {
        if (url.isBlank() || failed) {
            Icon(Icons.Outlined.DateRange, "No event poster available", tint = TextWhite.copy(alpha = 0.35f),
                modifier = Modifier.size(64.dp))
        }
        if (url.isNotBlank()) AsyncImage(model = url, contentDescription = "Event poster",
            contentScale = scale, modifier = Modifier.fillMaxSize(),
            onLoading = { loading = true; failed = false },
            onSuccess = { loading = false; failed = false },
            onError = { loading = false; failed = true })
        if (loading) CircularProgressIndicator(color = AccentGradientEnd, modifier = Modifier.size(28.dp))
    }
}
