package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.models.Severity
import com.example.data.models.SyncStatus

@Entity(tableName = "detection_events")
data class DetectionEventEntity(
    @PrimaryKey
    val eventId: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val impactScore: Float,
    val confidence: Float,
    val severity: Severity,
    val source: String = "automatic",
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val zDiffMax: Float = 0f,
    val speedKmh: Float = 0f
)

@Entity(tableName = "manual_reports")
data class ManualReportEntity(
    @PrimaryKey
    val reportId: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val severity: Severity,
    val description: String,
    val photoUri: String? = null,
    val roadName: String = "",
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val aiRiskAnalysis: String? = null
)
