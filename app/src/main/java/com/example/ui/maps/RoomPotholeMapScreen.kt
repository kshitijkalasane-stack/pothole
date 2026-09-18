package com.example.ui.maps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.local.AppDatabase
import com.example.data.local.PotholeEntity
import com.example.data.models.PotholeStatus
import com.example.data.models.Severity
import com.example.ui.components.SeverityBadge
import com.example.ui.components.StatusBadge
import com.example.ui.components.skeuomorphicCard
import com.example.ui.components.skeuomorphicInset
import com.example.ui.theme.SkeuoAmber
import com.example.ui.theme.SkeuoBorderLight
import com.example.ui.theme.SkeuoCanvas
import com.example.ui.theme.SkeuoCobalt
import com.example.ui.theme.SkeuoCrimson
import com.example.ui.theme.SkeuoEmerald
import com.example.ui.theme.SkeuoHighlight
import com.example.ui.theme.SkeuoPurple
import com.example.ui.theme.SkeuoTextPrimary
import com.example.ui.theme.SkeuoTextSecondary
import com.example.ui.theme.SkeuoTextTertiary
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

/**
 * Composable screen that displays a Google Map using maps-compose
 * and overlays markers dynamically from Room Database.
 */
@Composable
fun RoomPotholeMapScreen(
    modifier: Modifier = Modifier,
    initialLat: Double = 19.5687,
    initialLng: Double = 74.2112,
    onSelectPothole: ((PotholeEntity) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getInstance(context) }

    // Observe Room Database Entities directly
    val roomPotholes by db.potholeDao().observeAllPotholes().collectAsState(initial = emptyList())

    // Camera position state
    val sangamnerCenter = LatLng(initialLat, initialLng)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(sangamnerCenter, 14f)
    }

    var selectedMapType by remember { mutableStateOf(MapType.NORMAL) }
    var selectedPothole by remember { mutableStateOf<PotholeEntity?>(null) }
    var selectedSeverityFilter by remember { mutableStateOf<Severity?>(null) }

    val filteredRoomPotholes = remember(roomPotholes, selectedSeverityFilter) {
        if (selectedSeverityFilter == null) roomPotholes
        else roomPotholes.filter { it.severity == selectedSeverityFilter }
    }

    val mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = false,
                compassEnabled = true,
                myLocationButtonEnabled = false
            )
        )
    }

    val mapProperties by remember(selectedMapType) {
        mutableStateOf(
            MapProperties(
                mapType = selectedMapType,
                isMyLocationEnabled = false
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SkeuoCanvas)
            .testTag("room_pothole_google_map_screen")
    ) {
        // Google Map Composable
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings,
            onMapClick = { selectedPothole = null }
        ) {
            filteredRoomPotholes.forEach { pothole ->
                val position = LatLng(pothole.latitude, pothole.longitude)
                val markerHue = when (pothole.severity) {
                    Severity.HIGH -> BitmapDescriptorFactory.HUE_RED
                    Severity.MEDIUM -> BitmapDescriptorFactory.HUE_ORANGE
                    Severity.LOW -> BitmapDescriptorFactory.HUE_GREEN
                }

                Marker(
                    state = MarkerState(position = position),
                    title = pothole.potholeId,
                    snippet = "${pothole.severity.label} Severity - ${pothole.status.label}",
                    icon = BitmapDescriptorFactory.defaultMarker(markerHue),
                    onClick = {
                        selectedPothole = pothole
                        onSelectPothole?.invoke(pothole)
                        false
                    }
                )
            }
        }

        // Top Control Overlay Panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter)
        ) {
            // Header Stats Plate
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .skeuomorphicCard(cornerRadius = 18.dp, elevation = 6.dp)
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
                                .background(SkeuoCobalt.copy(alpha = 0.15f))
                                .border(1.dp, SkeuoCobalt.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = SkeuoCobalt,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ROOM DB MAP OVERLAY",
                                color = SkeuoTextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${filteredRoomPotholes.size} local markers cached",
                                color = SkeuoTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Recenter Button
                    Box(
                        modifier = Modifier
                            .shadow(3.dp, CircleShape)
                            .clip(CircleShape)
                            .background(SkeuoCobalt)
                            .clickable {
                                coroutineScope.launch {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(sangamnerCenter, 14f)
                                    )
                                }
                            }
                            .padding(8.dp)
                            .testTag("recenter_map_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Recenter Map",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Map Style Selector Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MapTypePill("Standard", selectedMapType == MapType.NORMAL) { selectedMapType = MapType.NORMAL }
                MapTypePill("Satellite", selectedMapType == MapType.SATELLITE) { selectedMapType = MapType.SATELLITE }
                MapTypePill("Terrain", selectedMapType == MapType.TERRAIN) { selectedMapType = MapType.TERRAIN }
                MapTypePill("Hybrid", selectedMapType == MapType.HYBRID) { selectedMapType = MapType.HYBRID }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Severity Filter Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SeverityFilterPill("ALL", selectedSeverityFilter == null, SkeuoCobalt) { selectedSeverityFilter = null }
                SeverityFilterPill("HIGH", selectedSeverityFilter == Severity.HIGH, SkeuoCrimson) { selectedSeverityFilter = Severity.HIGH }
                SeverityFilterPill("MEDIUM", selectedSeverityFilter == Severity.MEDIUM, SkeuoAmber) { selectedSeverityFilter = Severity.MEDIUM }
                SeverityFilterPill("LOW", selectedSeverityFilter == Severity.LOW, SkeuoEmerald) { selectedSeverityFilter = Severity.LOW }
            }
        }

        // Bottom Selected Pothole Detail Modal Card
        AnimatedVisibility(
            visible = selectedPothole != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            selectedPothole?.let { pothole ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .skeuomorphicCard(cornerRadius = 20.dp, elevation = 8.dp)
                        .padding(16.dp)
                        .testTag("selected_pothole_card")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pothole.potholeId,
                                    color = SkeuoTextPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = pothole.address,
                                    color = SkeuoTextSecondary,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                            IconButton(onClick = { selectedPothole = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = SkeuoTextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .skeuomorphicInset(cornerRadius = 12.dp)
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SeverityBadge(severity = pothole.severity)
                                StatusBadge(status = pothole.status)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Lat: %.4f, Lng: %.4f".format(pothole.latitude, pothole.longitude),
                                color = SkeuoTextTertiary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${pothole.reportCount} reports",
                                color = SkeuoCobalt,
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

@Composable
private fun MapTypePill(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .shadow(if (selected) 3.dp else 1.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) SkeuoCobalt else Color.White)
            .border(1.dp, if (selected) SkeuoCobalt else SkeuoBorderLight, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else SkeuoTextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SeverityFilterPill(label: String, selected: Boolean, activeColor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .shadow(if (selected) 2.dp else 1.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) activeColor else Color.White)
            .border(1.dp, if (selected) activeColor else SkeuoBorderLight, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else activeColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black
        )
    }
}
