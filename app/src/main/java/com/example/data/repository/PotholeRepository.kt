package com.example.data.repository

import com.example.data.local.DetectionEventEntity
import com.example.data.local.ManualReportEntity
import com.example.data.local.PotholeDao
import com.example.data.local.PotholeEntity
import com.example.data.local.toEntity
import com.example.data.models.Pothole
import com.example.data.models.PotholeStatus
import com.example.data.models.Severity
import com.example.data.models.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class PotholeRepository(
    private val dao: PotholeDao,
    private val applicationScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    // Reactive Cached Potholes from Room Database (Offline-first source of truth)
    val cloudPotholes: StateFlow<List<Pothole>> = dao.getAllPotholes()
        .map { entities -> entities.map { it.toDomainModel() } }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = initialSeedPotholes()
        )

    // Local DB Observables for Detections and Reports
    val localDetections: Flow<List<DetectionEventEntity>> = dao.getAllDetections()
    val localReports: Flow<List<ManualReportEntity>> = dao.getAllReports()
    val detectionCount: Flow<Int> = dao.getDetectionCount()

    init {
        // Pre-populate Room database with initial seed potholes if empty
        applicationScope.launch {
            seedDatabaseIfEmpty()
        }
    }

    private suspend fun seedDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        val count = dao.getPotholeCountSync()
        if (count == 0) {
            val entities = initialSeedPotholes().map { it.toEntity() }
            dao.insertPotholes(entities)
        }
    }

    suspend fun recordDetectedAnomaly(
        lat: Double,
        lng: Double,
        impactScore: Float,
        confidence: Float,
        severity: Severity,
        zDiffMax: Float,
        speedKmh: Float
    ): DetectionEventEntity = withContext(Dispatchers.IO) {
        val event = DetectionEventEntity(
            eventId = "DET-${UUID.randomUUID().toString().take(8).uppercase()}",
            latitude = lat,
            longitude = lng,
            timestamp = System.currentTimeMillis(),
            impactScore = impactScore,
            confidence = confidence,
            severity = severity,
            source = "automatic",
            syncStatus = SyncStatus.PENDING,
            zDiffMax = zDiffMax,
            speedKmh = speedKmh
        )
        dao.insertDetection(event)
        // Check for clustering & persist cached pothole in Room
        clusterOrAddPothole(lat, lng, severity, confidence)
        event
    }

    suspend fun submitManualReport(
        lat: Double,
        lng: Double,
        severity: Severity,
        description: String,
        photoUri: String?,
        roadName: String,
        aiRiskSummary: String? = null
    ): ManualReportEntity = withContext(Dispatchers.IO) {
        val report = ManualReportEntity(
            reportId = "REP-${UUID.randomUUID().toString().take(8).uppercase()}",
            latitude = lat,
            longitude = lng,
            timestamp = System.currentTimeMillis(),
            severity = severity,
            description = description,
            photoUri = photoUri,
            roadName = roadName,
            syncStatus = SyncStatus.PENDING,
            aiRiskAnalysis = aiRiskSummary
        )
        dao.insertReport(report)
        clusterOrAddPothole(lat, lng, severity, 0.85f, description, roadName)
        report
    }

    suspend fun triggerSyncBatch(): Int = withContext(Dispatchers.IO) {
        val pending = dao.getPendingDetections()
        for (item in pending) {
            dao.updateDetectionSyncStatus(item.eventId, SyncStatus.SYNCED)
        }
        pending.size
    }

    suspend fun syncWithCloudAndFetchLatest(): SyncResult = withContext(Dispatchers.IO) {
        // 1. Sync pending local items
        val pending = dao.getPendingDetections()
        for (item in pending) {
            dao.updateDetectionSyncStatus(item.eventId, SyncStatus.SYNCED)
        }

        // 2. Network simulation delay to simulate fetching cloud records
        kotlinx.coroutines.delay(1000)

        // 3. Ensure baseline potholes are populated in Room
        val count = dao.getPotholeCountSync()
        if (count == 0) {
            seedDatabaseIfEmpty()
        }

        SyncResult(
            syncedCount = pending.size,
            totalActivePotholes = dao.getPotholeCountSync()
        )
    }

    private suspend fun clusterOrAddPothole(
        lat: Double,
        lng: Double,
        severity: Severity,
        confidence: Float,
        note: String = "",
        roadName: String = ""
    ) = withContext(Dispatchers.IO) {
        val currentPotholes = dao.getPotholeListSnapshot()
        // Geographic clustering within ~40 meters (~0.0004 deg)
        val existing = currentPotholes.firstOrNull {
            val dLat = Math.abs(it.latitude - lat)
            val dLng = Math.abs(it.longitude - lng)
            dLat < 0.0004 && dLng < 0.0004
        }

        if (existing != null) {
            val updated = existing.copy(
                reportCount = existing.reportCount + 1,
                verificationCount = existing.verificationCount + 1,
                lastDetectedAt = System.currentTimeMillis(),
                confidence = ((existing.confidence + confidence) / 2.0).coerceAtMost(0.99),
                severity = if (severity == Severity.HIGH || existing.severity == Severity.HIGH) Severity.HIGH else existing.severity,
                notes = if (note.isNotBlank()) "${existing.notes}\n$note".trim() else existing.notes
            )
            dao.updatePothole(updated)
        } else {
            val addressText = if (roadName.isNotBlank()) roadName else "Near Highway Km ${(20..45).random()}, Sangamner"
            val newPothole = PotholeEntity(
                potholeId = "PTH-${(currentPotholes.size + 1001)}",
                latitude = lat,
                longitude = lng,
                severity = severity,
                confidence = confidence.toDouble(),
                reportCount = 1,
                verificationCount = 1,
                status = PotholeStatus.OPEN,
                firstDetectedAt = System.currentTimeMillis(),
                lastDetectedAt = System.currentTimeMillis(),
                address = addressText,
                notes = note
            )
            dao.insertPothole(newPothole)
        }
    }

    suspend fun updatePotholeStatus(potholeId: String, newStatus: PotholeStatus, assignedCrew: String? = null) = withContext(Dispatchers.IO) {
        val existing = dao.getPotholeById(potholeId)
        if (existing != null) {
            val updated = existing.copy(
                status = newStatus,
                assignedTo = assignedCrew ?: existing.assignedTo,
                verificationCount = if (newStatus == PotholeStatus.UNDER_VERIFICATION) existing.verificationCount + 1 else existing.verificationCount
            )
            dao.updatePothole(updated)
        }
    }

    suspend fun incrementPotholeVerification(potholeId: String) = withContext(Dispatchers.IO) {
        dao.incrementVerification(potholeId)
    }

    private fun initialSeedPotholes(): List<Pothole> {
        return listOf(
            Pothole(
                potholeId = "PTH-1001",
                latitude = 19.5687,
                longitude = 74.2112,
                severity = Severity.HIGH,
                confidence = 0.94,
                reportCount = 6,
                verificationCount = 5,
                status = PotholeStatus.OPEN,
                address = "College Gate, AVCOE, Sangamner",
                notes = "Deep impact along right lane, danger for two-wheelers."
            ),
            Pothole(
                potholeId = "PTH-1002",
                latitude = 19.5724,
                longitude = 74.2158,
                severity = Severity.MEDIUM,
                confidence = 0.82,
                reportCount = 3,
                verificationCount = 2,
                status = PotholeStatus.ASSIGNED,
                address = "Sangamner Bus Stand Chowk",
                notes = "Cracked depression near pedestrian crossing.",
                assignedTo = "Municipal Road Crew Alpha"
            ),
            Pothole(
                potholeId = "PTH-1003",
                latitude = 19.5641,
                longitude = 74.2085,
                severity = Severity.HIGH,
                confidence = 0.89,
                reportCount = 7,
                verificationCount = 6,
                status = PotholeStatus.IN_REPAIR,
                address = "Akole Bypass Road Junction",
                notes = "Large water-logged pothole under active patching.",
                assignedTo = "Public Works Squad 2"
            ),
            Pothole(
                potholeId = "PTH-1004",
                latitude = 19.5780,
                longitude = 74.2210,
                severity = Severity.LOW,
                confidence = 0.74,
                reportCount = 1,
                verificationCount = 1,
                status = PotholeStatus.RESOLVED,
                address = "Market Yard Approach",
                notes = "Surface repaired with cold-mix asphalt.",
                assignedTo = "City Maintenance 4"
            )
        )
    }
}

data class SyncResult(
    val syncedCount: Int,
    val totalActivePotholes: Int
)
