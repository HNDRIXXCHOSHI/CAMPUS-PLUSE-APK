package com.campuspulse.app.ui.screens

import android.widget.Toast
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalInspectionMode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.DocumentSnapshot


import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuspulse.app.R
import com.campuspulse.app.ui.theme.CampusPulseTheme


// =============================================================================
// CAMPUS PULSE / UMP COLOURS
// =============================================================================

private val CcBg = Color(0xFF021D38)

private val CcCard = Color(0xFF05294B)
private val CcSoft = Color(0xFF0D426F)
private val CcBorder = Color(0xFF315574)

private val CcOrange = Color(0xFFF7941D)
private val CcBlue = Color(0xFF38A9E0)
private val CcGreen = Color(0xFF4CAF50)
private val CcRed = Color(0xFFE11111)

private val CcWhite = Color.White
private val CcMuted = Color(0xFFB9C9D8)


// =============================================================================
// FEEDBACK MODEL
// =============================================================================

private data class FeedbackMessage(
    val subject: String,
    val preview: String,
    val time: String,
    val unread: Boolean
)


// =============================================================================
// CREATE CLUB SCREEN
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClubScreen(
    onBack: () -> Unit
) {

    val context = LocalContext.current

    var tab by remember {
        mutableIntStateOf(0)
    }

    var step by remember {
        mutableIntStateOf(1)
    }

    var showPolicy by remember {
        mutableStateOf(false)
    }

    var policyAccepted by remember {
        mutableStateOf(false)
    }

    var name by remember {
        mutableStateOf("")
    }

    var category by remember {
        mutableStateOf("")
    }

    var purpose by remember {
        mutableStateOf("")
    }

    var location by remember {
        mutableStateOf("Mbombela Campus")
    }

    var constitution by remember {
        mutableStateOf(false)
    }

    var signatories by remember {
        mutableStateOf(false)
    }

    var advisor by remember {
        mutableStateOf(false)
    }

    var submitted by remember {
        mutableStateOf(false)
    }


    val preview = LocalInspectionMode.current
    val auth = remember(preview) { if (preview) null else FirebaseAuth.getInstance() }
    val database = remember(preview) { if (preview) null else FirebaseFirestore.getInstance() }
    var applicantId by remember { mutableStateOf(auth?.currentUser?.uid) }
    var saving by remember { mutableStateOf(false) }
    var applicationLoading by remember { mutableStateOf(!preview) }
    var applicationError by remember { mutableStateOf<String?>(null) }
    var applicationDocument by remember { mutableStateOf<DocumentSnapshot?>(null) }
    var retry by remember { mutableIntStateOf(0) }
    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { applicantId = it.currentUser?.uid }
        auth?.addAuthStateListener(listener)
        onDispose { auth?.removeAuthStateListener(listener) }
    }
    DisposableEffect(database, applicantId, retry) {
        applicationDocument = null
        submitted = false
        applicationError = null
        val user = applicantId
        if (database == null || user == null) {
            applicationLoading = false
            return@DisposableEffect onDispose {}
        }
        applicationLoading = true
        val listener = database.collection("clubApplications").document(user).addSnapshotListener { document, error ->
            applicationLoading = false
            if (error != null) applicationError = "Unable to load your application. Check your connection and published rules."
            else {
                applicationError = null
                applicationDocument = document?.takeIf { it.exists() }
                submitted = applicationDocument != null
                applicationDocument?.let {
                    name = it.getString("name").orEmpty()
                    category = it.getString("category").orEmpty()
                    purpose = it.getString("purpose").orEmpty()
                    location = it.getString("location").orEmpty()
                    policyAccepted = it.getBoolean("policyAccepted") == true
                    constitution = it.getBoolean("constitutionPrepared") == true
                    signatories = it.getBoolean("signatoriesPrepared") == true
                    advisor = it.getBoolean("advisorIdentified") == true
                    step = 4
                }
            }
        }
        onDispose { listener.remove() }
    }
    fun submitApplication() {
        if (saving || applicationLoading) return
        val user = applicantId
        val db = database
        if (user == null || db == null) { applicationError = "Sign in to submit a club application."; return }
        if (submitted) { tab = 1; return }
        if (!policyAccepted || !constitution || !signatories || !advisor) {
            applicationError = "Accept the guide and complete all preparation checkboxes."; return
        }
        if (name.trim().length !in 3..150 || category.trim().length !in 1..100
            || purpose.trim().length !in 20..5000 || location.trim().length !in 1..300) {
            applicationError = "Enter a club name (3–150 characters), category, purpose (20–5000 characters) and location."; return
        }
        saving = true
        applicationError = null
        val reference = db.collection("clubApplications").document(user)
        val applicationValues = mapOf(
            "applicantId" to user, "name" to name.trim(), "category" to category.trim(),
            "purpose" to purpose.trim(), "location" to location.trim(),
            "policyAccepted" to true, "policyGuideVersion" to "PROTOTYPE_1",
            "constitutionPrepared" to constitution, "signatoriesPrepared" to signatories,
            "advisorIdentified" to advisor, "status" to "PENDING",
            "createdAt" to FieldValue.serverTimestamp(), "updatedAt" to FieldValue.serverTimestamp()
        )
        db.runTransaction { transaction ->
            val existing = transaction.get(reference)
            if (existing.exists()) return@runTransaction false
            transaction.set(reference, applicationValues)
            true
        }.addOnSuccessListener { created ->
            saving = false
            submitted = true
            tab = 1
            Toast.makeText(context, if (created) "Application saved: Pending review." else "Your saved application already exists.", Toast.LENGTH_LONG).show()
        }.addOnFailureListener {
            saving = false
            applicationError = "Application was not saved. Check your connection and published rules, then try again."
        }
    }

    // =========================================================================
    // PDF SAVE
    // =========================================================================

    val savePdf =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument(
                "application/pdf"
            )
        ) { uri ->

            uri?.let {

                context.contentResolver
                    .openOutputStream(it)
                    ?.use { output ->

                        context.resources
                            .openRawResource(
                                R.raw.campus_pulse_club_creation_guide
                            )
                            .use { input ->

                                input.copyTo(output)
                            }
                    }
            }
        }


    // =========================================================================
    // ROOT
    // =========================================================================

    Scaffold(
        containerColor = CcBg
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .safeDrawingPadding()
                .background(CcBg)
        ) {


            // =================================================================
            // HEADER
            // =================================================================

            CreateClubHeader(
                onBack = onBack
            )


            // =================================================================
            // TABS
            // =================================================================

            CreateClubTabs(
                selectedTab = tab,
                onTabSelected = {
                    tab = it
                }
            )


            // =================================================================
            // APPLICATION
            // =================================================================

            if (tab == 0) {
                if (applicationLoading || saving) Text(if (saving) "Saving application…" else "Loading application…", color = CcMuted, modifier = Modifier.padding(horizontal = 20.dp))
                applicationError?.let { message ->
                    Text(message, color = CcOrange, modifier = Modifier.padding(horizontal = 20.dp))
                    TextButton(onClick = { retry++ }) { Text("Retry", color = CcOrange) }
                }


                ApplicationContent(
                    step = step,
                    policyAccepted = policyAccepted,

                    onPolicyAcceptedChange = {
                        policyAccepted = it
                    },

                    onReadPolicy = {
                        showPolicy = true
                    },

                    onDownloadPolicy = {
                        savePdf.launch(
                            "CampusPulse-Club-Creation-Guide.pdf"
                        )
                    },

                    name = name,

                    onNameChange = {
                        name = it
                    },

                    category = category,

                    onCategoryChange = {
                        category = it
                    },

                    purpose = purpose,

                    onPurposeChange = {
                        purpose = it
                    },

                    location = location,

                    onLocationChange = {
                        location = it
                    },

                    constitution = constitution,

                    onConstitutionChange = {
                        constitution = it
                    },

                    signatories = signatories,

                    onSignatoriesChange = {
                        signatories = it
                    },

                    advisor = advisor,

                    onAdvisorChange = {
                        advisor = it
                    },

                    submitted = submitted,
                    busy = saving || applicationLoading,

                    onBackStep = {

                        if (step > 1) {
                            step--
                        }
                    },

                    onContinue = {

                        if (step < 4) {

                            step++

                        } else {

                            submitApplication()
                        }
                    }
                )

            } else {

                FeedbackContent(applicationDocument, applicationLoading, applicationError) { retry++ }
            }
        }
    }


    // =========================================================================
    // POLICY DIALOG
    // =========================================================================

    if (showPolicy) {

        AlertDialog(
            onDismissRequest = {
                showPolicy = false
            },

            containerColor = CcCard,

            icon = {

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(
                            RoundedCornerShape(
                                15.dp
                            )
                        )
                        .background(
                            CcOrange.copy(
                                alpha = 0.14f
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Filled.Policy,
                        contentDescription = null,
                        tint = CcOrange
                    )
                }
            },

            title = {

                Text(
                    text = "Club creation policy guide",
                    color = CcWhite,
                    fontWeight = FontWeight.Bold
                )
            },

            text = {

                Text(
                    text =
                        "Prototype checklist: define the club purpose, prepare founding-member/signatory information, prepare a constitution, identify an advisor/staff contact where required, provide campus/location details and submit for university review.\n\nReplace this guide with the current official UMP policy before production.",
                    color = CcMuted,
                    lineHeight = 20.sp
                )
            },

            confirmButton = {

                Button(
                    onClick = {

                        showPolicy = false
                        policyAccepted = true
                    },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = CcOrange
                        )
                ) {

                    Text(
                        text = "I understand",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showPolicy = false
                    }
                ) {

                    Text(
                        text = "Close",
                        color = CcMuted
                    )
                }
            }
        )
    }
}


// =============================================================================
// HEADER
// =============================================================================

@Composable
private fun CreateClubHeader(
    onBack: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 12.dp,
                end = 20.dp,
                top = 14.dp,
                bottom = 18.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {


        // =====================================================================
        // BACK
        // =====================================================================

        IconButton(
            onClick = onBack,
            modifier = Modifier.size(44.dp)
        ) {

            Icon(
                imageVector =
                    Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = CcWhite,
                modifier = Modifier.size(24.dp)
            )
        }


        Spacer(
            modifier = Modifier.width(5.dp)
        )


        // =====================================================================
        // TITLE
        // =====================================================================

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = "Create a Club",
                color = CcWhite,
                fontSize = 25.sp,
                lineHeight = 29.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(3.dp)
            )

            Text(
                text = "From idea to university review",
                color = CcMuted,
                fontSize = 12.sp
            )
        }


        Spacer(
            modifier = Modifier.width(14.dp)
        )


        // =====================================================================
        // ACTUAL CAMPUS PULSE LOGO
        //
        // IMPORTANT:
        // If your logo drawable has another name, replace:
        //
        // R.drawable.campus_pulse_logo
        //
        // with the name of your existing logo drawable.
        // =====================================================================

        androidx.compose.foundation.Image(
            painter = painterResource(
                id = R.drawable.campus_pulse_logo
            ),
            contentDescription = "Campus Pulse",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(percent = 15))
        )
    }
}


// =============================================================================
// TABS
// =============================================================================

@Composable
private fun CreateClubTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 20.dp
            )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = CcCard,
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(5.dp),
            horizontalArrangement =
                Arrangement.spacedBy(6.dp)
        ) {


            CreateClubTab(
                modifier = Modifier.weight(1f),
                selected = selectedTab == 0,
                icon = Icons.Filled.AddCircle,
                title = "Application",
                onClick = {
                    onTabSelected(0)
                }
            )


            CreateClubTab(
                modifier = Modifier.weight(1f),
                selected = selectedTab == 1,
                icon = Icons.Filled.Mail,
                title = "Feedback",
                onClick = {
                    onTabSelected(1)
                }
            )
        }


        Spacer(
            modifier = Modifier.height(8.dp)
        )
    }
}


// =============================================================================
// TAB
// =============================================================================

@Composable
private fun CreateClubTab(
    modifier: Modifier,
    selected: Boolean,
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {

    Surface(
        modifier = modifier
            .height(54.dp)
            .clickable(
                onClick = onClick
            ),
        color =
            if (selected) {
                CcSoft
            } else {
                Color.Transparent
            },
        shape = RoundedCornerShape(14.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint =
                    if (selected) {
                        CcOrange
                    } else {
                        CcMuted
                    },
                modifier = Modifier.size(18.dp)
            )


            Spacer(
                modifier = Modifier.width(8.dp)
            )


            Text(
                text = title,
                color =
                    if (selected) {
                        CcWhite
                    } else {
                        CcMuted
                    },
                fontSize = 13.sp,
                fontWeight =
                    if (selected) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Medium
                    }
            )
        }
    }
}


// =============================================================================
// APPLICATION CONTENT
// =============================================================================

@Composable
private fun ApplicationContent(
    step: Int,

    policyAccepted: Boolean,
    onPolicyAcceptedChange: (Boolean) -> Unit,

    onReadPolicy: () -> Unit,
    onDownloadPolicy: () -> Unit,

    name: String,
    onNameChange: (String) -> Unit,

    category: String,
    onCategoryChange: (String) -> Unit,

    purpose: String,
    onPurposeChange: (String) -> Unit,

    location: String,
    onLocationChange: (String) -> Unit,

    constitution: Boolean,
    onConstitutionChange: (Boolean) -> Unit,

    signatories: Boolean,
    onSignatoriesChange: (Boolean) -> Unit,

    advisor: Boolean,
    onAdvisorChange: (Boolean) -> Unit,

    submitted: Boolean,
    busy: Boolean,

    onBackStep: () -> Unit,
    onContinue: () -> Unit
) {

    val canContinue =
        !busy && !submitted && when (step) {

            1 ->
                policyAccepted

            2 ->
                name.isNotBlank() &&
                        purpose.isNotBlank()

            else ->
                true
        }


    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 20.dp,
            bottom = 120.dp
        ),
        verticalArrangement =
            Arrangement.spacedBy(22.dp)
    ) {


        // =====================================================================
        // PROGRESS
        // =====================================================================

        item {

            StepProgress(
                step = step
            )
        }


        // =====================================================================
        // STEP 1
        // =====================================================================

        if (step == 1) {

            item {

                PolicySection(
                    policyAccepted =
                        policyAccepted,
                    onPolicyAcceptedChange =
                        onPolicyAcceptedChange,
                    onReadPolicy =
                        onReadPolicy,
                    onDownloadPolicy =
                        onDownloadPolicy
                )
            }
        }


        // =====================================================================
        // STEP 2
        // =====================================================================

        if (step == 2) {

            item {

                ClubCard {

                    SectionHeading(
                        icon = Icons.Filled.Lightbulb,
                        title = "Your club concept",
                        description =
                            "Tell us what you want to create and how it will benefit students."
                    )


                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )


                    FormField(
                        label = "Proposed club name",
                        value = name,
                        onChange = onNameChange
                    )


                    FormField(
                        label = "Category",
                        value = category,
                        onChange = onCategoryChange
                    )


                    FormField(
                        label =
                            "Purpose / student benefit",
                        value = purpose,
                        height = 120,
                        onChange = onPurposeChange
                    )


                    FormField(
                        label =
                            "Campus / meeting location",
                        value = location,
                        onChange = onLocationChange
                    )
                }
            }
        }


        // =====================================================================
        // STEP 3
        // =====================================================================

        if (step == 3) {

            item {

                ClubCard {

                    SectionHeading(
                        icon = Icons.Filled.FactCheck,
                        title =
                            "University requirements",
                        description =
                            "Confirm the supporting information you have prepared."
                    )


                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )


                    RequirementRow(
                        label =
                            "Constitution prepared",
                        checked = constitution,
                        onChange =
                            onConstitutionChange
                    )


                    RequirementRow(
                        label =
                            "Founding members / signatories ready",
                        checked = signatories,
                        onChange =
                            onSignatoriesChange
                    )


                    RequirementRow(
                        label =
                            "Advisor / staff contact ready",
                        checked = advisor,
                        onChange =
                            onAdvisorChange
                    )


                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )


                    Text(
                        text =
                            "The final production checklist must follow the current university-approved club creation policy.",
                        color = CcMuted,
                        fontSize = 11.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }


        // =====================================================================
        // STEP 4
        // =====================================================================

        if (step == 4) {

            item {

                ReviewSection(
                    submitted = submitted,
                    name = name,
                    category = category,
                    location = location
                )
            }
        }


        // =====================================================================
        // NAVIGATION
        // =====================================================================

        item {

            StepNavigation(
                step = step,
                canContinue = canContinue,
                onBack = onBackStep,
                onContinue = onContinue
            )
        }
    }
}


// =============================================================================
// STEP PROGRESS
// =============================================================================

@Composable
private fun StepProgress(
    step: Int
) {

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "APPLICATION",
                    color = CcMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )


                Spacer(
                    modifier = Modifier.height(4.dp)
                )


                Text(
                    text =
                        when (step) {

                            1 -> "Review the policy"

                            2 -> "Describe your club"

                            3 -> "Prepare requirements"

                            else -> "Review & submit"
                        },
                    color = CcWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }


            Surface(
                color =
                    CcOrange.copy(
                        alpha = 0.14f
                    ),
                shape =
                    RoundedCornerShape(
                        100.dp
                    )
            ) {

                Text(
                    text = "$step / 4",
                    color = CcOrange,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 7.dp
                    )
                )
            }
        }


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        LinearProgressIndicator(
            progress = {
                step / 4f
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(CircleShape),
            color = CcOrange,
            trackColor = CcSoft
        )
    }
}


// =============================================================================
// POLICY SECTION
// =============================================================================

@Composable
private fun PolicySection(
    policyAccepted: Boolean,
    onPolicyAcceptedChange: (Boolean) -> Unit,
    onReadPolicy: () -> Unit,
    onDownloadPolicy: () -> Unit
) {

    ClubCard {


        // =====================================================================
        // HEADING
        // =====================================================================

        SectionHeading(
            icon = Icons.Filled.Policy,
            title = "Club creation policy",
            description =
                "Read the university requirements before starting your application."
        )


        Spacer(
            modifier = Modifier.height(12.dp)
        )


        HorizontalDivider(
            color = CcBorder.copy(
                alpha = 0.65f
            )
        )


        Spacer(
            modifier = Modifier.height(8.dp)
        )


        // =====================================================================
        // POLICY INFORMATION
        // =====================================================================

        Text(
            text = "Before you continue",
            color = CcWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )


        Text(
            text =
                "Review the club creation guide so you understand the documents, founding-member information and university review process.",
            color = CcMuted,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )


        Spacer(
            modifier = Modifier.height(12.dp)
        )


        // =====================================================================
        // READ POLICY
        // =====================================================================

        OutlinedButton(
            onClick = onReadPolicy,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape =
                RoundedCornerShape(
                    14.dp
                ),
            border =
                BorderStroke(
                    width = 1.dp,
                    color = CcBlue
                )
        ) {

            Icon(
                imageVector =
                    Icons.Filled.MenuBook,
                contentDescription = null,
                tint = CcBlue,
                modifier = Modifier.size(18.dp)
            )


            Spacer(
                modifier = Modifier.width(8.dp)
            )


            Text(
                text = "Read policy",
                color = CcWhite,
                fontWeight = FontWeight.Bold
            )
        }


        // =====================================================================
        // DOWNLOAD
        // =====================================================================

        Button(
            onClick = onDownloadPolicy,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape =
                RoundedCornerShape(
                    14.dp
                ),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = CcOrange
                )
        ) {

            Icon(
                imageVector =
                    Icons.Filled.Download,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(18.dp)
            )


            Spacer(
                modifier = Modifier.width(8.dp)
            )


            Text(
                text = "Download PDF",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }


        Spacer(
            modifier = Modifier.height(6.dp)
        )


        // =====================================================================
        // ACCEPT POLICY
        // =====================================================================

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {

                    onPolicyAcceptedChange(
                        !policyAccepted
                    )
                },
            color =
                if (policyAccepted) {
                    CcGreen.copy(
                        alpha = 0.10f
                    )
                } else {
                    CcSoft.copy(
                        alpha = 0.55f
                    )
                },
            shape =
                RoundedCornerShape(
                    15.dp
                ),
            border =
                BorderStroke(
                    width = 1.dp,
                    color =
                        if (policyAccepted) {
                            CcGreen.copy(
                                alpha = 0.45f
                            )
                        } else {
                            CcBorder
                        }
                )
        ) {

            Row(
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 10.dp
                ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Checkbox(
                    checked = policyAccepted,
                    onCheckedChange =
                        onPolicyAcceptedChange,
                    colors =
                        CheckboxDefaults.colors(
                            checkedColor =
                                CcGreen,
                            uncheckedColor =
                                CcMuted,
                            checkmarkColor =
                                CcWhite
                        )
                )


                Spacer(
                    modifier = Modifier.width(4.dp)
                )


                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        text =
                            "I have read the policy guide",
                        color = CcWhite,
                        fontSize = 13.sp,
                        fontWeight =
                            FontWeight.SemiBold
                    )

                    Text(
                        text =
                            "Required before continuing",
                        color = CcMuted,
                        fontSize = 10.sp
                    )
                }


                if (policyAccepted) {

                    Icon(
                        imageVector =
                            Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = CcGreen,
                        modifier =
                            Modifier.size(20.dp)
                    )
                }
            }
        }


        Spacer(
            modifier = Modifier.height(8.dp)
        )


        // =====================================================================
        // PROTOTYPE NOTICE
        // =====================================================================

        Row(
            verticalAlignment =
                Alignment.Top
        ) {

            Icon(
                imageVector =
                    Icons.Filled.Info,
                contentDescription = null,
                tint = CcMuted,
                modifier =
                    Modifier
                        .padding(top = 1.dp)
                        .size(14.dp)
            )


            Spacer(
                modifier = Modifier.width(7.dp)
            )


            Text(
                text =
                    "Prototype guide. Replace this with the current official UMP policy before production.",
                color = CcMuted,
                fontSize = 10.sp,
                lineHeight = 15.sp
            )
        }
    }
}


// =============================================================================
// SECTION HEADING
// =============================================================================

@Composable
private fun SectionHeading(
    icon: ImageVector,
    title: String,
    description: String
) {

    Row(
        verticalAlignment =
            Alignment.Top
    ) {

        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(
                    RoundedCornerShape(
                        14.dp
                    )
                )
                .background(
                    CcOrange.copy(
                        alpha = 0.14f
                    )
                ),
            contentAlignment =
                Alignment.Center
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CcOrange,
                modifier =
                    Modifier.size(22.dp)
            )
        }


        Spacer(
            modifier = Modifier.width(
                13.dp
            )
        )


        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = title,
                color = CcWhite,
                fontSize = 16.sp,
                fontWeight =
                    FontWeight.Bold
            )


            Spacer(
                modifier = Modifier.height(
                    4.dp
                )
            )


            Text(
                text = description,
                color = CcMuted,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}


// =============================================================================
// REVIEW SECTION
// =============================================================================

@Composable
private fun ReviewSection(
    submitted: Boolean,
    name: String,
    category: String,
    location: String
) {

    ClubCard {

        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(
                    RoundedCornerShape(
                        18.dp
                    )
                )
                .background(
                    if (submitted) {
                        CcGreen.copy(
                            alpha = 0.15f
                        )
                    } else {
                        CcOrange.copy(
                            alpha = 0.15f
                        )
                    }
                ),
            contentAlignment =
                Alignment.Center
        ) {

            Icon(
                imageVector =
                    if (submitted) {
                        Icons.Filled.CheckCircle
                    } else {
                        Icons.Filled.FactCheck
                    },
                contentDescription = null,
                tint =
                    if (submitted) {
                        CcGreen
                    } else {
                        CcOrange
                    },
                modifier =
                    Modifier.size(28.dp)
            )
        }


        Spacer(
            modifier = Modifier.height(
                6.dp
            )
        )


        Text(
            text =
                if (submitted) {
                    "Application submitted"
                } else {
                    "Review application"
                },
            color = CcWhite,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )


        Text(
            text =
                if (submitted) {

                    "Your application has been saved. Open Feedback to see its current review status."

                } else {

                    "Check the information below before submitting your application."
                },
            color = CcMuted,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )


        if (!submitted) {

            Spacer(
                modifier = Modifier.height(
                    10.dp
                )
            )


            ReviewItem(
                title = "Club name",
                value =
                    name.ifBlank {
                        "Not provided"
                    }
            )


            ReviewItem(
                title = "Category",
                value =
                    category.ifBlank {
                        "Not provided"
                    }
            )


            ReviewItem(
                title = "Location",
                value =
                    location.ifBlank {
                        "Not provided"
                    }
            )
        }
    }
}


// =============================================================================
// REVIEW ITEM
// =============================================================================

@Composable
private fun ReviewItem(
    title: String,
    value: String
) {

    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        color = CcSoft,
        shape =
            RoundedCornerShape(
                14.dp
            )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    13.dp
                )
        ) {

            Text(
                text = title,
                color = CcMuted,
                fontSize = 10.sp
            )


            Spacer(
                modifier =
                    Modifier.height(3.dp)
            )


            Text(
                text = value,
                color = CcWhite,
                fontSize = 13.sp,
                fontWeight =
                    FontWeight.SemiBold
            )
        }
    }
}


// =============================================================================
// STEP NAVIGATION
// =============================================================================

@Composable
private fun StepNavigation(
    step: Int,
    canContinue: Boolean,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        if (!canContinue) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        bottom = 10.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Icon(
                    imageVector =
                        Icons.Filled.Info,
                    contentDescription = null,
                    tint = CcMuted,
                    modifier =
                        Modifier.size(14.dp)
                )


                Spacer(
                    modifier =
                        Modifier.width(6.dp)
                )


                Text(
                    text =
                        if (step == 1) {
                            "Read and accept the policy guide to continue."
                        } else {
                            "Complete the required information to continue."
                        },
                    color = CcMuted,
                    fontSize = 11.sp
                )
            }
        }


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {


            if (step > 1) {

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape =
                        RoundedCornerShape(
                            15.dp
                        ),
                    border =
                        BorderStroke(
                            width = 1.dp,
                            color = CcBorder
                        )
                ) {

                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = CcWhite,
                        modifier =
                            Modifier.size(17.dp)
                    )


                    Spacer(
                        modifier =
                            Modifier.width(7.dp)
                    )


                    Text(
                        text = "Back",
                        color = CcWhite,
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }


            Button(
                onClick = onContinue,
                enabled = canContinue,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape =
                    RoundedCornerShape(
                        15.dp
                    ),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            CcOrange,
                        contentColor =
                            Color.Black,
                        disabledContainerColor =
                            CcSoft,
                        disabledContentColor =
                            CcMuted
                    )
            ) {

                Text(
                    text =
                        if (step == 4) {
                            "Submit"
                        } else {
                            "Continue"
                        },
                    fontWeight =
                        FontWeight.Bold
                )


                Spacer(
                    modifier =
                        Modifier.width(7.dp)
                )


                Icon(
                    imageVector =
                        if (step == 4) {
                            Icons.Filled.Send
                        } else {
                            Icons.AutoMirrored.Filled.ArrowForward
                        },
                    contentDescription = null,
                    modifier =
                        Modifier.size(17.dp)
                )
            }
        }
    }
}


// =============================================================================
// FEEDBACK
// =============================================================================

@Composable
private fun FeedbackContent(document: DocumentSnapshot?, loading: Boolean, error: String?, onRetry: () -> Unit) {
    val status = document?.getString("status").orEmpty()
    val date = document?.getTimestamp("createdAt")?.toDate()?.let {
        java.text.SimpleDateFormat("d MMM yyyy • HH:mm", java.util.Locale.getDefault()).format(it)
    }.orEmpty()
    val messages = if (document == null) emptyList() else listOf(
        FeedbackMessage(
            subject = "${document.getString("name").orEmpty()} — ${status.ifBlank { "Status unavailable" }}",
            preview = when (status) {
                "PENDING" -> "Your application is saved and waiting for review."
                "APPROVED" -> "Your application has been approved."
                "REJECTED" -> "Your application has been rejected."
                else -> "Your saved application status is $status."
            } + document.getString("feedback").orEmpty().takeIf { it.isNotBlank() }?.let { "\n\n$it" }.orEmpty(),
            time = date, unread = false
        )
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 22.dp,
            bottom = 100.dp
        ),
        verticalArrangement =
            Arrangement.spacedBy(
                12.dp
            )
    ) {


        item {

            Column {

                Text(
                    text = "Feedback inbox",
                    color = CcWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )


                Spacer(
                    modifier =
                        Modifier.height(5.dp)
                )


                Text(
                    text =
                        "Updates about your club application will appear here.",
                    color = CcMuted,
                    fontSize = 12.sp
                )
            }


            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )
        }


        if (loading || error != null || document == null) item {
            Text(if (loading) "Loading application…" else error ?: "No application submitted yet.", color = CcMuted)
            if (error != null) TextButton(onClick = onRetry) { Text("Retry", color = CcOrange) }
        }

        items(messages) { message ->

            FeedbackCard(
                message = message
            )
        }
    }
}


// =============================================================================
// FEEDBACK CARD
// =============================================================================

@Composable
private fun FeedbackCard(
    message: FeedbackMessage
) {

    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        color =
            if (message.unread) {
                CcSoft
            } else {
                CcCard
            },
        shape =
            RoundedCornerShape(
                18.dp
            ),
        border =
            BorderStroke(
                width = 1.dp,
                color =
                    if (message.unread) {
                        CcBlue.copy(
                            alpha = 0.35f
                        )
                    } else {
                        CcBorder
                    }
            )
    ) {

        Row(
            modifier = Modifier.padding(
                15.dp
            ),
            verticalAlignment =
                Alignment.Top
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
                        if (message.unread) {
                            CcOrange.copy(
                                alpha = 0.15f
                            )
                        } else {
                            CcSoft
                        }
                    ),
                contentAlignment =
                    Alignment.Center
            ) {

                Icon(
                    imageVector =
                        Icons.Filled.Mail,
                    contentDescription = null,
                    tint =
                        if (message.unread) {
                            CcOrange
                        } else {
                            CcMuted
                        },
                    modifier =
                        Modifier.size(19.dp)
                )
            }


            Spacer(
                modifier =
                    Modifier.width(12.dp)
            )


            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        text =
                            message.subject,
                        color = CcWhite,
                        fontSize = 13.sp,
                        fontWeight =
                            if (message.unread) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Medium
                            },
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis,
                        modifier =
                            Modifier.weight(1f)
                    )


                    if (message.unread) {

                        Spacer(
                            modifier =
                                Modifier.width(6.dp)
                        )


                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    CcOrange
                                )
                        )
                    }
                }


                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )


                Text(
                    text = message.preview,
                    color = CcMuted,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow =
                        TextOverflow.Ellipsis
                )


                Spacer(
                    modifier =
                        Modifier.height(7.dp)
                )


                Text(
                    text = message.time,
                    color =
                        if (message.unread) {
                            CcOrange
                        } else {
                            CcMuted
                        },
                    fontSize = 9.sp
                )
            }
        }
    }
}


// =============================================================================
// CLUB CARD
// =============================================================================

@Composable
private fun ClubCard(
    content:
    @Composable ColumnScope.() -> Unit
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                22.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor = CcCard
            ),
        border =
            BorderStroke(
                width = 1.dp,
                color = CcBorder
            )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    20.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    14.dp
                ),
            content = content
        )
    }
}


// =============================================================================
// FORM FIELD
// =============================================================================

@Composable
private fun FormField(
    label: String,
    value: String,
    height: Int = 58,
    onChange: (String) -> Unit
) {

    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = {

            Text(
                text = label
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(
                min = height.dp
            ),
        shape =
            RoundedCornerShape(
                14.dp
            ),
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedTextColor =
                    CcWhite,
                unfocusedTextColor =
                    CcWhite,

                focusedContainerColor =
                    CcBg.copy(
                        alpha = 0.30f
                    ),
                unfocusedContainerColor =
                    CcBg.copy(
                        alpha = 0.20f
                    ),

                focusedBorderColor =
                    CcOrange,
                unfocusedBorderColor =
                    CcBorder,

                focusedLabelColor =
                    CcOrange,
                unfocusedLabelColor =
                    CcMuted,

                cursorColor =
                    CcOrange
            )
    )
}


// =============================================================================
// REQUIREMENT ROW
// =============================================================================

@Composable
private fun RequirementRow(
    label: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {

                onChange(
                    !checked
                )
            },
        color =
            if (checked) {
                CcGreen.copy(
                    alpha = 0.10f
                )
            } else {
                CcSoft.copy(
                    alpha = 0.45f
                )
            },
        shape =
            RoundedCornerShape(
                14.dp
            ),
        border =
            BorderStroke(
                width = 1.dp,
                color =
                    if (checked) {
                        CcGreen.copy(
                            alpha = 0.40f
                        )
                    } else {
                        CcBorder
                    }
            )
    ) {

        Row(
            modifier =
                Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 7.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Checkbox(
                checked = checked,
                onCheckedChange =
                    onChange,
                colors =
                    CheckboxDefaults.colors(
                        checkedColor =
                            CcGreen,
                        uncheckedColor =
                            CcMuted
                    )
            )


            Spacer(
                modifier =
                    Modifier.width(4.dp)
            )


            Text(
                text = label,
                color = CcWhite,
                fontSize = 13.sp,
                modifier =
                    Modifier.weight(1f)
            )


            if (checked) {

                Icon(
                    imageVector =
                        Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = CcGreen,
                    modifier =
                        Modifier.size(18.dp)
                )
            }
        }
    }
}


// =============================================================================
// PREVIEW
// =============================================================================

@Preview(
    name = "Create Club - Redesigned",
    showBackground = true,
    backgroundColor = 0xFF021D38
)
@Composable
private fun CreateClubScreenPreview() {

    CampusPulseTheme {

        CreateClubScreen(
            onBack = {}
        )
    }
}
