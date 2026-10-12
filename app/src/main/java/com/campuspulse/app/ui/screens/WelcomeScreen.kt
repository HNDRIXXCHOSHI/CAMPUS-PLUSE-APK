package com.campuspulse.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuspulse.app.R


// =============================================================
// CAMPUS PULSE COLOURS
// =============================================================

// Orange CTA button
private val PrimaryButtonColor =
    Color(0xFFFF9100)

// Main white text
private val TextWhite =
    Color(0xFFFFFFFF)

// Secondary text
private val TextMuted =
    Color(0xFFE3E8EE)

// Main dark navy used for bottom fade
private val CampusNavy =
    Color(0xFF0A3768)

// Darker navy at very bottom
private val DeepNavy =
    Color(0xFF061D38)


// =============================================================
// WELCOME SCREEN
// =============================================================

@Composable
fun WelcomeScreen(
    onVerifyClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    onAdminClick: () -> Unit = {}
) {

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        // =====================================================
        // 1. CAMPUS BACKGROUND IMAGE
        // =====================================================
        //
        // THIS IS WHERE YOU CONTROL THE CAMPUS PHOTO.
        //
        // ContentScale.Crop:
        // Fills the whole phone screen and crops the excess.
        //
        // TO INCREASE THE "ZOOM":
        // Add .scale(1.10f) to the Modifier.
        //
        // Example:
        // modifier = Modifier
        //     .fillMaxSize()
        //     .scale(1.10f)
        //
        // 1.05f = small zoom
        // 1.10f = medium zoom
        // 1.20f = strong zoom
        //
        // =====================================================

        Image(
            painter = painterResource(
                id = R.drawable.campus_background
            ),
            contentDescription =
                "University of Mpumalanga Campus",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )


        // =====================================================
        // 2. VERY LIGHT DARK OVERLAY
        // =====================================================
        //
        // This slightly darkens the whole photograph.
        //
        // CHANGE alpha TO CONTROL IT:
        //
        // 0.05f = almost no darkening
        // 0.10f = subtle
        // 0.15f = darker
        // 0.25f = much darker
        //
        // Keep this LOW because the blue gradient below
        // will handle most of the readability.
        //
        // =====================================================

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(
                        alpha = 0.08f
                    )
                )
        )


        // =====================================================
        // 3. DARK BLUE BOTTOM GRADIENT
        // =====================================================
        //
        // THIS IS WHERE YOU CONTROL THE BLUE FADE.
        //
        // The first number controls WHERE on the screen
        // each stage of the gradient occurs.
        //
        // Example:
        //
        // 0.00f = very top
        // 0.50f = middle
        // 1.00f = very bottom
        //
        // If you want the blue to START HIGHER:
        // change 0.38f to 0.25f or 0.30f.
        //
        // If you want the blue to START LOWER:
        // change 0.38f to 0.45f or 0.50f.
        //
        // Alpha controls how STRONG the fade is.
        //
        // =====================================================

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(

                            // Top stays completely clear
                            0.00f to Color.Transparent,

                            // Campus photo remains clear here
                            0.30f to Color.Transparent,

                            // START OF BLUE FADE
                            // Change 0.38f to move fade up/down
                            0.38f to CampusNavy.copy(
                                alpha = 0.08f
                            ),

                            // Light blue/navy transition
                            0.50f to CampusNavy.copy(
                                alpha = 0.45f
                            ),

                            // Medium fade behind heading
                            0.62f to CampusNavy.copy(
                                alpha = 0.55f
                            ),

                            // Strong fade behind description
                            0.73f to CampusNavy.copy(
                                alpha = 0.80f
                            ),

                            // Very dark behind buttons
                            0.86f to DeepNavy.copy(
                                alpha = 0.94f
                            ),

                            // Bottom is effectively solid navy
                            1.00f to DeepNavy
                        )
                    )
                )
        )


        // =====================================================
        // 4. EVERYTHING ABOVE THE BACKGROUND
        // =====================================================

        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {


            // =================================================
            // TOP-LEFT CAMPUS PULSE LOGO
            // =================================================
            //
            // THIS IS WHERE YOU ALIGN THE LOGO.
            //
            // start = distance from LEFT
            // top   = distance from TOP
            //
            // Example:
            //
            // start = 20.dp  -> move right
            // start = 10.dp  -> move left
            //
            // top = 16.dp    -> move down
            // top = 8.dp     -> move up
            //
            // =================================================

            Row(
                verticalAlignment =
                    Alignment.CenterVertically,
                modifier = Modifier
                    .align(
                        Alignment.TopStart
                    )
                    .padding(
                        start = 18.dp, // <-- LOGO LEFT/RIGHT
                        top = 14.dp    // <-- LOGO UP/DOWN
                    )
            ) {


                // =============================================
                // ACTUAL CAMPUS PULSE LOGO IMAGE
                // =============================================
                //
                // CHANGE size() TO MAKE LOGO BIGGER/SMALLER:
                //
                // 40.dp = smaller
                // 48.dp = current
                // 55.dp = larger
                // 65.dp = much larger
                //
                // Because your uploaded logo already has
                // a navy background, we crop it into a circle.
                //
                // =============================================

                Image(
                    painter = painterResource(
                        id = R.drawable.campus_pulse_logo
                    ),
                    contentDescription = "Campus Pulse Logo",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp)), // Adjust dp value to change corner curvature
                    contentScale = ContentScale.Crop
                )

                // =============================================
                // SPACE BETWEEN LOGO AND CAMPUS PULSE TEXT
                // =============================================
                //
                // Increase width to move text further right.
                //
                // =============================================

                Spacer(
                    modifier =
                        Modifier.width(10.dp)
                )


                // =============================================
                // CAMPUS PULSE BRAND NAME
                // =============================================

                Text(
                    text = "",
                    color = TextWhite,
                    fontWeight =
                        FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    fontSize = 13.sp
                )
            }


            // =================================================
            // TOP-RIGHT ADMIN BUTTON
            // =================================================

            Box(
                modifier = Modifier
                    .align(
                        Alignment.TopEnd
                    )
                    .padding(
                        end = 16.dp,
                        top = 8.dp
                    )
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        DeepNavy.copy(
                            alpha = 0.90f
                        )
                    ),
                contentAlignment =
                    Alignment.Center
            ) {

                IconButton(
                    onClick = onAdminClick,
                    modifier =
                        Modifier.fillMaxSize()
                ) {

                    Icon(
                        imageVector =
                            Icons.Filled.AdminPanelSettings,
                        contentDescription =
                            "Admin Login",
                        tint = TextWhite,
                        modifier =
                            Modifier.size(25.dp)
                    )
                }
            }


            // =================================================
            // MAIN SCREEN CONTENT
            // =================================================

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 28.dp,
                        vertical = 48.dp
                    ),
                horizontalAlignment =
                    Alignment.CenterHorizontally,
                verticalArrangement =
                    Arrangement.SpaceBetween
            ) {


                // =============================================
                // TOP GRAPHIC / CAMPUS PULSE WAVE
                // =============================================
                //
                // height(260.dp) controls how much vertical
                // space this top graphic section uses.
                //
                // Reduce it to move everything below upward.
                // Increase it to move everything below downward.
                //
                // =============================================

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .padding(top = 40.dp),
                    contentAlignment =
                        Alignment.Center
                ) {


                    // =========================================
                    // LARGE FLOATING CIRCLE
                    // =========================================
                    //
                    // x controls LEFT/RIGHT.
                    // y controls UP/DOWN.
                    // size controls circle size.
                    //
                    // =========================================

                    Box(
                        modifier = Modifier
                            .offset(
                                x = (-60).dp,
                                y = (-10).dp
                            )
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Color.White.copy(
                                    alpha = 0.25f
                                )
                            )
                    )


                    // =========================================
                    // SMALL FLOATING CIRCLE
                    // =========================================

                    Box(
                        modifier = Modifier
                            .offset(
                                x = 40.dp,
                                y = (-60).dp
                            )
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Color.White.copy(
                                    alpha = 0.35f
                                )
                            )
                    )


                    // =========================================
                    // MEDIUM FLOATING CIRCLE
                    // =========================================

                    Box(
                        modifier = Modifier
                            .offset(
                                x = 80.dp,
                                y = 30.dp
                            )
                            .size(65.dp)
                            .clip(CircleShape)
                            .background(
                                Color.White.copy(
                                    alpha = 0.20f
                                )
                            )
                    )


                    // =========================================
                    // CENTER SOUND/WAVE BARS
                    // =========================================

                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(
                                8.dp
                            ),
                        verticalAlignment =
                            Alignment.Bottom,
                        modifier = Modifier
                            .height(70.dp)
                            .offset(
                                y = 20.dp
                            )
                    ) {

                        val barHeights =
                            listOf(
                                30.dp,
                                50.dp,
                                70.dp,
                                45.dp,
                                60.dp
                            )


                        barHeights.forEach {
                                barHeight ->


                        }
                    }
                }


                // =============================================
                // MAIN HEADING + DESCRIPTION
                // =============================================
                //
                // THIS IS WHERE YOU MOVE THE TEXT.
                //
                // Add:
                //
                // .offset(y = 20.dp)
                //
                // to move BOTH heading and description DOWN.
                //
                // Use:
                //
                // .offset(y = (-20).dp)
                //
                // to move them UP.
                //
                // =============================================

                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(
                            horizontal = 8.dp
                        )
                        .offset(
                            y = 10.dp // <-- MOVE TEXT UP/DOWN
                        )
                ) {


                    // =========================================
                    // MAIN HEADING
                    // =========================================
                    //
                    // Change 28.sp to increase/decrease size.
                    //
                    // =========================================

                    Text(
                        text =
                            "Everything happening on campus. Today.",
                        color = TextWhite,
                        fontSize = 28.sp,
                        fontFamily =
                            FontFamily.Serif,
                        fontWeight =
                            FontWeight.Medium,
                        textAlign =
                            TextAlign.Center,
                        lineHeight = 36.sp
                    )


                    Spacer(
                        modifier =
                            Modifier.height(16.dp)
                    )


                    // =========================================
                    // DESCRIPTION
                    // =========================================

                    Text(
                        text =
                            "Clubs, societies and events in one live feed — no scrolling past strangers to find your people.",
                        color = TextMuted,
                        fontSize = 14.sp,
                        textAlign =
                            TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }


                // =============================================
                // BOTTOM BUTTON AREA
                // =============================================
                //
                // Add .offset(y = ...)
                // if you want to move BOTH buttons.
                //
                // Positive = DOWN
                // Negative = UP
                //
                // =============================================

                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(
                            y = (-5).dp // <-- BUTTON POSITION
                        )
                ) {


                    // =========================================
                    // VERIFY BUTTON
                    // =========================================
                    //
                    // fillMaxWidth(0.75f)
                    //
                    // 0.65f = narrower
                    // 0.75f = current
                    // 0.85f = wider
                    // 1.00f = full width
                    //
                    // height(50.dp) controls button height.
                    //
                    // =========================================

                    Button(
                        onClick = onVerifyClick,
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .height(50.dp),
                        shape =
                            RoundedCornerShape(
                                25.dp
                            ),
                        colors =
                            ButtonDefaults
                                .buttonColors(
                                    containerColor =
                                        PrimaryButtonColor
                                )
                    ) {

                        Text(
                            text =
                                "Verify with student email",
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight =
                                FontWeight.Bold
                        )
                    }


                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )


                    // =========================================
                    // EXISTING ACCOUNT BUTTON
                    // =========================================

                    TextButton(
                        onClick = onLoginClick
                    ) {

                        Text(
                            text =
                                "I already have an account",
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight =
                                FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}


// =============================================================
// ANDROID STUDIO PREVIEW
// =============================================================

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun WelcomeScreenPreview() {

    WelcomeScreen()
}