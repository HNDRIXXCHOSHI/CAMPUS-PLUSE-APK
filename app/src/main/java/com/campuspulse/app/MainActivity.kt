package com.campuspulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.campuspulse.app.navigation.CampusPulseNavGraph
import com.campuspulse.app.ui.theme.CampusPulseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampusPulseTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CampusPulseNavGraph()
                }
            }
        }
    }
}
