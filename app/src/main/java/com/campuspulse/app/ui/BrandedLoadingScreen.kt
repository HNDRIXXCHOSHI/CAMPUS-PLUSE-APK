package com.campuspulse.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.campuspulse.app.R

/** Branding remains visible while the existing session check runs. */
@Composable
fun BrandedLoadingScreen(
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
    onUseAnotherAccount: () -> Unit = {}
) {
    Box(
        Modifier.fillMaxSize().background(Color(0xFF00172E)).safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.ump_crest_transparent),
                contentDescription = "University of Mpumalanga",
                modifier = Modifier.size(110.dp),
                contentScale = ContentScale.Fit
            )
            if (errorMessage == null) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color(0xFFF7941D),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = errorMessage,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 340.dp)
                )
                TextButton(
                    onClick = onRetry,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFF7941D))
                ) { Text("Retry") }
                TextButton(
                    onClick = onUseAnotherAccount,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                ) { Text("Use another account") }
            }
        }
        if (errorMessage == null) {
            Image(
                painter = painterResource(R.drawable.biglogo),
                contentDescription = "Campus Pulse",
                modifier = Modifier.align(Alignment.BottomCenter)
                    .padding(horizontal = 32.dp, vertical = 36.dp)
                    .widthIn(max = 210.dp).height(82.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}
