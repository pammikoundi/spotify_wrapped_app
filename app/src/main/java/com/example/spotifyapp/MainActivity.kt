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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.spotifyapp.viewmodels.MainViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.spotify.sdk.android.auth.AuthorizationClient

class MainActivity : ComponentActivity() {

    private val clientID = "8e7f849f40ba4e4d80a02604da0e3a76"
    private val redirectURI = "gt-wrapped://auth"
    private val spotifyRequests = SpotifyRequests(clientID, redirectURI)
    private lateinit var mAccessToken : String
    private lateinit var mAccessCode : String
    private var wrappedID = ""
    private val mediaPlayer = MediaPlayer()
    private val tanNimbus = FontFamily(Font(R.font.tan_nimbus))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uuid = Firebase.auth.currentUser!!.uid
        setContent {
            MyApp(uuid)
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
                WrappedScreen1(uuid, navController,wrappedID,mediaPlayer)
            }
            composable("wrappedTracks") {
                WrappedScreen2(uuid, navController,wrappedID,mediaPlayer)
            }
            composable("wrappedArtists") {
                WrappedScreen3(uuid, navController,wrappedID,mediaPlayer)
            }
        }
    }

    //Add View of old created, Notifications?, Share function, hold spotify info somehow
    @Composable
    fun MainScreen(uuid: String, navController: NavController) {
        // Main content
        mediaPlayer.reset()

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

            Text("Previously Created Wrappeds", fontSize = 24.sp, fontFamily = tanNimbus)

            Spacer(modifier = Modifier.height(16.dp))

            // State for holding wrapped IDs
            var wrappedIDs by remember { mutableStateOf<List<String>>(emptyList()) }

            // Fetch wrapped IDs from Firebase
            LaunchedEffect(Unit) {
                val database = FirebaseDatabase.getInstance().reference
                database.child("wrapped").child(uuid).get().addOnSuccessListener { snapshot ->
                    val ids = snapshot.children.mapNotNull { it.key }
                    wrappedIDs = ids
                    Log.i("firebase", "Got wrapped Ids $wrappedIDs")
                }.addOnFailureListener { error ->
                    Log.e("firebase", "Error getting data", error)
                }
            }

            // LazyRow to display wrapped items
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.fillMaxWidth(),
                userScrollEnabled = true
            ) {
                items(wrappedIDs) { wrappedUID ->
                    Box(
                        modifier = Modifier.clickable {
                            wrappedID = wrappedUID
                            navController.navigate("wrappedStart")
                        }
                    ) {
                        var wrappedName by remember { mutableStateOf("") }

                        // Fetch data for each wrapped UID
                        LaunchedEffect(wrappedUID) {
                            val database = FirebaseDatabase.getInstance().reference
                            val wrappedRef =
                                database.child("wrapped").child(uuid).child(wrappedUID)

                            wrappedRef.child("wrappedName").get()
                                .addOnSuccessListener { nameSnapshot ->
                                    wrappedName = nameSnapshot.value.toString()
                                    Log.i("firebase", "Got name value $wrappedName")
                                }
                        }
                        // Display the wrapped items for the current user
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((LocalConfiguration.current.screenHeightDp * 0.40).dp)
                                .padding(5.dp)
                        ) {
                            WrappedPreview(wrappedName)
                        }

                    }
                }
            }

                Button(
                    onClick = {
                        navController.navigate("wrappedSetup")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.End)
                ) {
                    Text("Create Wrapped")
                }
            }
        }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun WrappedSetupPage(uuid: String, navController: NavController, viewModel: MainViewModel) {
        val context = LocalContext.current
        mediaPlayer.reset()

            Box(
                modifier = Modifier.fillMaxSize()
            ){
                AnimatedPreloader(resource = R.raw.starfish, fillScreen = true)


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

                Spacer(modifier = Modifier.height(32.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 64.dp)
            ) {
                Text(text = "Create a New Wrapped", fontSize = 32.sp)
                Spacer(modifier = Modifier.height(32.dp))
                Text(text = "Select Time Frame", fontSize = 24.sp)

                Spacer(modifier = Modifier.height(32.dp))
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
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    label = { Text("Wrapped Name") },
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.padding(20.dp)) {
                    Column {
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
                                    Toast.makeText(
                                        context,
                                        "Please login with Spotify first",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }
                                // Proceed to the next screen with user's selected options
                                // Mapping between user-facing options and internal representation
                                val optionsMapping = mapOf(
                                    "Past 1 Year" to "long_term",
                                    "Past 6 Months" to "medium_term",
                                    "Past 4 Weeks" to "short_term"
                                )
                                val selectedOptionMapped =
                                    optionsMapping[selectedOption] ?: error("Invalid option")
                                val database = FirebaseDatabase.getInstance().reference
                                val wrappedRef = database.child("wrapped")
                                wrappedID = wrappedRef.push().key ?: ""
                                viewModel.retrieveSpotifyData(
                                    mAccessToken,
                                    selectedOptionMapped,
                                    uuid,
                                    wrappedName,
                                    wrappedID
                                )

                                navController.navigate("wrappedStart")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Create Wrapped")
                        }
                    }
                }

                }
            }
    }

    //Account Logout, Account Deletion
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SettingsPage(navController: NavController){
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
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
                Text(text = "Settings", fontSize = 32.sp)
                Spacer(modifier = Modifier.height(64.dp))
                Button(
                    onClick = {
                        Firebase.auth.signOut()
                        finish()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Logout")
                }
                Spacer(modifier = Modifier.height(32.dp))
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

                    },
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text("Forgot Password")
                }
                Spacer(modifier = Modifier.height(64.dp))
                Text(
                    "Danger!! This will Permanently delete this account and all data associated with it. No chance of recovery!!",
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        val user = Firebase.auth.currentUser!!
//Should Probably Create a confirmation for this!!!
                        user.delete()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val database = FirebaseDatabase.getInstance().reference
                                    database.child("wrapped").child(user.uid).removeValue()
                                    Log.d("Deletion!!!", "User account deleted.")
                                    finish()
                                }
                            }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete Account")
                }
                }

        }
    }

//Top Songs, Top Artists, Genre, ..., hear song clips while slide plays


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