package com.example.services.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.data.models.Severity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.math.abs
import kotlin.math.sqrt

data class DetectedAnomaly(
    val impactScore: Float,
    val confidence: Float,
    val severity: Severity,
    val zDiffMax: Float,
    val timestamp: Long = System.currentTimeMillis()
)

data class LiveSensorTelemetry(
    val ax: Float = 0f,
    val ay: Float = 0f,
    val az: Float = 9.8f,
    val gyroZ: Float = 0f,
    val zDiff: Float = 0f,
    val isImpactCandidate: Boolean = false
)

class SensorDetectionEngine(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private val _anomalyFlow = MutableSharedFlow<DetectedAnomaly>(extraBufferCapacity = 10)
    val anomalyFlow: SharedFlow<DetectedAnomaly> = _anomalyFlow.asSharedFlow()

    private val _telemetryFlow = MutableSharedFlow<LiveSensorTelemetry>(extraBufferCapacity = 5)
    val telemetryFlow: SharedFlow<LiveSensorTelemetry> = _telemetryFlow.asSharedFlow()

    private var gravityZ = 9.8f
    private val alpha = 0.8f // Low pass filter factor for baseline gravity estimation

    // Detection mathematical thresholds as specified in Architecture PRD
    // Z-THRESH: Threshold for significant vertical displacement
    private val zThresh = 4.2f // m/s^2 deviation from gravity
    // Z-DIFF: Difference between consecutive smoothed vertical samples to isolate abrupt potholes
    private var lastLinearZ = 0f

    // Euler / Tilt Compensation
    private var gyroRoll = 0f
    private var gyroPitch = 0f

    // False positive suppression (cooldown)
    private var lastImpactTime = 0L
    private val impactCooldownMs = 1200L

    var isMonitoring = false
        private set

    fun start() {
        if (isMonitoring) return
        isMonitoring = true
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        gyroscope?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        if (!isMonitoring) return
        isMonitoring = false
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!isMonitoring || event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_GYROSCOPE -> {
                gyroRoll = event.values[0]
                gyroPitch = event.values[1]
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val rawX = event.values[0]
                val rawY = event.values[1]
                val rawZ = event.values[2]

                // Step 1: Low-Pass Filter to estimate vehicle static gravity baseline
                gravityZ = alpha * gravityZ + (1 - alpha) * rawZ

                // Step 2: Linear acceleration along device vertical axis (Z-axis)
                val linearZ = rawZ - gravityZ

                // Step 3: Z-DIFF calculation (rate of vertical acceleration change)
                val zDiff = linearZ - lastLinearZ
                lastLinearZ = linearZ

                // Step 4: Euler angle & vehicle tilt compensation
                // If phone is experiencing sharp angular twist (e.g. sharp corner or user handling phone), suppress
                val angularSpeed = sqrt(gyroRoll * gyroRoll + gyroPitch * gyroPitch)
                val isDeviceHandling = angularSpeed > 2.5f

                val absLinearZ = abs(linearZ)
                val absZDiff = abs(zDiff)

                _telemetryFlow.tryEmit(
                    LiveSensorTelemetry(
                        ax = rawX,
                        ay = rawY,
                        az = rawZ,
                        gyroZ = event.values[2],
                        zDiff = absZDiff,
                        isImpactCandidate = absLinearZ > zThresh && !isDeviceHandling
                    )
                )

                // Step 5: Z-THRESH & Z-DIFF Trigger with False-Positive Filtering
                val currentTime = System.currentTimeMillis()
                if (absLinearZ >= zThresh && absZDiff >= 3.0f && !isDeviceHandling) {
                    if (currentTime - lastImpactTime >= impactCooldownMs) {
                        lastImpactTime = currentTime

                        // Step 6: Compute Impact Score & Severity
                        // Impact score normalized roughly between 0.0 and 1.0
                        val rawScore = (absLinearZ / 15f).coerceIn(0.2f, 1.0f)
                        val confidence = if (absZDiff > 5.5f) 0.92f else 0.78f

                        val severity = when {
                            absLinearZ >= 8.5f || absZDiff >= 8.0f -> Severity.HIGH
                            absLinearZ >= 5.5f || absZDiff >= 4.5f -> Severity.MEDIUM
                            else -> Severity.LOW
                        }

                        _anomalyFlow.tryEmit(
                            DetectedAnomaly(
                                impactScore = rawScore,
                                confidence = confidence,
                                severity = severity,
                                zDiffMax = absZDiff,
                                timestamp = currentTime
                            )
                        )
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    /**
     * For demonstration & driving simulation in testing environments
     */
    fun simulateImpact(severity: Severity = Severity.HIGH) {
        val score = when (severity) {
            Severity.HIGH -> 0.88f
            Severity.MEDIUM -> 0.65f
            Severity.LOW -> 0.42f
        }
        val zDiff = when (severity) {
            Severity.HIGH -> 9.4f
            Severity.MEDIUM -> 5.8f
            Severity.LOW -> 3.4f
        }
        _anomalyFlow.tryEmit(
            DetectedAnomaly(
                impactScore = score,
                confidence = 0.90f,
                severity = severity,
                zDiffMax = zDiff,
                timestamp = System.currentTimeMillis()
            )
        )
    }
}
