import android.media.MediaPlayer
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.spotifyapp.AnimatedPreloader
import com.example.spotifyapp.R
import com.example.spotifyapp.ui.theme.LightBlue
import com.example.spotifyapp.ui.theme.Purple
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrappedScreen1(uuid: String, navController: NavController, wrappedID:String, mediaPlayer:MediaPlayer) {
    mediaPlayer.reset()
    val trackPreviews = remember { mutableStateOf<List<String>>(emptyList()) }
    val selectedTrackPreview = remember { mutableStateOf<String?>(null) }
    val tanNimbus = FontFamily(Font(R.font.tan_nimbus))

    LaunchedEffect(wrappedID) {
        val database = FirebaseDatabase.getInstance().reference
        try {
            val snapshot =
                database.child("wrapped").child(uuid).child(wrappedID).child("trackPreview").get().await()
            val trackPreview =
                snapshot.getValue(object : GenericTypeIndicator<List<String>>() {})
            trackPreviews.value = trackPreview ?: emptyList()
            // Select a random track preview
            selectedTrackPreview.value = trackPreviews.value.randomOrNull()
        } catch (e: Exception) {
            Log.e("firebase", "Error getting data", e)
        }
    }
    // Play the selected track preview if required
    if (selectedTrackPreview.value != null) {
        mediaPlayer.apply {
            setDataSource(selectedTrackPreview.value)
            prepare()
            setOnPreparedListener {
                start()
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    // Pause music on screen change
                    mediaPlayer.pause()
                    navController.navigate("wrappedTracks")
                }
        ) {
            // Lottie animation as the background
            AnimatedPreloader(resource = R.raw.wrapped1_background, fillScreen = true)
            // Your main content goes here

            Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)) {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(
                            onClick = { navController.navigateUp() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors =  TopAppBarDefaults.centerAlignedTopAppBarColors(Color.Transparent)
                )

                val gradientColors = listOf(Color.Cyan, LightBlue, Purple)
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd){
                    Text(
                    modifier = Modifier,
                    text = "Welcome to Spotify Wrapped!",
                    fontSize = 30.sp,
                    style = TextStyle(
                        brush = Brush.linearGradient(
                            colors = gradientColors
                        )
                    ),
                    fontFamily = tanNimbus,
                    textAlign = TextAlign.Center,
                )
                }
            }
        }
    }
}

