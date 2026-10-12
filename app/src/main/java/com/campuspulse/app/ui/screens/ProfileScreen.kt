package com.campuspulse.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Lock
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Switch
import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Logout
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material3.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuspulse.app.ui.components.NavDestination
import com.campuspulse.app.ui.theme.CampusPulseTheme


// =============================================================================
// CAMPUS PULSE / UMP DARK MODE
// =============================================================================

private val ProfileBackground = Color(0xFF021D38)
private val ProfileSurface = Color(0xFF05294B)
private val ProfileSurfaceSoft = Color(0xFF0D426F)
private val ProfileBorder = Color(0xFF315574)
private val ProfileWhite = Color(0xFFFFFFFF)
private val ProfileMuted = Color(0xFFB9C9D8)
private val ProfileOrange = Color(0xFFF7941D)
private val ProfileBlue = Color(0xFF38A9E0)
private val ProfileRed = Color(0xFFE11111)
private val ProfileGreen = Color(0xFF4CAF50)
private val ProfileNavy = Color(0xFF00254A)


// =============================================================================
// PROFILE SCREEN
// =============================================================================

@Composable
fun ProfileScreen(
    onNavigate: (NavDestination) -> Unit,
    onLogout: () -> Unit = {}
) {

    var showEditProfile by remember {
        mutableStateOf(false)
    }

    var showSettings by remember {
        mutableStateOf(false)
    }

    var showHelp by remember {
        mutableStateOf(false)
    }

    var showAbout by remember {
        mutableStateOf(false)
    }

    var showLogout by remember {
        mutableStateOf(false)
    }

    var profileMenuExpanded by remember {
        mutableStateOf(false)
    }


    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val context = LocalContext.current
    val preferences = remember(context) { context.getSharedPreferences("profile_preferences", Context.MODE_PRIVATE) }
    var userId by remember { mutableStateOf(auth.currentUser?.uid) }
    var hideStudentNumber by remember(userId) { mutableStateOf(preferences.getBoolean("hide_student_number_${userId.orEmpty()}", true)) }
    var showAcademicDetails by remember(userId) { mutableStateOf(false) }
    var studentName by remember(userId) { mutableStateOf("") }
    var studentCourse by remember(userId) { mutableStateOf("Not provided") }
    var department by remember(userId) { mutableStateOf("Not provided") }
    var studentEmail by remember(userId) { mutableStateOf(auth.currentUser?.email.orEmpty()) }
    var studentNumber by remember(userId) { mutableStateOf("Not provided") }
    var studentYear by remember(userId) { mutableStateOf("") }
    var membershipIds by remember(userId) { mutableStateOf(emptyList<String>()) }
    var profileLoaded by remember(userId) { mutableStateOf(false) }
    var interests by remember(userId) { mutableStateOf(emptyList<String>()) }
    var emailVerified by remember(userId) { mutableStateOf(auth.currentUser?.isEmailVerified == true) }
    var joinedCount by remember(userId) { mutableStateOf<Int?>(null) }
    var loading by remember(userId) { mutableStateOf(true) }
    var profileError by remember(userId) { mutableStateOf<String?>(null) }
    var membershipError by remember(userId) { mutableStateOf<String?>(null) }
    var saveError by remember(userId) { mutableStateOf<String?>(null) }
    var saving by remember(userId) { mutableStateOf(false) }
    var retry by remember { mutableStateOf(0) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener {
            userId = it.currentUser?.uid
            emailVerified = it.currentUser?.isEmailVerified == true
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    DisposableEffect(userId, retry) {
        val uid = userId
        loading = true
        profileError = null
        membershipError = null
        joinedCount = null
        if (uid == null) {
            loading = false
            profileError = "Your session has ended. Please log in again."
            return@DisposableEffect onDispose {}
        }
        val profileListener = db.collection("users").document(uid)
            .addSnapshotListener { document, error ->
                loading = false
                if (error != null) {
                    profileError = "Unable to load your profile. Please retry."
                } else if (document == null || !document.exists()) {
                    profileError = "Your account profile could not be found."
                } else {
                    profileError = null
                    profileLoaded = true
                    studentYear = document.get("year")?.toString().orEmpty()
                    studentName = document.getString("name").orEmpty()
                    studentEmail = auth.currentUser?.email ?: document.getString("email").orEmpty()
                    studentCourse = document.getString("course")?.takeIf { it.isNotBlank() } ?: "Not provided"
                    department = document.getString("department")?.takeIf { it.isNotBlank() } ?: "Not provided"
                    studentNumber = document.get("studentNumber")?.toString()?.takeIf { it.isNotBlank() } ?: "Not provided"
                    interests = (document.get("interests") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                }
            }
        val membershipListener = db.collection("clubMembers")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    joinedCount = null
                    membershipError = "Unable to load your joined club count."
                } else {
                    membershipError = null
                    membershipIds = snapshot?.documents?.filter { it.getString("status") in listOf("ACTIVE", "PENDING") }?.map { it.id } ?: emptyList()
                    joinedCount = snapshot?.documents?.filter { it.getString("status") == "ACTIVE" }?.mapNotNull { it.getString("clubId") }
                        ?.filter { it.isNotBlank() }?.toSet()?.size ?: 0
                }
            }
        onDispose {
            profileListener.remove()
            membershipListener.remove()
        }
    }


    // =========================================================================
    // ROOT
    // =========================================================================

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileBackground)
            .safeDrawingPadding()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    bottom = 120.dp
                )
        ) {

            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp), color = ProfileOrange)
            }
            (profileError ?: membershipError)?.let { message ->
                Column(Modifier.padding(16.dp)) {
                    Text(message, color = ProfileWhite)
                    TextButton(onClick = { retry++ }) { Text("Retry", color = ProfileOrange) }
                }
            }

            // =================================================================
            // PROFILE HERO
            // =================================================================

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                ProfileSurfaceSoft,
                                ProfileSurface,
                                ProfileBackground
                            )
                        )
                    )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 20.dp,
                            end = 20.dp,
                            top = 14.dp,
                            bottom = 24.dp
                        ),
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    // =========================================================
                    // TOP BAR
                    // =========================================================

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Text(
                            text = "Profile",
                            color = ProfileWhite,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        Box {

                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        ProfileSurface
                                    )
                                    .clickable {
                                        profileMenuExpanded = true
                                    },
                                contentAlignment =
                                    Alignment.Center
                            ) {

                                Icon(
                                    imageVector =
                                        Icons.Filled.MoreHoriz,
                                    contentDescription =
                                        "Profile options",
                                    tint = ProfileWhite,
                                    modifier =
                                        Modifier.size(22.dp)
                                )
                            }

                            DropdownMenu(
                                expanded =
                                    profileMenuExpanded,

                                onDismissRequest = {
                                    profileMenuExpanded = false
                                },

                                modifier =
                                    Modifier.background(
                                        ProfileSurface
                                    )
                            ) {

                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector =
                                                Icons.Filled.Settings,
                                            contentDescription = null,
                                            tint = ProfileBlue
                                        )
                                    },

                                    text = {
                                        Text(
                                            text = "Settings",
                                            color = ProfileWhite
                                        )
                                    },

                                    onClick = {
                                        profileMenuExpanded = false
                                        showSettings = true
                                    }
                                )

                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector =
                                                Icons.Outlined.HelpOutline,
                                            contentDescription = null,
                                            tint = ProfileGreen
                                        )
                                    },

                                    text = {
                                        Text(
                                            text = "Help & support",
                                            color = ProfileWhite
                                        )
                                    },

                                    onClick = {
                                        profileMenuExpanded = false
                                        showHelp = true
                                    }
                                )

                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector =
                                                Icons.Outlined.Info,
                                            contentDescription = null,
                                            tint = ProfileOrange
                                        )
                                    },

                                    text = {
                                        Text(
                                            text = "About Campus Pulse",
                                            color = ProfileWhite
                                        )
                                    },

                                    onClick = {
                                        profileMenuExpanded = false
                                        showAbout = true
                                    }
                                )

                                HorizontalDivider(
                                    color = ProfileBorder
                                )

                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector =
                                                Icons.Outlined.Logout,
                                            contentDescription = null,
                                            tint = ProfileRed
                                        )
                                    },

                                    text = {
                                        Text(
                                            text = "Log out",
                                            color = ProfileRed,
                                            fontWeight =
                                                FontWeight.SemiBold
                                        )
                                    },

                                    onClick = {
                                        profileMenuExpanded = false
                                        showLogout = true
                                    }
                                )
                            }
                        }
                    }


                    Spacer(
                        modifier = Modifier.height(
                            30.dp
                        )
                    )


                    // =========================================================
                    // PROFILE IMAGE PLACEHOLDER
                    // =========================================================

                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        ProfileBlue,
                                        ProfileOrange
                                    )
                                )
                            ),
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Box(
                            modifier = Modifier
                                .size(102.dp)
                                .clip(CircleShape)
                                .background(
                                    ProfileNavy
                                ),
                            contentAlignment =
                                Alignment.Center
                        ) {

                            Text(
                                text =
                                    initialsFromName(
                                        studentName
                                    ),
                                color = ProfileWhite,
                                fontSize = 34.sp,
                                fontWeight =
                                    FontWeight.Black
                            )
                        }
                    }


                    Spacer(
                        modifier = Modifier.height(
                            16.dp
                        )
                    )


                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Text(
                            text = studentName.ifBlank { if (loading) "Loading…" else "Your profile" },
                            color = ProfileWhite,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow =
                                TextOverflow.Ellipsis
                        )

                        Spacer(
                            modifier = Modifier.width(
                                6.dp
                            )
                        )

                        if (emailVerified) {
                            Icon(
                                imageVector =
                                    Icons.Filled.Verified,
                                contentDescription =
                                    if (emailVerified) "Email verified" else "Email not verified",
                                tint = ProfileBlue,
                                modifier =
                                    Modifier.size(19.dp)
                            )
                        }
                    }


                    Spacer(
                        modifier = Modifier.height(
                            4.dp
                        )
                    )


                    Spacer(modifier = Modifier.height(10.dp))

                    if (emailVerified) {
                        Surface(
                            color =
                                ProfileGreen.copy(
                                    alpha = 0.12f
                                ),
                            shape =
                                RoundedCornerShape(
                                    percent = 25
                                ),
                            border =
                                BorderStroke(
                                    width = 1.dp,
                                    color =
                                        ProfileGreen.copy(
                                            alpha = 0.30f
                                        )
                                )
                        ) {

                            Row(
                                modifier = Modifier.padding(
                                    horizontal = 11.dp,
                                    vertical = 6.dp
                                ),
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {

                                Icon(
                                    imageVector =
                                        Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = ProfileGreen,
                                    modifier =
                                        Modifier.size(13.dp)
                                )

                                Spacer(
                                    modifier =
                                        Modifier.width(5.dp)
                                )

                                Text(
                                    text = if (emailVerified) "Email verified" else "Email not verified",
                                    color = ProfileGreen,
                                    fontSize = 11.sp,
                                    fontWeight =
                                        FontWeight.SemiBold
                                )
                            }
                        }


                    }

                    Spacer(
                        modifier = Modifier.height(
                            24.dp
                        )
                    )


                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 40.dp
                            ),
                        horizontalArrangement =
                            Arrangement.SpaceEvenly
                    ) {

                        ProfileStat(
                            value = joinedCount?.toString() ?: "—",
                            label = "Clubs"
                        )

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(38.dp)
                                .background(
                                    ProfileBorder
                                )
                        )

                        ProfileStat(
                            value = interests.size.toString(),
                            label = "Interests"
                        )
                    }


                    Spacer(
                        modifier = Modifier.height(
                            22.dp
                        )
                    )


                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            enabled = !loading && profileError == null && userId != null,
                            onClick = {
                                saveError = null
                                showEditProfile = true
                            },
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                        ProfileWhite
                                ),
                            shape =
                                RoundedCornerShape(
                                    percent = 25
                                ),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Filled.Edit,
                                contentDescription = null,
                                tint = ProfileNavy,
                                modifier =
                                    Modifier.size(18.dp)
                            )

                            Spacer(
                                modifier = Modifier.width(
                                    8.dp
                                )
                            )

                            Text(
                                text = "Edit profile",
                                color = ProfileNavy,
                                fontWeight =
                                    FontWeight.Bold
                            )
                        }
                        IconButton(
                            onClick = { onNavigate(NavDestination.MY_CLUBS) },
                            modifier = Modifier.size(48.dp)
                                .background(ProfileOrange, RoundedCornerShape(percent = 25))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Groups,
                                contentDescription = "View joined clubs",
                                tint = ProfileNavy,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                }
            }


            // =================================================================
            // BODY
            // =================================================================

            Column(
                modifier = Modifier.padding(
                    horizontal = 16.dp
                )
            ) {

                SectionTitle(title = "Interests")
                Spacer(Modifier.height(10.dp))
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = ProfileSurface), border = BorderStroke(1.dp, ProfileBorder)) {
                    Text(if (interests.isEmpty()) "No interests selected" else interests.joinToString(" • "),
                        color = ProfileMuted, modifier = Modifier.padding(16.dp), fontSize = 14.sp, lineHeight = 21.sp)
                }
                Spacer(Modifier.height(24.dp))
                Card(modifier = Modifier.fillMaxWidth().animateContentSize(), shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = ProfileSurface), border = BorderStroke(1.dp, ProfileBorder)) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth().clickable { showAcademicDetails = !showAcademicDetails }.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.School, contentDescription = null, tint = ProfileOrange, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Academic details", color = ProfileWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(if (showAcademicDetails) "Tap to hide your details" else "Tap to view your details",
                                    color = ProfileMuted, fontSize = 12.sp)
                            }
                            Icon(if (showAcademicDetails) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = if (showAcademicDetails) "Hide academic details" else "Show academic details", tint = ProfileWhite)
                        }
                        if (showAcademicDetails) {
                            InformationDivider()
                            ProfileInformationRow(icon = { Icon(Icons.Filled.Email, null, tint = ProfileBlue) },
                                title = "Student email", value = studentEmail.ifBlank { "Not provided" })
                            InformationDivider()
                            ProfileInformationRow(icon = { Icon(Icons.Filled.Badge, null, tint = ProfileOrange) },
                                title = "Student number",
                                value = if (studentNumber == "Not provided" || !hideStudentNumber) studentNumber
                                else "•".repeat((studentNumber.length - 3).coerceAtLeast(0)) + studentNumber.takeLast(3))
                            InformationDivider()
                            ProfileInformationRow(icon = { Icon(Icons.Filled.School, null, tint = ProfileBlue) }, title = "Programme", value = studentCourse)
                            InformationDivider()
                            ProfileInformationRow(icon = { Icon(Icons.Filled.School, null, tint = ProfileBlue) }, title = "Department / Faculty", value = department)
                            InformationDivider()
                            ProfileInformationRow(icon = { Icon(Icons.Filled.School, null, tint = ProfileGreen) }, title = "Year of study", value = studentYear.ifBlank { "Not provided" })
                            InformationDivider()
                            ProfileInformationRow(icon = { Icon(Icons.Filled.School, null, tint = ProfileGreen) },
                                title = "Institution", value = "University of Mpumalanga — Mbombela Campus")
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Lock, null, tint = ProfileMuted, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Student number, department and programme are read-only.", color = ProfileMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(26.dp))

                SectionTitle(
                    title = "Account"
                )

                Spacer(
                    modifier = Modifier.height(
                        10.dp
                    )
                )


                Card(
                    modifier =
                        Modifier.fillMaxWidth(),
                    shape =
                        RoundedCornerShape(
                            22.dp
                        ),
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                ProfileSurface
                        ),
                    border =
                        BorderStroke(
                            width = 1.dp,
                            color = ProfileBorder
                        )
                ) {

                    Column {

                        ProfileMenuRow(
                            icon = {
                                Icon(
                                    imageVector =
                                        Icons.Filled.Notifications,
                                    contentDescription =
                                        null,
                                    tint = ProfileOrange
                                )
                            },
                            title = "Notifications",
                            subtitle =
                                "Manage your Campus Pulse alerts",
                            onClick = {
                                onNavigate(NavDestination.NOTIFICATIONS)
                            }
                        )


                        MenuDivider()


                        ProfileMenuRow(
                            icon = {
                                Icon(
                                    imageVector =
                                        Icons.Filled.Settings,
                                    contentDescription =
                                        null,
                                    tint = ProfileBlue
                                )
                            },
                            title = "Settings",
                            subtitle =
                                "Account and app preferences",
                            onClick = {
                                showSettings = true
                            }
                        )
                    }
                }


                Spacer(
                    modifier = Modifier.height(
                        26.dp
                    )
                )


                SectionTitle(
                    title = "Support"
                )

                Spacer(
                    modifier = Modifier.height(
                        10.dp
                    )
                )


                Card(
                    modifier =
                        Modifier.fillMaxWidth(),
                    shape =
                        RoundedCornerShape(
                            22.dp
                        ),
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                ProfileSurface
                        ),
                    border =
                        BorderStroke(
                            width = 1.dp,
                            color = ProfileBorder
                        )
                ) {

                    Column {

                        ProfileMenuRow(
                            icon = {
                                Icon(
                                    imageVector =
                                        Icons.Outlined.HelpOutline,
                                    contentDescription =
                                        null,
                                    tint = ProfileGreen
                                )
                            },
                            title = "Help & support",
                            subtitle =
                                "Get help using Campus Pulse",
                            onClick = {
                                showHelp = true
                            }
                        )


                        MenuDivider()


                        ProfileMenuRow(
                            icon = {
                                Icon(
                                    imageVector =
                                        Icons.Outlined.Info,
                                    contentDescription =
                                        null,
                                    tint = ProfileBlue
                                )
                            },
                            title =
                                "About Campus Pulse",
                            subtitle =
                                "App information",
                            onClick = {
                                showAbout = true
                            }
                        )
                    }
                }


                Spacer(
                    modifier = Modifier.height(
                        26.dp
                    )
                )


                // =============================================================
                // LOG OUT
                // =============================================================

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showLogout = true
                        },
                    shape =
                        RoundedCornerShape(
                            20.dp
                        ),
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                ProfileRed.copy(
                                    alpha = 0.08f
                                )
                        ),
                    border =
                        BorderStroke(
                            width = 1.dp,
                            color =
                                ProfileRed.copy(
                                    alpha = 0.35f
                                )
                        )
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(
                                    RoundedCornerShape(
                                        13.dp
                                    )
                                )
                                .background(
                                    ProfileRed.copy(
                                        alpha = 0.15f
                                    )
                                ),
                            contentAlignment =
                                Alignment.Center
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Outlined.Logout,
                                contentDescription = null,
                                tint = ProfileRed,
                                modifier =
                                    Modifier.size(20.dp)
                            )
                        }


                        Spacer(
                            modifier =
                                Modifier.width(12.dp)
                        )


                        Text(
                            text = "Log out",
                            color = ProfileRed,
                            fontSize = 14.sp,
                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }
            }
        }
    }


    // =========================================================================
    // EDIT PROFILE
    // =========================================================================

    if (showEditProfile) {

        EditProfileDialog(
            currentName = studentName,
            currentCourse = studentCourse,
            currentStudentNumber = studentNumber,
            currentYear = studentYear,
            currentDepartment = department,

            onDismiss = {
                if (!saving) showEditProfile = false
            },

            saving = saving,
            errorMessage = saveError,
            onSave = { name, year ->
                val uid = userId
                if (uid == null || auth.currentUser?.uid != uid) {
                    saveError = "Please log in again before saving."
                } else {
                    saving = true
                    saveError = null
                    com.campuspulse.app.data.StudentProfileRepository().saveEditable(name, year,
                        onSuccess = {
                            if (auth.currentUser?.uid == uid) { saving = false; showEditProfile = false }
                        }, onError = {
                            if (auth.currentUser?.uid == uid) { saving = false; saveError = it }
                        })
                }
            }
        )
    }


    // =========================================================================
    // SETTINGS
    // =========================================================================

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            containerColor = ProfileSurface,
            title = { Text("Profile settings", color = ProfileWhite) },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Mask student number", color = ProfileWhite, modifier = Modifier.weight(1f))
                        Switch(
                            checked = hideStudentNumber,
                            enabled = userId != null,
                            onCheckedChange = { checked ->
                                val uid = userId
                                if (uid != null) {
                                    hideStudentNumber = checked
                                    preferences.edit().putBoolean("hide_student_number_$uid", checked).apply()
                                }
                            }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "This hides the number on your profile on this device. It does not change your saved details or the member details available to your club administrator.",
                        color = ProfileMuted,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettings = false }) { Text("Done", color = ProfileOrange) }
            }
        )
    }


    // =========================================================================
    // HELP
    // =========================================================================

    if (showHelp) {

        SimpleInformationDialog(
            title = "Help & support",

            message = """
                Account access
                Use Forgot password on the login screen to request a reset email. Check your spam folder if it does not arrive.

                Clubs
                Open a club from Search to join it. Use the clubs button beside Edit profile to view your joined clubs.

                Events and RSVP
                Open an event to see its details and choose your RSVP. Home and the event page should show the same response.

                Reporting
                Use the flag option on a post or event to submit a report for review.

                Loading or saving problems
                Check your connection and use Retry when shown. If the problem continues, keep the exact error message and the steps that caused it.
            """.trimIndent(),

            onDismiss = {
                showHelp = false
            }
        )
    }


    // =========================================================================
    // ABOUT
    // =========================================================================

    if (showAbout) {

        SimpleInformationDialog(
            title = "Campus Pulse",

            message = """
                Campus Pulse brings student clubs, societies, announcements and campus events together in one place.

                Student accounts, club memberships, posts, events and RSVP use Firebase-backed data.

                Notification read/clear state syncs across your devices when connected. Student-number masking is saved on this device.

                Club applications are submitted for review. Submitting an application does not immediately create a club.
            """.trimIndent(),

            onDismiss = {
                showAbout = false
            }
        )
    }


    // =========================================================================
    // LOGOUT
    // =========================================================================

    if (showLogout) {

        AlertDialog(
            onDismissRequest = {
                showLogout = false
            },

            containerColor =
                ProfileSurface,

            title = {
                Text(
                    text = "Log out?",
                    color = ProfileWhite,
                    fontWeight =
                        FontWeight.Bold
                )
            },

            text = {
                Text(
                    text =
                        "Are you sure you want to log out of Campus Pulse?",
                    color = ProfileMuted,
                    fontSize = 13.sp
                )
            },

            confirmButton = {

                Button(
                    onClick = {
                        showLogout = false

                        // REAL LOGOUT CALLBACK
                        onLogout()
                    },

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                ProfileRed
                        )
                ) {

                    Text(
                        text = "Log out",
                        color = ProfileWhite,
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showLogout = false
                    }
                ) {

                    Text(
                        text = "Cancel",
                        color = ProfileMuted
                    )
                }
            }
        )
    }
}


// =============================================================================
// PROFILE STAT
// =============================================================================

@Composable
private fun ProfileStat(
    value: String,
    label: String
) {

    Column(
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            text = value,
            color = ProfileWhite,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(
                2.dp
            )
        )

        Text(
            text = label,
            color = ProfileMuted,
            fontSize = 11.sp
        )
    }
}


// =============================================================================
// SECTION TITLE
// =============================================================================

@Composable
private fun SectionTitle(
    title: String
) {

    Text(
        text = title,
        color = ProfileWhite,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(
            start = 4.dp
        )
    )
}


// =============================================================================
// STUDENT INFORMATION ROW
// =============================================================================

@Composable
private fun ProfileInformationRow(
    icon: @Composable () -> Unit,
    title: String,
    value: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(
                    RoundedCornerShape(
                        13.dp
                    )
                )
                .background(
                    ProfileSurfaceSoft
                ),
            contentAlignment =
                Alignment.Center
        ) {

            Box(
                modifier = Modifier.size(
                    20.dp
                )
            ) {
                icon()
            }
        }


        Spacer(
            modifier = Modifier.width(
                12.dp
            )
        )


        Column(
            modifier = Modifier.weight(
                1f
            )
        ) {

            Text(
                text = title,
                color = ProfileWhite,
                fontSize = 13.sp,
                fontWeight =
                    FontWeight.SemiBold
            )

            Spacer(
                modifier = Modifier.height(
                    3.dp
                )
            )

            Text(
                text = value,
                color = ProfileMuted,
                fontSize = 11.sp,
                maxLines = 2,
                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}


// =============================================================================
// PROFILE MENU ROW
// =============================================================================

@Composable
private fun ProfileMenuRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            )
            .padding(15.dp),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(
                    RoundedCornerShape(
                        13.dp
                    )
                )
                .background(
                    ProfileSurfaceSoft
                ),
            contentAlignment =
                Alignment.Center
        ) {

            Box(
                modifier = Modifier.size(
                    20.dp
                )
            ) {
                icon()
            }
        }


        Spacer(
            modifier = Modifier.width(
                12.dp
            )
        )


        Column(
            modifier = Modifier.weight(
                1f
            )
        ) {

            Text(
                text = title,
                color = ProfileWhite,
                fontSize = 14.sp,
                fontWeight =
                    FontWeight.SemiBold
            )

            Spacer(
                modifier = Modifier.height(
                    3.dp
                )
            )

            Text(
                text = subtitle,
                color = ProfileMuted,
                fontSize = 11.sp
            )
        }


        Icon(
            imageVector =
                Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = ProfileMuted,
            modifier = Modifier.size(
                18.dp
            )
        )
    }
}


// =============================================================================
// DIVIDERS
// =============================================================================

@Composable
private fun InformationDivider() {

    HorizontalDivider(
        color = ProfileBorder,
        modifier = Modifier.padding(
            start = 69.dp
        )
    )
}


@Composable
private fun MenuDivider() {

    HorizontalDivider(
        color = ProfileBorder,
        modifier = Modifier.padding(
            start = 69.dp
        )
    )
}


// =============================================================================
// EDIT PROFILE
// =============================================================================

@Composable
private fun EditProfileDialog(
    currentName: String,
    currentCourse: String,
    currentStudentNumber: String,
    currentYear: String,
    currentDepartment: String,
    onDismiss: () -> Unit,
    saving: Boolean,
    errorMessage: String?,
    onSave: (String, String) -> Unit
) {

    var name by remember(
        currentName
    ) {

        mutableStateOf(
            currentName
        )
    }

    var year by remember(currentYear) { mutableStateOf(currentYear) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = ProfileWhite,
        unfocusedTextColor = ProfileWhite,
        disabledTextColor = ProfileMuted,
        cursorColor = ProfileOrange,
        focusedLabelColor = ProfileOrange,
        unfocusedLabelColor = ProfileMuted,
        disabledLabelColor = ProfileMuted,
        focusedBorderColor = ProfileOrange,
        unfocusedBorderColor = ProfileBorder,
        disabledBorderColor = ProfileBorder
    )



    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },

        containerColor =
            ProfileSurface,

        title = {
            Text(
                text = "Edit profile",
                color = ProfileWhite,
                fontWeight =
                    FontWeight.Bold
            )
        },

        text = {

            Column {
                errorMessage?.let { Text(it, color = ProfileRed, fontSize = 13.sp) }
                OutlinedTextField(
                    value = name,
                    colors = fieldColors,

                    onValueChange = {
                        name = it
                    },

                    label = {
                        Text(
                            text = "Name"
                        )
                    },

                    enabled = !saving,
                    singleLine = true,

                    modifier =
                        Modifier.fillMaxWidth()
                )


                Spacer(
                    modifier = Modifier.height(
                        14.dp
                    )
                )


                OutlinedTextField(colors = fieldColors, value = year, onValueChange = { year = it }, label = { Text("Year of study (1–8)") },
                    enabled = !saving, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                Text("You can update your name and year here. Academic identifiers are read-only.", color = ProfileMuted, fontSize = 12.sp, lineHeight = 18.sp)

            }
        },

        confirmButton = {

            Button(
                enabled =
                    !saving && name.trim().length in 2..80 && year.trim().matches(Regex("[1-8]")),

                onClick = {
                    onSave(
                        name.trim(), year.trim()
                    )
                },

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            ProfileOrange
                    )
            ) {

                Text(
                    text = if (saving) "Saving…" else "Save",
                    color = Color.Black,
                    fontWeight =
                        FontWeight.Bold
                )
            }
        },

        dismissButton = {

            TextButton(
                enabled = !saving,
                onClick = onDismiss
            ) {

                Text(
                    text = "Cancel",
                    color = ProfileMuted
                )
            }
        }
    )
}


// =============================================================================
// SIMPLE INFORMATION DIALOG
// =============================================================================

@Composable
private fun SimpleInformationDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {

    AlertDialog(
        onDismissRequest =
            onDismiss,

        containerColor =
            ProfileSurface,

        title = {
            Text(
                text = title,
                color = ProfileWhite,
                fontWeight =
                    FontWeight.Bold
            )
        },

        text = {
            Text(
                text = message,
                color = ProfileMuted,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        },

        confirmButton = {

            Button(
                onClick = onDismiss,

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            ProfileOrange
                    )
            ) {

                Text(
                    text = "Done",
                    color = Color.Black,
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    )
}


// =============================================================================
// INITIALS
// =============================================================================

private fun initialsFromName(
    name: String
): String {

    val words =
        name
            .trim()
            .split(" ")
            .filter {
                it.isNotBlank()
            }

    if (words.isEmpty()) {
        return ""
    }

    if (words.size == 1) {
        return words
            .first()
            .take(2)
            .uppercase()
    }

    return (
            words
                .first()
                .take(1) +
                    words
                        .last()
                        .take(1)
            ).uppercase()
}


// =============================================================================
// PREVIEW
// =============================================================================

@Preview(
    name = "Campus Pulse Profile - UMP Dark",
    showBackground = true,
    uiMode =
        Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ProfileScreenPreview() {

    CampusPulseTheme {

        ProfileScreen(
            onNavigate = {},
            onLogout = {}
        )
    }
}
