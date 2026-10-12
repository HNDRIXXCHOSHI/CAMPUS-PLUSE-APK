package com.campuspulse.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuspulse.app.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onSendCodeClicked: (
        name: String,
        email: String,
        password: String,
        studentNumber: String,
        course: String,
        department: String,
        year: String
    ) -> Unit = { _, _, _, _, _, _, _ -> },

    onBackClick: () -> Unit = {},

    errorMessage: String? = null,
    isLoading: Boolean = false
) {

    var name by remember {
        mutableStateOf("")
    }

    var studentNumber by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var isPasswordVisible by remember {
        mutableStateOf(false)
    }


    // =============================================================
    // CAMPUS PULSE COLOURS
    // =============================================================

    val campusNavy =
        Color(0xFF0B3768)

    val deepNavy =
        Color(0xFF061D38)

    val textFieldBackground =
        Color(0xFF102F50)

    val accentOrange =
        Color(0xFFFF9800)

    val primaryTextColor =
        Color.White

    val secondaryTextColor =
        Color(0xFFD4DCE5)

    val normalBorderColor =
        Color.White.copy(
            alpha = 0.18f
        )

    val errorColor =
        Color(0xFFFFB4AB)

    val successColor =
        Color(0xFF72D572)


    // =============================================================
    // PASSWORD POLICY
    // =============================================================

    val hasMinimumLength =
        password.length >= 8

    val hasUppercase =
        password.any {
            it.isUpperCase()
        }

    val hasLowercase =
        password.any {
            it.isLowerCase()
        }

    val hasNumber =
        password.any {
            it.isDigit()
        }

    val hasSpecialCharacter =
        password.any {
            !it.isLetterOrDigit()
        }

    val isPasswordValid =
        hasMinimumLength &&
                hasUppercase &&
                hasLowercase &&
                hasNumber &&
                hasSpecialCharacter


    // =============================================================
    // FORM VALIDITY
    // =============================================================

    val cleanName =
        name.trim()

    val cleanEmail =
        email
            .trim()
            .lowercase()

    val isEmailValid =
        cleanEmail.endsWith(
            "@ump.ac.za"
        ) &&
                cleanEmail.length >
                "@ump.ac.za".length

    val isFormValid =
        !isLoading && cleanName.length in 2..80 &&
                studentNumber.trim().matches(Regex("[0-9]{6,20}")) &&
                course.trim().length in 2..150 && department.trim().length in 2..150 &&
                year.trim().matches(Regex("[1-8]")) &&
                isEmailValid &&
                isPasswordValid


    // =============================================================
    // MAIN SCREEN
    // =============================================================

    Box(
        modifier =
            Modifier.fillMaxSize()
    ) {


        // =========================================================
        // CAMPUS BACKGROUND
        // =========================================================

        Image(
            painter =
                painterResource(
                    id =
                        R.drawable.login_campus_background
                ),
            contentDescription =
                "University Campus",
            modifier =
                Modifier.fillMaxSize(),
            contentScale =
                ContentScale.Crop
        )


        // =========================================================
        // DARKEN BACKGROUND
        // =========================================================

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(
                            alpha = 0.05f
                        )
                    )
        )


        // =========================================================
        // CAMPUS PULSE NAVY GRADIENT
        // =========================================================

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops =
                                arrayOf(

                                    0.00f to
                                            Color.Transparent,

                                    0.16f to
                                            campusNavy.copy(
                                                alpha = 0.08f
                                            ),

                                    0.28f to
                                            campusNavy.copy(
                                                alpha = 0.35f
                                            ),

                                    0.40f to
                                            campusNavy.copy(
                                                alpha = 0.68f
                                            ),

                                    0.52f to
                                            campusNavy.copy(
                                                alpha = 0.88f
                                            ),

                                    0.65f to
                                            deepNavy.copy(
                                                alpha = 0.96f
                                            ),

                                    0.80f to
                                            deepNavy.copy(
                                                alpha = 0.99f
                                            ),

                                    1.00f to
                                            deepNavy
                                )
                        )
                    )
        )


        // =========================================================
        // SIGN-UP CONTENT
        // =========================================================

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .imePadding()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(
                        horizontal = 24.dp,
                        vertical = 16.dp
                    ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {


            // =====================================================
            // TOP ROW — BACK BUTTON ON LEFT
            // =====================================================

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.Start,
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
                                textFieldBackground.copy(
                                    alpha = 0.90f
                                )
                            )
                            .border(
                                width =
                                    1.dp,
                                color =
                                    normalBorderColor,
                                shape =
                                    CircleShape
                            )
                            .clickable {

                                onBackClick()
                            },
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription =
                            "Back",
                        tint =
                            primaryTextColor,
                        modifier =
                            Modifier.size(
                                20.dp
                            )
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(
                        6.dp
                    )
            )


            // =====================================================
            // CAMPUS PULSE LOGO
            // =====================================================

            Image(
                painter =
                    painterResource(
                        id =
                            R.drawable.campus_pulse_logo
                    ),
                contentDescription =
                    "Campus Pulse Logo",
                modifier =
                    Modifier
                        .size(
                            68.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                16.dp
                            )
                        ),
                contentScale =
                    ContentScale.Crop
            )


            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
                    )
            )


            // =====================================================
            // BRAND NAME
            // =====================================================

            Text(
                text =
                    "CAMPUS PULSE",
                color =
                    primaryTextColor,
                fontSize =
                    17.sp,
                fontWeight =
                    FontWeight.Bold,
                letterSpacing =
                    2.sp
            )


            Spacer(
                modifier =
                    Modifier.height(
                        16.dp
                    )
            )


            // =====================================================
            // HEADING
            // =====================================================

            Text(
                text =
                    "Create your account",
                color =
                    primaryTextColor,
                fontSize =
                    27.sp,
                fontWeight =
                    FontWeight.Bold,
                textAlign =
                    TextAlign.Center
            )


            Spacer(
                modifier =
                    Modifier.height(
                        6.dp
                    )
            )


            Text(
                text =
                    "Use your UMP student email to join your campus community.",
                color =
                    secondaryTextColor,
                fontSize =
                    13.sp,
                lineHeight =
                    19.sp,
                textAlign =
                    TextAlign.Center,
                modifier =
                    Modifier.padding(
                        horizontal =
                            16.dp
                    )
            )


            Spacer(
                modifier =
                    Modifier.height(
                        22.dp
                    )
            )


            // =====================================================
            // FULL NAME
            // =====================================================

            TextField(
                value =
                    name,

                onValueChange = {
                    name =
                        it
                },

                placeholder = {

                    Text(
                        text =
                            "Full name",
                        color =
                            secondaryTextColor
                    )
                },

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            56.dp
                        )
                        .border(
                            width =
                                1.dp,
                            color =
                                normalBorderColor,
                            shape =
                                RoundedCornerShape(
                                    28.dp
                                )
                        ),

                shape =
                    RoundedCornerShape(
                        28.dp
                    ),

                colors =
                    campusTextFieldColors(
                        background =
                            textFieldBackground,
                        textColor =
                            primaryTextColor,
                        secondaryColor =
                            secondaryTextColor,
                        cursorColor =
                            accentOrange
                    ),

                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Text,
                        imeAction =
                            ImeAction.Next
                    ),

                singleLine =
                    true
            )


            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )


            // =====================================================
            // STUDENT EMAIL
            // =====================================================

            TextField(
                value =
                    email,

                onValueChange = {
                    email =
                        it
                },

                placeholder = {

                    Text(
                        text =
                            "StudentNumber@ump.ac.za",
                        color =
                            secondaryTextColor
                    )
                },

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            56.dp
                        )
                        .border(
                            width =
                                1.dp,
                            color =
                                normalBorderColor,
                            shape =
                                RoundedCornerShape(
                                    28.dp
                                )
                        ),

                shape =
                    RoundedCornerShape(
                        28.dp
                    ),

                colors =
                    campusTextFieldColors(
                        background =
                            textFieldBackground,
                        textColor =
                            primaryTextColor,
                        secondaryColor =
                            secondaryTextColor,
                        cursorColor =
                            accentOrange
                    ),

                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Email,
                        imeAction =
                            ImeAction.Next
                    ),

                singleLine =
                    true
            )


            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )


            listOf("Student number" to studentNumber, "Programme" to course,
                "Department / Faculty" to department, "Year of study (1–8)" to year).forEachIndexed { index, field ->
                TextField(value = field.second, onValueChange = { value ->
                    when (index) { 0 -> studentNumber = value; 1 -> course = value; 2 -> department = value; 3 -> year = value }
                }, placeholder = { Text(field.first, color = secondaryTextColor) },
                    enabled = !isLoading, singleLine = true,
                    modifier = Modifier.fillMaxWidth().border(1.dp, normalBorderColor, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = TextFieldDefaults.colors(focusedContainerColor = textFieldBackground,
                        unfocusedContainerColor = textFieldBackground, focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White, focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent))
                Spacer(Modifier.height(12.dp))
            }
            // =====================================================
            // PASSWORD
            // =====================================================

            TextField(
                value =
                    password,

                onValueChange = {
                    password =
                        it
                },

                placeholder = {

                    Text(
                        text =
                            "Password",
                        color =
                            secondaryTextColor
                    )
                },

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            56.dp
                        )
                        .border(
                            width =
                                1.dp,
                            color =
                                if (
                                    password.isNotEmpty() &&
                                    !isPasswordValid
                                ) {
                                    errorColor.copy(
                                        alpha = 0.65f
                                    )
                                } else if (
                                    isPasswordValid
                                ) {
                                    successColor.copy(
                                        alpha = 0.75f
                                    )
                                } else {
                                    normalBorderColor
                                },
                            shape =
                                RoundedCornerShape(
                                    28.dp
                                )
                        ),

                shape =
                    RoundedCornerShape(
                        28.dp
                    ),

                colors =
                    campusTextFieldColors(
                        background =
                            textFieldBackground,
                        textColor =
                            primaryTextColor,
                        secondaryColor =
                            secondaryTextColor,
                        cursorColor =
                            accentOrange
                    ),

                visualTransformation =
                    if (
                        isPasswordVisible
                    ) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },

                trailingIcon = {

                    val icon =
                        if (
                            isPasswordVisible
                        ) {
                            Icons.Filled.Visibility
                        } else {
                            Icons.Filled.VisibilityOff
                        }

                    IconButton(
                        onClick = {

                            isPasswordVisible =
                                !isPasswordVisible
                        }
                    ) {

                        Icon(
                            imageVector =
                                icon,
                            contentDescription =
                                "Toggle password visibility",
                            tint =
                                secondaryTextColor
                        )
                    }
                },

                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Password,
                        imeAction =
                            ImeAction.Done
                    ),

                singleLine =
                    true
            )


            // =====================================================
            // PASSWORD REQUIREMENTS
            // =====================================================

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )


            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(
                            textFieldBackground.copy(
                                alpha = 0.72f
                            ),
                            RoundedCornerShape(
                                16.dp
                            )
                        )
                        .border(
                            width =
                                1.dp,
                            color =
                                normalBorderColor,
                            shape =
                                RoundedCornerShape(
                                    16.dp
                                )
                        )
                        .padding(
                            horizontal =
                                14.dp,
                            vertical =
                                12.dp
                        )
            ) {

                Text(
                    text =
                        "Your password must include:",
                    color =
                        primaryTextColor,
                    fontSize =
                        12.sp,
                    fontWeight =
                        FontWeight.SemiBold
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            8.dp
                        )
                )


                PasswordRequirementRow(
                    text =
                        "At least 8 characters",
                    isMet =
                        hasMinimumLength,
                    emptyPassword =
                        password.isEmpty(),
                    successColor =
                        successColor,
                    secondaryTextColor =
                        secondaryTextColor,
                    errorColor =
                        errorColor
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            5.dp
                        )
                )


                PasswordRequirementRow(
                    text =
                        "One uppercase letter",
                    isMet =
                        hasUppercase,
                    emptyPassword =
                        password.isEmpty(),
                    successColor =
                        successColor,
                    secondaryTextColor =
                        secondaryTextColor,
                    errorColor =
                        errorColor
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            5.dp
                        )
                )


                PasswordRequirementRow(
                    text =
                        "One lowercase letter",
                    isMet =
                        hasLowercase,
                    emptyPassword =
                        password.isEmpty(),
                    successColor =
                        successColor,
                    secondaryTextColor =
                        secondaryTextColor,
                    errorColor =
                        errorColor
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            5.dp
                        )
                )


                PasswordRequirementRow(
                    text =
                        "One number",
                    isMet =
                        hasNumber,
                    emptyPassword =
                        password.isEmpty(),
                    successColor =
                        successColor,
                    secondaryTextColor =
                        secondaryTextColor,
                    errorColor =
                        errorColor
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            5.dp
                        )
                )


                PasswordRequirementRow(
                    text =
                        "One special character",
                    isMet =
                        hasSpecialCharacter,
                    emptyPassword =
                        password.isEmpty(),
                    successColor =
                        successColor,
                    secondaryTextColor =
                        secondaryTextColor,
                    errorColor =
                        errorColor
                )
            }


            // =====================================================
            // FIREBASE / SERVER ERROR
            // =====================================================

            if (
                !errorMessage.isNullOrBlank()
            ) {

                Spacer(
                    modifier =
                        Modifier.height(
                            10.dp
                        )
                )


                Text(
                    text =
                        errorMessage,
                    color =
                        errorColor,
                    fontSize =
                        13.sp,
                    lineHeight =
                        18.sp,
                    modifier =
                        Modifier.fillMaxWidth()
                )
            }


            // =====================================================
            // CREATE ACCOUNT BUTTON
            // =====================================================

            Spacer(
                modifier =
                    Modifier.height(
                        18.dp
                    )
            )


            Button(
                onClick = {

                    onSendCodeClicked(
                        cleanName,
                        cleanEmail,
                        password, studentNumber.trim(), course.trim(), department.trim(), year.trim()
                    )
                },

                enabled =
                    isFormValid,

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            56.dp
                        ),

                shape =
                    RoundedCornerShape(
                        28.dp
                    ),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            accentOrange,
                        contentColor =
                            Color.White,
                        disabledContainerColor =
                            accentOrange.copy(
                                alpha = 0.35f
                            ),
                        disabledContentColor =
                            Color.White.copy(
                                alpha = 0.55f
                            )
                    )
            ) {

                Text(
                    text =
                        "Create account",
                    fontWeight =
                        FontWeight.Bold,
                    fontSize =
                        16.sp
                )
            }


            Spacer(
                modifier =
                    Modifier.height(
                        24.dp
                    )
            )
        }
    }
}


// =================================================================
// PASSWORD REQUIREMENT ROW
// =================================================================

@Composable
private fun PasswordRequirementRow(
    text: String,
    isMet: Boolean,
    emptyPassword: Boolean,
    successColor: Color,
    secondaryTextColor: Color,
    errorColor: Color
) {

    val iconColor =
        when {

            emptyPassword ->
                secondaryTextColor.copy(
                    alpha = 0.65f
                )

            isMet ->
                successColor

            else ->
                errorColor
        }


    val textColor =
        when {

            emptyPassword ->
                secondaryTextColor.copy(
                    alpha = 0.82f
                )

            isMet ->
                successColor

            else ->
                secondaryTextColor
        }


    Row(
        modifier =
            Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(
            modifier =
                Modifier
                    .size(
                        20.dp
                    )
                    .background(
                        iconColor.copy(
                            alpha = 0.14f
                        ),
                        CircleShape
                    ),
            contentAlignment =
                Alignment.Center
        ) {

            Icon(
                imageVector =
                    if (
                        isMet
                    ) {
                        Icons.Filled.Check
                    } else {
                        Icons.Filled.Close
                    },
                contentDescription =
                    null,
                tint =
                    iconColor,
                modifier =
                    Modifier.size(
                        13.dp
                    )
            )
        }


        Spacer(
            modifier =
                Modifier.width(
                    9.dp
                )
        )


        Text(
            text =
                text,
            color =
                textColor,
            fontSize =
                11.sp,
            lineHeight =
                15.sp
        )
    }
}


// =================================================================
// CAMPUS PULSE TEXT FIELD COLORS
// =================================================================

@Composable
private fun campusTextFieldColors(
    background: Color,
    textColor: Color,
    secondaryColor: Color,
    cursorColor: Color
) =
    TextFieldDefaults.colors(

        focusedContainerColor =
            background.copy(
                alpha = 0.92f
            ),

        unfocusedContainerColor =
            background.copy(
                alpha = 0.92f
            ),

        focusedTextColor =
            textColor,

        unfocusedTextColor =
            textColor,

        focusedPlaceholderColor =
            secondaryColor,

        unfocusedPlaceholderColor =
            secondaryColor,

        cursorColor =
            cursorColor,

        focusedIndicatorColor =
            Color.Transparent,

        unfocusedIndicatorColor =
            Color.Transparent
    )


// =============================================================
// PREVIEW
// =============================================================

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun SignUpScreenPreview() {

    SignUpScreen()
}
