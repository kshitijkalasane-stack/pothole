package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.models.PotholeStatus
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

    // Cached Pothole Markers for Authorities & Citizens Offline Access
    @Query("SELECT * FROM potholes ORDER BY lastDetectedAt DESC")
    fun getAllPotholes(): Flow<List<PotholeEntity>>

    @Query("SELECT * FROM potholes WHERE potholeId = :potholeId")
    suspend fun getPotholeById(potholeId: String): PotholeEntity?

    @Query("SELECT * FROM potholes")
    suspend fun getPotholeListSnapshot(): List<PotholeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPothole(pothole: PotholeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPotholes(potholes: List<PotholeEntity>)

    @Update
    suspend fun updatePothole(pothole: PotholeEntity)

    @Query("UPDATE potholes SET status = :status, assignedTo = :assignedTo WHERE potholeId = :potholeId")
    suspend fun updatePotholeStatus(potholeId: String, status: PotholeStatus, assignedTo: String?)

    @Query("UPDATE potholes SET verificationCount = verificationCount + 1, lastDetectedAt = :timestamp WHERE potholeId = :potholeId")
    suspend fun incrementVerification(potholeId: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM potholes")
    fun getPotholeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM potholes")
    suspend fun getPotholeCountSync(): Int
}
