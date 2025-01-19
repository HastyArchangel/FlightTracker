package com.example.FlightTracker

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import android.net.ConnectivityManager
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.FirebaseApp
import com.google.maps.android.compose.MapEffect

class MainActivity : ComponentActivity() {
    private val networkChangeReceiver = NetworkChangeReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        getFirebaseMessagingToken()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        setContent {
            PlaneFlightTrackerApp()
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(networkChangeReceiver)
    }

    private fun getFirebaseMessagingToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FirebaseMessaging", "Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("FirebaseMessaging", "FCM Token: $token")
        }
    }
}

fun getMarkerIcon(context: Context, drawableId: Int, width: Int, height: Int): BitmapDescriptor? {
    val drawable: Drawable? = ContextCompat.getDrawable(context, drawableId)
    drawable?.let {
        val bitmap: Bitmap = it.toBitmap(width, height)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
    return null
}

@Composable
fun PlaneFlightTrackerApp() {
    var planes by remember { mutableStateOf<List<AircraftInformation>?>(null) }
    var isLoading by remember { mutableStateOf(false) } // Initially not loading
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    val marker1Position = remember { mutableStateOf(LatLng(47.1, 30.04)) }
    val marker2Position = remember { mutableStateOf(LatLng(40.96, 40.04)) }
    var isFirstMarkerNext by remember { mutableStateOf(true) }
    val context = LocalContext.current
    LaunchedEffect(refreshTrigger) { // Key is now the refreshTrigger
        isLoading = true
        val lat1 = marker1Position.value.latitude
        val lon1 = marker1Position.value.longitude
        val lat2 = marker2Position.value.latitude
        val lon2 = marker2Position.value.longitude
        val query = "-latlong \"$lat1 $lon1 $lat2 $lon2\"&max_pages=1"
        fetchAircraftPositions(query) { fetchedPositions, error ->
            if (error.isEmpty()) {
                planes = fetchedPositions
                Log.d("FlightTrackerAPI", "$planes")
            } else {
                errorMessage = error
                Log.e("FlightTrackerAPI", "Error: $errorMessage")
            }
            isLoading = false
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(45.94, 24.96), 4f)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            refreshTrigger++
        }) {
            Text("Refresh")
        }

        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (errorMessage != null) {
            Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
        } else if (planes != null) {
            if (planes!!.isNotEmpty()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng ->
                        if (isFirstMarkerNext) {
                            marker1Position.value = latLng
                            Log.d("Markers", "Marker 1 moved to: $latLng")
                        } else {
                            marker2Position.value = latLng
                            Log.d("Markers", "Marker 2 moved to: $latLng")
                        }
                        isFirstMarkerNext = !isFirstMarkerNext
                    }
                ) {
                    val planeIcon = remember {
                        getMarkerIcon(context, R.drawable.plane_marker_2, 150, 150)
                    }

                    Marker(
                        state = MarkerState(position = marker1Position.value),
                        title = "Marker 1",
                        snippet = "Click to adjust position",
                        draggable = true
                    )

                    Marker(
                        state = MarkerState(position = marker2Position.value),
                        title = "Marker 2",
                        snippet = "Click to adjust position",
                        draggable = true
                    )

                    planes!!.forEach { plane ->
                        if (plane.latitude != 0.0 && plane.longitude != 0.0) {
                            val latLng = LatLng(plane.latitude, plane.longitude)
                            Marker(
                                state = MarkerState(position = latLng),
                                title = plane.ident ?: "Unknown Ident",
                                snippet = "Tap for details",

                                icon = planeIcon,
                                rotation = plane.heading.toFloat(),
                                onClick = {
                                    // Display a custom info window with more details
                                    showDetailedInfoWindow(context, plane)
                                    true
                                }
                            )
                        }
                    }
                }

            } else {
                Text("No aircraft positions found")
            }
        }
    }
}

fun showDetailedInfoWindow(context: Context, plane: AircraftInformation) {
    AlertDialog.Builder(context)
        .setTitle(plane.ident ?: "Unknown Ident")
        .setMessage(
            """
            Origin: ${plane.origin?.code ?: "Unknown"}
            Destination: ${plane.destination?.code ?: "Unknown"}
            Altitude: ${plane.altitude * 100} feet
            Type: ${plane.aircraft_type ?: "Unknown"}
            Departure: ${plane.actual_off ?: "Unknown"}
            Arrival: ${plane.actual_on ?: "Unknown"}
            """.trimIndent()
        )
        .setPositiveButton("Close", null)
        .show()
}
