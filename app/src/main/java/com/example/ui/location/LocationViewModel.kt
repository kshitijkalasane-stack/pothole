package com.example.ui.location

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.services.location.LocationTrackingService
import com.example.services.location.UserLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

data class LocationState(
    val latitude: Double = 19.5687, // Fallback to Sangamner Central
    val longitude: Double = 74.2112,
    val accuracy: Float = 5.0f,
    val speedKmh: Float = 0f,
    val hasGpsFix: Boolean = false,
    val isTracking: Boolean = false,
    val isLoading: Boolean = false,
    val formattedCoordinates: String = "19.5687° N, 74.2112° E",
    val estimatedAddress: String = "Sangamner-Akole Bypass Rd, Ghulewadi",
    val lastUpdatedTime: Long = System.currentTimeMillis()
)

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context = application.applicationContext
    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _locationState = MutableStateFlow(LocationState())
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    private var locationCallback: LocationCallback? = null

    init {
        // Collect from shared system location flow if available
        viewModelScope.launch {
            LocationTrackingService.sharedLocationFlow.collect { userLoc ->
                updateStateFromUserLocation(userLoc)
            }
        }
        // Fetch last known device coordinates on creation
        fetchLastKnownLocation()
    }

    @SuppressLint("MissingPermission")
    fun fetchLastKnownLocation() {
        _locationState.value = _locationState.value.copy(isLoading = true)
        try {
            fusedClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    processLocationUpdate(location)
                } else {
                    requestSingleHighAccuracyUpdate()
                }
            }.addOnFailureListener {
                _locationState.value = _locationState.value.copy(isLoading = false)
            }
        } catch (_: SecurityException) {
            _locationState.value = _locationState.value.copy(isLoading = false)
        }
    }

    @SuppressLint("MissingPermission")
    fun requestSingleHighAccuracyUpdate() {
        _locationState.value = _locationState.value.copy(isLoading = true)
        try {
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        processLocationUpdate(location)
                    } else {
                        _locationState.value = _locationState.value.copy(isLoading = false)
                    }
                }
                .addOnFailureListener {
                    _locationState.value = _locationState.value.copy(isLoading = false)
                }
        } catch (_: SecurityException) {
            _locationState.value = _locationState.value.copy(isLoading = false)
        }
    }

    @SuppressLint("MissingPermission")
    fun startContinuousTracking(updateIntervalMs: Long = 2000L) {
        if (locationCallback != null) return

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, updateIntervalMs)
            .setMinUpdateIntervalMillis(1000L)
            .setMinUpdateDistanceMeters(1.0f)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    processLocationUpdate(loc)
                }
            }
        }

        try {
            fusedClient.requestLocationUpdates(request, locationCallback!!, Looper.getMainLooper())
            _locationState.value = _locationState.value.copy(isTracking = true)
        } catch (_: SecurityException) {
            _locationState.value = _locationState.value.copy(isTracking = false)
        }
    }

    fun stopContinuousTracking() {
        locationCallback?.let {
            fusedClient.removeLocationUpdates(it)
            locationCallback = null
        }
        _locationState.value = _locationState.value.copy(isTracking = false)
    }

    fun setManualCoordinates(latitude: Double, longitude: Double) {
        val location = Location("manual").apply {
            this.latitude = latitude
            this.longitude = longitude
            this.accuracy = 1.0f
        }
        processLocationUpdate(location)
    }

    private fun processLocationUpdate(location: Location) {
        val speedKmh = if (location.hasSpeed()) location.speed * 3.6f else 0f
        val latStr = "%.4f° N".format(location.latitude)
        val lngStr = "%.4f° E".format(location.longitude)
        val formatted = "$latStr, $lngStr"

        val userLocation = UserLocation(
            latitude = location.latitude,
            longitude = location.longitude,
            speedKmh = speedKmh,
            accuracy = location.accuracy,
            hasGpsFix = true
        )

        // Broadcast to shared service tracker
        LocationTrackingService.updateLocation(userLocation)

        _locationState.value = _locationState.value.copy(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            speedKmh = speedKmh,
            hasGpsFix = true,
            isLoading = false,
            formattedCoordinates = formatted,
            lastUpdatedTime = System.currentTimeMillis()
        )

        reverseGeocode(location.latitude, location.longitude)
    }

    private fun updateStateFromUserLocation(userLoc: UserLocation) {
        val latStr = "%.4f° N".format(userLoc.latitude)
        val lngStr = "%.4f° E".format(userLoc.longitude)
        _locationState.value = _locationState.value.copy(
            latitude = userLoc.latitude,
            longitude = userLoc.longitude,
            speedKmh = userLoc.speedKmh,
            accuracy = userLoc.accuracy,
            hasGpsFix = userLoc.hasGpsFix,
            formattedCoordinates = "$latStr, $lngStr",
            lastUpdatedTime = System.currentTimeMillis()
        )
    }

    private fun reverseGeocode(lat: Double, lng: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (Geocoder.isPresent()) {
                    @Suppress("DEPRECATION")
                    val geocoder = Geocoder(context, Locale.getDefault())
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val thoroughfare = address.thoroughfare ?: address.subLocality ?: "Sangamner Sector"
                        val locality = address.locality ?: "Sangamner"
                        val addressName = "$thoroughfare, $locality"
                        _locationState.value = _locationState.value.copy(estimatedAddress = addressName)
                    }
                }
            } catch (_: Exception) {
                // Keep default or previous address fallback
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopContinuousTracking()
    }
}
