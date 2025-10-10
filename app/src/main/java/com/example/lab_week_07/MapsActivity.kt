package com.example.lab_week_07

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    Log.d("MapsActivity", "Permission granted")
                    getLastLocation()
                } else {
                    Log.d("MapsActivity", "Permission denied")
                    showPermissionRationale {
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
            }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        Log.d("MapsActivity", "Map is ready")

        // Enable zoom controls
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        when {
            hasLocationPermission() -> {
                Log.d("MapsActivity", "Permission already granted")
                getLastLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Log.d("MapsActivity", "Showing rationale")
                showPermissionRationale {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
            else -> {
                Log.d("MapsActivity", "Requesting permission directly")
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showPermissionRationale(positiveAction: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Location Permission Required")
            .setMessage("This app needs location permission to show your current location on the map. Without this permission, the app cannot function properly.")
            .setPositiveButton("OK") { _, _ ->
                positiveAction()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                Log.d("MapsActivity", "User cancelled permission request")
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun getLastLocation() {
        Log.d("MapsActivity", "getLastLocation() called")

        if (hasLocationPermission()) {
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            val currentLatLng = LatLng(location.latitude, location.longitude)
                            Log.d("MapsActivity", "Location found: ${location.latitude}, ${location.longitude}")

                            // Add marker at current location
                            mMap.addMarker(
                                MarkerOptions()
                                    .position(currentLatLng)
                                    .title("Your Current Location")
                                    .snippet("Lat: ${location.latitude}, Lng: ${location.longitude}")
                            )

                            // Move camera to current location
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))

                            // Enable current location layer
                            if (ContextCompat.checkSelfPermission(
                                    this,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                mMap.isMyLocationEnabled = true
                            }

                        } else {
                            Log.d("MapsActivity", "Location is null - might need to request location updates")
                            showDefaultLocation()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("MapsActivity", "Error getting location: ${exception.message}")
                        showDefaultLocation()
                    }
            } catch (e: SecurityException) {
                Log.e("MapsActivity", "SecurityException: ${e.message}")
            }
        } else {
            Log.d("MapsActivity", "No location permission")
        }
    }

    private fun showDefaultLocation() {
        // Show a default location
        val defaultLocation = LatLng(-6.2088, 106.8456)
        mMap.addMarker(
            MarkerOptions()
                .position(defaultLocation)
                .title("Default Location")
                .snippet("Jakarta, Indonesia")
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))
        Log.d("MapsActivity", "Showing default location")
    }
}