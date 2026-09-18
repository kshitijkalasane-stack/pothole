package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
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

    @Query("SELECT * FROM detection_events ORDER BY timestamp DESC")
    fun observeAllDetections(): Flow<List<DetectionEventEntity>>

    @Query("SELECT * FROM detection_events WHERE syncStatus = 'PENDING'")
    suspend fun getPendingDetections(): List<DetectionEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetection(event: DetectionEventEntity)

    @Update
    suspend fun updateDetection(event: DetectionEventEntity)

    @Delete
    suspend fun deleteDetection(event: DetectionEventEntity)

    @Query("DELETE FROM detection_events WHERE eventId = :eventId")
    suspend fun deleteDetectionById(eventId: String)

    @Query("DELETE FROM detection_events")
    suspend fun deleteAllDetections()

    @Query("UPDATE detection_events SET syncStatus = :newStatus WHERE eventId = :id")
    suspend fun updateDetectionSyncStatus(id: String, newStatus: SyncStatus)

    @Query("SELECT COUNT(*) FROM detection_events WHERE syncStatus = 'PENDING'")
    fun getPendingDetectionsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM manual_reports WHERE syncStatus = 'PENDING'")
    fun getPendingReportsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM detection_events")
    fun getDetectionCount(): Flow<Int>

    // Manual Reports
    @Query("SELECT * FROM manual_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ManualReportEntity>>

    @Query("SELECT * FROM manual_reports ORDER BY timestamp DESC")
    fun observeAllReports(): Flow<List<ManualReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ManualReportEntity)

    @Delete
    suspend fun deleteReport(report: ManualReportEntity)

    @Query("DELETE FROM manual_reports WHERE reportId = :reportId")
    suspend fun deleteReportById(reportId: String)

    @Query("DELETE FROM manual_reports")
    suspend fun deleteAllReports()

    @Query("UPDATE manual_reports SET syncStatus = :newStatus WHERE reportId = :id")
    suspend fun updateReportSyncStatus(id: String, newStatus: SyncStatus)

    // Cached Pothole Markers / Reports
    @Query("SELECT * FROM potholes ORDER BY lastDetectedAt DESC")
    fun getAllPotholes(): Flow<List<PotholeEntity>>

    @Query("SELECT * FROM potholes ORDER BY lastDetectedAt DESC")
    fun observeAllPotholes(): Flow<List<PotholeEntity>>

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

    @Delete
    suspend fun deletePothole(pothole: PotholeEntity)

    @Query("DELETE FROM potholes WHERE potholeId = :potholeId")
    suspend fun deletePotholeById(potholeId: String)

    @Query("DELETE FROM potholes")
    suspend fun deleteAllPotholes()

    @Query("UPDATE potholes SET status = :status, assignedTo = :assignedTo WHERE potholeId = :potholeId")
    suspend fun updatePotholeStatus(potholeId: String, status: PotholeStatus, assignedTo: String?)

    @Query("UPDATE potholes SET verificationCount = verificationCount + 1, lastDetectedAt = :timestamp WHERE potholeId = :potholeId")
    suspend fun incrementVerification(potholeId: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM potholes")
    fun getPotholeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM potholes")
    suspend fun getPotholeCountSync(): Int
}
