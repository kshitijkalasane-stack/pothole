package com.example.services.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.SmartPotholeApp
import com.example.data.models.Pothole
import com.example.data.models.Severity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class PotholeLocationBackgroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var notificationManager: NotificationManager

    private var locationCallback: LocationCallback? = null
    private var lastLocation: Location? = null
    private var lastProximityAlertTime = 0L

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        observeSensorAnomalies()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START

        if (action == ACTION_STOP) {
            stopTracking()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            _isTrackingRunning.value = false
            return START_NOT_STICKY
        }

        _isTrackingRunning.value = true
        startForegroundWithNotification()
        startLocationUpdates()

        return START_STICKY
    }

    private fun startForegroundWithNotification() {
        val notification = buildOngoingNotification(
            title = "Smart Pothole Monitor Active",
            content = "Tracking GPS coordinates & scanning road surface telemetry"
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val foregroundServiceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            } else {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            }
            startForeground(NOTIFICATION_ID, notification, foregroundServiceType)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildOngoingNotification(title: String, content: String): Notification {
        val mainActivityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingMainActivity = PendingIntent.getActivity(
            this,
            0,
            mainActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, PotholeLocationBackgroundService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingStopIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingMainActivity)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop Monitoring",
                pendingStopIntent
            )
            .build()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (locationCallback != null) return

        val hasFineLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFineLocation && !hasCoarseLocation) {
            return
        }

        // High accuracy location request with fast interval for road tracking
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
            .setMinUpdateIntervalMillis(1000L)
            .setMinUpdateDistanceMeters(2.0f)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                lastLocation = loc

                val speedKmh = if (loc.hasSpeed()) loc.speed * 3.6f else 0f
                val userLoc = UserLocation(
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    speedKmh = speedKmh,
                    accuracy = loc.accuracy,
                    hasGpsFix = true
                )

                // Push location to shared application flow
                LocationTrackingService.updateLocation(userLoc)

                // Update notification text periodically with live coordinates
                updateLiveNotification(loc.latitude, loc.longitude, speedKmh)

                // Proximity scan for known high-risk potholes ahead
                checkNearbyPotholes(loc.latitude, loc.longitude, speedKmh)
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
        } catch (_: SecurityException) {
            // Handled gracefully
        }
    }

    private fun updateLiveNotification(lat: Double, lng: Double, speedKmh: Float) {
        val speedStr = String.format(Locale.US, "%.1f km/h", speedKmh)
        val coordStr = String.format(Locale.US, "%.4f N, %.4f E", lat, lng)
        val content = "$coordStr • $speedStr • Edge Radar Active"

        val updatedNotification = buildOngoingNotification(
            title = "Smart Pothole Monitor Active",
            content = content
        )
        notificationManager.notify(NOTIFICATION_ID, updatedNotification)
    }

    private fun checkNearbyPotholes(userLat: Double, userLng: Double, speedKmh: Float) {
        val app = application as? SmartPotholeApp ?: return
        val potholes = app.repository.cloudPotholes.value

        val now = System.currentTimeMillis()
        if (now - lastProximityAlertTime < 20_000L) return // 20s cooldown between proximity alerts

        for (p in potholes) {
            if (p.severity == Severity.HIGH) {
                val distMeters = calculateDistanceMeters(userLat, userLng, p.latitude, p.longitude)
                if (distMeters in 5.0..65.0) {
                    lastProximityAlertTime = now
                    triggerProximityAlert(p, distMeters)
                    break
                }
            }
        }
    }

    private fun triggerProximityAlert(pothole: Pothole, distanceMeters: Double) {
        triggerHapticWarning()

        val alertNotification = NotificationCompat.Builder(this, CHANNEL_HAZARD_ALERT_ID)
            .setContentTitle("⚠️ Caution: Severe Road Hazard Ahead!")
            .setContentText(
                String.format(
                    Locale.US,
                    "High-risk pothole %dm ahead near %s",
                    distanceMeters.toInt(),
                    pothole.address
                )
            )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(HAZARD_ALERT_NOTIFICATION_ID, alertNotification)
    }

    private fun observeSensorAnomalies() {
        val app = application as? SmartPotholeApp ?: return
        serviceScope.launch {
            app.sensorEngine.anomalyFlow.collect { anomaly ->
                val loc = lastLocation
                val currentLat = loc?.latitude ?: LocationTrackingService.sharedLocationFlow.value.latitude
                val currentLng = loc?.longitude ?: LocationTrackingService.sharedLocationFlow.value.longitude
                val currentSpeed = if (loc != null && loc.hasSpeed()) loc.speed * 3.6f else LocationTrackingService.sharedLocationFlow.value.speedKmh

                // Background anomaly logging
                app.repository.recordDetectedAnomaly(
                    lat = currentLat,
                    lng = currentLng,
                    impactScore = anomaly.impactScore,
                    confidence = anomaly.confidence,
                    severity = anomaly.severity,
                    zDiffMax = anomaly.zDiffMax,
                    speedKmh = currentSpeed
                )

                // Background hazard detected alert
                triggerHapticWarning()
                val hazardNotification = NotificationCompat.Builder(this@PotholeLocationBackgroundService, CHANNEL_HAZARD_ALERT_ID)
                    .setContentTitle("⚠️ Road Bump Geotagged!")
                    .setContentText(
                        String.format(
                            Locale.US,
                            "%s severity impact at %.4f, %.4f (Confidence: %d%%)",
                            anomaly.severity.label,
                            currentLat,
                            currentLng,
                            (anomaly.confidence * 100).toInt()
                        )
                    )
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(HAZARD_ALERT_NOTIFICATION_ID + 1, hazardNotification)
            }
        }
    }

    private fun triggerHapticWarning() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(300L, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(300L)
            }
        } catch (_: Exception) {
            // Ignore if vibration permission or hardware unavailable
        }
    }

    private fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    private fun stopTracking() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val trackingChannel = NotificationChannel(
                CHANNEL_ID,
                "Pothole Tracking & GPS Telemetry",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing notification for background road coordinates tracking"
                setShowBadge(false)
            }

            val alertChannel = NotificationChannel(
                CHANNEL_HAZARD_ALERT_ID,
                "Road Hazard Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Instant alerts for detected potholes and road depressions"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(trackingChannel)
            notificationManager.createNotificationChannel(alertChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopTracking()
        serviceScope.cancel()
        _isTrackingRunning.value = false
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.example.action.START_POTHOLE_TRACKING"
        const val ACTION_STOP = "com.example.action.STOP_POTHOLE_TRACKING"
        const val CHANNEL_ID = "pothole_tracking_foreground_channel"
        const val CHANNEL_HAZARD_ALERT_ID = "pothole_hazard_alerts_channel"
        const val NOTIFICATION_ID = 4001
        const val HAZARD_ALERT_NOTIFICATION_ID = 4002

        private val _isTrackingRunning = MutableStateFlow(false)
        val isTrackingRunning: StateFlow<Boolean> = _isTrackingRunning.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, PotholeLocationBackgroundService::class.java).apply {
                action = ACTION_START
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, PotholeLocationBackgroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
