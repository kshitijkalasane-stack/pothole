package com.example.data.repository

import com.example.data.local.DetectionEventEntity
import com.example.data.local.ManualReportEntity
import com.example.data.local.PotholeDao
import com.example.data.models.Pothole
import com.example.data.models.PotholeStatus
import com.example.data.models.Severity
import com.example.data.models.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class PotholeRepository(private val dao: PotholeDao) {

    // Global Cloud-like In-memory Potholes store (matching Firestore shared view)
    private val _cloudPotholes = MutableStateFlow<List<Pothole>>(initialSeedPotholes())
    val cloudPotholes: StateFlow<List<Pothole>> = _cloudPotholes.asStateFlow()

    // Local DB Observables
    val localDetections: Flow<List<DetectionEventEntity>> = dao.getAllDetections()
    val localReports: Flow<List<ManualReportEntity>> = dao.getAllReports()
    val detectionCount: Flow<Int> = dao.getDetectionCount()

    suspend fun recordDetectedAnomaly(
        lat: Double,
        lng: Double,
        impactScore: Float,
        confidence: Float,
        severity: Severity,
        zDiffMax: Float,
        speedKmh: Float
    ): DetectionEventEntity {
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
        // Check for clustering / update cloud representation
        clusterOrAddPothole(lat, lng, severity, confidence)
        return event
    }

    suspend fun submitManualReport(
        lat: Double,
        lng: Double,
        severity: Severity,
        description: String,
        photoUri: String?,
        roadName: String,
        aiRiskSummary: String? = null
    ): ManualReportEntity {
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
        clusterOrAddPothole(lat, lng, severity, 0.85f, description)
        return report
    }

    suspend fun triggerSyncBatch(): Int {
        val pending = dao.getPendingDetections()
        for (item in pending) {
            dao.updateDetectionSyncStatus(item.eventId, SyncStatus.SYNCED)
        }
        return pending.size
    }

    private fun clusterOrAddPothole(
        lat: Double,
        lng: Double,
        severity: Severity,
        confidence: Float,
        note: String = ""
    ) {
        val current = _cloudPotholes.value.toMutableList()
        // Geographic clustering within ~40 meters (~0.0004 deg)
        val existingIndex = current.indexOfFirst {
            val dLat = Math.abs(it.latitude - lat)
            val dLng = Math.abs(it.longitude - lng)
            dLat < 0.0004 && dLng < 0.0004
        }

        if (existingIndex >= 0) {
            val existing = current[existingIndex]
            val updated = existing.copy(
                reportCount = existing.reportCount + 1,
                verificationCount = existing.verificationCount + 1,
                lastDetectedAt = System.currentTimeMillis(),
                confidence = ((existing.confidence + confidence) / 2.0).coerceAtMost(0.99),
                severity = if (severity == Severity.HIGH || existing.severity == Severity.HIGH) Severity.HIGH else existing.severity
            )
            current[existingIndex] = updated
        } else {
            val newPothole = Pothole(
                potholeId = "PTH-${(current.size + 1001)}",
                latitude = lat,
                longitude = lng,
                severity = severity,
                confidence = confidence.toDouble(),
                reportCount = 1,
                verificationCount = 1,
                status = PotholeStatus.OPEN,
                firstDetectedAt = System.currentTimeMillis(),
                lastDetectedAt = System.currentTimeMillis(),
                address = "Near Highway Km ${(20..45).random()}, Sangamner",
                notes = note
            )
            current.add(0, newPothole)
        }
        _cloudPotholes.value = current
    }

    fun updatePotholeStatus(potholeId: String, newStatus: PotholeStatus, assignedCrew: String? = null) {
        val current = _cloudPotholes.value.toMutableList()
        val index = current.indexOfFirst { it.potholeId == potholeId }
        if (index >= 0) {
            val target = current[index]
            current[index] = target.copy(
                status = newStatus,
                assignedTo = assignedCrew ?: target.assignedTo,
                verificationCount = if (newStatus == PotholeStatus.UNDER_VERIFICATION) target.verificationCount + 1 else target.verificationCount
            )
            _cloudPotholes.value = current
        }
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
