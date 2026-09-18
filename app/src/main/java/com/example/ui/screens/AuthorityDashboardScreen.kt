package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.Pothole
import com.example.data.models.PotholeStatus
import com.example.data.models.Severity
import com.example.ui.components.SeverityBadge
import com.example.ui.components.SkeuomorphicButton
import com.example.ui.components.SkeuomorphicLed
import com.example.ui.components.StatusBadge
import com.example.ui.components.skeuomorphicCard
import com.example.ui.components.skeuomorphicInset
import com.example.ui.components.tactilePress
import com.example.ui.maps.PotholeClusterAnalyzer
import com.example.ui.theme.SkeuoAmber
import com.example.ui.theme.SkeuoBorderLight
import com.example.ui.theme.SkeuoCanvas
import com.example.ui.theme.SkeuoCobalt
import com.example.ui.theme.SkeuoCrimson
import com.example.ui.theme.SkeuoCyan
import com.example.ui.theme.SkeuoEmerald
import com.example.ui.theme.SkeuoHighlight
import com.example.ui.theme.SkeuoHighlightSoft
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorityDashboardScreen(
    potholes: List<Pothole>,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    aiSummary: String?,
    isAiLoading: Boolean,
    onGenerateAiSummary: () -> Unit,
    onSelectPothole: (Pothole) -> Unit,
    onUpdatePotholeStatus: (potholeId: String, newStatus: PotholeStatus, crew: String?) -> Unit,
    onViewClusterMap: ((lat: Double?, lng: Double?) -> Unit)? = null,
    onSignOut: () -> Unit
) {
    var selectedStatusFilter by remember { mutableStateOf<PotholeStatus?>(null) }
    var selectedSeverityFilter by remember { mutableStateOf<Severity?>(null) }
    var locationQuery by remember { mutableStateOf("") }
    var selectedWardFilter by remember { mutableStateOf<String?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }

    val knownLocations = remember(potholes) {
        listOf("All", "Sangamner City", "Akole Bypass", "Ghulewadi", "Pune Highway", "MIDC")
    }

    val clusterAreas = remember(potholes) {
        PotholeClusterAnalyzer.identifyClusters(potholes)
    }

    val filteredPotholes = remember(potholes, selectedStatusFilter, selectedSeverityFilter, locationQuery, selectedWardFilter) {
        potholes.filter { item ->
            val matchesStatus = selectedStatusFilter == null || item.status == selectedStatusFilter
            val matchesSeverity = selectedSeverityFilter == null || item.severity == selectedSeverityFilter
            val matchesLocationQuery = locationQuery.isBlank() ||
                    item.address.contains(locationQuery, ignoreCase = true) ||
                    item.notes.contains(locationQuery, ignoreCase = true) ||
                    item.potholeId.contains(locationQuery, ignoreCase = true)
            val matchesWard = selectedWardFilter == null || selectedWardFilter == "All" ||
                    item.address.contains(selectedWardFilter!!, ignoreCase = true)

            matchesStatus && matchesSeverity && matchesLocationQuery && matchesWard
        }
    }

    val openCount = remember(potholes) { potholes.count { it.status == PotholeStatus.OPEN } }
    val highPriorityCount = remember(potholes) { potholes.count { it.severity == Severity.HIGH } }
    val inRepairCount = remember(potholes) { potholes.count { it.status == PotholeStatus.IN_REPAIR || it.status == PotholeStatus.ASSIGNED } }
    val resolvedCount = remember(potholes) { potholes.count { it.status == PotholeStatus.RESOLVED } }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier
            .fillMaxSize()
            .background(SkeuoCanvas)
            .testTag("authority_pull_to_refresh_box")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Skeuomorphic Authority Console Header
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .skeuomorphicCard(cornerRadius = 20.dp, elevation = 5.dp)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SkeuomorphicLed(isOn = true, color = SkeuoPurple)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PWD MUNICIPAL DESK",
                                    color = SkeuoPurple,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Authority Console",
                                color = SkeuoTextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                letterSpacing = (-0.3).sp
                            )
                            Text(
                                text = "Sangamner Municipal Corporation // Road Works",
                                color = SkeuoTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Manual Refresh Button
                            Box(
                                modifier = Modifier
                                    .shadow(2.dp, RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SkeuoSurfaceElevated)
                                    .border(1.dp, SkeuoBorderLight, RoundedCornerShape(12.dp))
                                    .clickable { onRefresh() }
                                    .padding(horizontal = 9.dp, vertical = 7.dp)
                                    .testTag("authority_sync_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isRefreshing) {
                                        CircularProgressIndicator(
                                            color = SkeuoCobalt,
                                            modifier = Modifier.size(13.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Sync,
                                            contentDescription = "Sync Cloud Reports",
                                            tint = SkeuoCobalt,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isRefreshing) "SYNCING..." else "SYNC",
                                        color = SkeuoCobalt,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            // Exit / Sign Out Button
                            Box(
                                modifier = Modifier
                                    .shadow(2.dp, RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SkeuoSurfaceElevated)
                                    .border(1.dp, SkeuoBorderLight, RoundedCornerShape(12.dp))
                                    .clickable { onSignOut() }
                                    .padding(horizontal = 9.dp, vertical = 7.dp)
                                    .testTag("authority_sign_out_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                        contentDescription = "Sign Out",
                                        tint = SkeuoCrimson,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "LOGOUT",
                                        color = SkeuoCrimson,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }

        // Skeuomorphic Metric Cards Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeuoMetricCard(
                    title = "New Cases",
                    count = openCount.toString(),
                    color = SkeuoCrimson,
                    isSelected = selectedStatusFilter == PotholeStatus.OPEN,
                    modifier = Modifier.weight(1f)
                ) {
                    selectedStatusFilter = if (selectedStatusFilter == PotholeStatus.OPEN) null else PotholeStatus.OPEN
                }

                SkeuoMetricCard(
                    title = "High Risk",
                    count = highPriorityCount.toString(),
                    color = SkeuoAmber,
                    isSelected = selectedSeverityFilter == Severity.HIGH,
                    modifier = Modifier.weight(1f)
                ) {
                    selectedSeverityFilter = if (selectedSeverityFilter == Severity.HIGH) null else Severity.HIGH
                }

                SkeuoMetricCard(
                    title = "In Repair",
                    count = inRepairCount.toString(),
                    color = SkeuoCobalt,
                    isSelected = selectedStatusFilter == PotholeStatus.IN_REPAIR,
                    modifier = Modifier.weight(1f)
                ) {
                    selectedStatusFilter = if (selectedStatusFilter == PotholeStatus.IN_REPAIR) null else PotholeStatus.IN_REPAIR
                }

                SkeuoMetricCard(
                    title = "Resolved",
                    count = resolvedCount.toString(),
                    color = SkeuoEmerald,
                    isSelected = selectedStatusFilter == PotholeStatus.RESOLVED,
                    modifier = Modifier.weight(1f)
                ) {
                    selectedStatusFilter = if (selectedStatusFilter == PotholeStatus.RESOLVED) null else PotholeStatus.RESOLVED
                }
            }
        }

        // Google Maps Cluster Identification Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .skeuomorphicCard(cornerRadius = 18.dp, elevation = 5.dp)
                    .padding(14.dp)
                    .testTag("authority_cluster_map_banner")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(SkeuoCobalt.copy(alpha = 0.15f))
                                    .border(1.dp, SkeuoCobalt, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    tint = SkeuoCobalt,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "GOOGLE MAPS SDK CLUSTERS",
                                    color = SkeuoCobalt,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "${clusterAreas.size} Active Hotspot Zones Identified",
                                    color = SkeuoTextPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        // Launch Map View Button
                        if (onViewClusterMap != null) {
                            SkeuomorphicButton(
                                onClick = { onViewClusterMap(null, null) },
                                backgroundColor = SkeuoCobalt,
                                cornerRadius = 12.dp,
                                tag = "open_cluster_map_btn"
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "OPEN MAP",
                                        color = SkeuoTextInverse,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = null,
                                        tint = SkeuoTextInverse,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (clusterAreas.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            clusterAreas.forEach { cluster ->
                                Box(
                                    modifier = Modifier
                                        .tactilePress(pressedScale = 0.95f, pressedTranslationY = 1.dp) {
                                            onViewClusterMap?.invoke(cluster.centerLatitude, cluster.centerLongitude)
                                        }
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SkeuoWellInset)
                                        .border(1.dp, SkeuoBorderLight, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(if (cluster.highRiskCount > 0) SkeuoCrimson else SkeuoAmber)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${cluster.name} (${cluster.potholes.size})",
                                            color = SkeuoTextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Gemini AI Smart Area Briefing Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .skeuomorphicCard(cornerRadius = 18.dp, elevation = 4.dp)
                    .padding(14.dp)
                    .testTag("ai_summary_card")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SkeuoPurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI ROAD DEFECT SYNTHESIS",
                                color = SkeuoPurple,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                letterSpacing = 0.8.sp
                            )
                        }

                        if (!isAiLoading) {
                            Box(
                                modifier = Modifier
                                    .shadow(2.dp, RoundedCornerShape(10.dp))
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SkeuoPurple)
                                    .clickable { onGenerateAiSummary() }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                    .testTag("refresh_ai_briefing_btn")
                            ) {
                                Text(
                                    text = if (aiSummary == null) "GENERATE" else "UPDATE",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .skeuomorphicInset(cornerRadius = 12.dp)
                            .padding(12.dp)
                    ) {
                        if (isAiLoading) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = SkeuoPurple,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Analyzing municipal sensor telemetry with Gemini AI...",
                                    color = SkeuoTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else if (aiSummary != null) {
                            Text(
                                text = aiSummary,
                                color = SkeuoTextPrimary,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                text = "Tap 'Generate' to synthesize cluster hotspots, priority road triage, and dispatch recommendations.",
                                color = SkeuoTextTertiary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Search & Filter Panel
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .skeuomorphicCard(cornerRadius = 18.dp, elevation = 4.dp)
                    .padding(14.dp)
            ) {
                Column {
                    OutlinedTextField(
                        value = locationQuery,
                        onValueChange = { locationQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("authority_location_search_input"),
                        placeholder = {
                            Text(
                                "Search road, landmark, or case ID...",
                                color = SkeuoTextTertiary,
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = SkeuoCobalt,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (locationQuery.isNotEmpty()) {
                                IconButton(onClick = { locationQuery = "" }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = SkeuoTextTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SkeuoWellInset,
                            unfocusedContainerColor = SkeuoWellInset,
                            focusedBorderColor = SkeuoCobalt,
                            unfocusedBorderColor = SkeuoBorderLight,
                            focusedTextColor = SkeuoTextPrimary,
                            unfocusedTextColor = SkeuoTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Location Ward Quick Badges
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        knownLocations.forEach { ward ->
                            val isSelected = (selectedWardFilter == null && ward == "All") || selectedWardFilter == ward
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) SkeuoCobalt else SkeuoWellInset)
                                    .border(1.dp, if (isSelected) SkeuoCobalt else SkeuoBorderLight, RoundedCornerShape(14.dp))
                                    .clickable {
                                        selectedWardFilter = if (ward == "All" || selectedWardFilter == ward) null else ward
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = ward,
                                    color = if (isSelected) Color.White else SkeuoTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section Title: Incident Dispatch Queue
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Engineering,
                        contentDescription = null,
                        tint = SkeuoCobalt,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "INCIDENT DISPATCH QUEUE",
                        color = SkeuoTextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            color = SkeuoCobalt,
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Syncing...",
                            color = SkeuoCobalt,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "${filteredPotholes.size} records • Pull down to sync",
                            color = SkeuoTextTertiary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        items(filteredPotholes, key = { it.potholeId }) { pothole ->
            SkeuoAuthorityPotholeCard(
                pothole = pothole,
                onSelect = { onSelectPothole(pothole) },
                onUpdateStatus = { newStatus, crew ->
                    onUpdatePotholeStatus(pothole.potholeId, newStatus, crew)
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(84.dp))
        }
    }

    // Floating Action Button for CSV Export
    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 18.dp, bottom = 22.dp)
            .tactilePress(pressedScale = 0.92f, pressedTranslationY = 2.dp) {
                showExportDialog = true
            }
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = SkeuoCobalt.copy(alpha = 0.35f),
                spotColor = SkeuoShadowDark.copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SkeuoCobalt,
                        Color(0xFF1E3A8A)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        SkeuoHighlightSoft,
                        Color(0xFF60A5FA),
                        Color(0xFF1E3A8A)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("export_csv_fab")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FileDownload,
                contentDescription = "Export CSV Report",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "EXPORT CSV (${filteredPotholes.size})",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                letterSpacing = 0.4.sp
            )
        }
    }
    }

    if (showExportDialog) {
        ExportCsvDialog(
            potholes = filteredPotholes,
            selectedStatusFilter = selectedStatusFilter,
            selectedSeverityFilter = selectedSeverityFilter,
            selectedWardFilter = selectedWardFilter,
            onDismiss = { showExportDialog = false }
        )
    }
}

@Composable
private fun SkeuoMetricCard(
    title: String,
    count: String,
    color: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .tactilePress(pressedScale = 0.94f, pressedTranslationY = 2.dp) { onClick() }
            .shadow(
                elevation = if (isSelected) 1.dp else 4.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = color.copy(alpha = 0.25f)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    if (isSelected) {
                        listOf(color.copy(alpha = 0.15f), color.copy(alpha = 0.25f))
                    } else {
                        listOf(Color.White, Color(0xFFF6F9FD))
                    }
                )
            )
            .border(
                1.2.dp,
                if (isSelected) color else SkeuoBorderLight,
                RoundedCornerShape(14.dp)
            )
            .padding(10.dp)
            .testTag("metric_${title.lowercase().replace(' ', '_')}"),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count,
                color = if (isSelected) color else SkeuoTextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
            Text(
                text = title,
                color = SkeuoTextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SkeuoAuthorityPotholeCard(
    pothole: Pothole,
    onSelect: () -> Unit,
    onUpdateStatus: (PotholeStatus, String?) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .tactilePress(pressedScale = 0.975f, pressedTranslationY = 1.5.dp) { onSelect() }
            .skeuomorphicCard(cornerRadius = 16.dp, elevation = 4.dp)
            .padding(14.dp)
            .testTag("authority_pothole_${pothole.potholeId}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SkeuoWellInset)
                            .border(1.dp, SkeuoBorderLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = when (pothole.severity) {
                                Severity.HIGH -> SkeuoCrimson
                                Severity.MEDIUM -> SkeuoAmber
                                Severity.LOW -> SkeuoEmerald
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = pothole.potholeId,
                            color = SkeuoTextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                        Text(
                            text = pothole.address,
                            color = SkeuoTextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
                SeverityBadge(severity = pothole.severity)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(status = pothole.status)

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (pothole.status == PotholeStatus.OPEN || pothole.status == PotholeStatus.UNDER_VERIFICATION) {
                        Box(
                            modifier = Modifier
                                .tactilePress(pressedScale = 0.92f, pressedTranslationY = 1.dp) {
                                    onUpdateStatus(PotholeStatus.ASSIGNED, "Sangamner PWD Squad #1")
                                }
                                .shadow(2.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(SkeuoPurple)
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "ASSIGN SQUAD",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    } else if (pothole.status == PotholeStatus.ASSIGNED) {
                        Box(
                            modifier = Modifier
                                .tactilePress(pressedScale = 0.92f, pressedTranslationY = 1.dp) {
                                    onUpdateStatus(PotholeStatus.IN_REPAIR, pothole.assignedTo)
                                }
                                .shadow(2.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(SkeuoCobalt)
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "START REPAIR",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    } else if (pothole.status == PotholeStatus.IN_REPAIR) {
                        Box(
                            modifier = Modifier
                                .tactilePress(pressedScale = 0.92f, pressedTranslationY = 1.dp) {
                                    onUpdateStatus(PotholeStatus.RESOLVED, pothole.assignedTo)
                                }
                                .shadow(2.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(SkeuoEmerald)
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "RESOLVE",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Generates a clean, standard CSV payload from the filtered list of potholes.
 */
private fun generatePotholesCsv(potholes: List<Pothole>): String {
    val header = "Pothole_ID,Severity,Status,Latitude,Longitude,Address,Confidence,Reports_Count,Assigned_Squad,Created_At,Notes"
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    val rows = potholes.map { p ->
        val dateStr = try {
            dateFormat.format(Date(p.firstDetectedAt))
        } catch (e: Exception) {
            ""
        }
        val safeNotes = p.notes.replace("\"", "\"\"").replace("\n", " ")
        val safeAddress = p.address.replace("\"", "\"\"").replace("\n", " ")
        val squad = (p.assignedTo ?: "Unassigned").replace("\"", "\"\"")

        "\"${p.potholeId}\",\"${p.severity.name}\",\"${p.status.name}\",${p.latitude},${p.longitude},\"$safeAddress\",${p.confidence},${p.reportCount},\"$squad\",\"$dateStr\",\"$safeNotes\""
    }
    return (listOf(header) + rows).joinToString("\n")
}

@Composable
private fun ExportCsvDialog(
    potholes: List<Pothole>,
    selectedStatusFilter: PotholeStatus?,
    selectedSeverityFilter: Severity?,
    selectedWardFilter: String?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val csvContent = remember(potholes) { generatePotholesCsv(potholes) }
    val timestamp = remember { SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date()) }
    val filename = "sangamner_potholes_report_$timestamp.csv"

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SkeuoSurfaceElevated)
                .border(1.2.dp, SkeuoBorderLight, RoundedCornerShape(20.dp))
                .shadow(12.dp, RoundedCornerShape(20.dp))
                .padding(20.dp)
                .testTag("export_csv_dialog")
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(SkeuoCobalt.copy(alpha = 0.15f))
                                .border(1.dp, SkeuoCobalt.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = SkeuoCobalt,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Export CSV Report",
                                color = SkeuoTextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Filtered Dataset Export",
                                color = SkeuoTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = SkeuoTextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Inset metadata chamber
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .skeuomorphicInset(cornerRadius = 12.dp)
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Filtered Count:",
                                color = SkeuoTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${potholes.size} records",
                                color = SkeuoTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Status Filter:",
                                color = SkeuoTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = selectedStatusFilter?.label ?: "All Statuses",
                                color = SkeuoCobalt,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Severity Filter:",
                                color = SkeuoTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = selectedSeverityFilter?.label ?: "All Severities",
                                color = SkeuoAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Ward / Zone:",
                                color = SkeuoTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = selectedWardFilter ?: "All",
                                color = SkeuoTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Target Filename:",
                                color = SkeuoTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = filename,
                                color = SkeuoTextTertiary,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Columns Included label
                Text(
                    text = "COLUMNS INCLUDED",
                    color = SkeuoTextTertiary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "ID, Severity, Status, Latitude, Longitude, Address, Confidence, Report Count, Assigned Squad, Created At, Notes",
                    color = SkeuoTextSecondary,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Copy to Clipboard Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .tactilePress(pressedScale = 0.94f, pressedTranslationY = 1.5.dp) {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Potholes CSV", csvContent)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "CSV copied to clipboard (${potholes.size} records)", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }
                            .shadow(2.dp, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(SkeuoWellInset)
                            .border(1.dp, SkeuoBorderLight, RoundedCornerShape(12.dp))
                            .padding(vertical = 10.dp)
                            .testTag("copy_csv_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = SkeuoTextPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Copy CSV",
                                color = SkeuoTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Share CSV Report Intent Button
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .tactilePress(pressedScale = 0.94f, pressedTranslationY = 1.5.dp) {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Sangamner Municipal Potholes Report ($filename)")
                                putExtra(Intent.EXTRA_TEXT, csvContent)
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Export Potholes CSV Report")
                            context.startActivity(shareIntent)
                            Toast.makeText(context, "Exporting ${potholes.size} potholes report...", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                            .shadow(3.dp, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(SkeuoCobalt, Color(0xFF1E3A8A))
                                )
                            )
                            .border(1.dp, SkeuoHighlightSoft, RoundedCornerShape(12.dp))
                            .padding(vertical = 10.dp)
                            .testTag("share_csv_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Share Report",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
