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
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
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
import com.example.services.location.UserLocation
import com.example.services.sensor.LiveSensorTelemetry
import com.example.ui.components.PotholeItemCard
import com.example.ui.components.SkeuomorphicButton
import com.example.ui.components.SkeuomorphicGauge
import com.example.ui.components.SkeuomorphicLed
import com.example.ui.components.skeuomorphicCard
import com.example.ui.components.skeuomorphicInset
import com.example.ui.components.tactilePress
import com.example.ui.theme.SkeuoAmber
import com.example.ui.theme.SkeuoBorderLight
import com.example.ui.theme.SkeuoCanvas
import com.example.ui.theme.SkeuoChromeBezel
import com.example.ui.theme.SkeuoCobalt
import com.example.ui.theme.SkeuoCrimson
import com.example.ui.theme.SkeuoCyan
import com.example.ui.theme.SkeuoElectricLime
import com.example.ui.theme.SkeuoEmerald
import com.example.ui.theme.SkeuoHighlight
import com.example.ui.theme.SkeuoHighlightSoft
import com.example.ui.theme.SkeuoLedAmberOn
import com.example.ui.theme.SkeuoLedGreenOn
import com.example.ui.theme.SkeuoLedRedOn
import com.example.ui.theme.SkeuoPurple
import com.example.ui.theme.SkeuoShadowDark
import com.example.ui.theme.SkeuoSurface
import com.example.ui.theme.SkeuoSurfaceElevated
import com.example.ui.theme.SkeuoTextInverse
import com.example.ui.theme.SkeuoTextPrimary
import com.example.ui.theme.SkeuoTextSecondary
import com.example.ui.theme.SkeuoTextTertiary
import com.example.ui.theme.SkeuoWellInset

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
    onSelectPothole: (Pothole) -> Unit = {},
    onViewActivity: () -> Unit = {},
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
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SkeuoCanvas)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Skeuomorphic Hero Header with Beveled Plate & Chrome Bezel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .skeuomorphicCard(cornerRadius = 22.dp, elevation = 6.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
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
                                            Color(0x99FFFFFF),
                                            SkeuoSurface
                                        )
                                    )
                                )
                        )
                    }

                    // Bottom info bar on hero
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SkeuomorphicLed(isOn = true, color = SkeuoEmerald)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ROAD SENSOR COCKPIT",
                                    color = SkeuoCobalt,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Smart Pothole System",
                                color = SkeuoTextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                letterSpacing = (-0.3).sp
                            )
                            Text(
                                text = "Sangamner Sector MH-17",
                                color = SkeuoTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Tactile Sign Out Button
                        Box(
                            modifier = Modifier
                                .shadow(2.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(SkeuoSurfaceElevated)
                                .border(1.dp, SkeuoBorderLight, RoundedCornerShape(12.dp))
                                .clickable { onSignOut() }
                                .padding(horizontal = 10.dp, vertical = 7.dp)
                                .testTag("sign_out_button")
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
                                    text = "EXIT",
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

        // Tactile Hardware Instrument Status Strip
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .skeuomorphicCard(cornerRadius = 16.dp, elevation = 4.dp)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SkeuomorphicLed(
                        isOn = isMonitoring,
                        color = SkeuoEmerald,
                        label = if (isMonitoring) "SENSOR ENGINE ACTIVE" else "STANDBY"
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SkeuomorphicLed(
                            isOn = hasLocationPermission,
                            color = SkeuoCobalt,
                            label = "GPS"
                        )
                        SkeuomorphicLed(
                            isOn = true,
                            color = SkeuoAmber,
                            label = "50Hz"
                        )
                    }
                }
            }
        }

        // Skeuomorphic Analog Telemetry Gauges (Speedometer & G-Force Meter)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SkeuomorphicGauge(
                    value = userLocation.speedKmh,
                    maxValue = 100f,
                    unit = "km/h",
                    title = "Speedometer",
                    modifier = Modifier.weight(1f),
                    needleColor = SkeuoCobalt
                )

                SkeuomorphicGauge(
                    value = telemetry.zDiff,
                    maxValue = 5.0f,
                    unit = "ΔG Peak",
                    title = "Impact Shock",
                    modifier = Modifier.weight(1f),
                    needleColor = if (telemetry.zDiff > 2.5f) SkeuoCrimson else SkeuoAmber
                )
            }
        }

        // Primary 3D Physical Rocker Button (Start / Stop Monitoring)
        item {
            val mainBtnColor = if (isMonitoring) SkeuoCrimson else SkeuoEmerald
            val btnModifier = if (isMonitoring) Modifier.scale(pulseScale) else Modifier

            SkeuomorphicButton(
                onClick = {
                    if (isMonitoring) {
                        onStopMonitoring()
                    } else {
                        if (hasLocationPermission) {
                            onStartMonitoring()
                        } else {
                            permissionLauncher.launch(permissionsToRequest)
                        }
                    }
                },
                backgroundColor = mainBtnColor,
                cornerRadius = 18.dp,
                tag = if (isMonitoring) "stop_monitoring_btn" else "start_monitoring_btn",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .then(btnModifier)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isMonitoring) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = SkeuoTextInverse,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = if (isMonitoring) "STOP MONITORING" else "ENGAGE SENSOR MONITORING",
                            color = SkeuoTextInverse,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (isMonitoring) "Background high-frequency logging running" else "Continuous AI anomaly detection",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Recessed LCD Telemetry Well
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .skeuomorphicInset(cornerRadius = 16.dp)
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = null,
                                tint = SkeuoCobalt,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "EDGE TELEMETRY READOUT",
                                color = SkeuoTextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = "ACCEL Z: %.2f m/s²".format(telemetry.az),
                            color = SkeuoCobalt,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "CURRENT GPS", color = SkeuoTextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "%.4f° N, %.4f° E".format(userLocation.latitude, userLocation.longitude),
                                color = SkeuoTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "GPS ACCURACY", color = SkeuoTextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "±%.1f meters".format(userLocation.accuracy),
                                color = if (userLocation.accuracy < 10f) SkeuoEmerald else SkeuoAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Quick Manual Actions (Report Pothole & Simulate Impact)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quick Manual Report Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .tactilePress(pressedScale = 0.94f, pressedTranslationY = 1.5.dp) { onQuickReport() }
                        .skeuomorphicCard(cornerRadius = 14.dp, elevation = 3.dp)
                        .padding(12.dp)
                        .testTag("quick_report_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AddAlert,
                            contentDescription = null,
                            tint = SkeuoCobalt,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Report Hazard",
                                color = SkeuoTextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Photo & GPS form",
                                color = SkeuoTextTertiary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Simulate Bump Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .tactilePress(pressedScale = 0.94f, pressedTranslationY = 1.5.dp) { onSimulateBump() }
                        .skeuomorphicCard(cornerRadius = 14.dp, elevation = 3.dp)
                        .padding(12.dp)
                        .testTag("test_bump_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = SkeuoAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Simulate Shock",
                                color = SkeuoTextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Trigger 3.8G anomaly",
                                color = SkeuoTextTertiary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Community Hazards Hub Card (Transferred to Recent Activity)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .skeuomorphicCard(cornerRadius = 16.dp, elevation = 4.dp)
                    .padding(14.dp)
                    .testTag("community_hazards_hub_card")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = SkeuoCrimson,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "COMMUNITY HAZARDS LEDGER",
                                color = SkeuoTextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SkeuoCrimson.copy(alpha = 0.12f))
                                .border(1.dp, SkeuoCrimson.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${nearbyPotholes.size} Verified",
                                color = SkeuoCrimson,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "All community-detected road defects and edge impact data are actively tracked and aggregated in the Recent Activity ledger.",
                        color = SkeuoTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SkeuomorphicButton(
                        onClick = onViewActivity,
                        backgroundColor = SkeuoCobalt,
                        cornerRadius = 12.dp,
                        tag = "view_hazards_activity_btn",
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = SkeuoTextInverse,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "VIEW COMMUNITY HAZARDS IN RECENT ACTIVITY",
                                color = SkeuoTextInverse,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.4.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
