package com.example.ui.maps

import com.example.data.models.Pothole
import com.example.data.models.Severity

data class PotholeClusterItem(
    val pothole: Pothole
) {
    val latitude: Double get() = pothole.latitude
    val longitude: Double get() = pothole.longitude
    val title: String get() = pothole.potholeId
    val snippet: String get() = "${pothole.severity.label} - ${pothole.address}"
}

data class PotholeClusterArea(
    val id: String,
    val name: String,
    val centerLatitude: Double,
    val centerLongitude: Double,
    val potholes: List<Pothole>,
    val highRiskCount: Int,
    val mediumRiskCount: Int,
    val lowRiskCount: Int,
    val radiusMeters: Double
)

object PotholeClusterAnalyzer {
    // Groups potholes within ~450 meters
    fun identifyClusters(potholes: List<Pothole>, thresholdMeters: Double = 450.0): List<PotholeClusterArea> {
        if (potholes.isEmpty()) return emptyList()

        val clusters = mutableListOf<MutableList<Pothole>>()
        val visited = mutableSetOf<String>()

        for (pothole in potholes) {
            if (pothole.potholeId in visited) continue

            val currentGroup = mutableListOf<Pothole>()
            currentGroup.add(pothole)
            visited.add(pothole.potholeId)

            for (other in potholes) {
                if (other.potholeId in visited) continue
                val dist = distanceInMeters(
                    pothole.latitude, pothole.longitude,
                    other.latitude, other.longitude
                )
                if (dist <= thresholdMeters) {
                    currentGroup.add(other)
                    visited.add(other.potholeId)
                }
            }
            clusters.add(currentGroup)
        }

        return clusters.mapIndexed { index, group ->
            val avgLat = group.map { it.latitude }.average()
            val avgLng = group.map { it.longitude }.average()
            val maxDist = group.maxOfOrNull {
                distanceInMeters(avgLat, avgLng, it.latitude, it.longitude)
            } ?: 100.0
            val effectiveRadius = (maxDist + 120.0).coerceIn(200.0, 600.0)

            val primaryAddress = group.groupBy { it.address.substringBefore(",").trim() }
                .maxByOrNull { it.value.size }?.key ?: "Corridor Zone #${index + 1}"

            PotholeClusterArea(
                id = "cluster_${index + 1}",
                name = primaryAddress,
                centerLatitude = avgLat,
                centerLongitude = avgLng,
                potholes = group,
                highRiskCount = group.count { it.severity == Severity.HIGH },
                mediumRiskCount = group.count { it.severity == Severity.MEDIUM },
                lowRiskCount = group.count { it.severity == Severity.LOW },
                radiusMeters = effectiveRadius
            )
        }.sortedByDescending { it.potholes.size * 2 + it.highRiskCount * 3 }
    }

    private fun distanceInMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }
}
