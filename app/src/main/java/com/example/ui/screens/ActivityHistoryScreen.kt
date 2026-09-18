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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.DetectionEventEntity
import com.example.data.local.ManualReportEntity
import com.example.data.models.Pothole
import com.example.data.models.SyncStatus
import com.example.ui.components.PotholeItemCard
import com.example.ui.components.SeverityBadge
import com.example.ui.components.SkeuomorphicButton
import com.example.ui.components.SkeuomorphicLed
import com.example.ui.components.skeuomorphicCard
import com.example.ui.components.skeuomorphicInset
import com.example.ui.components.tactilePress
import com.example.ui.theme.SkeuoAmber
import com.example.ui.theme.SkeuoBorderLight
import com.example.ui.theme.SkeuoCanvas
import com.example.ui.theme.SkeuoCobalt
import com.example.ui.theme.SkeuoCrimson
import com.example.ui.theme.SkeuoEmerald
import com.example.ui.theme.SkeuoHighlight
import com.example.ui.theme.SkeuoPurple
import com.example.ui.theme.SkeuoShadowDark
import com.example.ui.theme.SkeuoSurface
import com.example.ui.theme.SkeuoSurfaceElevated
import com.example.ui.theme.SkeuoTextInverse
import com.example.ui.theme.SkeuoTextPrimary
import com.example.ui.theme.SkeuoTextSecondary
import com.example.ui.theme.SkeuoTextTertiary
import com.example.ui.theme.SkeuoWellInset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActivityHistoryScreen(
    communityHazards: List<Pothole> = emptyList(),
    detections: List<DetectionEventEntity>,
    reports: List<ManualReportEntity>,
    onSelectPothole: (Pothole) -> Unit = {},
    onSyncNow: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    val pendingCount = remember(detections, reports) {
        detections.count { it.syncStatus == SyncStatus.PENDING } + reports.count { it.syncStatus == SyncStatus.PENDING }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkeuoCanvas)
            .padding(horizontal = 16.dp)
            .testTag("activity_screen")
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Skeuomorphic Header Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .skeuomorphicCard(cornerRadius = 18.dp, elevation = 4.dp)
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SkeuomorphicLed(isOn = true, color = if (pendingCount > 0) SkeuoAmber else SkeuoEmerald)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "COMMUNITY & EDGE LEDGER",
                            color = SkeuoCobalt,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp
                        )
                    }
                    Text(
                        text = "Recent Activity",
                        color = SkeuoTextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "${communityHazards.size} Community • ${detections.size} Edge • ${reports.size} Reports",
                        color = SkeuoTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Sync Action Button
                SkeuomorphicButton(
                    onClick = onSyncNow,
                    backgroundColor = if (pendingCount > 0) SkeuoAmber else SkeuoCobalt,
                    cornerRadius = 12.dp,
                    tag = "sync_now_btn"
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (pendingCount > 0) "SYNC ($pendingCount)" else "SYNCED",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Skeuomorphic Segmented Switch (3 Tabs)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .skeuomorphicInset(cornerRadius = 14.dp)
                .padding(4.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Tab 0: Community Hazards
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .tactilePress(pressedScale = 0.94f, pressedTranslationY = 1.dp) { selectedTab = 0 }
                        .shadow(
                            elevation = if (selectedTab == 0) 3.dp else 0.dp,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 0) Color.White else Color.Transparent)
                        .padding(vertical = 8.dp)
                        .testTag("tab_community_hazards"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Community (${communityHazards.size})",
                        color = if (selectedTab == 0) SkeuoCrimson else SkeuoTextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                // Tab 1: Sensor Detections
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .tactilePress(pressedScale = 0.94f, pressedTranslationY = 1.dp) { selectedTab = 1 }
                        .shadow(
                            elevation = if (selectedTab == 1) 3.dp else 0.dp,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 1) Color.White else Color.Transparent)
                        .padding(vertical = 8.dp)
                        .testTag("tab_sensor_detections"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Edge (${detections.size})",
                        color = if (selectedTab == 1) SkeuoCobalt else SkeuoTextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                // Tab 2: Manual Reports
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .tactilePress(pressedScale = 0.94f, pressedTranslationY = 1.dp) { selectedTab = 2 }
                        .shadow(
                            elevation = if (selectedTab == 2) 3.dp else 0.dp,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 2) Color.White else Color.Transparent)
                        .padding(vertical = 8.dp)
                        .testTag("tab_manual_reports"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Reports (${reports.size})",
                        color = if (selectedTab == 2) SkeuoCobalt else SkeuoTextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // History Ledger Content Views
        when (selectedTab) {
            0 -> {
                // Community Detected Hazards Tab
                if (communityHazards.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = SkeuoTextTertiary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No community hazards logged yet.",
                                color = SkeuoTextTertiary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("community_hazards_list"),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            // Information / Status Banner
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SkeuoCobalt.copy(alpha = 0.08f))
                                    .border(1.dp, SkeuoCobalt.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = SkeuoCobalt,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Aggregated road hazards detected across Sangamner community sensors & citizen reports. Tap to inspect defect details.",
                                        color = SkeuoTextSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }

                        items(communityHazards, key = { it.potholeId }) { pothole ->
                            PotholeItemCard(
                                pothole = pothole,
                                onClick = { onSelectPothole(pothole) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
            1 -> {
                // Sensor Edge Detections Tab
                if (detections.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = null,
                                tint = SkeuoTextTertiary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No edge anomalies logged yet. Start monitoring to record road impacts.",
                                color = SkeuoTextTertiary,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("sensor_detections_list"),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(detections, key = { it.eventId }) { item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .tactilePress(pressedScale = 0.98f, pressedTranslationY = 1.dp) {}
                                    .skeuomorphicCard(cornerRadius = 14.dp, elevation = 3.dp)
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(SkeuoWellInset),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Sensors,
                                                contentDescription = null,
                                                tint = SkeuoCobalt,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "ΔG Peak: %.2f G (Speed: %.0f km/h)".format(item.zDiffMax, item.speedKmh),
                                                color = SkeuoTextPrimary,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = "${dateFormat.format(Date(item.timestamp))} • ${item.reportedByName}",
                                                color = SkeuoTextTertiary,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        SeverityBadge(severity = item.severity)
                                        Icon(
                                            imageVector = if (item.syncStatus == SyncStatus.SYNCED) Icons.Default.CloudDone else Icons.Default.SyncProblem,
                                            contentDescription = null,
                                            tint = if (item.syncStatus == SyncStatus.SYNCED) SkeuoEmerald else SkeuoAmber,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
            2 -> {
                // Manual Citizen Reports Tab
                if (reports.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ReportProblem,
                                contentDescription = null,
                                tint = SkeuoTextTertiary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No manual citizen reports filed yet.",
                                color = SkeuoTextTertiary,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("manual_reports_list"),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(reports, key = { it.reportId }) { report ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .tactilePress(pressedScale = 0.98f, pressedTranslationY = 1.dp) {}
                                    .skeuomorphicCard(cornerRadius = 14.dp, elevation = 3.dp)
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = report.roadName,
                                            color = SkeuoTextPrimary,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp
                                        )
                                        SeverityBadge(severity = report.severity)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = report.description,
                                        color = SkeuoTextSecondary,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Reported by: ${report.reportedByName} (${report.userId.take(12)})",
                                        color = SkeuoCobalt,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp
                                    )

                                    if (!report.photoUri.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(130.dp)
                                                .skeuomorphicInset(cornerRadius = 10.dp)
                                                .padding(3.dp)
                                        ) {
                                            AsyncImage(
                                                model = report.photoUri,
                                                contentDescription = "Report photo evidence",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(8.dp))
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .padding(6.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color.Black.copy(alpha = 0.65f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.PhotoCamera,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "PHOTO ATTACHED",
                                                        color = Color.White,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = dateFormat.format(Date(report.timestamp)),
                                            color = SkeuoTextTertiary,
                                            fontSize = 10.sp
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (report.syncStatus == SyncStatus.SYNCED) Icons.Default.CloudDone else Icons.Default.SyncProblem,
                                                contentDescription = null,
                                                tint = if (report.syncStatus == SyncStatus.SYNCED) SkeuoEmerald else SkeuoAmber,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (report.syncStatus == SyncStatus.SYNCED) "Synced" else "Offline Queue",
                                                color = if (report.syncStatus == SyncStatus.SYNCED) SkeuoEmerald else SkeuoAmber,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

