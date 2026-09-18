package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Pothole
import com.example.data.models.Severity
import com.example.services.location.UserLocation
import com.example.ui.components.PotholeItemCard
import com.example.ui.components.SkeuomorphicLed
import com.example.ui.components.skeuomorphicCard
import com.example.ui.components.skeuomorphicInset
import com.example.ui.maps.PotholeClusterAnalyzer
import com.example.ui.maps.PotholeClusterArea
import com.example.ui.maps.PotholeClusterItem
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
import com.example.ui.theme.SkeuoTextPrimary
import com.example.ui.theme.SkeuoTextSecondary
import com.example.ui.theme.SkeuoTextTertiary
import com.example.ui.theme.SkeuoWellInset
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

private const val LIGHT_MAP_STYLE_JSON = """
[
  {"elementType": "geometry", "stylers": [{"color": "#f1f5f9"}]},
  {"elementType": "labels.text.fill", "stylers": [{"color": "#475569"}]},
  {"elementType": "labels.text.stroke", "stylers": [{"color": "#ffffff"}]},
  {"featureType": "administrative.locality", "elementType": "labels.text.fill", "stylers": [{"color": "#2563eb"}]},
  {"featureType": "poi", "elementType": "labels.text.fill", "stylers": [{"color": "#64748b"}]},
  {"featureType": "poi.park", "elementType": "geometry", "stylers": [{"color": "#dcfce7"}]},
  {"featureType": "road", "elementType": "geometry", "stylers": [{"color": "#ffffff"}]},
  {"featureType": "road", "elementType": "geometry.stroke", "stylers": [{"color": "#e2e8f0"}]},
  {"featureType": "road.highway", "elementType": "geometry", "stylers": [{"color": "#cbd5e1"}]},
  {"featureType": "road.highway", "elementType": "geometry.stroke", "stylers": [{"color": "#94a3b8"}]},
  {"featureType": "road.highway", "elementType": "labels.text.fill", "stylers": [{"color": "#1e293b"}]},
  {"featureType": "transit", "elementType": "geometry", "stylers": [{"color": "#e2e8f0"}]},
  {"featureType": "water", "elementType": "geometry", "stylers": [{"color": "#bae6fd"}]},
  {"featureType": "water", "elementType": "labels.text.fill", "stylers": [{"color": "#0284c7"}]}
]
"""

@Composable
fun InteractivePotholeMapScreen(
    potholes: List<Pothole>,
    userLocation: UserLocation,
    focusCoordinates: Pair<Double, Double>? = null,
    onSelectPothole: (Pothole) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedSeverityFilter by remember { mutableStateOf<Severity?>(null) }
    var currentMapType by remember { mutableStateOf(MapType.NORMAL) }
    var showDensityRings by remember { mutableStateOf(true) }
    var isClusterHotspotsOpen by remember { mutableStateOf(true) }
    var activeInspectedPothole by remember { mutableStateOf<Pothole?>(null) }

    val filteredPotholes = remember(potholes, selectedSeverityFilter) {
        if (selectedSeverityFilter == null) potholes else potholes.filter { it.severity == selectedSeverityFilter }
    }

    val clusterItems = remember(filteredPotholes) {
        filteredPotholes.map { PotholeClusterItem(it) }
    }

    val clusterAreas = remember(filteredPotholes) {
        PotholeClusterAnalyzer.identifyClusters(filteredPotholes)
    }

    val sangamnerCenter = LatLng(19.5687, 74.2112)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(sangamnerCenter, 13.5f)
    }

    LaunchedEffect(focusCoordinates) {
        focusCoordinates?.let { (lat, lng) ->
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 15.5f)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SkeuoCanvas)
            .testTag("interactive_map_screen")
    ) {
        val mapProperties = remember(currentMapType) {
            MapProperties(
                mapStyleOptions = if (currentMapType == MapType.NORMAL) MapStyleOptions(LIGHT_MAP_STYLE_JSON) else null,
                mapType = currentMapType,
                isMyLocationEnabled = false
            )
        }
        val mapUiSettings = remember {
            MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                compassEnabled = true,
                mapToolbarEnabled = false
            )
        }

        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .testTag("google_map_container"),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings
        ) {
            if (showDensityRings) {
                clusterAreas.filter { it.potholes.size >= 2 }.forEach { area ->
                    val ringColor = when {
                        area.highRiskCount > 0 -> SkeuoCrimson
                        area.mediumRiskCount > 0 -> SkeuoAmber
                        else -> SkeuoPurple
                    }
                    Circle(
                        center = LatLng(area.centerLatitude, area.centerLongitude),
                        radius = 260.0,
                        fillColor = ringColor.copy(alpha = 0.20f),
                        strokeColor = ringColor.copy(alpha = 0.85f),
                        strokeWidth = 3f
                    )
                }
            }

            Clustering(
                items = clusterItems,
                onClusterClick = { cluster ->
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(cluster.position.latitude, cluster.position.longitude),
                                cameraPositionState.position.zoom + 2.5f
                            )
                        )
                    }
                    true
                },
                onClusterItemClick = { item ->
                    activeInspectedPothole = item.pothole
                    onSelectPothole(item.pothole)
                    true
                },
                clusterContent = { cluster ->
                    val hasHighRisk = cluster.items.any { it.pothole.severity == Severity.HIGH }
                    val hasMediumRisk = cluster.items.any { it.pothole.severity == Severity.MEDIUM }
                    val badgeColor = when {
                        hasHighRisk -> SkeuoCrimson
                        hasMediumRisk -> SkeuoAmber
                        else -> SkeuoCobalt
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .shadow(6.dp, CircleShape, ambientColor = badgeColor.copy(alpha = 0.5f))
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.White,
                                        badgeColor,
                                        badgeColor.copy(alpha = 0.9f)
                                    )
                                )
                            )
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${cluster.size}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                },
                clusterItemContent = { item ->
                    val markerColor = when (item.pothole.severity) {
                        Severity.HIGH -> SkeuoCrimson
                        Severity.MEDIUM -> SkeuoAmber
                        Severity.LOW -> SkeuoEmerald
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .shadow(4.dp, CircleShape, ambientColor = markerColor.copy(alpha = 0.4f))
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, markerColor, CircleShape)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = markerColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )

            // User Location Marker
            if (userLocation.latitude != 0.0) {
                Circle(
                    center = LatLng(userLocation.latitude, userLocation.longitude),
                    radius = 20.0,
                    fillColor = SkeuoCobalt.copy(alpha = 0.35f),
                    strokeColor = SkeuoCobalt,
                    strokeWidth = 3f
                )
            }
        }

        // Top Floating Skeuomorphic Controls Header
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .skeuomorphicCard(cornerRadius = 18.dp, elevation = 6.dp)
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SkeuomorphicLed(isOn = true, color = SkeuoCobalt)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "ROAD DEFECT RADAR",
                                    color = SkeuoCobalt,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "Sangamner Sector Live Grid",
                                    color = SkeuoTextPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        // Hotspots toggle badge
                        Box(
                            modifier = Modifier
                                .shadow(2.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isClusterHotspotsOpen) SkeuoCobalt else SkeuoWellInset)
                                .clickable { isClusterHotspotsOpen = !isClusterHotspotsOpen }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CrisisAlert,
                                    contentDescription = null,
                                    tint = if (isClusterHotspotsOpen) Color.White else SkeuoTextSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${clusterAreas.size} ZONES",
                                    color = if (isClusterHotspotsOpen) Color.White else SkeuoTextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    // Priority Filter Strip
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SkeuoFilterPill(
                            label = "All (${potholes.size})",
                            color = SkeuoTextSecondary,
                            isSelected = selectedSeverityFilter == null,
                            onClick = { selectedSeverityFilter = null },
                            modifier = Modifier.weight(1f)
                        )
                        SkeuoFilterPill(
                            label = "High",
                            color = SkeuoCrimson,
                            isSelected = selectedSeverityFilter == Severity.HIGH,
                            onClick = { selectedSeverityFilter = if (selectedSeverityFilter == Severity.HIGH) null else Severity.HIGH },
                            modifier = Modifier.weight(1f)
                        )
                        SkeuoFilterPill(
                            label = "Medium",
                            color = SkeuoAmber,
                            isSelected = selectedSeverityFilter == Severity.MEDIUM,
                            onClick = { selectedSeverityFilter = if (selectedSeverityFilter == Severity.MEDIUM) null else Severity.MEDIUM },
                            modifier = Modifier.weight(1f)
                        )
                        SkeuoFilterPill(
                            label = "Low",
                            color = SkeuoEmerald,
                            isSelected = selectedSeverityFilter == Severity.LOW,
                            onClick = { selectedSeverityFilter = if (selectedSeverityFilter == Severity.LOW) null else Severity.LOW },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Expandable Cluster Hotspot Quick Jump Bar
            AnimatedVisibility(
                visible = isClusterHotspotsOpen && clusterAreas.isNotEmpty(),
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    clusterAreas.forEach { area ->
                        val badgeColor = when {
                            area.highRiskCount > 0 -> SkeuoCrimson
                            area.mediumRiskCount > 0 -> SkeuoAmber
                            else -> SkeuoCobalt
                        }

                        Box(
                            modifier = Modifier
                                .shadow(3.dp, RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White)
                                .border(1.dp, SkeuoBorderLight, RoundedCornerShape(14.dp))
                                .clickable {
                                    coroutineScope.launch {
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(
                                                LatLng(area.centerLatitude, area.centerLongitude),
                                                15.5f
                                            )
                                        )
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(badgeColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${area.name} (${area.potholes.size})",
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

        // Floating Action Tool Buttons (Right Edge)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Re-center to User Location
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, SkeuoBorderLight, CircleShape)
                    .clickable {
                        if (userLocation.latitude != 0.0) {
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(userLocation.latitude, userLocation.longitude),
                                        15f
                                    )
                                )
                            }
                        } else {
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(sangamnerCenter, 13.5f)
                                )
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "My Location",
                    tint = SkeuoCobalt,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Map Type Toggle (Normal / Hybrid / Satellite)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, SkeuoBorderLight, CircleShape)
                    .clickable {
                        currentMapType = when (currentMapType) {
                            MapType.NORMAL -> MapType.HYBRID
                            MapType.HYBRID -> MapType.TERRAIN
                            else -> MapType.NORMAL
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "Map Style",
                    tint = SkeuoTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Density Heat Rings Toggle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(if (showDensityRings) SkeuoCobalt else Color.White)
                    .border(1.dp, SkeuoBorderLight, CircleShape)
                    .clickable { showDensityRings = !showDensityRings },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Radio,
                    contentDescription = "Toggle Heat Rings",
                    tint = if (showDensityRings) Color.White else SkeuoTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Bottom Selected Pothole Card
        if (activeInspectedPothole != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                PotholeItemCard(
                    pothole = activeInspectedPothole!!,
                    onClick = { onSelectPothole(activeInspectedPothole!!) }
                )
            }
        }
    }
}

@Composable
private fun SkeuoFilterPill(
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = if (isSelected) 1.dp else 2.dp,
                shape = RoundedCornerShape(10.dp),
                ambientColor = color.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.verticalGradient(
                    if (isSelected) {
                        listOf(color.copy(alpha = 0.2f), color.copy(alpha = 0.3f))
                    } else {
                        listOf(Color.White, SkeuoWellInset)
                    }
                )
            )
            .border(
                1.dp,
                if (isSelected) color else SkeuoBorderLight,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) color else SkeuoTextSecondary,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
        )
    }
}
