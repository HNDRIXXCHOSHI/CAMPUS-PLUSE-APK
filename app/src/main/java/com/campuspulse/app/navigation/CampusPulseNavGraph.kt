package com.campuspulse.app.navigation

import androidx.compose.foundation.background
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.firestore.FirebaseFirestore
import com.campuspulse.app.model.Club
import com.campuspulse.app.model.Category
import com.campuspulse.app.model.EventItem
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.campuspulse.app.data.ClubAdminRepository
import com.campuspulse.app.data.AuthRepository
import com.campuspulse.app.ui.components.CampusPulseBottomNav
import com.campuspulse.app.ui.components.NavDestination
import com.campuspulse.app.ui.screens.AdminLoginScreen
import com.campuspulse.app.ui.screens.ClubAccountScreen
import com.campuspulse.app.ui.screens.ClubAdminScreen
import com.campuspulse.app.ui.screens.CreateClubScreen
import com.campuspulse.app.ui.screens.DirectoryScreen
import com.campuspulse.app.ui.screens.EventsCalendarScreen
import com.campuspulse.app.ui.screens.EventDetailScreen
import com.campuspulse.app.ui.screens.ForgotPasswordScreen
import com.campuspulse.app.ui.screens.CampusPulseHomeScreen
import com.campuspulse.app.ui.screens.InterestsScreen
import com.campuspulse.app.ui.screens.LoginScreen
import com.campuspulse.app.ui.screens.MyClubsScreen
import com.campuspulse.app.ui.screens.NotificationsScreen
import com.campuspulse.app.ui.screens.ProfileScreen
import com.campuspulse.app.ui.screens.SignUpScreen
import com.campuspulse.app.ui.screens.VerifyEmailScreen
import com.campuspulse.app.ui.screens.WelcomeScreen


object Routes {

    const val COMPLETE_PROFILE = "complete_profile"
    const val SESSION = "session"
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val ADMIN_LOGIN = "admin_login"
    const val SIGN_UP = "sign_up"
    const val VERIFY = "verify"
    const val INTERESTS = "interests"
    const val FORGOT_PASSWORD = "forgot_password"

    const val HOME = "home"
    const val DIRECTORY = "directory"
    const val MY_CLUBS = "my_clubs"
    const val NOTIFICATIONS = "notifications"

    const val CLUB_ACCOUNT = "club_account"
    const val EVENT_DETAIL = "event_detail"
    const val EVENTS_CALENDAR = "events_calendar"
    const val PROFILE = "profile"
    const val CLUB_ADMIN = "club_admin"
    const val CREATE_CLUB = "create_club"
}


@Composable
fun CampusPulseNavGraph() {

    val navController: NavHostController =
        rememberNavController()


    // =============================================================
    // FIREBASE AUTHENTICATION
    // =============================================================

    val authRepository = remember {
        AuthRepository()
    }


    val clubAdminRepository = remember { ClubAdminRepository() }
    var adminLoginError by remember { mutableStateOf<String?>(null) }
    var adminLoginLoading by remember { mutableStateOf(false) }
    var assignedAdminClub by remember { mutableStateOf<com.campuspulse.app.model.Club?>(null) }

    // =============================================================
    // LOGIN STATE
    // =============================================================

    var loginErrorMessage by remember {
        mutableStateOf<String?>(null)
    }


    // =============================================================
    // SIGN UP STATE
    // =============================================================

    var signUpLoading by remember { mutableStateOf(false) }
    var signUpErrorMessage by remember {
        mutableStateOf<String?>(null)
    }


    // =============================================================
    // EMAIL VERIFICATION STATE
    // =============================================================

    var verificationErrorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var verificationSuccessMessage by remember {
        mutableStateOf<String?>(null)
    }


    // =============================================================
    // INTERESTS STATE
    // =============================================================

    var interestsErrorMessage by remember {
        mutableStateOf<String?>(null)
    }


    // =============================================================
    // SELECTED CLUB
    // =============================================================

    var selectedClub by remember { mutableStateOf<Club?>(null) }
    var selectedEvent by remember { mutableStateOf<EventItem?>(null) }


    // =============================================================
    // REMEMBER WHERE CLUB ACCOUNT WAS OPENED FROM
    // =============================================================

    var clubAccountParent by remember {
        mutableStateOf(
            NavDestination.SEARCH
        )
    }


    val routeContext = LocalContext.current
    val routeDatabase = remember { FirebaseFirestore.getInstance() }
    val sessionPreferences = remember(routeContext) {
        routeContext.getSharedPreferences("campuspulse_session_route", android.content.Context.MODE_PRIVATE)
    }
    fun rememberLoginMode(mode: String) {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        sessionPreferences.edit().putString("uid", uid).putString("mode", mode).apply()
    }
    var syncUserId by remember { mutableStateOf(com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid) }
    var memberSyncError by remember(syncUserId) { mutableStateOf<String?>(null) }
    var memberSyncRetry by remember { mutableIntStateOf(0) }
    androidx.compose.runtime.DisposableEffect(Unit) {
        val firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val listener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { syncUserId = it.currentUser?.uid }
        firebaseAuth.addAuthStateListener(listener)
        onDispose { firebaseAuth.removeAuthStateListener(listener) }
    }
    androidx.compose.runtime.DisposableEffect(syncUserId, memberSyncRetry) {
        memberSyncError = null
        val uid = syncUserId
        val stop = if (uid == null) null else com.campuspulse.app.data.StudentProfileRepository().observe(uid) { memberSyncError = it }
        onDispose { stop?.invoke() }
    }
    var resolvingContent by remember { mutableStateOf(false) }

    fun routeError(message: String) {
        Toast.makeText(routeContext, message, Toast.LENGTH_LONG).show()
    }

    fun openClubById(clubId: String) {
        if (resolvingContent) return
        if (clubId.isBlank()) { routeError("This content has no assigned club."); return }
        val origin = navController.currentDestination?.route
        resolvingContent = true
        routeDatabase.collection("clubs").document(clubId).get()
            .addOnSuccessListener { document ->
                resolvingContent = false
                if (navController.currentDestination?.route != origin) return@addOnSuccessListener
                if (!document.exists() || document.getString("status") != "APPROVED" || document.getBoolean("isActive") != true) {
                    routeError("This club is no longer available.")
                    return@addOnSuccessListener
                }
                selectedClub = Club(
                    id = document.id,
                    name = document.getString("name").orEmpty(),
                    category = Category.values().firstOrNull {
                        it.name.equals(document.getString("category"), ignoreCase = true)
                    } ?: Category.ACADEMIC,
                    members = document.getLong("memberCount")?.toInt() ?: 0,
                    description = document.getString("description").orEmpty(),
                    location = document.getString("location").orEmpty()
                )
                clubAccountParent = NavDestination.HOME
                navController.navigate(Routes.CLUB_ACCOUNT) { launchSingleTop = true }
            }
            .addOnFailureListener {
                resolvingContent = false
                if (navController.currentDestination?.route == origin)
                    routeError("Unable to open this club. Check your connection and try again.")
            }
    }

    fun openEventById(event: EventItem) {
        if (resolvingContent) return
        val origin = navController.currentDestination?.route
        resolvingContent = true
        routeDatabase.collection("events").document(event.id).get()
            .addOnSuccessListener { document ->
                resolvingContent = false
                if (navController.currentDestination?.route != origin) return@addOnSuccessListener
                val clubId = document.getString("clubId").orEmpty()
                if (!document.exists() || clubId.isBlank() || document.getString("status") != "PUBLISHED"
                    || document.getBoolean("isActive") != true) {
                    routeError("This event is no longer available.")
                    return@addOnSuccessListener
                }
                selectedEvent = event
                selectedClub = Club(id = clubId, name = document.getString("clubName") ?: event.hostClub,
                    category = event.category, members = 0, description = "", location = "")
                clubAccountParent = NavDestination.HOME
                navController.navigate(Routes.EVENT_DETAIL) { launchSingleTop = true }
            }
            .addOnFailureListener {
                resolvingContent = false
                if (navController.currentDestination?.route == origin)
                    routeError("Unable to open this event. Check your connection and try again.")
            }
    }

    // =============================================================
    // REMEMBER WHO OPENED FORGOT PASSWORD
    // =============================================================

    var forgotPasswordReturnRoute by remember {
        mutableStateOf(
            Routes.LOGIN
        )
    }


    // =============================================================
    // SCROLL-TO-TOP SIGNALS
    // =============================================================

    var homeScrollToTopSignal by remember {
        mutableIntStateOf(0)
    }

    var searchScrollToTopSignal by remember {
        mutableIntStateOf(0)
    }

    var myClubsScrollToTopSignal by remember {
        mutableIntStateOf(0)
    }

    var notificationsScrollToTopSignal by remember {
        mutableIntStateOf(0)
    }


    // =============================================================
    // CURRENT ROUTE
    // =============================================================

    val backStackEntry by
    navController.currentBackStackEntryAsState()

    val currentRoute =
        backStackEntry?.destination?.route


    // =============================================================
    // BOTTOM NAV VISIBILITY
    // =============================================================

    val showBottomBar =
        currentRoute in listOf(
            Routes.HOME,
            Routes.DIRECTORY,
            Routes.MY_CLUBS,
            Routes.NOTIFICATIONS,
            Routes.PROFILE,
            Routes.CLUB_ACCOUNT,
            Routes.EVENT_DETAIL,
            Routes.EVENTS_CALENDAR
        )


    // =============================================================
    // CURRENT BOTTOM NAV ITEM
    // =============================================================

    val currentDestination =
        when (currentRoute) {

            Routes.HOME ->
                NavDestination.HOME

            Routes.DIRECTORY ->
                NavDestination.SEARCH

            Routes.MY_CLUBS ->
                NavDestination.MY_CLUBS

            Routes.NOTIFICATIONS ->
                NavDestination.NOTIFICATIONS

            Routes.PROFILE ->
                NavDestination.ACCOUNT

            Routes.CLUB_ACCOUNT ->
                clubAccountParent

            Routes.EVENT_DETAIL ->
                NavDestination.HOME

            else ->
                NavDestination.HOME
        }


    // =============================================================
    // BOTTOM NAVIGATION HANDLER
    // =============================================================

    fun navigateFor(
        destination: NavDestination
    ) {

        val targetRoute =
            when (destination) {

                NavDestination.HOME ->
                    Routes.HOME

                NavDestination.SEARCH ->
                    Routes.DIRECTORY

                NavDestination.MY_CLUBS ->
                    Routes.MY_CLUBS

                NavDestination.NOTIFICATIONS ->
                    Routes.NOTIFICATIONS

                NavDestination.ACCOUNT ->
                    Routes.PROFILE
            }


        if (currentRoute == targetRoute) {

            when (destination) {

                NavDestination.HOME ->
                    homeScrollToTopSignal++

                NavDestination.SEARCH ->
                    searchScrollToTopSignal++

                NavDestination.MY_CLUBS ->
                    myClubsScrollToTopSignal++

                NavDestination.NOTIFICATIONS ->
                    notificationsScrollToTopSignal++

                else -> {}
            }

            return
        }


        navController.navigate(
            targetRoute
        ) {

            launchSingleTop = true
            restoreState = true

            popUpTo(
                Routes.HOME
            ) {
                saveState = true
            }
        }
    }


    // =============================================================
    // LOGOUT FOR STUDENTS AND CLUB ADMINS
    // =============================================================

    fun logoutAndReturnToWelcome() {
        sessionPreferences.edit().clear().apply()
        authRepository.logout()
        assignedAdminClub = null
        selectedClub = null
        selectedEvent = null
        adminLoginError = null
        adminLoginLoading = false

        loginErrorMessage = null
        signUpErrorMessage = null
        verificationErrorMessage = null
        verificationSuccessMessage = null
        interestsErrorMessage = null
        forgotPasswordReturnRoute = Routes.LOGIN

        // Bottom navigation saves screens. Discard those saved stacks so
        // another account cannot restore the previous account's screens.
        listOf(
            Routes.HOME,
            Routes.DIRECTORY,
            Routes.MY_CLUBS,
            Routes.NOTIFICATIONS,
            Routes.PROFILE,
            Routes.CLUB_ACCOUNT,
            Routes.EVENT_DETAIL,
            Routes.EVENTS_CALENDAR,
            Routes.CLUB_ADMIN,
            Routes.CREATE_CLUB
        ).forEach { route ->
            navController.clearBackStack(route)
        }

        navController.navigate(Routes.WELCOME) {
            popUpTo(navController.graph.id) {
                inclusive = true
                saveState = false
            }
            launchSingleTop = true
            restoreState = false
        }
    }


    // =============================================================
    // ROOT
    // =============================================================

    Box(
        modifier =
            Modifier.fillMaxSize()
    ) {

        NavHost(
            navController =
                navController,

            startDestination =
                Routes.SESSION,

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        bottom =
                            if (showBottomBar) {
                                72.dp
                            } else {
                                0.dp
                            }
                    )
        ) {


            composable(Routes.COMPLETE_PROFILE) {
                com.campuspulse.app.ui.screens.CompleteProfileScreen(
                    onComplete = {
                        navController.navigate(Routes.SESSION) {
                            popUpTo(Routes.COMPLETE_PROFILE) { inclusive = true }
                            launchSingleTop = true
                        }
                    }, onLogout = ::logoutAndReturnToWelcome
                )
            }
            composable(Routes.SESSION) {
                var sessionError by remember { mutableStateOf<String?>(null) }
                var retrySession by remember { mutableIntStateOf(0) }
                androidx.compose.runtime.DisposableEffect(retrySession) {
                    var active = true
                    val firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()
                    var handledAuth = false
                    fun finish(route: String) {
                        if (active) navController.navigate(route) {
                            popUpTo(Routes.SESSION) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                    fun fail(message: String) { if (active) sessionError = message }
                    sessionError = null
                    val authListener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { auth ->
                        if (!handledAuth && active) {
                            handledAuth = true
                            val user = auth.currentUser
                            if (user == null) finish(Routes.WELCOME)
                            else {
                                routeDatabase.collection("adminAccess").document(user.uid).get()
                                    .addOnSuccessListener { access ->
                                        if (active && firebaseAuth.currentUser?.uid == user.uid) {
                                            val restoreAdmin = sessionPreferences.getString("uid", null) == user.uid &&
                                                    sessionPreferences.getString("mode", null) == "ADMIN"
                                            if (!restoreAdmin) {
                                                authRepository.checkStudentAccess(
                                                    onNeedsProfile = { finish(Routes.COMPLETE_PROFILE) },
                                                    onNeedsVerification = { finish(Routes.VERIFY) },
                                                    onNeedsOnboarding = { finish(Routes.INTERESTS) },
                                                    onReadyForHome = { finish(Routes.HOME) },
                                                    onError = { fail(it) }
                                                )
                                            } else {
                                                val clubId = access.getString("clubId").orEmpty()
                                                if (!access.exists() || access.getBoolean("isActive") != true || clubId.isBlank()) {
                                                    fail("Your club admin access is inactive. Contact your administrator.")
                                                } else {
                                                    routeDatabase.collection("users").document(user.uid).get()
                                                        .addOnSuccessListener { profile ->
                                                            if (active) {
                                                                if (profile.exists() && profile.getString("accountStatus") != "ACTIVE") {
                                                                    fail("Your account is inactive. Contact your administrator.")
                                                                } else routeDatabase.collection("clubs").document(clubId).get()
                                                                    .addOnSuccessListener { club ->
                                                                        if (active) {
                                                                            if (!club.exists() || club.getString("status") != "APPROVED" || club.getBoolean("isActive") != true) {
                                                                                fail("Your assigned club is unavailable. Contact your administrator.")
                                                                            } else {
                                                                                assignedAdminClub = Club(
                                                                                    id = club.id, name = club.getString("name").orEmpty(),
                                                                                    category = Category.values().firstOrNull { it.name.equals(club.getString("category"), true) } ?: Category.ACADEMIC,
                                                                                    members = club.getLong("memberCount")?.toInt() ?: 0,
                                                                                    description = club.getString("description").orEmpty(),
                                                                                    location = club.getString("location").orEmpty()
                                                                                )
                                                                                finish(Routes.CLUB_ADMIN)
                                                                            }
                                                                        }
                                                                    }.addOnFailureListener { fail("Unable to load your club. Check your connection and retry.") }
                                                            }
                                                        }.addOnFailureListener { fail("Unable to check your account. Check your connection and retry.") }
                                                }
                                            }
                                        }
                                    }.addOnFailureListener { fail("Unable to restore your session. Check your connection and retry.") }
                            }
                        }
                    }
                    firebaseAuth.addAuthStateListener(authListener)
                    onDispose { active = false; firebaseAuth.removeAuthStateListener(authListener) }
                }
                com.campuspulse.app.ui.screens.BrandedLoadingScreen(
                    errorMessage = sessionError,
                    onRetry = { retrySession++ },
                    onUseAnotherAccount = ::logoutAndReturnToWelcome
                )
            }

            // =====================================================
            // WELCOME
            // =====================================================

            composable(
                Routes.WELCOME
            ) {

                WelcomeScreen(

                    onVerifyClick = {

                        signUpErrorMessage =
                            null

                        navController.navigate(
                            Routes.SIGN_UP
                        )
                    },

                    onLoginClick = {

                        loginErrorMessage =
                            null

                        navController.navigate(
                            Routes.LOGIN
                        )
                    },

                    onAdminClick = {

                        navController.navigate(
                            Routes.ADMIN_LOGIN
                        ) {
                            launchSingleTop =
                                true
                        }
                    }
                )
            }


            // =====================================================
            // STUDENT LOGIN — FIREBASE CONNECTED
            // =====================================================

            composable(
                Routes.LOGIN
            ) {

                LoginScreen(

                    onLoginClick = {
                            email,
                            password ->

                        loginErrorMessage =
                            null

                        authRepository.login(
                            email =
                                email,

                            password =
                                password,

                            onSuccess = {
                                rememberLoginMode("STUDENT")

                                loginErrorMessage =
                                    null


                                authRepository.checkStudentAccess(
                                    onNeedsProfile = {
                                        navController.navigate(Routes.COMPLETE_PROFILE) {
                                            popUpTo(Routes.WELCOME) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    },
                                    onNeedsVerification = {

                                        verificationErrorMessage =
                                            null

                                        verificationSuccessMessage =
                                            null

                                        navController.navigate(
                                            Routes.VERIFY
                                        ) {

                                            popUpTo(
                                                Routes.LOGIN
                                            ) {
                                                inclusive =
                                                    true
                                            }

                                            launchSingleTop =
                                                true
                                        }
                                    },


                                    onNeedsOnboarding = {

                                        interestsErrorMessage =
                                            null

                                        navController.navigate(
                                            Routes.INTERESTS
                                        ) {

                                            popUpTo(
                                                Routes.LOGIN
                                            ) {
                                                inclusive =
                                                    true
                                            }

                                            launchSingleTop =
                                                true
                                        }
                                    },


                                    onReadyForHome = {

                                        navController.navigate(
                                            Routes.HOME
                                        ) {

                                            popUpTo(
                                                Routes.WELCOME
                                            ) {
                                                inclusive =
                                                    true
                                            }

                                            launchSingleTop =
                                                true
                                        }
                                    },


                                    onError = {
                                            errorMessage ->

                                        loginErrorMessage =
                                            errorMessage
                                    }
                                )
                            },

                            onError = {
                                    errorMessage ->

                                loginErrorMessage =
                                    errorMessage
                            }
                        )
                    },

                    onSignUpClick = {

                        loginErrorMessage =
                            null

                        signUpErrorMessage =
                            null

                        navController.navigate(
                            Routes.SIGN_UP
                        )
                    },

                    onForgotPasswordClick = {

                        loginErrorMessage =
                            null

                        forgotPasswordReturnRoute =
                            Routes.LOGIN

                        navController.navigate(
                            Routes.FORGOT_PASSWORD
                        )
                    },

                    errorMessage =
                        loginErrorMessage
                )
            }


            // =====================================================
            // ADMIN LOGIN
            // =====================================================

            composable(
                Routes.ADMIN_LOGIN
            ) {

                AdminLoginScreen(

                    isLoading = adminLoginLoading,
                    errorMessage = adminLoginError,
                    onLoginClick = { email, password, clubCode ->
                        if (!adminLoginLoading) {
                            adminLoginLoading = true
                            adminLoginError = null
                            assignedAdminClub = null
                            clubAdminRepository.login(email, password, clubCode,
                                onSuccess = { assignedClub ->
                                    rememberLoginMode("ADMIN")
                                    assignedAdminClub = assignedClub
                                    adminLoginLoading = false
                                    navController.navigate(Routes.CLUB_ADMIN) {
                                        popUpTo(Routes.ADMIN_LOGIN) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                                onError = { message ->
                                    adminLoginLoading = false
                                    adminLoginError = message
                                }
                            )
                        }
                    },

                    onForgotPasswordClick = {

                        forgotPasswordReturnRoute =
                            Routes.ADMIN_LOGIN

                        navController.navigate(
                            Routes.FORGOT_PASSWORD
                        )
                    },

                    onBackClick = {

                        navController.popBackStack()
                    }
                )
            }


            // =====================================================
            // FORGOT PASSWORD — FIREBASE CONNECTED
            // =====================================================

            composable(
                Routes.FORGOT_PASSWORD
            ) {

                ForgotPasswordScreen(

                    onBack = {

                        navController.popBackStack()
                    },


                    onSendResetLink = {
                            email,
                            onSuccess,
                            onError ->

                        authRepository
                            .sendPasswordResetEmail(

                                email =
                                    email,

                                onSuccess = {

                                    onSuccess()
                                },

                                onError = {
                                        errorMessage ->

                                    onError(
                                        errorMessage
                                    )
                                }
                            )
                    },


                    onPasswordChanged = {

                        navController.navigate(
                            forgotPasswordReturnRoute
                        ) {

                            popUpTo(
                                Routes.FORGOT_PASSWORD
                            ) {
                                inclusive =
                                    true
                            }

                            launchSingleTop =
                                true
                        }
                    }
                )
            }


            // =====================================================
            // SIGN UP — FIREBASE CONNECTED
            // =====================================================

            composable(
                Routes.SIGN_UP
            ) {

                SignUpScreen(
                    isLoading = signUpLoading,
                    onSendCodeClicked = { name, email, password, studentNumber, course, department, year ->
                        if (signUpLoading) return@SignUpScreen
                        signUpLoading = true

                        signUpErrorMessage =
                            null

                        authRepository.signUp(
                            studentNumber = studentNumber, course = course, department = department, year = year,
                            name =
                                name,

                            email =
                                email,

                            password =
                                password,

                            onSuccess = {
                                signUpLoading = false
                                rememberLoginMode("STUDENT")
                                signUpErrorMessage =
                                    null

                                verificationErrorMessage =
                                    null

                                verificationSuccessMessage =
                                    "Verification email sent. Please check your inbox."

                                navController.navigate(
                                    Routes.VERIFY
                                ) {
                                    launchSingleTop =
                                        true
                                }
                            },

                            onError = {
                                    errorMessage ->
                                signUpLoading = false
                                signUpErrorMessage =
                                    errorMessage
                            }
                        )
                    },


                    // =============================================
                    // SIGN UP BACK BUTTON
                    // =============================================

                    onBackClick = {

                        signUpErrorMessage =
                            null

                        navController.popBackStack()
                    },


                    errorMessage =
                        signUpErrorMessage
                )
            }


            // =====================================================
            // VERIFY EMAIL — FIREBASE CONNECTED
            // =====================================================

            composable(
                Routes.VERIFY
            ) {

                VerifyEmailScreen(

                    email =
                        authRepository
                            .getCurrentUserEmail(),

                    onVerifyClicked = {

                        verificationErrorMessage =
                            null

                        verificationSuccessMessage =
                            null


                        authRepository
                            .checkEmailVerification(

                                onVerified = {

                                    verificationErrorMessage =
                                        null

                                    verificationSuccessMessage =
                                        null

                                    interestsErrorMessage =
                                        null

                                    navController.navigate(
                                        Routes.SESSION
                                    ) {

                                        popUpTo(
                                            Routes.VERIFY
                                        ) {
                                            inclusive =
                                                true
                                        }

                                        launchSingleTop =
                                            true
                                    }
                                },

                                onNotVerified = {

                                    verificationSuccessMessage =
                                        null

                                    verificationErrorMessage =
                                        "Your email has not been verified yet. Open the verification email, tap the link, then return here and try again."
                                },

                                onError = {
                                        errorMessage ->

                                    verificationSuccessMessage =
                                        null

                                    verificationErrorMessage =
                                        errorMessage
                                }
                            )
                    },


                    onResendClicked = {

                        verificationErrorMessage =
                            null

                        verificationSuccessMessage =
                            null


                        authRepository
                            .resendVerificationEmail(

                                onSuccess = {

                                    verificationErrorMessage =
                                        null

                                    verificationSuccessMessage =
                                        "A new verification email has been sent. Please check your inbox."
                                },

                                onError = {
                                        errorMessage ->

                                    verificationSuccessMessage =
                                        null

                                    verificationErrorMessage =
                                        errorMessage
                                }
                            )
                    },

                    errorMessage =
                        verificationErrorMessage,

                    successMessage =
                        verificationSuccessMessage
                )
            }


            // =====================================================
            // INTERESTS — FIREBASE CONNECTED
            // =====================================================

            composable(
                Routes.INTERESTS
            ) {

                InterestsScreen(

                    onContinue = {
                            selectedInterests ->

                        interestsErrorMessage =
                            null


                        authRepository
                            .saveInterests(

                                interests =
                                    selectedInterests,

                                onSuccess = {

                                    interestsErrorMessage =
                                        null


                                    navController.navigate(
                                        Routes.SESSION
                                    ) {

                                        popUpTo(
                                            Routes.WELCOME
                                        ) {
                                            inclusive =
                                                true
                                        }

                                        launchSingleTop =
                                            true
                                    }
                                },

                                onError = {
                                        errorMessage ->

                                    interestsErrorMessage =
                                        errorMessage
                                }
                            )
                    }
                )
            }


            // =====================================================
            // HOME
            // =====================================================

            composable(
                Routes.HOME
            ) {

                CampusPulseHomeScreen(

                    onOpenEvent = { event -> openEventById(event) },
                    onNavigateToClubHome = { clubId -> openClubById(clubId) },

                    onNavigate =
                        ::navigateFor,

                    onOpenCalendar = {
                        navController.navigate(Routes.EVENTS_CALENDAR) { launchSingleTop = true }
                    },
                    scrollToTopSignal =
                        homeScrollToTopSignal
                )
            }


            composable(Routes.EVENTS_CALENDAR) {
                EventsCalendarScreen(
                    onBack = { navController.popBackStack() },
                    onOpenEvent = { event, _ -> openEventById(event) }
                )
            }

            // =====================================================
            // DIRECTORY / SEARCH
            // =====================================================

            composable(
                Routes.DIRECTORY
            ) {

                DirectoryScreen(

                    onOpenClub = {
                            club ->

                        selectedClub =
                            club

                        clubAccountParent =
                            NavDestination.SEARCH

                        navController.navigate(
                            Routes.CLUB_ACCOUNT
                        )
                    },

                    onNavigate =
                        ::navigateFor,

                    scrollToTopSignal =
                        searchScrollToTopSignal
                )
            }


            // =====================================================
            // MY CLUBS
            // =====================================================

            composable(
                Routes.MY_CLUBS
            ) {

                MyClubsScreen(

                    clubs = emptyList(),

                    onOpenClub = {
                            club ->

                        selectedClub =
                            club

                        clubAccountParent =
                            NavDestination.MY_CLUBS

                        navController.navigate(
                            Routes.CLUB_ACCOUNT
                        )
                    },

                    onCreateClub = {

                        navController.navigate(
                            Routes.CREATE_CLUB
                        )
                    },

                    scrollToTopSignal =
                        myClubsScrollToTopSignal
                )
            }


            // =====================================================
            // CREATE CLUB
            // =====================================================

            composable(
                Routes.CREATE_CLUB
            ) {

                CreateClubScreen(

                    onBack = {

                        navController.popBackStack()
                    }
                )
            }


            // =====================================================
            // NOTIFICATIONS
            // =====================================================

            composable(
                Routes.NOTIFICATIONS
            ) {

                NotificationsScreen(

                    scrollToTopSignal =
                        notificationsScrollToTopSignal,

                    onOpenClub = { clubId ->
                        openClubById(clubId)
                    },

                    onOpenEvent = { event ->
                        openEventById(event)
                    }
                )
            }


            // =====================================================
            // CLUB ACCOUNT
            // =====================================================

            composable(
                Routes.CLUB_ACCOUNT
            ) {

                val currentClub = selectedClub
                if (currentClub == null) {
                    MissingSelectedContent(onHome = { navigateFor(NavDestination.HOME) })
                } else {
                    ClubAccountScreen(

                        club =
                            currentClub,

                        onBack = {

                            navController.popBackStack()
                        },

                        onManage = {

                            navController.navigate(
                                Routes.CLUB_ADMIN
                            )
                        },

                        onOpenEvent = { event -> openEventById(event) },
                        onNavigate =
                            ::navigateFor
                    )
                }
            }


            // =====================================================
            // EVENT DETAIL
            // =====================================================

            composable(
                Routes.EVENT_DETAIL
            ) {

                val currentEvent = selectedEvent
                if (currentEvent == null) {
                    MissingSelectedContent(onHome = { navigateFor(NavDestination.HOME) })
                } else {
                    EventDetailScreen(

                        event =
                            currentEvent,

                        onNavigate = {
                                destination ->

                            navigateFor(
                                destination
                            )
                        },

                        onNavigateToClubHome = { selectedClub?.id?.let { openClubById(it) } },

                        onBack = {

                            navController.popBackStack()
                        }
                    )
                }
            }


            // =====================================================
            // PROFILE
            // =====================================================

            composable(
                Routes.PROFILE
            ) {

                ProfileScreen(

                    onNavigate =
                        ::navigateFor,

                    onLogout =
                        ::logoutAndReturnToWelcome
                )
            }


            // =====================================================
            // CLUB ADMIN
            // =====================================================

            composable(
                Routes.CLUB_ADMIN
            ) {

                val adminClub = assignedAdminClub
                if (adminClub != null) {
                    ClubAdminScreen(
                        club = adminClub,
                        onLogout = ::logoutAndReturnToWelcome,
                        onAddEvent = {}
                    )
                } else {
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        navController.navigate(Routes.SESSION) {
                            popUpTo(Routes.CLUB_ADMIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            }
        }


        // =========================================================
        // FIXED BOTTOM NAVIGATION
        // =========================================================

        if (memberSyncError != null && currentRoute in listOf(Routes.PROFILE, Routes.CLUB_ADMIN)) {
            androidx.compose.material3.Surface(modifier = Modifier.align(Alignment.TopCenter), color = androidx.compose.ui.graphics.Color(0xFF00254A)) {
                androidx.compose.foundation.layout.Column(Modifier.padding(16.dp)) {
                    androidx.compose.material3.Text(memberSyncError.orEmpty(), color = androidx.compose.ui.graphics.Color.White)
                    androidx.compose.material3.TextButton(onClick = { memberSyncRetry++ }) { androidx.compose.material3.Text("Retry sync") }
                }
            }
        }
        if (showBottomBar) {

            Box(
                modifier =
                    Modifier.align(
                        Alignment.BottomCenter
                    ),

                contentAlignment =
                    Alignment.BottomCenter
            ) {

                CampusPulseBottomNav(

                    current =
                        currentDestination,

                    onSelect =
                        ::navigateFor
                )
            }
        }
    }
}

@Composable
private fun MissingSelectedContent(onHome: () -> Unit) {
    androidx.compose.foundation.layout.Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.material3.Text("Open this item again", color = androidx.compose.ui.graphics.Color.White)
        androidx.compose.material3.Text("Return Home to select a club or event after the app restarts.",
            color = androidx.compose.ui.graphics.Color(0xFFB9C9D8))
        androidx.compose.material3.TextButton(onClick = onHome) {
            androidx.compose.material3.Text("Return Home", color = androidx.compose.ui.graphics.Color(0xFFF7941D))
        }
    }
}
