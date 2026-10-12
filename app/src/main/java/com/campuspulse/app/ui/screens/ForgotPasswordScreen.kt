package com.campuspulse.app.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuspulse.app.R
import com.campuspulse.app.ui.theme.CampusPulseTheme


// =============================================================================
// CAMPUS PULSE COLORS
// =============================================================================

private val CampusNavy =
    Color(0xFF0B3768)

private val DeepNavy =
    Color(0xFF061D38)

private val CardNavy =
    Color(0xFF102F50)

private val AccentOrange =
    Color(0xFFFF9800)

private val TextPrimary =
    Color.White

private val TextSecondary =
    Color(0xFFD4DCE5)

private val BorderColor =
    Color.White.copy(alpha = 0.16f)

private val ErrorRed =
    Color(0xFFFF6B6B)

private val SuccessGreen =
    Color(0xFF4CAF50)


// =============================================================================
// FORGOT PASSWORD SCREEN
// =============================================================================

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit = {},
    onSendResetLink: (
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) -> Unit = { _, onSuccess, _ ->
        onSuccess()
    },
    onPasswordChanged: () -> Unit = {}
) {

    var email by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var resetEmailSent by remember {
        mutableStateOf(false)
    }

    var isLoading by remember {
        mutableStateOf(false)
    }


    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    DeepNavy
                )
    ) {

        // =====================================================================
        // CAMPUS BACKGROUND
        // =====================================================================

        Image(
            painter =
                painterResource(
                    id =
                        R.drawable.login_campus_background
                ),
            contentDescription =
                null,
            modifier =
                Modifier.fillMaxSize(),
            contentScale =
                ContentScale.Crop,
            alpha =
                0.28f
        )


        // =====================================================================
        // NAVY OVERLAY
        // =====================================================================

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    CampusNavy.copy(
                                        alpha = 0.72f
                                    ),
                                    DeepNavy.copy(
                                        alpha = 0.94f
                                    ),
                                    DeepNavy
                                )
                        )
                    )
        )


        // =====================================================================
        // SCROLLABLE CONTENT
        // =====================================================================

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing
                    )
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(
                        horizontal = 20.dp,
                        vertical = 12.dp
                    )
        ) {

            // =================================================================
            // TOP BAR
            // =================================================================

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
                                40.dp
                            )
                            .clip(
                                CircleShape
                            )
                            .background(
                                CardNavy.copy(
                                    alpha = 0.92f
                                )
                            )
                            .border(
                                width =
                                    1.dp,
                                color =
                                    BorderColor,
                                shape =
                                    CircleShape
                            )
                            .clickable(
                                enabled =
                                    !isLoading
                            ) {

                                if (resetEmailSent) {

                                    resetEmailSent =
                                        false

                                    errorMessage =
                                        null

                                } else {

                                    onBack()
                                }
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
                            TextPrimary,
                        modifier =
                            Modifier.size(
                                19.dp
                            )
                    )
                }


                Spacer(
                    modifier =
                        Modifier.size(
                            11.dp
                        )
                )


                Text(
                    text =
                        "Forgot password",
                    color =
                        TextPrimary,
                    fontSize =
                        16.sp,
                    fontWeight =
                        FontWeight.SemiBold
                )
            }


            Spacer(
                modifier =
                    Modifier.height(
                        14.dp
                    )
            )


            // =================================================================
            // SMALLER LOGO
            // =================================================================

            Image(
                painter =
                    painterResource(
                        id =
                            R.drawable.campus_pulse_logo
                    ),
                contentDescription =
                    "Campus Pulse",
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            42.dp
                        ),
                contentScale =
                    ContentScale.Fit
            )


            Spacer(
                modifier =
                    Modifier.height(
                        18.dp
                    )
            )


            // =================================================================
            // PAGE
            // =================================================================

            if (resetEmailSent) {

                ResetEmailSentContent(
                    email =
                        email,
                    onBackToLogin =
                        onPasswordChanged
                )

            } else {

                ResetPasswordContent(
                    email =
                        email,

                    onEmailChange = {

                        email =
                            it

                        errorMessage =
                            null
                    },

                    errorMessage =
                        errorMessage,

                    isLoading =
                        isLoading,

                    onSendResetLink = {

                        val cleanEmail =
                            email
                                .trim()
                                .lowercase()


                        when {

                            cleanEmail.isBlank() -> {

                                errorMessage =
                                    "Please enter your UMP email address."
                            }


                            !cleanEmail.endsWith(
                                "@ump.ac.za"
                            ) -> {

                                errorMessage =
                                    "Please use your UMP email address."
                            }


                            else -> {

                                errorMessage =
                                    null

                                isLoading =
                                    true


                                onSendResetLink(

                                    cleanEmail,

                                    {

                                        isLoading =
                                            false

                                        errorMessage =
                                            null

                                        resetEmailSent =
                                            true
                                    },

                                    { message ->

                                        isLoading =
                                            false

                                        errorMessage =
                                            message
                                    }
                                )
                            }
                        }
                    }
                )
            }


            Spacer(
                modifier =
                    Modifier.height(
                        16.dp
                    )
            )
        }
    }
}


// =============================================================================
// RESET PASSWORD FORM
// =============================================================================

@Composable
private fun ResetPasswordContent(
    email: String,
    onEmailChange: (String) -> Unit,
    errorMessage: String?,
    isLoading: Boolean,
    onSendResetLink: () -> Unit
) {

    Column(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        // =====================================================================
        // COMPACT ICON
        // =====================================================================

        Box(
            modifier =
                Modifier
                    .size(
                        56.dp
                    )
                    .background(
                        CardNavy.copy(
                            alpha = 0.94f
                        ),
                        RoundedCornerShape(
                            17.dp
                        )
                    )
                    .border(
                        width =
                            1.dp,
                        color =
                            BorderColor,
                        shape =
                            RoundedCornerShape(
                                17.dp
                            )
                    ),
            contentAlignment =
                Alignment.Center
        ) {

            Icon(
                imageVector =
                    Icons.Filled.LockReset,
                contentDescription =
                    null,
                tint =
                    AccentOrange,
                modifier =
                    Modifier.size(
                        27.dp
                    )
            )
        }


        Spacer(
            modifier =
                Modifier.height(
                    16.dp
                )
        )


        // =====================================================================
        // TITLE
        // =====================================================================

        Text(
            text =
                "Reset your password",
            color =
                TextPrimary,
            fontSize =
                25.sp,
            lineHeight =
                30.sp,
            fontWeight =
                FontWeight.Bold
        )


        Spacer(
            modifier =
                Modifier.height(
                    6.dp
                )
        )


        Text(
            text =
                "Enter the UMP email linked to your Campus Pulse account. We'll send you a secure reset link.",
            color =
                TextSecondary,
            fontSize =
                13.sp,
            lineHeight =
                19.sp
        )


        Spacer(
            modifier =
                Modifier.height(
                    20.dp
                )
        )


        // =====================================================================
        // EMAIL
        // =====================================================================

        Text(
            text =
                "UMP email address",
            color =
                TextPrimary,
            fontSize =
                13.sp,
            fontWeight =
                FontWeight.SemiBold
        )


        Spacer(
            modifier =
                Modifier.height(
                    7.dp
                )
        )


        TextField(
            value =
                email,

            onValueChange =
                onEmailChange,

            enabled =
                !isLoading,

            placeholder = {

                Text(
                    text =
                        "student@ump.ac.za",
                    color =
                        TextSecondary.copy(
                            alpha = 0.70f
                        ),
                    fontSize =
                        14.sp
                )
            },

            leadingIcon = {

                Icon(
                    imageVector =
                        Icons.Filled.Email,
                    contentDescription =
                        null,
                    tint =
                        if (
                            errorMessage != null
                        ) {
                            ErrorRed
                        } else {
                            TextSecondary
                        },
                    modifier =
                        Modifier.size(
                            20.dp
                        )
                )
            },

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(
                        54.dp
                    )
                    .border(
                        width =
                            1.dp,
                        color =
                            if (
                                errorMessage != null
                            ) {
                                ErrorRed
                            } else {
                                BorderColor
                            },
                        shape =
                            RoundedCornerShape(
                                27.dp
                            )
                    ),

            shape =
                RoundedCornerShape(
                    27.dp
                ),

            colors =
                forgotPasswordFieldColors(),

            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.Email
                ),

            singleLine =
                true
        )


        AnimatedVisibility(
            visible =
                !errorMessage.isNullOrBlank(),
            enter =
                fadeIn(),
            exit =
                fadeOut()
        ) {

            Text(
                text =
                    errorMessage ?: "",
                color =
                    ErrorRed,
                fontSize =
                    12.sp,
                lineHeight =
                    16.sp,
                modifier =
                    Modifier.padding(
                        start =
                            10.dp,
                        top =
                            6.dp
                    )
            )
        }


        Spacer(
            modifier =
                Modifier.height(
                    16.dp
                )
        )


        // =====================================================================
        // SEND RESET LINK
        // =====================================================================

        Button(
            onClick =
                onSendResetLink,

            enabled =
                !isLoading,

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(
                        54.dp
                    ),

            shape =
                RoundedCornerShape(
                    27.dp
                ),

            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        AccentOrange,
                    contentColor =
                        DeepNavy,
                    disabledContainerColor =
                        AccentOrange.copy(
                            alpha = 0.55f
                        ),
                    disabledContentColor =
                        DeepNavy.copy(
                            alpha = 0.65f
                        )
                )
        ) {

            if (isLoading) {

                CircularProgressIndicator(
                    modifier =
                        Modifier.size(
                            20.dp
                        ),
                    strokeWidth =
                        2.5.dp,
                    color =
                        DeepNavy
                )


                Spacer(
                    modifier =
                        Modifier.size(
                            9.dp
                        )
                )


                Text(
                    text =
                        "Sending...",
                    fontSize =
                        14.sp,
                    fontWeight =
                        FontWeight.Bold
                )

            } else {

                Icon(
                    imageVector =
                        Icons.Filled.Email,
                    contentDescription =
                        null,
                    modifier =
                        Modifier.size(
                            18.dp
                        )
                )


                Spacer(
                    modifier =
                        Modifier.size(
                            8.dp
                        )
                )


                Text(
                    text =
                        "Send reset link",
                    fontSize =
                        14.sp,
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }


        Spacer(
            modifier =
                Modifier.height(
                    15.dp
                )
        )


        // =====================================================================
        // COMPACT INFORMATION CARD
        // =====================================================================

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        CardNavy.copy(
                            alpha = 0.78f
                        ),
                        RoundedCornerShape(
                            16.dp
                        )
                    )
                    .border(
                        width =
                            1.dp,
                        color =
                            BorderColor,
                        shape =
                            RoundedCornerShape(
                                16.dp
                            )
                    )
                    .padding(
                        13.dp
                    )
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Box(
                    modifier =
                        Modifier
                            .size(
                                30.dp
                            )
                            .background(
                                AccentOrange.copy(
                                    alpha = 0.14f
                                ),
                                CircleShape
                            ),
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            Icons.Filled.LockReset,
                        contentDescription =
                            null,
                        tint =
                            AccentOrange,
                        modifier =
                            Modifier.size(
                                16.dp
                            )
                    )
                }


                Spacer(
                    modifier =
                        Modifier.size(
                            10.dp
                        )
                )


                Column(
                    modifier =
                        Modifier.weight(
                            1f
                        )
                ) {

                    Text(
                        text =
                            "Secure password reset",
                        color =
                            TextPrimary,
                        fontSize =
                            12.sp,
                        fontWeight =
                            FontWeight.SemiBold
                    )


                    Spacer(
                        modifier =
                            Modifier.height(
                                2.dp
                            )
                    )


                    Text(
                        text =
                            "We'll send the reset link directly to your UMP email.",
                        color =
                            TextSecondary,
                        fontSize =
                            11.sp,
                        lineHeight =
                            16.sp
                    )
                }
            }
        }


        Spacer(
            modifier =
                Modifier.height(
                    12.dp
                )
        )


        Text(
            text =
                "Campus Pulse will never ask you to send your password by email.",
            color =
                TextSecondary.copy(
                    alpha = 0.82f
                ),
            fontSize =
                11.sp,
            lineHeight =
                16.sp,
            textAlign =
                TextAlign.Center,
            modifier =
                Modifier.fillMaxWidth()
        )
    }
}


// =============================================================================
// RESET EMAIL SENT
// =============================================================================

@Composable
private fun ResetEmailSentContent(
    email: String,
    onBackToLogin: () -> Unit
) {

    Column(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        // =====================================================================
        // COMPACT SUCCESS ICON
        // =====================================================================

        Box(
            modifier =
                Modifier
                    .size(
                        68.dp
                    )
                    .background(
                        SuccessGreen.copy(
                            alpha = 0.14f
                        ),
                        CircleShape
                    )
                    .border(
                        width =
                            1.dp,
                        color =
                            SuccessGreen.copy(
                                alpha = 0.32f
                            ),
                        shape =
                            CircleShape
                    ),
            contentAlignment =
                Alignment.Center
        ) {

            Box(
                modifier =
                    Modifier
                        .size(
                            42.dp
                        )
                        .background(
                            SuccessGreen,
                            CircleShape
                        ),
                contentAlignment =
                    Alignment.Center
            ) {

                Icon(
                    imageVector =
                        Icons.Filled.Check,
                    contentDescription =
                        null,
                    tint =
                        TextPrimary,
                    modifier =
                        Modifier.size(
                            24.dp
                        )
                )
            }
        }


        Spacer(
            modifier =
                Modifier.height(
                    16.dp
                )
        )


        // =====================================================================
        // SUCCESS MESSAGE
        // =====================================================================

        Text(
            text =
                "Check your email",
            color =
                TextPrimary,
            fontSize =
                25.sp,
            lineHeight =
                30.sp,
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
                "We've sent password reset instructions to",
            color =
                TextSecondary,
            fontSize =
                13.sp,
            lineHeight =
                18.sp,
            textAlign =
                TextAlign.Center
        )


        Spacer(
            modifier =
                Modifier.height(
                    3.dp
                )
        )


        Text(
            text =
                email,
            color =
                AccentOrange,
            fontSize =
                13.sp,
            fontWeight =
                FontWeight.Bold,
            textAlign =
                TextAlign.Center
        )


        Spacer(
            modifier =
                Modifier.height(
                    18.dp
                )
        )


        // =====================================================================
        // COMPACT INSTRUCTIONS
        // =====================================================================

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        CardNavy.copy(
                            alpha = 0.82f
                        ),
                        RoundedCornerShape(
                            16.dp
                        )
                    )
                    .border(
                        width =
                            1.dp,
                        color =
                            BorderColor,
                        shape =
                            RoundedCornerShape(
                                16.dp
                            )
                    )
                    .padding(
                        14.dp
                    )
        ) {

            Column {

                ResetInstruction(
                    number =
                        "1",
                    text =
                        "Open your UMP email."
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            10.dp
                        )
                )


                ResetInstruction(
                    number =
                        "2",
                    text =
                        "Open the Campus Pulse password reset email."
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            10.dp
                        )
                )


                ResetInstruction(
                    number =
                        "3",
                    text =
                        "Tap the link and create your new password."
                )
            }
        }


        Spacer(
            modifier =
                Modifier.height(
                    16.dp
                )
        )


        // =====================================================================
        // BACK TO LOGIN
        // =====================================================================

        Button(
            onClick =
                onBackToLogin,

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(
                        54.dp
                    ),

            shape =
                RoundedCornerShape(
                    27.dp
                ),

            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        AccentOrange,
                    contentColor =
                        DeepNavy
                )
        ) {

            Text(
                text =
                    "Back to login",
                fontSize =
                    14.sp,
                fontWeight =
                    FontWeight.Bold
            )
        }


        Spacer(
            modifier =
                Modifier.height(
                    12.dp
                )
        )


        Text(
            text =
                "Didn't receive it? Check your spam or junk folder, or use the back arrow to send another link.",
            color =
                TextSecondary,
            fontSize =
                11.sp,
            lineHeight =
                16.sp,
            textAlign =
                TextAlign.Center
        )
    }
}


// =============================================================================
// RESET INSTRUCTION
// =============================================================================

@Composable
private fun ResetInstruction(
    number: String,
    text: String
) {

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
                        27.dp
                    )
                    .background(
                        AccentOrange.copy(
                            alpha = 0.15f
                        ),
                        CircleShape
                    )
                    .border(
                        width =
                            1.dp,
                        color =
                            AccentOrange.copy(
                                alpha = 0.32f
                            ),
                        shape =
                            CircleShape
                    ),
            contentAlignment =
                Alignment.Center
        ) {

            Text(
                text =
                    number,
                color =
                    AccentOrange,
                fontSize =
                    11.sp,
                fontWeight =
                    FontWeight.Bold
            )
        }


        Spacer(
            modifier =
                Modifier.size(
                    10.dp
                )
        )


        Text(
            text =
                text,
            color =
                TextSecondary,
            fontSize =
                12.sp,
            lineHeight =
                17.sp,
            modifier =
                Modifier.weight(
                    1f
                )
        )
    }
}


// =============================================================================
// TEXT FIELD COLORS
// =============================================================================

@Composable
private fun forgotPasswordFieldColors() =
    TextFieldDefaults.colors(

        focusedContainerColor =
            CardNavy,

        unfocusedContainerColor =
            CardNavy,

        disabledContainerColor =
            CardNavy.copy(
                alpha = 0.70f
            ),

        focusedTextColor =
            TextPrimary,

        unfocusedTextColor =
            TextPrimary,

        disabledTextColor =
            TextPrimary.copy(
                alpha = 0.70f
            ),

        focusedPlaceholderColor =
            TextSecondary,

        unfocusedPlaceholderColor =
            TextSecondary,

        focusedLeadingIconColor =
            TextSecondary,

        unfocusedLeadingIconColor =
            TextSecondary,

        cursorColor =
            AccentOrange,

        focusedIndicatorColor =
            Color.Transparent,

        unfocusedIndicatorColor =
            Color.Transparent,

        disabledIndicatorColor =
            Color.Transparent
    )


// =============================================================================
// PREVIEW
// =============================================================================

@Preview(
    name =
        "Forgot Password",
    uiMode =
        Configuration.UI_MODE_NIGHT_YES,
    showBackground =
        true
)
@Composable
private fun ForgotPasswordScreenPreview() {

    CampusPulseTheme {

        ForgotPasswordScreen()
    }
}