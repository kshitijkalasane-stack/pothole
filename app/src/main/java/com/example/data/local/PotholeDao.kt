package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.models.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PotholeDao {
    // Detections
    @Query("SELECT * FROM detection_events ORDER BY timestamp DESC")
    fun getAllDetections(): Flow<List<DetectionEventEntity>>

    @Query("SELECT * FROM detection_events WHERE syncStatus = 'PENDING'")
    suspend fun getPendingDetections(): List<DetectionEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetection(event: DetectionEventEntity)

    @Update
    suspend fun updateDetection(event: DetectionEventEntity)

    @Query("UPDATE detection_events SET syncStatus = :newStatus WHERE eventId = :id")
    suspend fun updateDetectionSyncStatus(id: String, newStatus: SyncStatus)

    @Query("SELECT COUNT(*) FROM detection_events")
    fun getDetectionCount(): Flow<Int>

    // Reports
    @Query("SELECT * FROM manual_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ManualReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ManualReportEntity)

    @Query("UPDATE manual_reports SET syncStatus = :newStatus WHERE reportId = :id")
    suspend fun updateReportSyncStatus(id: String, newStatus: SyncStatus)
}
