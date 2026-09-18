package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
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
import com.example.ui.theme.SkeuoPurple
import com.example.ui.theme.SkeuoShadowDark
import com.example.ui.theme.SkeuoSurfaceElevated
import com.example.ui.theme.SkeuoTextPrimary
import com.example.ui.theme.SkeuoTextSecondary
import com.example.ui.theme.SkeuoWellInset
import com.example.ui.maps.RoomPotholeMapScreen
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class GisMapStyle(val label: String) {
    TACTICAL_BLUEPRINT("Blueprint"),
    RADAR_NIGHT("Radar Night"),
    TERRAIN_GIS("Terrain GIS"),
    GOOGLE_MAPS_ROOM("Google Maps (Room)")
}

@Composable
fun InteractivePotholeMapScreen(
    potholes: List<Pothole>,
    userLocation: UserLocation,
    focusCoordinates: Pair<Double, Double>? = null,
    onSelectPothole: (Pothole) -> Unit
) {
    var selectedSeverityFilter by remember { mutableStateOf<Severity?>(null) }
    var currentMapStyle by remember { mutableStateOf(GisMapStyle.TACTICAL_BLUEPRINT) }
    var showDensityRings by remember { mutableStateOf(true) }
    var isClusterHotspotsOpen by remember { mutableStateOf(true) }
    var activeInspectedPothole by remember { mutableStateOf<Pothole?>(null) }

    // Map Center and Scale state
    val defaultCenterLat = 19.5687
    val defaultCenterLng = 74.2112
    var centerLat by remember { mutableStateOf(defaultCenterLat) }
    var centerLng by remember { mutableStateOf(defaultCenterLng) }
    var zoomScale by remember { mutableFloatStateOf(1.2f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Radar pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "radar_anim")
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )
    val radarSweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    val filteredPotholes = remember(potholes, selectedSeverityFilter) {
        if (selectedSeverityFilter == null) potholes else potholes.filter { it.severity == selectedSeverityFilter }
    }

    val clusterAreas = remember(filteredPotholes) {
        PotholeClusterAnalyzer.identifyClusters(filteredPotholes)
    }

    LaunchedEffect(focusCoordinates) {
        focusCoordinates?.let { (lat, lng) ->
            centerLat = lat
            centerLng = lng
            panOffsetX = 0f
            panOffsetY = 0f
            zoomScale = 2.2f
            activeInspectedPothole = potholes.find { it.latitude == lat && it.longitude == lng }
        }
    }

    if (currentMapStyle == GisMapStyle.GOOGLE_MAPS_ROOM) {
        RoomPotholeMapScreen(
            modifier = Modifier.fillMaxSize(),
            initialLat = centerLat,
            initialLng = centerLng
        )
        return
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                when (currentMapStyle) {
                    GisMapStyle.TACTICAL_BLUEPRINT -> Color(0xFFF1F5F9)
                    GisMapStyle.RADAR_NIGHT -> Color(0xFF0A0F1D)
                    GisMapStyle.TERRAIN_GIS -> Color(0xFF1E293B)
                    GisMapStyle.GOOGLE_MAPS_ROOM -> Color(0xFF1E293B)
                }
            )
            .testTag("interactive_map_screen")
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val canvasCenter = Offset(widthPx / 2f + panOffsetX, heightPx / 2f + panOffsetY)

        // GIS coordinate converter: maps (lat, lng) to Canvas Pixel Offset
        fun projectLatLng(lat: Double, lng: Double): Offset {
            val latDelta = (lat - centerLat)
            val lngDelta = (lng - centerLng)
            // 1 degree lat ~ 111km, 1 degree lng ~ 104km at 19 deg N
            val meterPerDegLat = 111000.0
            val meterPerDegLng = 104500.0

            val xMeters = lngDelta * meterPerDegLng
            val yMeters = -latDelta * meterPerDegLat // Invert Y for screen coordinates

            val pixelsPerMeter = (0.28f * zoomScale)
            val px = canvasCenter.x + (xMeters * pixelsPerMeter).toFloat()
            val py = canvasCenter.y + (yMeters * pixelsPerMeter).toFloat()
            return Offset(px, py)
        }

        // Gesture handling for interactive Pan and Zoom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.5f, 5.0f)
                        panOffsetX += pan.x
                        panOffsetY += pan.y
                    }
                }
                .pointerInput(filteredPotholes, zoomScale, panOffsetX, panOffsetY) {
                    detectTapGestures { tapOffset ->
                        // Hit-test potholes within 32dp touch radius
                        var matchedPothole: Pothole? = null
                        var minDistance = Float.MAX_VALUE
                        for (pothole in filteredPotholes) {
                            val pos = projectLatLng(pothole.latitude, pothole.longitude)
                            val dist = sqrt((pos.x - tapOffset.x) * (pos.x - tapOffset.x) + (pos.y - tapOffset.y) * (pos.y - tapOffset.y))
                            if (dist < 50f && dist < minDistance) {
                                minDistance = dist
                                matchedPothole = pothole
                            }
                        }
                        if (matchedPothole != null) {
                            activeInspectedPothole = matchedPothole
                            onSelectPothole(matchedPothole)
                        } else {
                            activeInspectedPothole = null
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize().testTag("gis_vector_map_canvas")) {
                val isDark = currentMapStyle != GisMapStyle.TACTICAL_BLUEPRINT
                val gridColor = if (isDark) Color(0xFF1E293B).copy(alpha = 0.7f) else Color(0xFFCBD5E1).copy(alpha = 0.8f)
                val roadPrimary = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                val roadHighlight = if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1)

                // 1. Draw GIS Coordinate Grid Lines
                val gridSize = 80f * zoomScale
                val startX = (canvasCenter.x % gridSize) - gridSize
                val startY = (canvasCenter.y % gridSize) - gridSize

                var x = startX
                while (x < size.width + gridSize) {
                    drawLine(
                        color = gridColor,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                    x += gridSize
                }

                var y = startY
                while (y < size.height + gridSize) {
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                    y += gridSize
                }

                // 2. Draw Sangamner Arterial Road Network Schematic
                drawSchematicRoadNetwork(
                    project = ::projectLatLng,
                    roadColor = roadPrimary,
                    highlightColor = roadHighlight,
                    zoom = zoomScale,
                    isDark = isDark
                )

                // 3. Draw Radar concentric range rings & scanning beam (Radar Night mode)
                if (currentMapStyle == GisMapStyle.RADAR_NIGHT) {
                    val radarCenter = canvasCenter
                    val maxRadius = size.maxDimension * 0.8f

                    for (r in 1..4) {
                        val ringRadius = maxRadius * (r / 4f) * (zoomScale * 0.6f)
                        drawCircle(
                            color = SkeuoCyan.copy(alpha = 0.12f),
                            radius = ringRadius,
                            center = radarCenter,
                            style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
                        )
                    }

                    // Rotating radar beam
                    rotate(degrees = radarSweepAngle, pivot = radarCenter) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color.Transparent,
                                    SkeuoCyan.copy(alpha = 0.05f),
                                    SkeuoCyan.copy(alpha = 0.22f)
                                )
                            ),
                            startAngle = 0f,
                            sweepAngle = 45f,
                            useCenter = true,
                            topLeft = Offset(radarCenter.x - maxRadius, radarCenter.y - maxRadius),
                            size = Size(maxRadius * 2, maxRadius * 2)
                        )
                        drawLine(
                            color = SkeuoCyan.copy(alpha = 0.7f),
                            start = radarCenter,
                            end = Offset(radarCenter.x + maxRadius, radarCenter.y),
                            strokeWidth = 2f
                        )
                    }
                }

                // 4. Draw Cluster Density Heat Rings
                if (showDensityRings) {
                    clusterAreas.filter { it.potholes.size >= 2 }.forEach { area ->
                        val centerPos = projectLatLng(area.centerLatitude, area.centerLongitude)
                        val ringColor = when {
                            area.highRiskCount > 0 -> SkeuoCrimson
                            area.mediumRiskCount > 0 -> SkeuoAmber
                            else -> SkeuoPurple
                        }
                        val baseRadiusPx = (area.radiusMeters * 0.28f * zoomScale).toFloat().coerceIn(40f, 220f)
                        val pulsingRadius = baseRadiusPx * (1f + (radarPulse * 0.25f))

                        drawCircle(
                            color = ringColor.copy(alpha = 0.12f * (1f - radarPulse)),
                            radius = pulsingRadius,
                            center = centerPos
                        )
                        drawCircle(
                            color = ringColor.copy(alpha = 0.22f),
                            radius = baseRadiusPx,
                            center = centerPos
                        )
                        drawCircle(
                            color = ringColor.copy(alpha = 0.75f),
                            radius = baseRadiusPx,
                            center = centerPos,
                            style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)))
                        )
                    }
                }

                // 5. Draw User Location GPS Beacon
                if (userLocation.latitude != 0.0) {
                    val userPos = projectLatLng(userLocation.latitude, userLocation.longitude)
                    val userPulseRadius = 24f * zoomScale * (1f + (radarPulse * 0.4f))

                    drawCircle(
                        color = SkeuoCobalt.copy(alpha = 0.25f * (1f - radarPulse)),
                        radius = userPulseRadius,
                        center = userPos
                    )
                    drawCircle(
                        color = SkeuoCobalt,
                        radius = 8f * zoomScale.coerceIn(1f, 1.8f),
                        center = userPos
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4f * zoomScale.coerceIn(1f, 1.8f),
                        center = userPos
                    )
                }

                // 6. Draw Pothole Hazards Markers
                for (pothole in filteredPotholes) {
                    val pos = projectLatLng(pothole.latitude, pothole.longitude)
                    val isInspected = activeInspectedPothole?.potholeId == pothole.potholeId

                    val color = when (pothole.severity) {
                        Severity.HIGH -> SkeuoCrimson
                        Severity.MEDIUM -> SkeuoAmber
                        Severity.LOW -> SkeuoEmerald
                    }

                    val pinRadius = if (isInspected) 16f else 11f

                    // Glowing backdrop
                    drawCircle(
                        color = color.copy(alpha = if (isInspected) 0.45f else 0.25f),
                        radius = pinRadius * 1.8f,
                        center = pos
                    )

                    // Pin Outer ring
                    drawCircle(
                        color = if (isDark) Color(0xFF0F172A) else Color.White,
                        radius = pinRadius,
                        center = pos
                    )
                    drawCircle(
                        color = color,
                        radius = pinRadius,
                        center = pos,
                        style = Stroke(width = if (isInspected) 3.5f else 2.5f)
                    )

                    // Pin Inner Center
                    drawCircle(
                        color = color,
                        radius = pinRadius * 0.55f,
                        center = pos
                    )

                    // Selected Target Crosshair
                    if (isInspected) {
                        val chLen = 26f
                        drawLine(color = color, start = Offset(pos.x - chLen, pos.y), end = Offset(pos.x + chLen, pos.y), strokeWidth = 1.5f)
                        drawLine(color = color, start = Offset(pos.x, pos.y - chLen), end = Offset(pos.x, pos.y + chLen), strokeWidth = 1.5f)
                    }
                }
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
                                    text = "ROAD DEFECT RADAR GIS",
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
                                .tactilePress(pressedScale = 0.94f) {
                                    isClusterHotspotsOpen = !isClusterHotspotsOpen
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("toggle_hotspots_btn")
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
                                .tactilePress(pressedScale = 0.94f) {
                                    centerLat = area.centerLatitude
                                    centerLng = area.centerLongitude
                                    panOffsetX = 0f
                                    panOffsetY = 0f
                                    zoomScale = 2.4f
                                    activeInspectedPothole = area.potholes.firstOrNull()
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
            // Zoom In Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, SkeuoBorderLight, CircleShape)
                    .tactilePress(pressedScale = 0.92f) {
                        zoomScale = (zoomScale * 1.3f).coerceAtMost(5.0f)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Zoom In",
                    tint = SkeuoTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Zoom Out Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, SkeuoBorderLight, CircleShape)
                    .tactilePress(pressedScale = 0.92f) {
                        zoomScale = (zoomScale / 1.3f).coerceAtLeast(0.5f)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Zoom Out",
                    tint = SkeuoTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Reset Center / Overview Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, SkeuoBorderLight, CircleShape)
                    .tactilePress(pressedScale = 0.92f) {
                        centerLat = defaultCenterLat
                        centerLng = defaultCenterLng
                        panOffsetX = 0f
                        panOffsetY = 0f
                        zoomScale = 1.2f
                        activeInspectedPothole = null
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomOutMap,
                    contentDescription = "Reset View",
                    tint = SkeuoTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Re-center to User GPS Location
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, SkeuoBorderLight, CircleShape)
                    .tactilePress(pressedScale = 0.92f) {
                        if (userLocation.latitude != 0.0) {
                            centerLat = userLocation.latitude
                            centerLng = userLocation.longitude
                            panOffsetX = 0f
                            panOffsetY = 0f
                            zoomScale = 2.0f
                        } else {
                            centerLat = defaultCenterLat
                            centerLng = defaultCenterLng
                            panOffsetX = 0f
                            panOffsetY = 0f
                            zoomScale = 1.2f
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

            // GIS Style Switcher (Blueprint / Radar Night / Terrain)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, SkeuoBorderLight, CircleShape)
                    .tactilePress(pressedScale = 0.92f) {
                        currentMapStyle = when (currentMapStyle) {
                            GisMapStyle.TACTICAL_BLUEPRINT -> GisMapStyle.RADAR_NIGHT
                            GisMapStyle.RADAR_NIGHT -> GisMapStyle.TERRAIN_GIS
                            GisMapStyle.TERRAIN_GIS -> GisMapStyle.GOOGLE_MAPS_ROOM
                            GisMapStyle.GOOGLE_MAPS_ROOM -> GisMapStyle.TACTICAL_BLUEPRINT
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "Map Style: ${currentMapStyle.label}",
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
                    .tactilePress(pressedScale = 0.92f) {
                        showDensityRings = !showDensityRings
                    },
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

/**
 * Renders Sangamner schematic road network vectors onto the GIS canvas
 */
private fun DrawScope.drawSchematicRoadNetwork(
    project: (Double, Double) -> Offset,
    roadColor: Color,
    highlightColor: Color,
    zoom: Float,
    isDark: Boolean
) {
    // Key Sangamner Corridors coordinates
    val puneNashikHwy = listOf(
        Pair(19.5450, 74.2000),
        Pair(19.5580, 74.2060),
        Pair(19.5687, 74.2112),
        Pair(19.5800, 74.2180),
        Pair(19.5950, 74.2250)
    )

    val akoleBypass = listOf(
        Pair(19.5600, 74.1950),
        Pair(19.5650, 74.2040),
        Pair(19.5687, 74.2112),
        Pair(19.5720, 74.2240),
        Pair(19.5760, 74.2380)
    )

    val midcArterial = listOf(
        Pair(19.5520, 74.2180),
        Pair(19.5610, 74.2140),
        Pair(19.5687, 74.2112),
        Pair(19.5780, 74.2010)
    )

    fun drawCorridor(points: List<Pair<Double, Double>>, width: Float) {
        val path = Path()
        points.forEachIndexed { i, (lat, lng) ->
            val pt = project(lat, lng)
            if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
        }
        // Road casing
        drawPath(
            path = path,
            color = highlightColor,
            style = Stroke(width = width + (4f * zoom.coerceIn(0.8f, 2.5f)))
        )
        // Road surface
        drawPath(
            path = path,
            color = roadColor,
            style = Stroke(width = width)
        )
    }

    drawCorridor(puneNashikHwy, 14f * zoom.coerceIn(0.8f, 2.5f))
    drawCorridor(akoleBypass, 10f * zoom.coerceIn(0.8f, 2.5f))
    drawCorridor(midcArterial, 8f * zoom.coerceIn(0.8f, 2.5f))
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
            .tactilePress(pressedScale = 0.94f) { onClick() }
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
