package com.example.spotifyapp

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Cyan
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.spotifyapp.ui.theme.LightBlue
import com.example.spotifyapp.ui.theme.Purple
import com.example.spotifyapp.viewmodels.MainViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.spotify.sdk.android.auth.AuthorizationClient
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {

    private val clientID = "8e7f849f40ba4e4d80a02604da0e3a76"
    private val redirectURI = "gt-wrapped://auth"
    private val spotifyRequests = SpotifyRequests(clientID, redirectURI)
    private lateinit var mAccessToken : String
    private lateinit var mAccessCode : String
    private var wrappedID = ""
    private val mediaPlayer = MediaPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uuid = intent.getStringExtra("uuid")
        setContent {
            if (uuid != null) {
                MyApp(uuid)
            }
        }
    }

    @Composable
    fun MyApp(uuid: String) {
        // Obtain ViewModel instance
        val mainViewModel: MainViewModel = viewModel()

        // Now you can use mainViewModel
        AppContent(mainViewModel, uuid)
    }

    @Composable
    fun AppContent(viewModel: MainViewModel, uuid: String) {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "main") {
            composable("main") {
                MainScreen(uuid, navController)
            }
            composable("wrappedSetup") {
                WrappedSetupPage(uuid, navController, viewModel)
            }
            composable("settings") {
                SettingsPage(navController)
            }
            composable("wrappedStart") {
                WrappedScreen1(navController)
            }
            composable("wrappedTracks") {
                WrappedScreen2(navController)
            }
            composable("wrappedArtists") {
                WrappedScreen3(navController)
            }
        }
    }

    //Add View of old created, Notifications?, Share function, hold spotify info somehow
    @Composable
    fun MainScreen(uuid: String, navController: NavController) {
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            IconButton(onClick = { navController.navigate("settings") }) {
                Icon(imageVector = Icons.Filled.Settings, contentDescription = "Settings")
            }

            // State for holding wrapped IDs
            var wrappedIDs by remember { mutableStateOf<List<String>>(emptyList()) }

            // Fetch wrapped IDs from Firebase
            LaunchedEffect(Unit) {
                val database = FirebaseDatabase.getInstance().reference
                database.child("wrapped").get().addOnSuccessListener { snapshot ->
                    val ids = snapshot.children.mapNotNull { it.key }
                    wrappedIDs = ids
                    Log.i("firebase", "Got value $wrappedIDs")
                }.addOnFailureListener { error ->
                    Log.e("firebase", "Error getting data", error)
                }
            }

            // LazyRow to display wrapped items
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(wrappedIDs) { wrappedUID ->
                    Box(
                        modifier = Modifier.clickable {
                            wrappedID = wrappedUID
                            navController.navigate("wrappedStart")
                        }
                    ) {
                        var wrappedCreationUser by remember { mutableStateOf("") }
                        var wrappedName by remember { mutableStateOf("") }

                        // Fetch data for each wrapped UID
                        LaunchedEffect(wrappedUID) {
                            val database = FirebaseDatabase.getInstance().reference
                            val wrappedRef = database.child("wrapped").child(wrappedUID)
                            wrappedRef.child("CreatingUser").get().addOnSuccessListener { userSnapshot ->
                                wrappedCreationUser = userSnapshot.value.toString()
                                Log.i("firebase", "Got user value $wrappedCreationUser")
                            }
                            wrappedRef.child("wrappedName").get().addOnSuccessListener { nameSnapshot ->
                                wrappedName = nameSnapshot.value.toString()
                                Log.i("firebase", "Got name value $wrappedName")
                            }
                        }
                        // Display the wrapped item if created by the current user
                        if (wrappedCreationUser == uuid) {
                            Text(
                                text = wrappedName,
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                    }
                }
            }



                Button(
                    onClick = {
                        navController.navigate("wrappedSetup")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Wrapped")
                }
            }
        }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun WrappedSetupPage(uuid: String, navController: NavController, viewModel: MainViewModel) {
        val context = LocalContext.current
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Wrapped Setup") },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Select Time Frame")

                Spacer(modifier = Modifier.height(16.dp))
                val radioOptions = listOf("Past 1 Year", "Past 6 Months", "Past 4 Weeks")
                val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[1]) }
                Column {
                    radioOptions.forEach { text ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onOptionSelected(text) }
                                .padding(horizontal = 16.dp)
                        ) {
                            RadioButton(
                                selected = (text == selectedOption),
                                onClick = { onOptionSelected(text) }
                            )
                            Text(
                                text = text,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                var wrappedName by remember { mutableStateOf("") }
                TextField(
                    value = wrappedName,
                    onValueChange = { wrappedName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Wrapped Name") },
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        // Handle login with Spotify here
                        spotifyRequests.getToken(this@MainActivity)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Login With Spotify")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        // Check if the user is logged in with Spotify
                        if (!::mAccessToken.isInitialized) {
                            Toast.makeText(context, "Please login with Spotify first", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        // Proceed to the next screen with user's selected options
                        // Mapping between user-facing options and internal representation
                        val optionsMapping = mapOf(
                            "Past 1 Year" to "long_term",
                            "Past 6 Months" to "medium_term",
                            "Past 4 Weeks" to "short_term"
                        )
                        val selectedOptionMapped = optionsMapping[selectedOption] ?: error("Invalid option")
                        viewModel.retrieveSpotifyData(mAccessToken, selectedOptionMapped, uuid, wrappedName)
                        wrappedID = viewModel.wrappedId
                        navController.navigate("wrappedStart")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Wrapped")
                }
            }
        }
    }


    //Account Logout, Account Deletion, Dark Mode Toggle
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SettingsPage(navController: NavController){
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Settings") },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Settings Page")
                Button(
                    onClick = {
                        Firebase.auth.signOut()
                        finish()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Logout")
                }
                Button(onClick = {
                    val user = Firebase.auth.currentUser!!
//Should Probably Create a confirmation for this!!!
                    user.delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("Deletion!!!", "User account deleted.")
                                finish()
                            }
                        }
                }){
                    Text("Delete Account")
                }
                Button(
                    onClick = {
                        val user = Firebase.auth.currentUser!!
                        val emailAddress = user.email

                        if (emailAddress != null) {
                            Firebase.auth.sendPasswordResetEmail(emailAddress)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.d("RESET USER", "Email sent.")
                                    }
                                }
                        }

                    }
                ){

                    Text("Forgot Password")
                }
            }
        }
    }

//Top Songs, Top Artists, Genre, ..., hear song clips while slide plays

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun WrappedScreen1(navController: NavController) {
        mediaPlayer.reset()
        val trackPreviews = remember { mutableStateOf<List<String>>(emptyList()) }
        val selectedTrackPreview = remember { mutableStateOf<String?>(null) }


        LaunchedEffect(wrappedID) {
            val database = FirebaseDatabase.getInstance().reference
            try {
                val snapshot =
                    database.child("wrapped").child(wrappedID).child("trackPreview").get().await()
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
                        navController.navigate("wrappedTracks") }
            ) {
                // Lottie animation as the background
                AnimatedPreloader(resource = R.raw.wrapped1_background, fillScreen = true)
                // Your main content goes here
                LazyColumn(contentPadding = innerPadding) {
                    item {

                        TopAppBar(
                            title = {},
                            navigationIcon = {
                                IconButton(onClick = { navController.navigateUp() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            },
                            colors =  TopAppBarDefaults.centerAlignedTopAppBarColors(Color.Transparent)
                        )

                        val gradientColors = listOf(Cyan, LightBlue, Purple)
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                modifier = Modifier,
                                text = "Welcome to Spotify Wrapped!",
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
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun WrappedScreen2(navController: NavController) {

        mediaPlayer.reset()
        val trackPreviews = remember { mutableStateOf<List<String>>(emptyList()) }
        val selectedTrackPreview = remember { mutableStateOf<String?>(null) }

        // MutableState to hold the list of track names
        Log.i("WrappedIDPage2", wrappedID)
        val trackNamesState = remember { mutableStateOf<List<String>>(emptyList()) }

        LaunchedEffect(wrappedID) {
            val database = FirebaseDatabase.getInstance().reference
            try {
                val snapshot =
                    database.child("wrapped").child(wrappedID).child("trackName").get().await()
                val trackNames = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {})
                trackNamesState.value = trackNames ?: emptyList()
            } catch (e: Exception) {
                Log.e("firebase", "Error getting data", e)
            }
        }

        LaunchedEffect(wrappedID) {
            val database = FirebaseDatabase.getInstance().reference
            try {
                val snapshot =
                    database.child("wrapped").child(wrappedID).child("trackPreview").get().await()
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
                    .clickable { navController.navigate("wrappedArtists") }
            ) {
                // Lottie animation as the background
                AnimatedPreloader(
                    resource = R.raw.starfish, fillScreen = false
                )
                // Your main content goes here
                LazyColumn(contentPadding = innerPadding) {
                    item {
                        TopAppBar(
                            title = {},
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(Color.Transparent),
                            navigationIcon = {
                                IconButton(onClick = { navController.navigateUp() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            },
                        )
                    }
                    // Use the track names from the MutableState
                    items(trackNamesState.value) { trackName ->
                        Text(
                            text = trackName,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }
        }
    }



    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun WrappedScreen3(navController: NavController) {
        // MutableState to hold the list of artist names

        mediaPlayer.reset()
        val trackPreviews = remember { mutableStateOf<List<String>>(emptyList()) }
        val selectedTrackPreview = remember { mutableStateOf<String?>(null) }

        val artistNamesState = remember { mutableStateOf<List<String>>(emptyList()) }

        LaunchedEffect(wrappedID) {
            val database = FirebaseDatabase.getInstance().reference
            try {
                val snapshot =
                    database.child("wrapped").child(wrappedID).child("artists").get().await()
                val artistNames = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {})
                artistNamesState.value = artistNames ?: emptyList()
            } catch (e: Exception) {
                Log.e("firebase", "Error getting data", e)
            }
        }

        LaunchedEffect(wrappedID) {
            val database = FirebaseDatabase.getInstance().reference
            try {
                val snapshot =
                    database.child("wrapped").child(wrappedID).child("trackPreview").get().await()
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
                modifier = Modifier.fillMaxSize()
            ) {
                // Lottie animation as the background
                AnimatedPreloader(resource = R.raw.wrapped1_background, fillScreen = true)
                // Your main content goes here
                LazyColumn(contentPadding = innerPadding) {
                    item {
                        TopAppBar(
                            title = {},
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(Color.Transparent),
                            navigationIcon = {
                                IconButton(onClick = { navController.navigateUp() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            },
                        )
                    }
                    // Use the artist names from the MutableState
                    items(artistNamesState.value) { artistName ->
                        Text(
                            text = artistName,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val response = AuthorizationClient.getResponse(resultCode, data)

        // Check which request code is present (if any)
        if (AuthenticationActivity.AUTH_TOKEN_REQUEST_CODE == requestCode) {
            mAccessToken = response.accessToken
            Log.d("Token", mAccessToken)
            spotifyRequests.getCode(this@MainActivity)
        } else if (AuthenticationActivity.AUTH_CODE_REQUEST_CODE == requestCode) {
            mAccessCode = response.code
            Log.d("Code", mAccessCode)
        }
    }

}