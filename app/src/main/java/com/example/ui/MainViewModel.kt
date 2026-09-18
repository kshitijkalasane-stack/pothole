package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.DetectionEventEntity
import com.example.data.local.ManualReportEntity
import com.example.data.models.MonitoringState
import com.example.data.models.Pothole
import com.example.data.models.PotholeStatus
import com.example.data.models.Severity
import com.example.data.models.UserProfile
import com.example.data.models.UserRole
import com.example.data.repository.PotholeRepository
import com.example.services.ai.AiClient
import com.example.services.location.LocationTrackingService
import com.example.services.location.UserLocation
import com.example.services.sensor.LiveSensorTelemetry
import com.example.services.sensor.SensorDetectionEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainUiState(
    val isLoggedIn: Boolean = false,
    val currentRole: UserRole = UserRole.CITIZEN,
    val userProfile: UserProfile = UserProfile(
        userId = "USR-9921",
        name = "Samyak Gawale",
        role = UserRole.CITIZEN,
        email = "commuter.sangamner@smartpothole.in"
    ),
    val monitoringState: MonitoringState = MonitoringState.IDLE,
    val selectedTab: Int = 0, // 0: Home, 1: Map, 2: Report, 3: Activity, 4: Profile
    val authorityTab: Int = 0, // 0: Dashboard, 1: Map, 2: Settings
    val lastImpactSeverity: Severity? = null,
    val lastImpactScore: Float = 0f,
    val lastImpactTime: Long = 0L,
    val activePotholeDetail: Pothole? = null,
    val mapFocusCoordinates: Pair<Double, Double>? = null,
    val aiSummaryText: String? = null,
    val isAiLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val alertMessage: String? = null
)

class MainViewModel(
    private val repository: PotholeRepository,
    private val sensorEngine: SensorDetectionEngine,
    private val locationService: LocationTrackingService
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val cloudPotholes: StateFlow<List<Pothole>> = repository.cloudPotholes

    val localDetections: StateFlow<List<DetectionEventEntity>> = repository.localDetections
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val localReports: StateFlow<List<ManualReportEntity>> = repository.localReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userLocation: StateFlow<UserLocation> = locationService.locationFlow

    private val _telemetry = MutableStateFlow(LiveSensorTelemetry())
    val telemetry: StateFlow<LiveSensorTelemetry> = _telemetry.asStateFlow()

    init {
        // Collect sensor detections
        viewModelScope.launch {
            sensorEngine.anomalyFlow.collect { anomaly ->
                handleDetectedAnomaly(anomaly.impactScore, anomaly.confidence, anomaly.severity, anomaly.zDiffMax)
            }
        }
        // Collect live sensor telemetry
        viewModelScope.launch {
            sensorEngine.telemetryFlow.collect { telem ->
                _telemetry.value = telem
            }
        }
    }

    fun loginCitizen(identifier: String, name: String) {
        val emailOrPhone = if (identifier.contains("@")) identifier else "$identifier@citizen.sangamner.in"
        val profile = UserProfile(
            userId = "CIT-${identifier.takeLast(4)}",
            name = name.ifBlank { "Citizen Commuter" },
            role = UserRole.CITIZEN,
            email = emailOrPhone
        )
        _uiState.value = _uiState.value.copy(
            isLoggedIn = true,
            currentRole = UserRole.CITIZEN,
            userProfile = profile,
            selectedTab = 0,
            alertMessage = "Welcome, ${profile.name}! Sensor-assisted road monitoring is ready."
        )
    }

    fun loginAuthority(officerId: String, passkey: String, dept: String) {
        val profile = UserProfile(
            userId = officerId.ifBlank { "PWD-OFFICER" },
            name = "Officer ($officerId)",
            role = UserRole.AUTHORITY,
            email = "${officerId.lowercase().replace('-', '.')}@pwd.sangamner.gov.in"
        )
        _uiState.value = _uiState.value.copy(
            isLoggedIn = true,
            currentRole = UserRole.AUTHORITY,
            userProfile = profile,
            authorityTab = 0,
            alertMessage = "Authorized access granted for $dept."
        )
    }

    fun logout() {
        stopMonitoring()
        _uiState.value = _uiState.value.copy(
            isLoggedIn = false,
            selectedTab = 0,
            authorityTab = 0,
            alertMessage = "Signed out successfully."
        )
    }

    fun switchRole(newRole: UserRole) {
        val updatedProfile = _uiState.value.userProfile.copy(
            role = newRole,
            name = if (newRole == UserRole.CITIZEN) "Samyak Gawale" else "Municipal Officer Sharma",
            email = if (newRole == UserRole.CITIZEN) "commuter.sangamner@smartpothole.in" else "authority.pwd@sangamner.gov.in"
        )
        _uiState.value = _uiState.value.copy(
            currentRole = newRole,
            userProfile = updatedProfile
        )
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }

    fun selectAuthorityTab(index: Int) {
        _uiState.value = _uiState.value.copy(authorityTab = index)
    }

    fun navigateToClusterMap(lat: Double? = null, lng: Double? = null) {
        _uiState.value = _uiState.value.copy(
            authorityTab = 1,
            selectedTab = 1,
            mapFocusCoordinates = if (lat != null && lng != null) Pair(lat, lng) else null
        )
    }

    fun clearMapFocusCoordinates() {
        _uiState.value = _uiState.value.copy(mapFocusCoordinates = null)
    }

    fun startMonitoring() {
        locationService.startAdaptiveTracking()
        sensorEngine.start()
        _uiState.value = _uiState.value.copy(monitoringState = MonitoringState.MONITORING)
    }

    fun stopMonitoring() {
        sensorEngine.stop()
        locationService.stopTracking()
        _uiState.value = _uiState.value.copy(monitoringState = MonitoringState.IDLE)
    }

    private fun handleDetectedAnomaly(
        impactScore: Float,
        confidence: Float,
        severity: Severity,
        zDiffMax: Float
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                monitoringState = MonitoringState.IMPACT_DETECTED,
                lastImpactSeverity = severity,
                lastImpactScore = impactScore,
                lastImpactTime = System.currentTimeMillis()
            )

            // Current location tag
            val loc = userLocation.value
            repository.recordDetectedAnomaly(
                lat = loc.latitude,
                lng = loc.longitude,
                impactScore = impactScore,
                confidence = confidence,
                severity = severity,
                zDiffMax = zDiffMax,
                speedKmh = loc.speedKmh
            )

            kotlinx.coroutines.delay(1000)
            _uiState.value = _uiState.value.copy(
                monitoringState = if (sensorEngine.isMonitoring) MonitoringState.MONITORING else MonitoringState.IDLE,
                alertMessage = "New Road Impact Detected: ${severity.label} Severity (Score: ${(impactScore * 100).toInt()}%)"
            )
        }
    }

    fun simulateDrivingBump(severity: Severity = Severity.HIGH) {
        sensorEngine.simulateImpact(severity)
    }

    fun submitReport(
        severity: Severity,
        description: String,
        roadName: String,
        photoUri: String?
    ) {
        viewModelScope.launch {
            val loc = userLocation.value
            repository.submitManualReport(
                lat = loc.latitude,
                lng = loc.longitude,
                severity = severity,
                description = description,
                photoUri = photoUri,
                roadName = roadName
            )
            _uiState.value = _uiState.value.copy(
                alertMessage = "Manual pothole report recorded offline and added to sync queue!"
            )
        }
    }

    fun syncPendingData() {
        viewModelScope.launch {
            val count = repository.triggerSyncBatch()
            _uiState.value = _uiState.value.copy(
                alertMessage = if (count > 0) "Successfully synced $count pending event(s) to cloud database." else "All records are already up to date."
            )
        }
    }

    fun refreshCloudReports() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            val result = repository.syncWithCloudAndFetchLatest()
            _uiState.value = _uiState.value.copy(
                isRefreshing = false,
                alertMessage = "Synced with cloud: ${result.totalActivePotholes} road hazards active in cache (${result.syncedCount} uploads synced)."
            )
        }
    }

    fun inspectPothole(pothole: Pothole?) {
        _uiState.value = _uiState.value.copy(activePotholeDetail = pothole)
    }

    fun updatePotholeStatus(potholeId: String, newStatus: PotholeStatus, crew: String? = null) {
        viewModelScope.launch {
            repository.updatePotholeStatus(potholeId, newStatus, crew)
            _uiState.value.activePotholeDetail?.let {
                if (it.potholeId == potholeId) {
                    _uiState.value = _uiState.value.copy(
                        activePotholeDetail = it.copy(status = newStatus, assignedTo = crew ?: it.assignedTo)
                    )
                }
            }
        }
    }

    fun generateAiAreaSummary() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAiLoading = true)
            val potholes = cloudPotholes.value
            val openCount = potholes.count { it.status == PotholeStatus.OPEN }
            val highCount = potholes.count { it.severity == Severity.HIGH }
            val inRepairCount = potholes.count { it.status == PotholeStatus.IN_REPAIR }
            val resolvedCount = potholes.count { it.status == PotholeStatus.RESOLVED }

            val prompt = """
                You are the AI Assistant for the Smart Pothole Locator & Management System in Sangamner.
                Provide a concise, professional operational briefing (3-4 bullet points) for municipal authorities:
                Total Tracked Potholes: ${potholes.size}
                Open Unattended: $openCount
                High Severity / Two-wheeler hazard: $highCount
                Currently In Repair: $inRepairCount
                Resolved This Week: $resolvedCount
                Focus on priority allocation, high-risk zones, and crew scheduling.
            """.trimIndent()

            val result = AiClient.queryGemini(prompt)
            result.onSuccess { summary ->
                _uiState.value = _uiState.value.copy(aiSummaryText = summary, isAiLoading = false)
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    aiSummaryText = "Area Briefing:\n• $highCount high-severity road hazards identified requiring immediate asphalt patching.\n• $openCount reports awaiting municipal verification along Pune-Nashik highway corridor.\n• Recommendation: Prioritize AVCOE College Gate and Sangamner Bus Stand intersection crews.",
                    isAiLoading = false
                )
            }
        }
    }

    fun clearAlert() {
        _uiState.value = _uiState.value.copy(alertMessage = null)
    }

    companion object {
        fun provideFactory(
            repository: PotholeRepository,
            sensorEngine: SensorDetectionEngine,
            locationService: LocationTrackingService
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(repository, sensorEngine, locationService) as T
            }
        }
    }
}
