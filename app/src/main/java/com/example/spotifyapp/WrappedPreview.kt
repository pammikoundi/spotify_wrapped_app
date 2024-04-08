package com.example.spotifyapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.spotifyapp.ui.theme.LightBlue
import com.example.spotifyapp.ui.theme.Purple

@Composable
fun WrappedPreview(modifier: Modifier = Modifier, name: String) {
    Scaffold(
        modifier = Modifier
    ) { innerPadding ->
            // Lottie animation as the background
            AnimatedPreloader(resource = R.raw.wrapped1_background, fillScreen = true)
            // Your main content goes here
            LazyColumn(contentPadding = innerPadding) {
                item {
                    val gradientColors = listOf(Color.Cyan, LightBlue, Purple)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            modifier = Modifier,
                            text = "Welcome to Spotify Wrapped named: $name!",
                            fontSize = 30.sp,
                            style = TextStyle(
                                brush = Brush.linearGradient(
                                    colors = gradientColors
                                )
                            )
                        )
                    }
                }
            }
        }
    }