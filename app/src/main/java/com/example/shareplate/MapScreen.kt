package com.example.shareplate

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.*
import android.net.Uri
import com.example.shareplate.components.FoodDonationDialog
import com.example.shareplate.data.FoodDonation

@Composable
fun MapScreen(mapActivity: MapActivity) {
    var mapView: MapView? by remember { mutableStateOf(null) }
    var myLocationOverlay: MyLocationNewOverlay? by remember { mutableStateOf(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showDonationDialog by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<GeoPoint?>(null) }
    
    // Calculate the center point of all food bank locations
    val foodBankLocations = listOf(
        GeoPoint(17.4400, 78.4800), // Food Bank 1
        GeoPoint(17.3850, 78.4567), // Food Bank 2
        GeoPoint(17.3950, 78.4967), // Food Bank 3
        GeoPoint(17.3750, 78.4767), // Food Bank 4
        GeoPoint(17.4150, 78.4667)  // Food Bank 5
    )
    
    // Calculate center point of all markers
    val centerLat = foodBankLocations.map { it.latitude }.average()
    val centerLon = foodBankLocations.map { it.longitude }.average()
    val markersCenter = GeoPoint(centerLat, centerLon)
    
    // India coordinates for initial view
    val indiaLocation = GeoPoint(20.5937, 78.9629)

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    mapView = this
                    setMultiTouchControls(true)
                    setBuiltInZoomControls(false) // Remove zoom controls
                    
                    // Initial view of India
                    controller.setZoom(5.0)
                    controller.setCenter(indiaLocation)

                    // Add my location overlay
                    myLocationOverlay = MyLocationNewOverlay(this).apply {
                        enableMyLocation()
                        enableFollowLocation()
                    }
                    overlays.add(myLocationOverlay)

                    // Add markers for food banks
                    foodBankLocations.forEach { location ->
                        val marker = Marker(this).apply {
                            position = location
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = "Food Bank"
                            snippet = "Click to donate food"
                            setOnMarkerClickListener { clickedMarker, _ ->
                                selectedLocation = clickedMarker.position
                                showDonationDialog = true
                                true
                            }
                        }
                        overlays.add(marker)
                    }

                    // Start animation sequence
                    scope.launch {
                        delay(1000) // Wait for 1 second after map loads
                        
                        // Create a smooth zoom animation sequence
                        mapActivity.runOnUiThread {
                            // First move to a position that shows both India and markers
                            controller.animateTo(
                                GeoPoint(19.0, 78.5), // Intermediate point
                                8.0, // Intermediate zoom level
                                2000L // Duration for first phase
                            )
                        }
                        
                        delay(2000) // Wait for first animation
                        
                        // Then zoom into the markers center
                        mapActivity.runOnUiThread {
                            controller.animateTo(
                                markersCenter,
                                14.0, // Final zoom level
                                3000L // Longer duration for final zoom
                            )
                        }
                    }
                }
            },
            update = { view ->
                mapView = view
            }
        )

        // Add the dialog
        if (showDonationDialog && selectedLocation != null) {
            FoodDonationDialog(
                onDismiss = { showDonationDialog = false },
                onSubmit = { donation, imageUri ->
                    mapActivity.handleFoodDonation(donation, imageUri)
                    showDonationDialog = false  // Close dialog after submission
                },
                latitude = selectedLocation!!.latitude,
                longitude = selectedLocation!!.longitude
            )
        }

        // My Location Button
        IconButton(
            onClick = {
                myLocationOverlay?.let { overlay ->
                    mapView?.controller?.apply {
                        if (overlay.myLocation != null) {
                            animateTo(overlay.myLocation, 17.0, 1500L)
                        }
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_my_location),
                contentDescription = "My Location",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
    
    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            mapView?.onDetach()
        }
    }
} 