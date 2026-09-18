package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DetectionEventEntity
import com.example.data.local.ManualReportEntity
import com.example.data.models.SyncStatus
import com.example.ui.components.SeverityBadge
import com.example.ui.theme.PhenomenonBorder
import com.example.ui.theme.PhenomenonBorderActive
import com.example.ui.theme.PhenomenonCanvas
import com.example.ui.theme.PhenomenonCrimson
import com.example.ui.theme.PhenomenonCyanElectric
import com.example.ui.theme.PhenomenonElectricLime
import com.example.ui.theme.PhenomenonEmerald
import com.example.ui.theme.PhenomenonFlameAmber
import com.example.ui.theme.PhenomenonPurpleNeon
import com.example.ui.theme.PhenomenonSurface
import com.example.ui.theme.PhenomenonSurfaceElevated
import com.example.ui.theme.PhenomenonTextPrimary
import com.example.ui.theme.PhenomenonTextSecondary
import com.example.ui.theme.PhenomenonTextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActivityHistoryScreen(
    detections: List<DetectionEventEntity>,
    reports: List<ManualReportEntity>,
    onSyncNow: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Auto Detections, 1: Manual Reports
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PhenomenonCanvas)
            .padding(horizontal = 16.dp)
            .testTag("activity_screen")
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Activity & History",
                    color = PhenomenonTextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    letterSpacing = (-0.3).sp
                )
                Text(
                    text = "EDGE LOGS // OFFLINE QUEUE",
                    color = PhenomenonTextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(PhenomenonSurface)
                    .border(1.dp, PhenomenonCyanElectric.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .clickable { onSyncNow() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Sync,
                        contentDescription = null,
                        tint = PhenomenonCyanElectric,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Sync Queue",
                        color = PhenomenonCyanElectric,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Phenomenon Capsule Tab Selector
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(PhenomenonSurface)
                .border(1.dp, PhenomenonBorder, RoundedCornerShape(14.dp))
                .padding(4.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 0) PhenomenonElectricLime else Color.Transparent)
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sensor Detections (${detections.size})",
                        fontWeight = FontWeight.Black,
                        color = if (selectedTab == 0) PhenomenonCanvas else PhenomenonTextSecondary,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 1) PhenomenonElectricLime else Color.Transparent)
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "My Reports (${reports.size})",
                        fontWeight = FontWeight.Black,
                        color = if (selectedTab == 1) PhenomenonCanvas else PhenomenonTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            if (detections.isEmpty()) {
                EmptyStateCard(message = "No sensor detections recorded yet. Start monitoring in the Cockpit to passively scan roads while driving.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(detections) { item ->
                        DetectionItemCard(item = item, dateFormat = dateFormat)
                    }
                }
            }
        } else {
            if (reports.isEmpty()) {
                EmptyStateCard(message = "No manual reports submitted yet. Use 'Report' to submit geolocated road hazards.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(reports) { item ->
                        ReportItemCard(item = item, dateFormat = dateFormat)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetectionItemCard(item: DetectionEventEntity, dateFormat: SimpleDateFormat) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PhenomenonSurface)
            .border(1.dp, PhenomenonBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PhenomenonElectricLime.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Sensors,
                        contentDescription = null,
                        tint = PhenomenonElectricLime,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = item.eventId,
                        fontWeight = FontWeight.Black,
                        color = PhenomenonTextPrimary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Impact: ${(item.impactScore * 100).toInt()}% • Z-DIFF: ${String.format(Locale.US, "%.2f", item.zDiffMax)}",
                        color = PhenomenonCyanElectric,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = dateFormat.format(Date(item.timestamp)),
                        color = PhenomenonTextTertiary,
                        fontSize = 10.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                SeverityBadge(severity = item.severity)
                Spacer(modifier = Modifier.height(6.dp))
                SyncStatusIndicator(status = item.syncStatus)
            }
        }
    }
}

@Composable
private fun ReportItemCard(item: ManualReportEntity, dateFormat: SimpleDateFormat) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PhenomenonSurface)
            .border(1.dp, PhenomenonBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.reportId,
                    fontWeight = FontWeight.Black,
                    color = PhenomenonTextPrimary,
                    fontSize = 13.sp
                )
                SeverityBadge(severity = item.severity)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.roadName,
                color = PhenomenonCyanElectric,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = item.description,
                color = PhenomenonTextSecondary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(Date(item.timestamp)),
                    color = PhenomenonTextTertiary,
                    fontSize = 10.sp
                )
                SyncStatusIndicator(status = item.syncStatus)
            }
        }
    }
}

@Composable
private fun SyncStatusIndicator(status: SyncStatus) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val (color, icon, text) = when (status) {
            SyncStatus.SYNCED -> Triple(PhenomenonEmerald, Icons.Default.CloudDone, "Synced")
            SyncStatus.PENDING -> Triple(PhenomenonFlameAmber, Icons.Default.Sync, "Pending")
            SyncStatus.FAILED -> Triple(PhenomenonCrimson, Icons.Default.SyncProblem, "Failed")
        }
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(11.dp))
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = text.uppercase(),
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PhenomenonSurface)
            .border(1.dp, PhenomenonBorder, RoundedCornerShape(16.dp))
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.History,
                contentDescription = null,
                tint = PhenomenonTextTertiary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = PhenomenonTextSecondary,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
