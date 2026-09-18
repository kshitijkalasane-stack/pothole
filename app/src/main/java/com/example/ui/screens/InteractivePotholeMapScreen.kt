package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Pothole
import com.example.data.models.PotholeStatus
import com.example.data.models.Severity
import com.example.services.location.UserLocation
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

@Composable
fun InteractivePotholeMapScreen(
    potholes: List<Pothole>,
    userLocation: UserLocation,
    onSelectPothole: (Pothole) -> Unit
) {
    var selectedSeverityFilter by remember { mutableStateOf<Severity?>(null) }
    val filteredPotholes = remember(potholes, selectedSeverityFilter) {
        if (selectedSeverityFilter == null) potholes else potholes.filter { it.severity == selectedSeverityFilter }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PhenomenonCanvas)
            .testTag("interactive_map_screen")
    ) {
        // Custom Styled Geospatial Radar Vector Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(filteredPotholes) {
                    detectTapGestures { tapOffset ->
                        val w = size.width
                        val h = size.height
                        val tapped = filteredPotholes.minByOrNull { p ->
                            val px = w * 0.5f + ((p.longitude - 74.2112) * 25000f).toFloat()
                            val py = h * 0.5f - ((p.latitude - 19.5687) * 25000f).toFloat()
                            val distSq = (tapOffset.x - px) * (tapOffset.x - px) + (tapOffset.y - py) * (tapOffset.y - py)
                            distSq
                        }
                        if (tapped != null) {
                            onSelectPothole(tapped)
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // Phenomenon Studio Radar Grid Lines
            val gridColor = Color(0xFF131720)
            val roadMajor = Color(0xFF1E2533)
            val roadNationalHighway = Color(0xFF2B3346)

            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            for (y in 0 until height.toInt() step 64) {
                drawLine(gridColor, Offset(0f, y.toFloat()), Offset(width, y.toFloat()), strokeWidth = 1f, pathEffect = dashEffect)
            }
            for (x in 0 until width.toInt() step 64) {
                drawLine(gridColor, Offset(x.toFloat(), 0f), Offset(x.toFloat(), height), strokeWidth = 1f, pathEffect = dashEffect)
            }

            // Highway NH-60 Pune-Nashik diagonal corridor
            drawLine(
                color = roadNationalHighway,
                start = Offset(width * 0.1f, height * 0.9f),
                end = Offset(width * 0.9f, height * 0.1f),
                strokeWidth = 18f
            )
            // Highway Center Line
            drawLine(
                color = PhenomenonElectricLime.copy(alpha = 0.5f),
                start = Offset(width * 0.1f, height * 0.9f),
                end = Offset(width * 0.9f, height * 0.1f),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
            )

            // Bypass & College Arterial roads
            drawLine(
                color = roadMajor,
                start = Offset(0f, height * 0.45f),
                end = Offset(width, height * 0.48f),
                strokeWidth = 12f
            )
            drawLine(
                color = roadMajor,
                start = Offset(width * 0.45f, 0f),
                end = Offset(width * 0.52f, height),
                strokeWidth = 10f
            )

            // Current User GPS Marker with Radar Ping Ring
            val userPx = width * 0.5f + ((userLocation.longitude - 74.2112) * 25000f).toFloat()
            val userPy = height * 0.5f - ((userLocation.latitude - 19.5687) * 25000f).toFloat()

            drawCircle(color = PhenomenonCyanElectric.copy(alpha = 0.15f), radius = 36f, center = Offset(userPx, userPy))
            drawCircle(color = PhenomenonCyanElectric.copy(alpha = 0.35f), radius = 20f, center = Offset(userPx, userPy))
            drawCircle(color = PhenomenonCyanElectric, radius = 9f, center = Offset(userPx, userPy))
            drawCircle(color = Color.White, radius = 4f, center = Offset(userPx, userPy))

            // Draw Pothole Markers with Phenomenon Signature Accents
            filteredPotholes.forEach { p ->
                val px = width * 0.5f + ((p.longitude - 74.2112) * 25000f).toFloat()
                val py = height * 0.5f - ((p.latitude - 19.5687) * 25000f).toFloat()

                val markerColor = when {
                    p.status == PotholeStatus.RESOLVED -> Color(0xFF64748B)
                    p.severity == Severity.HIGH -> PhenomenonCrimson
                    p.severity == Severity.MEDIUM -> PhenomenonFlameAmber
                    else -> PhenomenonEmerald
                }

                drawCircle(color = markerColor.copy(alpha = 0.25f), radius = 22f, center = Offset(px, py))
                drawCircle(color = markerColor, radius = 8f, center = Offset(px, py))
                drawCircle(color = PhenomenonCanvas, radius = 3.5f, center = Offset(px, py))
            }
        }

        // Top Filter Bar Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(PhenomenonSurface.copy(alpha = 0.92f))
                    .border(1.dp, PhenomenonBorder, RoundedCornerShape(18.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Sangamner Sector Map",
                            color = PhenomenonTextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Showing ${filteredPotholes.size} geolocated road points",
                            color = PhenomenonTextTertiary,
                            fontSize = 11.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterPill(
                            label = "ALL",
                            isSelected = selectedSeverityFilter == null,
                            color = PhenomenonElectricLime
                        ) { selectedSeverityFilter = null }

                        FilterPill(
                            label = "HIGH",
                            isSelected = selectedSeverityFilter == Severity.HIGH,
                            color = PhenomenonCrimson
                        ) { selectedSeverityFilter = Severity.HIGH }

                        FilterPill(
                            label = "MED",
                            isSelected = selectedSeverityFilter == Severity.MEDIUM,
                            color = PhenomenonFlameAmber
                        ) { selectedSeverityFilter = Severity.MEDIUM }
                    }
                }
            }
        }

        // Bottom Map Legend with Phenomenon Styling
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PhenomenonSurface.copy(alpha = 0.92f))
                    .border(1.dp, PhenomenonBorder, RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = PhenomenonCrimson, text = "High Risk")
                    LegendItem(color = PhenomenonFlameAmber, text = "Medium")
                    LegendItem(color = PhenomenonEmerald, text = "Low")
                    LegendItem(color = PhenomenonCyanElectric, text = "Current GPS")
                }
            }
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) color else PhenomenonCanvas)
            .border(1.dp, if (isSelected) color else PhenomenonBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) PhenomenonCanvas else PhenomenonTextSecondary,
            fontWeight = FontWeight.Black,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = text, color = PhenomenonTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}
