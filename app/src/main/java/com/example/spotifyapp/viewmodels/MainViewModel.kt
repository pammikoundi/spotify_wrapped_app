package com.example.spotifyapp.viewmodels
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotifyapp.SpotifyRequests
import com.example.spotifyapp.callbacks.SpotifyArtistHistoryCallback
import com.example.spotifyapp.callbacks.SpotifyTrackHistoryCallback
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import datamodels.SpotifyTopArtistsResponse
import datamodels.SpotifyTopTracksResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class MainViewModel : ViewModel() {
    private val clientID = "8e7f849f40ba4e4d80a02604da0e3a76"
    private val redirectURI = "gt-wrapped://auth"
    private val _trackNames = MutableStateFlow<List<String>>(emptyList())
    val trackNames: StateFlow<List<String>> = _trackNames
    private val _trackImg = MutableStateFlow<List<String>>(emptyList())
    val trackImg: StateFlow<List<String>> = _trackImg
    private val _trackPreview = MutableStateFlow<List<String>>(emptyList())
    val trackPreview: StateFlow<List<String>> = _trackPreview
    private val _artistNames = MutableStateFlow<List<String>>(emptyList())
    val artistNames: StateFlow<List<String>> = _artistNames
    val spotifyRequests = SpotifyRequests(clientID, redirectURI)
    private var database: DatabaseReference = FirebaseDatabase.getInstance().reference

    private val wrappedRef = database.child("wrapped")
    var wrappedId = ""
    fun retrieveSpotifyData(mAccessToken: String, timeRange : String, currUser: String, wrappedName: String) {
        viewModelScope.launch {
            // Assume spotifyRequests is accessible here or passed somehow
            spotifyRequests.getSpotifyTrackHistory(mAccessToken, timeRange , object :
                SpotifyTrackHistoryCallback {
                override fun onSuccess(jsonResponse: String) {
                    val json = Json { ignoreUnknownKeys = true }
                    try {
                        val trackHistory = json.decodeFromString<SpotifyTopTracksResponse>(jsonResponse)
                        _trackNames.value = trackHistory.items.map { it.name }
                        _trackImg.value = trackHistory.items.map { it.album.images[0].url }
                        _trackPreview.value = trackHistory.items.map { it.preview_url!! }
                        spotifyRequests.getSpotifyArtistHistory(mAccessToken, timeRange, object :
                            SpotifyArtistHistoryCallback {
                            override fun onSuccess(jsonResponse: String) {
                                val json = Json { ignoreUnknownKeys = true }
                                try {
                                    val artistHistory = json.decodeFromString<SpotifyTopArtistsResponse>(jsonResponse)
                                    _artistNames.value = artistHistory.items.map { it.name }
                                    val currentTrackNames = trackNames.value
                                    val currentTrackImg = trackImg.value
                                    val currentTrackPreview = trackPreview.value
                                    val currentArtistNames = artistNames.value

                                    wrappedId = wrappedRef.push().key ?:""
                                    saveTracksToDatabase(
                                        currentTrackNames,
                                        currentTrackImg,
                                        currentTrackPreview,
                                        currentArtistNames,
                                        currUser,
                                        wrappedId,
                                        wrappedName
                                    )
                                } catch (e: Exception) {
                                    Log.e("SpotifyHistory", "Failed to parse Spotify Artist history: ${e.message}")
                                    // Consider how you might handle errors, possibly using another StateFlow
                                }
                            }
                            override fun onFailure(e: Exception) {
                                Log.e("SpotifyHistory", "Error fetching Spotify Artist history: ${e.message}")
                                // Handle failure, similarly to success
                            }
                        })



                    } catch (e: Exception) {
                        Log.e("SpotifyHistory", "Failed to parse Spotify Tracks history: ${e.message}")
                        // Consider how you might handle errors, possibly using another StateFlow
                    }
                }

                private fun saveTracksToDatabase(
                    tracks: List<String>,
                    trackImages: List<String>,
                    trackPreview: List<String>,
                    artists: List<String>, currUser: String, wrappedId: String, wrappedName: String
                ) {
                    val wrappedRef = database.child("wrapped")
                    Log.d("Firebase", "Wrapped Database ID: $wrappedId")
                    val wrappedData = hashMapOf(
                        "trackName" to tracks,
                        "trackImage" to trackImages, // Assuming you have image URLs
                        "trackPreview" to trackPreview,
                        "artists" to artists,
                        "wrappedName" to wrappedName,
                    )

                    wrappedRef.child(currUser).child(wrappedId).setValue(wrappedData)
                        .addOnSuccessListener {
                            Log.d("Firebase", "Track saved successfully: $wrappedId")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firebase", "Failed to save track: ${e.message}")
                            // Handle failure
                        }
                }

                override fun onFailure(e: Exception) {
                    Log.e("SpotifyHistory", "Error fetching Spotify Tracks history: ${e.message}")
                    // Handle failure, similarly to success
                }
            })


        }
    }

}
