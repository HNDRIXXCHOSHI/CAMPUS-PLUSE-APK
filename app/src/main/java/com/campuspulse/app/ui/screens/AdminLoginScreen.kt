package com.campuspulse.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.campuspulse.app.R
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AdminBackgroundTop = Color(0xFF17152D)
private val AdminBackgroundMiddle = Color(0xFF211F3D)
private val AdminBackgroundBottom = Color(0xFF0F0E1E)

private val AdminOrange = Color(0xFFF9AD38)
private val AdminWhite = Color(0xFFFFFFFF)
private val AdminMuted = Color(0xFFA8A6B3)
private val AdminFieldBackground = Color(0xFF24223B)
private val AdminBorder = Color(0xFF3A3753)

@Composable
fun AdminLoginScreen(
    onLoginClick: (email: String, password: String, clubCode: String) -> Unit = { _, _, _ -> },
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onForgotPasswordClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    BackHandler(enabled = isLoading) {}
    var clubCode by remember { mutableStateOf("") }
    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var passwordVisible by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AdminBackgroundTop,
                        AdminBackgroundMiddle,
                        AdminBackgroundBottom
                    )
                )
            )
            .safeDrawingPadding()
    ) {

        // =============================================================
        // BACK BUTTON
        // =============================================================

        IconButton(
            onClick = onBackClick,
            enabled = !isLoading,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = 12.dp,
                    top = 8.dp
                )
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = AdminWhite
            )
        }

        // =============================================================
        // MAIN CONTENT
        // =============================================================

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 28.dp,
                    vertical = 32.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // =========================================================
            // CAMPUS PULSE LOGO
            // =========================================================

            Image(
                painter = painterResource(
                    id = R.drawable.campus_pulse_logo
                ),
                contentDescription = "Campus Pulse Logo",
                modifier = Modifier
                    .size(80.dp)
                    .clip(
                        RoundedCornerShape(18.dp)
                    ),
                contentScale = ContentScale.Crop
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            // =========================================================
            // BRAND
            // =========================================================

            Text(
                text = "CAMPUS PULSE",
                color = AdminOrange,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = "Club Admin",
                color = AdminWhite,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = "Sign in to manage your club, events and campus community.",
                color = AdminMuted,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(
                    horizontal = 16.dp
                )
            )

            Spacer(
                modifier = Modifier.height(34.dp)
            )

            // =========================================================
            // EMAIL
            // =========================================================

            OutlinedTextField(
                enabled = !isLoading,
                value = email,
                onValueChange = {
                    email = it
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = {
                    Text("Admin email")
                },
                placeholder = {
                    Text("Enter your email")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = null
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AdminWhite,
                    unfocusedTextColor = AdminWhite,
                    focusedContainerColor = AdminFieldBackground,
                    unfocusedContainerColor = AdminFieldBackground,
                    focusedBorderColor = AdminOrange,
                    unfocusedBorderColor = AdminBorder,
                    focusedLabelColor = AdminOrange,
                    unfocusedLabelColor = AdminMuted,
                    focusedLeadingIconColor = AdminOrange,
                    unfocusedLeadingIconColor = AdminMuted,
                    cursorColor = AdminOrange,
                    focusedPlaceholderColor = AdminMuted,
                    unfocusedPlaceholderColor = AdminMuted
                )
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            // =========================================================
            // PASSWORD
            // =========================================================

            OutlinedTextField(
                enabled = !isLoading,
                value = password,
                onValueChange = {
                    password = it
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = {
                    Text("Password")
                },
                placeholder = {
                    Text("Enter your password")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            passwordVisible = !passwordVisible
                        }
                    ) {
                        Icon(
                            imageVector =
                                if (passwordVisible) {
                                    Icons.Filled.VisibilityOff
                                } else {
                                    Icons.Filled.Visibility
                                },
                            contentDescription =
                                if (passwordVisible) {
                                    "Hide password"
                                } else {
                                    "Show password"
                                }
                        )
                    }
                },
                visualTransformation =
                    if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AdminWhite,
                    unfocusedTextColor = AdminWhite,
                    focusedContainerColor = AdminFieldBackground,
                    unfocusedContainerColor = AdminFieldBackground,
                    focusedBorderColor = AdminOrange,
                    unfocusedBorderColor = AdminBorder,
                    focusedLabelColor = AdminOrange,
                    unfocusedLabelColor = AdminMuted,
                    focusedLeadingIconColor = AdminOrange,
                    unfocusedLeadingIconColor = AdminMuted,
                    focusedTrailingIconColor = AdminOrange,
                    unfocusedTrailingIconColor = AdminMuted,
                    cursorColor = AdminOrange,
                    focusedPlaceholderColor = AdminMuted,
                    unfocusedPlaceholderColor = AdminMuted
                )
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            OutlinedTextField(
                value = clubCode,
                onValueChange = { clubCode = it },
                enabled = !isLoading,
                label = { Text("Assigned club code") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AdminWhite, unfocusedTextColor = AdminWhite,
                    focusedBorderColor = AdminOrange, unfocusedBorderColor = AdminBorder,
                    focusedLabelColor = AdminOrange, unfocusedLabelColor = AdminMuted,
                    cursorColor = AdminOrange
                )
            )
            errorMessage?.let {
                Text(it, color = Color(0xFFFF8080), modifier = Modifier.padding(vertical = 12.dp))
            }

            // =========================================================
            // FORGOT PASSWORD
            // =========================================================

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Forgot password?",
                    color = AdminOrange,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable(enabled = !isLoading) {
                        onForgotPasswordClick()
                    }
                )
            }

            Spacer(
                modifier = Modifier.height(26.dp)
            )

            // =========================================================
            // LOGIN BUTTON
            // =========================================================

            Button(
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && clubCode.isNotBlank(),
                onClick = {
                    onLoginClick(
                        email.trim(),
                        password,
                        clubCode.trim()
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AdminOrange,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = if (isLoading) "Signing in…" else "Log in as Admin",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            // =========================================================
            // SECURITY MESSAGE
            // =========================================================

            Text(
                text = "Admin access is reserved for authorised club administrators.",
                color = AdminMuted,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(
                    horizontal = 24.dp
                )
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.clickable(enabled = !isLoading) {
                    onBackClick()
                }
            ) {
                Text(
                    text = "Not an administrator? ",
                    color = AdminMuted,
                    fontSize = 13.sp
                )

                Text(
                    text = "Go back",
                    color = AdminOrange,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun AdminLoginScreenPreview() {
    MaterialTheme {
        AdminLoginScreen()
    }
}
