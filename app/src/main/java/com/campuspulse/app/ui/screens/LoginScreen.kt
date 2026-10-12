package com.campuspulse.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
fun LoginScreen(
    onLoginClick: (email: String, password: String) -> Unit = { _, _ -> },
    onSignUpClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    errorMessage: String? = null
) {

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
    // COLOURS
    // =============================================================

    val campusNavy =
        Color(0xFF0B3768)

    val deepNavy =
        Color(0xFF061D38)

    val textFieldBackground =
        Color(0xFF102F50)

    val buttonColor =
        Color(0xFFFF9800)

    val primaryTextColor =
        Color.White

    val secondaryTextColor =
        Color(0xFFD4DCE5)

    val textFieldBorderColor =
        Color.White.copy(alpha = 0.18f)

    val errorColor =
        Color(0xFFFFB4AB)


    // =============================================================
    // MAIN SCREEN
    // =============================================================

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        // =========================================================
        // CAMPUS BACKGROUND IMAGE
        // =========================================================

        Image(
            painter = painterResource(
                id = R.drawable.login_campus_background
            ),
            contentDescription = "University Campus",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )


        // =========================================================
        // SMALL GLOBAL DARKENING
        // =========================================================

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(
                        alpha = 0.05f
                    )
                )
        )


        // =========================================================
        // NAVY GRADIENT
        // =========================================================

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(

                            0.00f to Color.Transparent,

                            0.20f to campusNavy.copy(
                                alpha = 0.05f
                            ),

                            0.32f to campusNavy.copy(
                                alpha = 0.28f
                            ),

                            0.45f to campusNavy.copy(
                                alpha = 0.58f
                            ),

                            0.58f to campusNavy.copy(
                                alpha = 0.82f
                            ),

                            0.70f to deepNavy.copy(
                                alpha = 0.94f
                            ),

                            0.82f to deepNavy.copy(
                                alpha = 0.98f
                            ),

                            1.00f to deepNavy
                        )
                    )
                )
        )


        // =========================================================
        // LOGIN CONTENT
        // =========================================================

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    horizontal = 24.dp,
                    vertical = 24.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(
                modifier = Modifier.height(40.dp)
            )


            // =====================================================
            // CAMPUS PULSE LOGO
            // =====================================================

            Image(
                painter = painterResource(
                    id = R.drawable.campus_pulse_logo
                ),
                contentDescription = "Campus Pulse Logo",
                modifier = Modifier
                    .size(85.dp)
                    .clip(
                        RoundedCornerShape(18.dp)
                    ),
                contentScale = ContentScale.Crop
            )


            Spacer(
                modifier = Modifier.height(10.dp)
            )


            Text(
                text = "CAMPUS PULSE",
                color = primaryTextColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )


            Spacer(
                modifier = Modifier.height(32.dp)
            )


            // =====================================================
            // WELCOME
            // =====================================================

            Text(
                text = "Welcome back",
                color = primaryTextColor,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )


            Spacer(
                modifier = Modifier.height(8.dp)
            )


            Text(
                text = "Log in to stay connected with your campus community.",
                color = secondaryTextColor,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(
                    horizontal = 16.dp
                )
            )


            Spacer(
                modifier = Modifier.height(32.dp)
            )


            // =====================================================
            // EMAIL FIELD
            // =====================================================

            TextField(
                value = email,

                onValueChange = {
                    email = it
                },

                placeholder = {
                    Text(
                        text = "Enter your email",
                        color = secondaryTextColor
                    )
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(
                        width = 1.dp,
                        color = if (errorMessage != null) {
                            errorColor
                        } else {
                            textFieldBorderColor
                        },
                        shape = RoundedCornerShape(28.dp)
                    ),

                shape = RoundedCornerShape(28.dp),

                colors = TextFieldDefaults.colors(

                    focusedContainerColor =
                        textFieldBackground.copy(
                            alpha = 0.92f
                        ),

                    unfocusedContainerColor =
                        textFieldBackground.copy(
                            alpha = 0.92f
                        ),

                    disabledContainerColor =
                        textFieldBackground,

                    focusedTextColor =
                        primaryTextColor,

                    unfocusedTextColor =
                        primaryTextColor,

                    cursorColor =
                        buttonColor,

                    focusedIndicatorColor =
                        Color.Transparent,

                    unfocusedIndicatorColor =
                        Color.Transparent
                ),

                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),

                singleLine = true
            )


            Spacer(
                modifier = Modifier.height(14.dp)
            )


            // =====================================================
            // PASSWORD FIELD
            // =====================================================

            TextField(
                value = password,

                onValueChange = {
                    password = it
                },

                placeholder = {
                    Text(
                        text = "Enter your password",
                        color = secondaryTextColor
                    )
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(
                        width = 1.dp,
                        color = if (errorMessage != null) {
                            errorColor
                        } else {
                            textFieldBorderColor
                        },
                        shape = RoundedCornerShape(28.dp)
                    ),

                shape = RoundedCornerShape(28.dp),

                colors = TextFieldDefaults.colors(

                    focusedContainerColor =
                        textFieldBackground.copy(
                            alpha = 0.92f
                        ),

                    unfocusedContainerColor =
                        textFieldBackground.copy(
                            alpha = 0.92f
                        ),

                    disabledContainerColor =
                        textFieldBackground,

                    focusedTextColor =
                        primaryTextColor,

                    unfocusedTextColor =
                        primaryTextColor,

                    cursorColor =
                        buttonColor,

                    focusedIndicatorColor =
                        Color.Transparent,

                    unfocusedIndicatorColor =
                        Color.Transparent
                ),

                visualTransformation =
                    if (isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },

                trailingIcon = {

                    val icon =
                        if (isPasswordVisible) {
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
                            imageVector = icon,
                            contentDescription =
                                "Toggle password visibility",
                            tint = secondaryTextColor
                        )
                    }
                },

                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),

                singleLine = true
            )


            // =====================================================
            // ERROR MESSAGE
            // =====================================================

            if (!errorMessage.isNullOrBlank()) {

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Text(
                    text = errorMessage,
                    color = errorColor,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }


            // =====================================================
            // FORGOT PASSWORD
            // =====================================================

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 8.dp
                    ),
                contentAlignment = Alignment.CenterEnd
            ) {

                Text(
                    text = "Forgot password?",
                    color = buttonColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        onForgotPasswordClick()
                    }
                )
            }


            Spacer(
                modifier = Modifier.height(24.dp)
            )


            // =====================================================
            // LOGIN BUTTON
            // =====================================================

            Button(
                onClick = {

                    onLoginClick(
                        email,
                        password
                    )
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),

                shape = RoundedCornerShape(28.dp),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = buttonColor
                    )
            ) {

                Text(
                    text = "Log In",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }


            Spacer(
                modifier = Modifier.height(20.dp)
            )


            // =====================================================
            // SIGN-UP LINK
            // =====================================================

            Row(
                horizontalArrangement =
                    Arrangement.Center,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text = "Don't have an account? ",
                    color = secondaryTextColor,
                    fontSize = 14.sp
                )

                Text(
                    text = "Sign up",
                    color = buttonColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        onSignUpClick()
                    }
                )
            }


            Spacer(
                modifier = Modifier.height(24.dp)
            )
        }
    }
}


// =============================================================
// PREVIEW
// =============================================================

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun LoginScreenPreview() {

    LoginScreen()
}