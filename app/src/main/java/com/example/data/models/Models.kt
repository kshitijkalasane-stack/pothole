package com.example.data.models

enum class UserRole {
    CITIZEN,
    AUTHORITY
}

enum class Severity(val label: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High")
}

enum class PotholeStatus(val label: String) {
    OPEN("Open"),
    UNDER_VERIFICATION("Under Verification"),
    ASSIGNED("Assigned"),
    IN_REPAIR("In Repair"),
    RESOLVED("Resolved")
}

enum class SyncStatus {
    PENDING,
    SYNCED,
    FAILED
}

enum class MonitoringState {
    IDLE,
    STARTING,
    MONITORING,
    IMPACT_DETECTED,
    VALIDATING,
    LOCAL_SAVE,
    SYNCED
}

data class UserProfile(
    val userId: String,
    val name: String,
    val role: UserRole,
    val email: String,
    val jurisdictionArea: String = "Sangamner Central",
    val detectionsCount: Int = 0
)

data class Pothole(
    val potholeId: String,
    val latitude: Double,
    val longitude: Double,
    val severity: Severity,
    val confidence: Double,
    val reportCount: Int,
    val verificationCount: Int,
    val status: PotholeStatus,
    val firstDetectedAt: Long = System.currentTimeMillis(),
    val lastDetectedAt: Long = System.currentTimeMillis(),
    val address: String = "Pune-Nashik Highway, Sangamner",
    val notes: String = "",
    val assignedTo: String? = null
)
