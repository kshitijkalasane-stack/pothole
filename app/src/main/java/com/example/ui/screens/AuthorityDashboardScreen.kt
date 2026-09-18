package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.data.models.Pothole
import com.example.data.models.PotholeStatus
import com.example.data.models.Severity
import com.example.ui.components.SeverityBadge
import com.example.ui.components.StatusBadge
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
fun AuthorityDashboardScreen(
    potholes: List<Pothole>,
    aiSummary: String?,
    isAiLoading: Boolean,
    onGenerateAiSummary: () -> Unit,
    onSelectPothole: (Pothole) -> Unit,
    onUpdatePotholeStatus: (potholeId: String, newStatus: PotholeStatus, crew: String?) -> Unit,
    onSignOut: () -> Unit
) {
    var selectedStatusFilter by remember { mutableStateOf<PotholeStatus?>(null) }
    var selectedSeverityFilter by remember { mutableStateOf<Severity?>(null) }
    var locationQuery by remember { mutableStateOf("") }
    var selectedWardFilter by remember { mutableStateOf<String?>(null) }

    // Distinct wards/areas extracted from addresses
    val knownLocations = remember(potholes) {
        listOf("All", "Sangamner City", "Akole Bypass", "Ghulewadi", "Pune Highway", "MIDC")
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

    val openCount = potholes.count { it.status == PotholeStatus.OPEN }
    val highPriorityCount = potholes.count { it.severity == Severity.HIGH && it.status != PotholeStatus.RESOLVED }
    val inRepairCount = potholes.count { it.status == PotholeStatus.IN_REPAIR || it.status == PotholeStatus.ASSIGNED }
    val resolvedCount = potholes.count { it.status == PotholeStatus.RESOLVED }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PhenomenonCanvas)
            .padding(horizontal = 16.dp)
            .testTag("authority_dashboard"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Phenomenon Header Bar with Signature Electric Capsule & Exit Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PhenomenonPurpleNeon.copy(alpha = 0.2f))
                                .border(1.dp, PhenomenonPurpleNeon.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Engineering,
                                contentDescription = null,
                                tint = PhenomenonPurpleNeon,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "COMMAND CENTER",
                            color = PhenomenonTextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = (-0.2).sp
                        )
                    }
                    Text(
                        text = "SANGAMNER PWD // WARD DISPATCH CONSOLE",
                        color = PhenomenonTextTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(PhenomenonSurfaceElevated)
                        .border(1.dp, PhenomenonBorder, RoundedCornerShape(20.dp))
                        .clickable { onSignOut() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = PhenomenonPurpleNeon,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Sign Out",
                            color = PhenomenonPurpleNeon,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // Operational Metrics Grid with Phenomenon Studio Minimal Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "New Cases",
                    count = openCount.toString(),
                    color = PhenomenonCrimson,
                    isSelected = selectedStatusFilter == PotholeStatus.OPEN,
                    modifier = Modifier.weight(1f)
                ) {
                    selectedStatusFilter = if (selectedStatusFilter == PotholeStatus.OPEN) null else PotholeStatus.OPEN
                }

                MetricCard(
                    title = "High Risk",
                    count = highPriorityCount.toString(),
                    color = PhenomenonFlameAmber,
                    isSelected = selectedSeverityFilter == Severity.HIGH,
                    modifier = Modifier.weight(1f)
                ) {
                    selectedSeverityFilter = if (selectedSeverityFilter == Severity.HIGH) null else Severity.HIGH
                }

                MetricCard(
                    title = "In Repair",
                    count = inRepairCount.toString(),
                    color = PhenomenonCyanElectric,
                    isSelected = selectedStatusFilter == PotholeStatus.IN_REPAIR,
                    modifier = Modifier.weight(1f)
                ) {
                    selectedStatusFilter = if (selectedStatusFilter == PotholeStatus.IN_REPAIR) null else PotholeStatus.IN_REPAIR
                }

                MetricCard(
                    title = "Resolved",
                    count = resolvedCount.toString(),
                    color = PhenomenonEmerald,
                    isSelected = selectedStatusFilter == PotholeStatus.RESOLVED,
                    modifier = Modifier.weight(1f)
                ) {
                    selectedStatusFilter = if (selectedStatusFilter == PotholeStatus.RESOLVED) null else PotholeStatus.RESOLVED
                }
            }
        }

        // Filter Controls Card: Priority & Location Search
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(PhenomenonSurface)
                    .border(1.dp, PhenomenonBorder, RoundedCornerShape(20.dp))
                    .padding(14.dp)
                    .testTag("authority_filter_panel")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = PhenomenonPurpleNeon,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "FILTER BY PRIORITY & LOCATION",
                                color = PhenomenonTextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                letterSpacing = 0.6.sp
                            )
                        }

                        if (selectedStatusFilter != null || selectedSeverityFilter != null || locationQuery.isNotBlank() || selectedWardFilter != null) {
                            Text(
                                text = "Reset All",
                                color = PhenomenonCrimson,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        selectedStatusFilter = null
                                        selectedSeverityFilter = null
                                        locationQuery = ""
                                        selectedWardFilter = null
                                    }
                                    .testTag("reset_filters_btn")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search by Road or Area
                    OutlinedTextField(
                        value = locationQuery,
                        onValueChange = { locationQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("authority_location_search_input"),
                        placeholder = {
                            Text(
                                "Search road, landmark, or case ID...",
                                color = PhenomenonTextTertiary,
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = PhenomenonCyanElectric,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (locationQuery.isNotEmpty()) {
                                IconButton(onClick = { locationQuery = "" }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = PhenomenonTextTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = PhenomenonSurfaceElevated,
                            unfocusedContainerColor = PhenomenonSurfaceElevated,
                            focusedBorderColor = PhenomenonPurpleNeon,
                            unfocusedBorderColor = PhenomenonBorder,
                            focusedTextColor = PhenomenonTextPrimary,
                            unfocusedTextColor = PhenomenonTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Priority Filter Pills
                    Text(
                        text = "PRIORITY / SEVERITY",
                        color = PhenomenonTextTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PriorityPill(
                            label = "All",
                            color = PhenomenonTextSecondary,
                            isSelected = selectedSeverityFilter == null,
                            onClick = { selectedSeverityFilter = null }
                        )
                        PriorityPill(
                            label = "High Priority",
                            color = PhenomenonCrimson,
                            isSelected = selectedSeverityFilter == Severity.HIGH,
                            onClick = {
                                selectedSeverityFilter = if (selectedSeverityFilter == Severity.HIGH) null else Severity.HIGH
                            }
                        )
                        PriorityPill(
                            label = "Medium",
                            color = PhenomenonFlameAmber,
                            isSelected = selectedSeverityFilter == Severity.MEDIUM,
                            onClick = {
                                selectedSeverityFilter = if (selectedSeverityFilter == Severity.MEDIUM) null else Severity.MEDIUM
                            }
                        )
                        PriorityPill(
                            label = "Low",
                            color = PhenomenonCyanElectric,
                            isSelected = selectedSeverityFilter == Severity.LOW,
                            onClick = {
                                selectedSeverityFilter = if (selectedSeverityFilter == Severity.LOW) null else Severity.LOW
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Location Ward Quick Badges
                    Text(
                        text = "MUNICIPAL CORRIDORS",
                        color = PhenomenonTextTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
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
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) PhenomenonPurpleNeon.copy(alpha = 0.2f) else PhenomenonSurfaceElevated)
                                    .border(
                                        1.dp,
                                        if (isSelected) PhenomenonPurpleNeon else PhenomenonBorder,
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        selectedWardFilter = if (ward == "All" || selectedWardFilter == ward) null else ward
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = if (isSelected) PhenomenonPurpleNeon else PhenomenonTextTertiary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = ward,
                                        color = if (isSelected) PhenomenonTextPrimary else PhenomenonTextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Gemini AI Smart Area Briefing Card with Cyber Violet Accents
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(PhenomenonSurface)
                    .border(1.dp, PhenomenonPurpleNeon.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
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
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(PhenomenonPurpleNeon.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = PhenomenonPurpleNeon,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GEMINI MUNICIPAL BRIEFING",
                                color = PhenomenonPurpleNeon,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                letterSpacing = 0.8.sp
                            )
                        }

                        Button(
                            onClick = onGenerateAiSummary,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PhenomenonPurpleNeon,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isAiLoading
                        ) {
                            if (isAiLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "Generate Briefing",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = aiSummary ?: "Tap 'Generate Briefing' to create an AI-orchestrated operational summary of open cases, severity distribution, and recommended crew allocations for the Sangamner municipal grid.",
                        color = PhenomenonTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Work Queue Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reported Potholes (${filteredPotholes.size})",
                    color = PhenomenonTextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    letterSpacing = (-0.2).sp
                )
                if (selectedStatusFilter != null || selectedSeverityFilter != null || locationQuery.isNotBlank() || selectedWardFilter != null) {
                    Text(
                        text = "Filters Active",
                        color = PhenomenonCyanElectric,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (filteredPotholes.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PhenomenonSurface)
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = PhenomenonTextTertiary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No potholes match current filter criteria",
                            color = PhenomenonTextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try clearing search or toggling priority filters",
                            color = PhenomenonTextTertiary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        } else {
            items(filteredPotholes, key = { it.potholeId }) { pothole ->
                AuthorityPotholeCard(
                    pothole = pothole,
                    onClick = { onSelectPothole(pothole) },
                    onQuickUpdateStatus = { newStatus ->
                        val crew = if (newStatus == PotholeStatus.ASSIGNED) "Ward 4 Quick-Patch Squad" else null
                        onUpdatePotholeStatus(pothole.potholeId, newStatus, crew)
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PriorityPill(
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) color.copy(alpha = 0.2f) else PhenomenonSurfaceElevated)
            .border(
                1.dp,
                if (isSelected) color else PhenomenonBorder,
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) color else PhenomenonTextSecondary,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold
        )
    }
}

@Composable
private fun AuthorityPotholeCard(
    pothole: Pothole,
    onClick: () -> Unit,
    onQuickUpdateStatus: (PotholeStatus) -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(PhenomenonSurface)
            .border(1.dp, PhenomenonBorder, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(16.dp)
            .testTag("authority_pothole_card_${pothole.potholeId}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = pothole.potholeId,
                        fontWeight = FontWeight.Black,
                        color = PhenomenonTextPrimary,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                }
                SeverityBadge(severity = pothole.severity)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = pothole.address,
                color = PhenomenonTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            if (pothole.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = pothole.notes,
                    color = PhenomenonTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 2,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(status = pothole.status)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = PhenomenonCyanElectric,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "%.4f, %.4f".format(pothole.latitude, pothole.longitude),
                        color = PhenomenonTextTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Quick Status Actions directly on the card
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (pothole.status != PotholeStatus.ASSIGNED && pothole.status != PotholeStatus.RESOLVED) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PhenomenonPurpleNeon.copy(alpha = 0.15f))
                            .border(1.dp, PhenomenonPurpleNeon.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable { onQuickUpdateStatus(PotholeStatus.ASSIGNED) }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Engineering,
                                contentDescription = null,
                                tint = PhenomenonPurpleNeon,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Assign Crew",
                                color = PhenomenonPurpleNeon,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (pothole.status != PotholeStatus.IN_REPAIR && pothole.status != PotholeStatus.RESOLVED) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PhenomenonCyanElectric.copy(alpha = 0.15f))
                            .border(1.dp, PhenomenonCyanElectric.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable { onQuickUpdateStatus(PotholeStatus.IN_REPAIR) }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "In Repair",
                            color = PhenomenonCyanElectric,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (pothole.status != PotholeStatus.RESOLVED) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PhenomenonEmerald.copy(alpha = 0.15f))
                            .border(1.dp, PhenomenonEmerald.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable { onQuickUpdateStatus(PotholeStatus.RESOLVED) }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = PhenomenonEmerald,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Resolve",
                                color = PhenomenonEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(PhenomenonSurfaceElevated)
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "COMPLETED // REOPEN CASE",
                            color = PhenomenonTextTertiary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onQuickUpdateStatus(PotholeStatus.OPEN) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    count: String,
    color: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(PhenomenonSurface)
            .border(
                1.5.dp,
                if (isSelected) color else PhenomenonBorder,
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = count,
                color = color,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = title.uppercase(),
                color = PhenomenonTextTertiary,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
