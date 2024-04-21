package com.example.spotifyapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spotifyapp.ui.theme.LightBlue
import com.example.spotifyapp.ui.theme.Purple

@Composable
fun WrappedPreview(wrappedName: String) {
    val tanNimbus = FontFamily(Font(R.font.tan_nimbus))

    Box(modifier = Modifier) {
        // Lottie animation as the background
        AnimatedPreloader(resource = R.raw.wrapped1_background, fillScreen = true)
        // Your main content goes here
        val gradientColors = listOf(Color.Cyan, LightBlue, Purple)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    modifier = Modifier.padding(5.dp),
                    text = "Welcome",
                    fontSize = 12.sp,
                    style = TextStyle(
                        brush = Brush.linearGradient(
                            colors = gradientColors
                        ),

                        ),
                    fontFamily = tanNimbus,
                )
                Text(
                    modifier = Modifier.padding(5.dp),
                    text = "to Spotify Wrapped!",
                    fontSize = 12.sp,
                    style = TextStyle(
                        brush = Brush.linearGradient(
                            colors = gradientColors
                        ),

                        ),
                    fontFamily = tanNimbus,
                )
                Text(
                    modifier = Modifier.padding(5.dp),
                    text = wrappedName,
                    fontSize = 12.sp,
                    style = TextStyle(
                        brush = Brush.linearGradient(
                            colors = gradientColors
                        ),

                        ),
                    fontFamily = tanNimbus,
                )
            }
        }
    }
}
