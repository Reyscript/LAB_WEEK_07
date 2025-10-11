package com.example.lab_week_07

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Setup permission request
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                // Permission granted, get location and setup map
                getCurrentLocation()
            } else {
                // Permission denied, show default map
                showDefaultMap()
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        // Initialize map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Enable basic map controls
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        // Check location permission
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                getCurrentLocation()
            }
            else -> {
                // Request permission
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    // Use the location
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    updateMapWithCurrentLocation(currentLatLng)

                    // Enable my location layer
                    try {
                        mMap.isMyLocationEnabled = true
                    } catch (e: SecurityException) {
                        Log.e("MapsActivity", "Error enabling my location layer", e)
                    }

                } else {
                    // Location is null, show default map
                    showDefaultMap()
                    Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MapsActivity", "Error getting location", exception)
                showDefaultMap()
                Toast.makeText(this, "Error getting location", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateMapWithCurrentLocation(location: LatLng) {
        mMap.addMarker(
            MarkerOptions()
                .position(location)
                .title("Your Location")
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))

        Log.d("MapsActivity", "Location: ${location.latitude}, ${location.longitude}")
    }

    private fun showDefaultMap() {
        // Default location (Jakarta)
        val defaultLocation = LatLng(-6.2088, 106.8456)
        mMap.addMarker(
            MarkerOptions()
                .position(defaultLocation)
                .title("Jakarta")
                .snippet("Default location")
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))
    }
}