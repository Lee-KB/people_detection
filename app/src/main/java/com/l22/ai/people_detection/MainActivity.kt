package com.l22.ai.people_detection

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.l22.ai.people_detection.ui.theme.PeopleDetectionTheme
import com.l22.ai.people_detection.ui.view.MainScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge mode for the activity
        enableEdgeToEdge()

        // Set the content view to the MainScreen composable wrapped in the theme
        setContent {
            PeopleDetectionTheme {
                MainScreen()
            }
        }
    }
}
