package com.campuspulse.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class AuthRepository {

    private val auth: FirebaseAuth =
        FirebaseAuth.getInstance()

    private val firestore: FirebaseFirestore =
        FirebaseFirestore.getInstance()


    // =============================================================
    // LOGIN
    // =============================================================

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        if (email.isBlank() || password.isBlank()) {

            onError(
                "Please enter your email and password."
            )

            return
        }


        auth.signInWithEmailAndPassword(
            email.trim(),
            password
        )
            .addOnSuccessListener {

                onSuccess()
            }
            .addOnFailureListener {

                onError(
                    "Incorrect email or password."
                )
            }
    }


    // =============================================================
    // CHECK STUDENT ACCESS AFTER LOGIN
    // =============================================================

    fun checkStudentAccess(
        onNeedsVerification: () -> Unit,
        onNeedsOnboarding: () -> Unit,
        onReadyForHome: () -> Unit,
        onError: (String) -> Unit,
        onNeedsProfile: () -> Unit = { onError("Complete your student profile before continuing.") }
    ) {

        val firebaseUser =
            auth.currentUser


        // =========================================================
        // CHECK CURRENT USER
        // =========================================================

        if (firebaseUser == null) {

            onError(
                "No signed-in account was found. Please log in again."
            )

            return
        }


        // =========================================================
        // RELOAD AUTHENTICATION ACCOUNT
        // =========================================================

        firebaseUser
            .reload()
            .addOnSuccessListener {

                val refreshedUser =
                    auth.currentUser


                if (refreshedUser == null) {

                    onError(
                        "Unable to load your account."
                    )

                    return@addOnSuccessListener
                }


                // =================================================
                // EMAIL NOT VERIFIED
                // =================================================

                if (!refreshedUser.isEmailVerified) {

                    onNeedsVerification()

                    return@addOnSuccessListener
                }


                // =================================================
                // LOAD FIRESTORE PROFILE
                // =================================================

                firestore
                    .collection("users")
                    .document(refreshedUser.uid)
                    .get()
                    .addOnSuccessListener { document ->


                        // =========================================
                        // PROFILE DOES NOT EXIST
                        // =========================================

                        if (!document.exists()) {

                            onError(
                                "Your Campus Pulse profile could not be found."
                            )

                            return@addOnSuccessListener
                        }


                        // =========================================
                        // CHECK ACCOUNT STATUS
                        // =========================================

                        val accountStatus =
                            document.getString(
                                "accountStatus"
                            ) ?: "ACTIVE"


                        if (accountStatus != "ACTIVE") {

                            onError(
                                "Your Campus Pulse account is not active."
                            )

                            return@addOnSuccessListener
                        }


                        // =========================================
                        // CHECK ROLE
                        // =========================================

                        val role =
                            document.getString(
                                "role"
                            ) ?: ""


                        if (role != "STUDENT") {

                            onError(
                                "This login is for student accounts only."
                            )

                            return@addOnSuccessListener
                        }


                        // =========================================
                        // KEEP FIRESTORE VERIFICATION IN SYNC
                        // =========================================

                        val firestoreEmailVerified =
                            document.getBoolean(
                                "emailVerified"
                            ) ?: false


                        if (!firestoreEmailVerified) {

                            firestore
                                .collection("users")
                                .document(refreshedUser.uid)
                                .update(
                                    "emailVerified",
                                    true
                                )
                        }


                        // =========================================
                        // CHECK ONBOARDING
                        // =========================================

                        val requiredDetails = listOf("name", "studentNumber", "course", "department", "year")
                        if (requiredDetails.any { document.get(it)?.toString().isNullOrBlank() }
                            || document.get("name")?.toString().orEmpty().trim().length !in 2..80
                            || document.get("course")?.toString().orEmpty().trim().length !in 2..150
                            || document.get("department")?.toString().orEmpty().trim().length !in 2..150
                            || document.get("studentNumber")?.toString()?.matches(Regex("[0-9]{6,20}")) != true
                            || document.get("year")?.toString()?.matches(Regex("[1-8]")) != true) {
                            onNeedsProfile()
                            return@addOnSuccessListener
                        }

                        val onboardingCompleted =
                            document.getBoolean(
                                "onboardingCompleted"
                            ) ?: false


                        if (onboardingCompleted) {

                            onReadyForHome()

                        } else {

                            onNeedsOnboarding()
                        }
                    }
                    .addOnFailureListener {

                        onError(
                            "Unable to load your Campus Pulse profile. Please try again."
                        )
                    }
            }
            .addOnFailureListener {

                onError(
                    "Unable to check your account. Please try again."
                )
            }
    }


    // =============================================================
    // CREATE STUDENT ACCOUNT
    // =============================================================

    fun signUp(
        name: String,
        email: String,
        password: String,
        studentNumber: String,
        course: String,
        department: String,
        year: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        if (name.trim().length !in 2..80 || !studentNumber.trim().matches(Regex("[0-9]{6,20}"))
            || course.trim().length !in 2..150 || department.trim().length !in 2..150
            || !year.trim().matches(Regex("[1-8]"))) {
            onError("Complete your name, student number, programme, department and year (1–8).")
            return
        }
        val cleanName =
            name.trim()

        val cleanEmail =
            email.trim().lowercase()


        // =========================================================
        // VALIDATE NAME
        // =========================================================

        if (cleanName.isBlank()) {

            onError(
                "Please enter your full name."
            )

            return
        }


        // =========================================================
        // VALIDATE EMAIL
        // =========================================================

        if (cleanEmail.isBlank()) {

            onError(
                "Please enter your student email."
            )

            return
        }


        if (!cleanEmail.endsWith("@ump.ac.za")) {

            onError(
                "Please use your UMP student email address."
            )

            return
        }


        // =========================================================
        // VALIDATE PASSWORD
        // =========================================================

        if (password.isBlank()) {

            onError(
                "Please enter a password."
            )

            return
        }


        if (password.length < 8) {

            onError(
                "Password must contain at least 8 characters."
            )

            return
        }


        if (!password.any { it.isUpperCase() }) {

            onError(
                "Password must contain at least one uppercase letter."
            )

            return
        }


        if (!password.any { it.isLowerCase() }) {

            onError(
                "Password must contain at least one lowercase letter."
            )

            return
        }


        if (!password.any { it.isDigit() }) {

            onError(
                "Password must contain at least one number."
            )

            return
        }


        if (!password.any { !it.isLetterOrDigit() }) {

            onError(
                "Password must contain at least one special character."
            )

            return
        }


        // =========================================================
        // CREATE FIREBASE AUTH ACCOUNT
        // =========================================================

        auth.createUserWithEmailAndPassword(
            cleanEmail,
            password
        )
            .addOnSuccessListener {

                val firebaseUser =
                    auth.currentUser


                if (firebaseUser == null) {

                    onError(
                        "Account was created, but the user could not be loaded."
                    )

                    return@addOnSuccessListener
                }


                val uid =
                    firebaseUser.uid


                // =================================================
                // CREATE FIRESTORE USER PROFILE
                // =================================================

                val userData =
                    hashMapOf<String, Any>(
                        "uid" to uid,
                        "name" to cleanName,
                        "studentNumber" to studentNumber.trim(), "course" to course.trim(),
                        "department" to department.trim(), "year" to year.trim(),
                        "email" to cleanEmail,
                        "role" to "STUDENT",
                        "emailVerified" to false,
                        "accountStatus" to "ACTIVE",
                        "interests" to emptyList<String>(),
                        "onboardingCompleted" to false,
                        "createdAt" to
                                com.google.firebase.Timestamp.now()
                    )


                firestore
                    .collection("users")
                    .document(uid)
                    .set(userData)

                    .addOnSuccessListener {

                        // =========================================
                        // SEND VERIFICATION EMAIL
                        // =========================================

                        firebaseUser
                            .sendEmailVerification()

                            .addOnSuccessListener {

                                onSuccess()
                            }

                            .addOnFailureListener {

                                onError(
                                    "Account created, but the verification email could not be sent."
                                )
                            }
                    }

                    .addOnFailureListener {

                        onError(
                            "Account created, but your profile could not be saved."
                        )
                    }
            }

            .addOnFailureListener { exception ->

                val message =
                    when {

                        exception.message
                            ?.contains(
                                "email address is already in use",
                                ignoreCase = true
                            ) == true -> {

                            "An account already exists with this email."
                        }


                        exception.message
                            ?.contains(
                                "badly formatted",
                                ignoreCase = true
                            ) == true -> {

                            "Please enter a valid email address."
                        }


                        exception.message
                            ?.contains(
                                "network",
                                ignoreCase = true
                            ) == true -> {

                            "Network error. Please check your internet connection."
                        }


                        exception.message
                            ?.contains(
                                "password",
                                ignoreCase = true
                            ) == true -> {

                            "Your password does not meet the Campus Pulse security requirements."
                        }


                        else -> {

                            exception.message
                                ?: "Unable to create your account."
                        }
                    }


                onError(
                    message
                )
            }
    }


    // =============================================================
    // CHECK EMAIL VERIFICATION
    // =============================================================

    fun checkEmailVerification(
        onVerified: () -> Unit,
        onNotVerified: () -> Unit,
        onError: (String) -> Unit
    ) {

        val firebaseUser =
            auth.currentUser


        if (firebaseUser == null) {

            onError(
                "No signed-in account was found. Please log in again."
            )

            return
        }


        firebaseUser
            .reload()
            .addOnSuccessListener {

                val refreshedUser =
                    auth.currentUser


                if (refreshedUser == null) {

                    onError(
                        "Unable to load your account."
                    )

                    return@addOnSuccessListener
                }


                if (refreshedUser.isEmailVerified) {

                    firestore
                        .collection("users")
                        .document(refreshedUser.uid)
                        .update(
                            "emailVerified",
                            true
                        )
                        .addOnSuccessListener {

                            onVerified()
                        }
                        .addOnFailureListener {

                            onError(
                                "Your email is verified, but your profile could not be updated."
                            )
                        }

                } else {

                    onNotVerified()
                }
            }
            .addOnFailureListener {

                onError(
                    "Unable to check your verification status. Please try again."
                )
            }
    }


    // =============================================================
    // RESEND EMAIL VERIFICATION
    // =============================================================

    fun resendVerificationEmail(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        val firebaseUser =
            auth.currentUser


        if (firebaseUser == null) {

            onError(
                "No signed-in account was found. Please log in again."
            )

            return
        }


        if (firebaseUser.isEmailVerified) {

            onError(
                "Your email address is already verified."
            )

            return
        }


        firebaseUser
            .sendEmailVerification()
            .addOnSuccessListener {

                onSuccess()
            }
            .addOnFailureListener { exception ->

                val message =
                    when {

                        exception.message
                            ?.contains(
                                "network",
                                ignoreCase = true
                            ) == true -> {

                            "Network error. Please check your internet connection."
                        }


                        exception.message
                            ?.contains(
                                "too many",
                                ignoreCase = true
                            ) == true -> {

                            "Too many verification emails have been requested. Please wait before trying again."
                        }


                        else -> {

                            "Unable to resend the verification email. Please try again."
                        }
                    }


                onError(
                    message
                )
            }
    }


    // =============================================================
    // SEND PASSWORD RESET EMAIL
    // =============================================================

    fun sendPasswordResetEmail(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        val cleanEmail =
            email
                .trim()
                .lowercase()


        // =========================================================
        // VALIDATE EMAIL
        // =========================================================

        if (cleanEmail.isBlank()) {

            onError(
                "Please enter your UMP email address."
            )

            return
        }


        if (!cleanEmail.endsWith("@ump.ac.za")) {

            onError(
                "Please use your UMP email address."
            )

            return
        }


        // =========================================================
        // SEND FIREBASE PASSWORD RESET EMAIL
        // =========================================================

        auth
            .sendPasswordResetEmail(
                cleanEmail
            )
            .addOnSuccessListener {

                onSuccess()
            }
            .addOnFailureListener { exception ->

                val message =
                    when {

                        exception.message
                            ?.contains(
                                "network",
                                ignoreCase = true
                            ) == true -> {

                            "Network error. Please check your internet connection."
                        }


                        exception.message
                            ?.contains(
                                "badly formatted",
                                ignoreCase = true
                            ) == true -> {

                            "Please enter a valid email address."
                        }


                        exception.message
                            ?.contains(
                                "too many",
                                ignoreCase = true
                            ) == true -> {

                            "Too many password reset requests. Please wait before trying again."
                        }


                        else -> {

                            "Unable to send the password reset email. Please try again."
                        }
                    }


                onError(
                    message
                )
            }
    }


    // =============================================================
    // SAVE STUDENT INTERESTS
    // =============================================================

    fun saveInterests(
        interests: List<String>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        val firebaseUser =
            auth.currentUser


        // =========================================================
        // CHECK USER
        // =========================================================

        if (firebaseUser == null) {

            onError(
                "No signed-in account was found. Please log in again."
            )

            return
        }


        // =========================================================
        // CHECK EMAIL VERIFICATION
        // =========================================================

        if (!firebaseUser.isEmailVerified) {

            onError(
                "Please verify your email before continuing."
            )

            return
        }


        // =========================================================
        // CHECK NUMBER OF INTERESTS
        // =========================================================

        if (interests.size < 3) {

            onError(
                "Please select at least 3 interests."
            )

            return
        }


        // =========================================================
        // CLEAN INTEREST LIST
        // =========================================================

        val cleanInterests =
            interests
                .map {
                    it.trim()
                }
                .filter {
                    it.isNotBlank()
                }
                .distinct()


        if (cleanInterests.size < 3) {

            onError(
                "Please select at least 3 interests."
            )

            return
        }


        // =========================================================
        // SAVE TO FIRESTORE
        // =========================================================

        val updates =
            hashMapOf<String, Any>(
                "interests" to cleanInterests,
                "onboardingCompleted" to true,
                "updatedAt" to
                        com.google.firebase.Timestamp.now()
            )


        firestore
            .collection("users")
            .document(firebaseUser.uid)
            .update(updates)

            .addOnSuccessListener {

                onSuccess()
            }

            .addOnFailureListener {

                onError(
                    "Unable to save your interests. Please try again."
                )
            }
    }


    // =============================================================
    // GET CURRENT USER EMAIL
    // =============================================================

    fun getCurrentUserEmail(): String {

        return auth.currentUser
            ?.email
            ?: ""
    }


    // =============================================================
    // CURRENT USER
    // =============================================================

    fun getCurrentUser() =
        auth.currentUser


    // =============================================================
    // LOGOUT
    // =============================================================

    fun logout() {

        auth.signOut()
    }
}
