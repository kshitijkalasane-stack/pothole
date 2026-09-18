package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.models.MonitoringState
import com.example.data.models.Pothole
import com.example.data.models.Severity
import com.example.data.models.UserRole
import com.example.services.location.UserLocation
import com.example.services.sensor.LiveSensorTelemetry
import com.example.ui.components.PotholeItemCard
import com.example.ui.theme.PhenomenonBorder
import com.example.ui.theme.PhenomenonBorderActive
import com.example.ui.theme.PhenomenonCanvas
import com.example.ui.theme.PhenomenonCrimson
import com.example.ui.theme.PhenomenonCyanElectric
import com.example.ui.theme.PhenomenonElectricLime
import com.example.ui.theme.PhenomenonEmerald
import com.example.ui.theme.PhenomenonPurpleNeon
import com.example.ui.theme.PhenomenonSurface
import com.example.ui.theme.PhenomenonSurfaceElevated
import com.example.ui.theme.PhenomenonSurfaceHover
import com.example.ui.theme.PhenomenonTextPrimary
import com.example.ui.theme.PhenomenonTextSecondary
import com.example.ui.theme.PhenomenonTextTertiary

@Composable
fun CitizenHomeScreen(
    monitoringState: MonitoringState,
    userLocation: UserLocation,
    telemetry: LiveSensorTelemetry,
    nearbyPotholes: List<Pothole>,
    onStartMonitoring: () -> Unit,
    onStopMonitoring: () -> Unit,
    onSimulateBump: () -> Unit,
    onQuickReport: () -> Unit,
    onSyncNow: () -> Unit,
    onSelectPothole: (Pothole) -> Unit,
    onSignOut: () -> Unit
) {
    val isMonitoring = monitoringState == MonitoringState.MONITORING || monitoringState == MonitoringState.IMPACT_DETECTED

    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionsToRequest = remember {
        val list = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        list.toTypedArray()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        val granted = fineGranted || coarseGranted
        hasLocationPermission = granted
        if (granted) {
            onStartMonitoring()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PhenomenonCanvas)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Phenomenon Signature Hero Header with Glass Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(PhenomenonSurface)
                    .border(1.dp, PhenomenonBorderActive, RoundedCornerShape(24.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(145.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_road_banner),
                        contentDescription = "Smart Road Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        PhenomenonCanvas.copy(alpha = 0.85f),
                                        PhenomenonCanvas
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(PhenomenonElectricLime)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "COMMUTER COCKPIT",
                                        color = PhenomenonElectricLime,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Smart Pothole System",
                                    color = PhenomenonTextPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    letterSpacing = (-0.3).sp
                                )
                                Text(
                                    text = "Sangamner Highway Corridor // MH-17",
                                    color = PhenomenonCyanElectric,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            // Phenomenon Capsule Sign Out Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(PhenomenonSurfaceElevated)
                                    .border(1.dp, PhenomenonBorder, RoundedCornerShape(20.dp))
                                    .clickable { onSignOut() }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                        contentDescription = "Sign Out",
                                        tint = PhenomenonElectricLime,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Sign Out",
                                        color = PhenomenonElectricLime,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Phenomenon Permission Request Card if not yet granted
        if (!hasLocationPermission) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(PhenomenonSurfaceElevated)
                        .border(1.dp, PhenomenonElectricLime.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                        .padding(18.dp)
                        .testTag("location_permission_card")
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PhenomenonElectricLime.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location Access",
                                    tint = PhenomenonElectricLime,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "LOCATION PERMISSION REQUIRED",
                                    color = PhenomenonElectricLime,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "Google Play Services Location Tracking",
                                    color = PhenomenonTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Precise GPS and background service permissions are required to continuously record coordinates and detect road potholes even when driving with the screen off.",
                            color = PhenomenonTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { permissionLauncher.launch(permissionsToRequest) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("grant_location_permission_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PhenomenonElectricLime,
                                contentColor = PhenomenonCanvas
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GRANT LOCATION PERMISSIONS",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        // Phenomenon Studio Core Monitoring HUD Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(PhenomenonSurface)
                    .border(
                        1.dp,
                        if (isMonitoring) PhenomenonElectricLime.copy(alpha = 0.5f) else PhenomenonBorder,
                        RoundedCornerShape(24.dp)
                    )
                    .testTag("monitoring_card")
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(if (isMonitoring) PhenomenonElectricLime else PhenomenonTextTertiary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isMonitoring) "RADAR SCANNING ACTIVE" else "MONITORING STANDBY",
                                color = if (isMonitoring) PhenomenonElectricLime else PhenomenonTextSecondary,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                        }

                        // Phenomenon GPS Badge Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(PhenomenonCanvas)
                                .border(1.dp, PhenomenonBorder, RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = null,
                                tint = if (userLocation.hasGpsFix) PhenomenonCyanElectric else PhenomenonTextTertiary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "%.1f km/h".format(userLocation.speedKmh),
                                color = PhenomenonTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Background Service Active Beacon
                    if (isMonitoring) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(PhenomenonCyanElectric.copy(alpha = 0.10f))
                                .border(1.dp, PhenomenonCyanElectric.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(PhenomenonCyanElectric)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PLAY SERVICES BACKGROUND LOCATION SERVICE ACTIVE",
                                color = PhenomenonCyanElectric,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Phenomenon Minimalist Circular Power Hub
                    Box(
                        modifier = Modifier
                            .size(136.dp)
                            .scale(if (isMonitoring) pulseScale else 1.0f)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = if (isMonitoring) {
                                        listOf(PhenomenonCrimson, Color(0xFFB91C1C))
                                    } else {
                                        listOf(PhenomenonElectricLime, PhenomenonCyanElectric)
                                    }
                                )
                            )
                            .clickable {
                                if (isMonitoring) {
                                    onStopMonitoring()
                                } else {
                                    if (!hasLocationPermission) {
                                        permissionLauncher.launch(permissionsToRequest)
                                    } else {
                                        onStartMonitoring()
                                    }
                                }
                            }
                            .testTag("start_stop_monitoring_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (isMonitoring) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (isMonitoring) "Stop" else "Start",
                                tint = if (isMonitoring) Color.White else PhenomenonCanvas,
                                modifier = Modifier.size(46.dp)
                            )
                            Text(
                                text = if (isMonitoring) "TERMINATE" else "ACTIVATE",
                                color = if (isMonitoring) Color.White else PhenomenonCanvas,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = if (isMonitoring)
                            "Processing 50Hz IMU samples via local Z-variance thresholds"
                        else
                            "Activate passive background hazard detection while driving",
                        color = PhenomenonTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )

                    // Phenomenon Live Edge Telemetry Strip
                    if (isMonitoring) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(PhenomenonCanvas)
                                .border(1.dp, PhenomenonBorder, RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("LINEAR Z", color = PhenomenonTextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                Text("%.2f".format(telemetry.az - 9.8f), color = PhenomenonTextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Z-DIFF", color = PhenomenonTextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                Text("%.2f".format(telemetry.zDiff), color = PhenomenonElectricLime, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("GYRO PITCH", color = PhenomenonTextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                Text("%.2f".format(telemetry.gyroZ), color = PhenomenonCyanElectric, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // Quick Controls Row with Phenomenon Elevated Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Test Simulate Bump
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PhenomenonSurface)
                        .border(1.dp, PhenomenonBorder, RoundedCornerShape(16.dp))
                        .clickable { onSimulateBump() }
                        .padding(14.dp)
                        .testTag("simulate_bump_btn")
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PhenomenonElectricLime.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = "Simulate",
                                tint = PhenomenonElectricLime,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Simulate", color = PhenomenonTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("IMU Impulse", color = PhenomenonTextTertiary, fontSize = 9.sp)
                    }
                }

                // Quick Manual Report
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PhenomenonSurface)
                        .border(1.dp, PhenomenonBorder, RoundedCornerShape(16.dp))
                        .clickable { onQuickReport() }
                        .padding(14.dp)
                        .testTag("quick_report_btn")
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PhenomenonCrimson.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAlert,
                                contentDescription = "Report",
                                tint = PhenomenonCrimson,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Report", color = PhenomenonTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Geotag Photo", color = PhenomenonTextTertiary, fontSize = 9.sp)
                    }
                }

                // Sync Queue
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PhenomenonSurface)
                        .border(1.dp, PhenomenonBorder, RoundedCornerShape(16.dp))
                        .clickable { onSyncNow() }
                        .padding(14.dp)
                        .testTag("sync_batch_btn")
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PhenomenonCyanElectric.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync",
                                tint = PhenomenonCyanElectric,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Sync Queue", color = PhenomenonTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Cloud Push", color = PhenomenonTextTertiary, fontSize = 9.sp)
                    }
                }
            }
        }

        // Nearby Road Hazards Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nearby Hazards (${nearbyPotholes.size})",
                    color = PhenomenonTextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    letterSpacing = (-0.2).sp
                )
                Text(
                    text = "LIVE MAP // SYNCED",
                    color = PhenomenonCyanElectric,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )
            }
        }

        items(nearbyPotholes.take(3)) { pothole ->
            PotholeItemCard(pothole = pothole, onClick = { onSelectPothole(pothole) })
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
