package com.example.services.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserLocation(
    val latitude: Double = 19.5687, // Default fallback to Sangamner Central
    val longitude: Double = 74.2112,
    val speedKmh: Float = 0f,
    val accuracy: Float = 5.0f,
    val hasGpsFix: Boolean = false
)

class LocationTrackingService(private val context: Context) {
    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    val locationFlow: StateFlow<UserLocation> = _sharedLocationFlow.asStateFlow()

    private var callback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    fun startAdaptiveTracking() {
        // Start Android background service to maintain continuous GPS updates
        try {
            PotholeLocationBackgroundService.start(context)
        } catch (_: Exception) {
            // Fallback handled gracefully
        }

        if (callback != null) return

        // Also run in-process client for immediate UI responsiveness
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2500L)
            .setMinUpdateIntervalMillis(1500L)
            .setMinUpdateDistanceMeters(2.0f)
            .build()

        callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    val speed = if (loc.hasSpeed()) loc.speed * 3.6f else 0f
                    updateLocation(
                        UserLocation(
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            speedKmh = speed,
                            accuracy = loc.accuracy,
                            hasGpsFix = true
                        )
                    )
                }
            }
        }

        try {
            fusedClient.requestLocationUpdates(request, callback!!, Looper.getMainLooper())
        } catch (_: SecurityException) {
            // Permission not yet granted, fallback is retained
        }
    }

    fun stopTracking() {
        try {
            PotholeLocationBackgroundService.stop(context)
        } catch (_: Exception) {
            // Fallback handled gracefully
        }

        callback?.let {
            fusedClient.removeLocationUpdates(it)
            callback = null
        }
    }

    fun setSimulatedLocation(lat: Double, lng: Double, speed: Float) {
        updateLocation(
            UserLocation(
                latitude = lat,
                longitude = lng,
                speedKmh = speed,
                accuracy = 3.0f,
                hasGpsFix = true
            )
        )
    }

    companion object {
        private val _sharedLocationFlow = MutableStateFlow(UserLocation())
        val sharedLocationFlow: StateFlow<UserLocation> = _sharedLocationFlow.asStateFlow()

        fun updateLocation(location: UserLocation) {
            _sharedLocationFlow.value = location
        }
    }
}
