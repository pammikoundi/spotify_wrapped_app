import android.media.MediaPlayer
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import com.example.spotifyapp.AnimatedPreloader
import com.example.spotifyapp.R
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrappedScreen2(uuid: String, navController: NavController, wrappedID:String,mediaPlayer:MediaPlayer) {

    mediaPlayer.reset()
    val trackPreviews = remember { mutableStateOf<List<String>>(emptyList()) }
    val trackImages = remember { mutableStateOf<List<String>>(emptyList()) }
    val selectedTrackPreview = remember { mutableStateOf<String?>(null) }
    val selectedImage = remember { mutableStateOf<String?>(null) }
    val tanNimbus = FontFamily(Font(R.font.tan_nimbus))

    // MutableState to hold the list of track names
    Log.i("WrappedIDPage2", wrappedID)
    val trackNamesState = remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(wrappedID) {
        val database = FirebaseDatabase.getInstance().reference
        try {
            val wrappedRef = database.child("wrapped").child(uuid).child(wrappedID)

            // Get track names
            val trackNamesSnapshot = wrappedRef.child("trackName").get().await()
            val trackNames = trackNamesSnapshot.getValue(object : GenericTypeIndicator<List<String>>() {})
            trackNamesState.value = trackNames ?: emptyList()

            // Get track previews
            val trackPreviewsSnapshot = wrappedRef.child("trackPreview").get().await()
            val trackPreview = trackPreviewsSnapshot.getValue(object : GenericTypeIndicator<List<String>>() {})
            trackPreviews.value = trackPreview ?: emptyList()
            // Select a random track preview
            selectedTrackPreview.value = trackPreviews.value.randomOrNull()

            // Get track images
            val trackImagesSnapshot = wrappedRef.child("trackImage").get().await()
            val trackImage = trackImagesSnapshot.getValue(object : GenericTypeIndicator<List<String>>() {})
            trackImages.value = trackImage ?: emptyList()
            // Select a random track preview image
            selectedImage.value = trackImages.value.randomOrNull()
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
                .clickable { navController.navigate("wrappedArtists") }
        ) {
            // Lottie animation as the background
            AnimatedPreloader(
                resource = R.raw.wrapped2_background, fillScreen = true
            )

            // Your main content goes here
            LazyColumn(contentPadding = innerPadding) {
                item {
                    TopAppBar(
                        title = {
                            Box(modifier = Modifier
                            .height(60.dp)
                            .fillMaxWidth()
                            .align(Alignment.CenterEnd)){
                                AnimatedPreloader(
                            resource = R.raw.starfish2, fillScreen = false
                        )
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { navController.navigateUp() }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                        },
                        colors =  TopAppBarDefaults.centerAlignedTopAppBarColors(Color.Transparent)
                    )

                    // Display the image if selectedImage is not null
                    selectedImage.value?.let { imageUrl ->
                        Image(
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(LocalContext.current).data(data = imageUrl)
                                    .apply {
                                        scale(Scale.FILL) // Adjust the scale as needed
                                    }.build()
                            ),
                            contentDescription = "Track Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((LocalConfiguration.current.screenHeightDp * 0.40).dp)
                                .padding(5.dp)
                        )
                    }

                    Text(
                        text = "Top Tracks",
                        style = TextStyle(
                            fontFamily = tanNimbus,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        ),
                        color = Color.White,
                        modifier = Modifier
                            .padding(32.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    trackNamesState.value.forEachIndexed { index, trackName ->
                        Row(Modifier.padding(18.dp)) {
                            Text(
                                text = "${index + 1}.",
                                modifier = Modifier.width(30.dp),
                                style =
                                TextStyle(
                                    fontFamily = tanNimbus,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                ),
                                color = Color.White
                            )
                            Text(
                                text = trackName,
                                modifier = Modifier.padding(start = 8.dp),
                                style =
                                TextStyle(
                                    fontFamily = tanNimbus,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = Color.White
                            )
                        }
                    }

                }
            }
        }
    }
}