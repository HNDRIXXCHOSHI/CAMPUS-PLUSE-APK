package com.campuspulse.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.campuspulse.app.R
import com.campuspulse.app.data.FirestoreClub
import com.campuspulse.app.data.HomeRepository
import com.campuspulse.app.model.Category
import com.campuspulse.app.model.Club
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private val PageColor = Color(0xFF021D38)
private val CardColor = Color(0xFF05294B)
private val BorderColor = Color(0xFF315574)
private val MutedColor = Color(0xFFB9C9D8)
private val OrangeColor = Color(0xFFF7941D)
private val BlueColor = Color(0xFF38A9E0)

@Suppress("UNUSED_PARAMETER")
@Composable
fun MyClubsScreen(
    clubs: List<Club>,
    onOpenClub: (Club) -> Unit,
    onCreateClub: () -> Unit = {},
    scrollToTopSignal: Int = 0
) {
    // Existing navigation may pass sample clubs.
    // This screen deliberately uses only Firestore clubs and memberships.
    val preview = LocalInspectionMode.current
    val auth = remember(preview) {
        if (preview) null else FirebaseAuth.getInstance()
    }
    val db = remember(preview) {
        if (preview) null else FirebaseFirestore.getInstance()
    }
    val repository = remember(preview) {
        if (preview) null else HomeRepository()
    }

    var uid by remember(auth) {
        mutableStateOf(auth?.currentUser?.uid)
    }
    var liveClubs by remember {
        mutableStateOf<List<FirestoreClub>>(emptyList())
    }
    var joinedIds by remember {
        mutableStateOf<Set<String>>(emptySet())
    }
    var loadingClubs by remember { mutableStateOf(false) }
    var loadingMemberships by remember { mutableStateOf(false) }
    var clubsError by remember { mutableStateOf<String?>(null) }
    var membershipError by remember { mutableStateOf<String?>(null) }
    var retry by remember { mutableStateOf(0) }
    var grid by rememberSaveable { mutableStateOf(true) }
    var showSearch by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }

    val listState = rememberLazyListState()

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener {
            uid = it.currentUser?.uid
        }
        auth?.addAuthStateListener(listener)
        onDispose { auth?.removeAuthStateListener(listener) }
    }

    DisposableEffect(repository, db, uid, retry) {
        liveClubs = emptyList()
        joinedIds = emptySet()
        clubsError = null
        membershipError = null
        loadingClubs = false
        loadingMemberships = false

        val userId = uid
        if (repository == null || db == null || userId == null) {
            return@DisposableEffect onDispose {}
        }

        loadingClubs = true
        loadingMemberships = true

        val clubListener = repository.listenToClubs(
            onSuccess = {
                liveClubs = it
                clubsError = null
                loadingClubs = false
            },
            onError = {
                liveClubs = emptyList()
                clubsError = it
                loadingClubs = false
            }
        )

        val memberListener = db.collection("clubMembers")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "ACTIVE")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    joinedIds = emptySet()
                    membershipError =
                        error.message ?: "Unable to load memberships."
                } else {
                    joinedIds = snapshot?.documents
                        ?.mapNotNull {
                            it.getString("clubId")
                                ?.takeIf { id -> id.isNotBlank() }
                        }?.toSet() ?: emptySet()
                    membershipError = null
                }
                loadingMemberships = false
            }

        onDispose {
            clubListener.remove()
            memberListener.remove()
        }
    }

    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > 0) {
            listState.animateScrollToItem(0)
        }
    }

    val joined = remember(liveClubs, joinedIds) {
        liveClubs.filter {
            it.id in joinedIds &&
                    it.status == "APPROVED" &&
                    it.isActive
        }.distinctBy { it.id }.sortedBy { it.name.lowercase() }
    }

    val filtered = remember(joined, query) {
        val text = query.trim()
        joined.filter {
            text.isEmpty() ||
                    it.name.contains(text, true) ||
                    it.category.contains(text, true)
        }
    }

    val loading = loadingClubs || loadingMemberships
    val error = clubsError ?: membershipError

    Column(
        Modifier.fillMaxSize()
            .background(PageColor)
            .safeDrawingPadding()
    ) {
        Row(
            Modifier.fillMaxWidth()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 22.dp,
                    bottom = 22.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "My Clubs",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    when {
                        uid == null -> "Sign in to view your clubs"
                        loading -> "Loading your clubs…"
                        error != null -> "Unable to load clubs"
                        joined.size == 1 -> "1 club joined"
                        else -> "${joined.size} clubs joined"
                    },
                    color = MutedColor,
                    fontSize = 13.sp
                )
            }

            ClubControl(
                selected = showSearch,
                onClick = {
                    showSearch = !showSearch
                    if (!showSearch) query = ""
                }
            ) {
                Icon(
                    if (showSearch) Icons.Default.Close
                    else Icons.Default.Search,
                    contentDescription = "Toggle search",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            ClubControl(onClick = { grid = !grid }) {
                Icon(
                    if (grid) Icons.Default.ViewList
                    else Icons.Default.GridView,
                    contentDescription = "Switch club layout",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            ClubControl(selected = true, onClick = onCreateClub) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Create a club",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        AnimatedVisibility(showSearch) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                placeholder = { Text("Search your clubs") },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedPlaceholderColor = MutedColor,
                    unfocusedPlaceholderColor = MutedColor,
                    cursorColor = OrangeColor,
                    focusedBorderColor = OrangeColor,
                    unfocusedBorderColor = BorderColor
                )
            )
        }

        when {
            uid == null -> ClubMessage(
                "Sign in to see the clubs you have joined."
            )
            loading -> Box(
                Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = OrangeColor)
            }
            error != null -> Column(
                Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    error,
                    color = MutedColor,
                    textAlign = TextAlign.Center
                )
                TextButton(onClick = { retry++ }) {
                    Text("Retry", color = OrangeColor)
                }
            }
            filtered.isEmpty() -> ClubMessage(
                if (query.isNotBlank()) "No matching clubs found."
                else "No joined clubs yet.\nClubs you join will appear here."
            )
            else -> LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 2.dp,
                    bottom = 110.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (grid) {
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                Modifier.weight(1f),
                                verticalArrangement =
                                    Arrangement.spacedBy(10.dp)
                            ) {
                                filtered.filterIndexed { index, _ ->
                                    index % 2 == 0
                                }.forEachIndexed { index, club ->
                                    key(club.id) {
                                        ClubPhotoCard(
                                            club,
                                            cardHeight(index * 2)
                                        ) {
                                            onOpenClub(club.navigationClub())
                                        }
                                    }
                                }
                            }

                            Column(
                                Modifier.weight(1f),
                                verticalArrangement =
                                    Arrangement.spacedBy(10.dp)
                            ) {
                                Spacer(Modifier.height(38.dp))
                                filtered.filterIndexed { index, _ ->
                                    index % 2 != 0
                                }.forEachIndexed { index, club ->
                                    key(club.id) {
                                        ClubPhotoCard(
                                            club,
                                            cardHeight(index * 2 + 1)
                                        ) {
                                            onOpenClub(club.navigationClub())
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(filtered, key = { it.id }) { club ->
                        ClubListRow(club) {
                            onOpenClub(club.navigationClub())
                        }
                    }
                }
            }
        }
    }
}

private fun cardHeight(index: Int): Dp = when (index % 6) {
    0 -> 238.dp
    1 -> 204.dp
    2 -> 188.dp
    3 -> 244.dp
    4 -> 216.dp
    else -> 194.dp
}

@Composable
private fun ClubPhotoCard(
    club: FirestoreClub,
    height: Dp,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth()
            .height(height)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = CardColor,
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Box(Modifier.fillMaxSize()) {
            ClubPhoto(club, Modifier.fillMaxSize())

            // Keep the top of the photo clear; darken only the text area.
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            0.40f to Color.Transparent,
                            0.70f to PageColor.copy(alpha = 0.75f),
                            1f to PageColor.copy(alpha = 0.98f)
                        )
                    )
                )
            )

            Surface(
                modifier = Modifier.align(Alignment.TopStart)
                    .padding(10.dp),
                color = PageColor.copy(alpha = 0.85f),
                shape = RoundedCornerShape(100.dp),
                border = BorderStroke(
                    1.dp,
                    Color.White.copy(alpha = 0.10f)
                )
            ) {
                Row(
                    Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 9.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = OrangeColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "Joined",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                Modifier.align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                ClubCategory(club)
                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        club.name,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(5.dp))
                    ApprovedBadge()
                }

                Spacer(Modifier.height(12.dp))
                ClubLocation(club)
            }
        }
    }
}

@Composable
private fun ClubListRow(
    club: FirestoreClub,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = CardColor,
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            Modifier.padding(11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClubPhoto(
                club,
                Modifier.size(66.dp)
                    .clip(RoundedCornerShape(18.dp))
            )
            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        club.name,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(5.dp))
                    ApprovedBadge()
                }
                Spacer(Modifier.height(6.dp))
                ClubCategory(club)
                Spacer(Modifier.height(5.dp))
                ClubLocation(club)
            }
        }
    }
}

@Composable
private fun ClubPhoto(
    club: FirestoreClub,
    modifier: Modifier
) {
    Box(
        modifier.background(CardColor),
        contentAlignment = Alignment.Center
    ) {
        val photoUrl = club.profileImageUrl.trim()

        if (photoUrl.isEmpty()) {
            ClubPhotoFallback(club)
        } else {
            SubcomposeAsyncImage(
                model = photoUrl,
                contentDescription = "${club.name} profile photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = OrangeColor,
                            strokeWidth = 2.dp
                        )
                    }
                },
                error = {
                    ClubPhotoFallback(club)
                }
            )
        }
    }
}

@Composable
private fun ClubPhotoFallback(club: FirestoreClub) {
    val codingClub =
        club.id.equals("Coding_club", ignoreCase = true) ||
                club.name.trim().equals("Coding Club", ignoreCase = true)

    if (codingClub) {
        // Same profile-photo resource used by ClubAccountScreen.
        Image(
            painter = painterResource(R.drawable.code),
            contentDescription = "${club.name} profile photo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Groups,
                contentDescription = "Club photo unavailable",
                tint = MutedColor,
                modifier = Modifier.size(42.dp)
            )
        }
    }
}

@Composable
private fun ClubCategory(club: FirestoreClub) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(6.dp)
                .clip(CircleShape)
                .background(club.clubCategory().color)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            club.clubCategory().label,
            color = MutedColor,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ClubLocation(club: FirestoreClub) {
    if (club.location.isBlank()) return

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.LocationOn,
            contentDescription = null,
            tint = MutedColor,
            modifier = Modifier.size(13.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            club.location,
            color = MutedColor,
            fontSize = 10.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ApprovedBadge() {
    Box(
        Modifier.size(16.dp)
            .clip(CircleShape)
            .background(BlueColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Check,
            contentDescription = "Approved club",
            tint = Color.White,
            modifier = Modifier.size(10.dp)
        )
    }
}

@Composable
private fun ClubControl(
    selected: Boolean = false,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        Modifier.size(42.dp)
            .clip(CircleShape)
            .background(
                if (selected) Color(0xFF0D426F) else CardColor
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun ClubMessage(message: String) {
    Text(
        message,
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 60.dp),
        color = MutedColor,
        textAlign = TextAlign.Center,
        fontSize = 14.sp
    )
}

private fun FirestoreClub.clubCategory(): Category =
    when (category.trim().lowercase()) {
        "sport", "sports" -> Category.SPORT
        "culture", "culture & arts", "arts" -> Category.CULTURE
        "advocacy", "advocacy & politics", "politics" ->
            Category.ADVOCACY
        "identity", "identity & community", "community" ->
            Category.IDENTITY
        else -> Category.ACADEMIC
    }

private fun FirestoreClub.navigationClub(): Club = Club(
    id = id,
    name = name,
    category = clubCategory(),
    members = 0,
    description = description,
    location = location
)