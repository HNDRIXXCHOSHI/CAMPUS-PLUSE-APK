package com.campuspulse.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuspulse.app.R


@Composable
fun VerifyEmailScreen(

    email: String = "your UMP student email",

    onVerifyClicked: () -> Unit = {},

    onResendClicked: () -> Unit = {},

    errorMessage: String? = null,

    successMessage: String? = null
) {

    // =============================================================
    // CAMPUS PULSE COLOURS
    // =============================================================

    val campusNavy =
        Color(0xFF0B3768)

    val deepNavy =
        Color(0xFF061D38)

    val cardNavy =
        Color(0xFF102F50)

    val accentOrange =
        Color(0xFFFF9800)

    val primaryText =
        Color.White

    val secondaryText =
        Color(0xFFD4DCE5)

    val borderColor =
        Color.White.copy(
            alpha = 0.15f
        )

    val errorColor =
        Color(0xFFFFB4AB)

    val successColor =
        Color(0xFF9EE6B8)


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
        // SLIGHT DARK OVERLAY
        // =========================================================

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(
                            alpha = 0.08f
                        )
                    )
        )


        // =========================================================
        // NAVY GRADIENT
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

                                    0.18f to
                                            campusNavy.copy(
                                                alpha = 0.10f
                                            ),

                                    0.32f to
                                            campusNavy.copy(
                                                alpha = 0.42f
                                            ),

                                    0.46f to
                                            campusNavy.copy(
                                                alpha = 0.75f
                                            ),

                                    0.60f to
                                            deepNavy.copy(
                                                alpha = 0.94f
                                            ),

                                    0.75f to
                                            deepNavy.copy(
                                                alpha = 0.98f
                                            ),

                                    1.00f to
                                            deepNavy
                                )
                        )
                    )
        )


        // =========================================================
        // CONTENT
        // =========================================================

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(
                        horizontal = 24.dp,
                        vertical = 24.dp
                    ),

            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.Center
        ) {


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
                        .size(76.dp)
                        .clip(
                            RoundedCornerShape(
                                18.dp
                            )
                        ),

                contentScale =
                    ContentScale.Crop
            )


            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )


            Text(
                text =
                    "CAMPUS PULSE",

                color =
                    primaryText,

                fontSize =
                    18.sp,

                fontWeight =
                    FontWeight.Bold,

                letterSpacing =
                    2.sp
            )


            Spacer(
                modifier =
                    Modifier.height(32.dp)
            )


            // =====================================================
            // EMAIL ICON
            // =====================================================

            Box(
                modifier =
                    Modifier
                        .size(92.dp)
                        .background(
                            color =
                                cardNavy.copy(
                                    alpha = 0.95f
                                ),
                            shape =
                                CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color =
                                accentOrange.copy(
                                    alpha = 0.55f
                                ),
                            shape =
                                CircleShape
                        ),

                contentAlignment =
                    Alignment.Center
            ) {

                Icon(
                    imageVector =
                        Icons.Filled.Email,

                    contentDescription =
                        "Email verification",

                    tint =
                        accentOrange,

                    modifier =
                        Modifier.size(42.dp)
                )
            }


            Spacer(
                modifier =
                    Modifier.height(28.dp)
            )


            // =====================================================
            // HEADING
            // =====================================================

            Text(
                text =
                    "Verify your email",

                color =
                    primaryText,

                fontSize =
                    28.sp,

                fontWeight =
                    FontWeight.Bold,

                textAlign =
                    TextAlign.Center
            )


            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )


            // =====================================================
            // INSTRUCTIONS
            // =====================================================

            Text(
                text =
                    "We've sent a verification link to",

                color =
                    secondaryText,

                fontSize =
                    14.sp,

                textAlign =
                    TextAlign.Center
            )


            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )


            Text(
                text =
                    email,

                color =
                    accentOrange,

                fontSize =
                    15.sp,

                fontWeight =
                    FontWeight.SemiBold,

                textAlign =
                    TextAlign.Center
            )


            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )


            Text(
                text =
                    "Open the email and tap the verification link. Then return to Campus Pulse and confirm that you've verified your email.",

                color =
                    secondaryText,

                fontSize =
                    14.sp,

                lineHeight =
                    21.sp,

                textAlign =
                    TextAlign.Center,

                modifier =
                    Modifier.padding(
                        horizontal = 8.dp
                    )
            )


            // =====================================================
            // ERROR MESSAGE
            // =====================================================

            if (
                !errorMessage.isNullOrBlank()
            ) {

                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )


                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                color =
                                    errorColor.copy(
                                        alpha = 0.10f
                                    ),
                                shape =
                                    RoundedCornerShape(
                                        12.dp
                                    )
                            )
                            .border(
                                width = 1.dp,
                                color =
                                    errorColor.copy(
                                        alpha = 0.35f
                                    ),
                                shape =
                                    RoundedCornerShape(
                                        12.dp
                                    )
                            )
                            .padding(14.dp)
                ) {

                    Text(
                        text =
                            errorMessage,

                        color =
                            errorColor,

                        fontSize =
                            13.sp,

                        lineHeight =
                            18.sp
                    )
                }
            }


            // =====================================================
            // SUCCESS MESSAGE
            // =====================================================

            if (
                !successMessage.isNullOrBlank()
            ) {

                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )


                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                color =
                                    successColor.copy(
                                        alpha = 0.10f
                                    ),
                                shape =
                                    RoundedCornerShape(
                                        12.dp
                                    )
                            )
                            .border(
                                width = 1.dp,
                                color =
                                    successColor.copy(
                                        alpha = 0.35f
                                    ),
                                shape =
                                    RoundedCornerShape(
                                        12.dp
                                    )
                            )
                            .padding(14.dp)
                ) {

                    Text(
                        text =
                            successMessage,

                        color =
                            successColor,

                        fontSize =
                            13.sp,

                        lineHeight =
                            18.sp
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(30.dp)
            )


            // =====================================================
            // I'VE VERIFIED BUTTON
            // =====================================================

            Button(
                onClick = {

                    onVerifyClicked()
                },

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),

                shape =
                    RoundedCornerShape(
                        28.dp
                    ),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            accentOrange,
                        contentColor =
                            Color.White
                    )
            ) {

                Icon(
                    imageVector =
                        Icons.Filled.MarkEmailRead,

                    contentDescription =
                        null,

                    modifier =
                        Modifier.size(21.dp)
                )


                Spacer(
                    modifier =
                        Modifier.width(10.dp)
                )


                Text(
                    text =
                        "I've verified my email",

                    fontSize =
                        16.sp,

                    fontWeight =
                        FontWeight.Bold
                )
            }


            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )


            // =====================================================
            // RESEND
            // =====================================================

            Text(
                text =
                    "Didn't receive the email?",

                color =
                    secondaryText,

                fontSize =
                    13.sp
            )


            TextButton(
                onClick = {

                    onResendClicked()
                }
            ) {

                Text(
                    text =
                        "Resend verification email",

                    color =
                        accentOrange,

                    fontSize =
                        14.sp,

                    fontWeight =
                        FontWeight.SemiBold
                )
            }


            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )


            Text(
                text =
                    "Check your spam or junk folder if you don't see the email.",

                color =
                    secondaryText.copy(
                        alpha = 0.75f
                    ),

                fontSize =
                    12.sp,

                lineHeight =
                    17.sp,

                textAlign =
                    TextAlign.Center
            )
        }
    }
}


// =============================================================
// PREVIEW
// =============================================================

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Verify Email Screen"
)
@Composable
fun VerifyEmailScreenPreview() {

    VerifyEmailScreen(
        email =
            "demo2@ump.ac.za"
    )
}