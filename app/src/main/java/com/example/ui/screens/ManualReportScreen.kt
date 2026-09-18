package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.models.Severity
import com.example.services.ai.AiClient
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
import com.example.ui.theme.PhenomenonSurfaceHover
import com.example.ui.theme.PhenomenonTextPrimary
import com.example.ui.theme.PhenomenonTextSecondary
import com.example.ui.theme.PhenomenonTextTertiary
import kotlinx.coroutines.launch

@Composable
fun ManualReportScreen(
    userLocation: UserLocation,
    onSubmitReport: (severity: Severity, description: String, roadName: String, photoUri: String?) -> Unit
) {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasLocationPermission = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    var roadName by remember { mutableStateOf("Sangamner-Akole Bypass Rd, near Ghulewadi") }
    var description by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf(Severity.MEDIUM) }
    var hasSimulatedPhoto by remember { mutableStateOf(false) }
    var isAnalyzingAi by remember { mutableStateOf(false) }
    var aiInsight by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PhenomenonCanvas)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("manual_report_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Manual Road Hazard Report",
                color = PhenomenonTextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                letterSpacing = (-0.3).sp
            )
            Text(
                text = "CITIZEN DISPATCH // GEOTAGGED TELEMETRY",
                color = PhenomenonTextTertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        }

        // Live GPS Geotag HUD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(PhenomenonSurface)
                .border(
                    1.dp,
                    if (hasLocationPermission) PhenomenonBorder else PhenomenonElectricLime.copy(alpha = 0.6f),
                    RoundedCornerShape(18.dp)
                )
                .clickable {
                    if (!hasLocationPermission) {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }
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
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = PhenomenonElectricLime,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Geotag Coordinates (Play Services)",
                            color = PhenomenonTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = if (hasLocationPermission) {
                                "%.5f, %.5f (Accuracy: %.1fm)".format(
                                    userLocation.latitude,
                                    userLocation.longitude,
                                    userLocation.accuracy
                                )
                            } else {
                                "Tap to grant GPS location access"
                            },
                            color = if (hasLocationPermission) PhenomenonCyanElectric else PhenomenonElectricLime,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (hasLocationPermission) PhenomenonEmerald.copy(alpha = 0.15f)
                            else PhenomenonElectricLime.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (hasLocationPermission) "GPS LOCK" else "TAP TO GRANT",
                        color = if (hasLocationPermission) PhenomenonEmerald else PhenomenonElectricLime,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Road Name Field
        OutlinedTextField(
            value = roadName,
            onValueChange = { roadName = it },
            label = { Text("Road / Landmark / Corridor", color = PhenomenonTextTertiary, fontSize = 12.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("report_road_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PhenomenonTextPrimary,
                unfocusedTextColor = PhenomenonTextPrimary,
                focusedBorderColor = PhenomenonElectricLime,
                unfocusedBorderColor = PhenomenonBorder,
                focusedContainerColor = PhenomenonSurface,
                unfocusedContainerColor = PhenomenonSurface
            ),
            shape = RoundedCornerShape(14.dp)
        )

        // Severity Selector with Phenomenon styling
        Column {
            Text(
                text = "Observed Severity",
                color = PhenomenonTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SeveritySelectButton(
                    label = "LOW",
                    color = PhenomenonEmerald,
                    isSelected = severity == Severity.LOW,
                    modifier = Modifier.weight(1f)
                ) { severity = Severity.LOW }

                SeveritySelectButton(
                    label = "MEDIUM",
                    color = PhenomenonFlameAmber,
                    isSelected = severity == Severity.MEDIUM,
                    modifier = Modifier.weight(1f)
                ) { severity = Severity.MEDIUM }

                SeveritySelectButton(
                    label = "HIGH",
                    color = PhenomenonCrimson,
                    isSelected = severity == Severity.HIGH,
                    modifier = Modifier.weight(1f)
                ) { severity = Severity.HIGH }
            }
        }

        // Description Input
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Describe road issue (e.g. deep crater near bridge, bikes skidding)", color = PhenomenonTextTertiary, fontSize = 12.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .testTag("report_desc_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PhenomenonTextPrimary,
                unfocusedTextColor = PhenomenonTextPrimary,
                focusedBorderColor = PhenomenonElectricLime,
                unfocusedBorderColor = PhenomenonBorder,
                focusedContainerColor = PhenomenonSurface,
                unfocusedContainerColor = PhenomenonSurface
            ),
            shape = RoundedCornerShape(14.dp)
        )

        // Photo Attachment & Gemini AI Analysis Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PhenomenonSurface)
                    .border(
                        1.dp,
                        if (hasSimulatedPhoto) PhenomenonEmerald else PhenomenonBorder,
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { hasSimulatedPhoto = !hasSimulatedPhoto }
                    .padding(14.dp)
                    .testTag("attach_photo_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (hasSimulatedPhoto) Icons.Default.CheckCircle else Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = if (hasSimulatedPhoto) PhenomenonEmerald else PhenomenonTextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (hasSimulatedPhoto) "Photo Attached" else "Attach Photo",
                        color = PhenomenonTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // AI Report Structuring / Gemini Assist
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PhenomenonSurface)
                    .border(1.dp, PhenomenonPurpleNeon.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .clickable {
                        if (description.isBlank()) {
                            aiInsight = "Please enter a brief description first for AI structuring."
                            return@clickable
                        }
                        coroutineScope.launch {
                            isAnalyzingAi = true
                            val prompt = "Analyze this road report: '$description' at '$roadName'. Provide structured JSON-like key points: Issue Type, Severity Level, Immediate Risk."
                            val res = AiClient.queryGemini(prompt)
                            res.onSuccess {
                                aiInsight = it
                                isAnalyzingAi = false
                            }.onFailure {
                                aiInsight = "AI Structure: Pothole detected, recommended severity: $severity. Immediate attention for two-wheelers."
                                isAnalyzingAi = false
                            }
                        }
                    }
                    .padding(14.dp)
                    .testTag("ai_assist_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isAnalyzingAi) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = PhenomenonPurpleNeon, strokeWidth = 2.dp)
                    } else {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = PhenomenonPurpleNeon, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "AI Assist", color = PhenomenonPurpleNeon, fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        if (aiInsight != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(PhenomenonSurface)
                    .border(1.dp, PhenomenonPurpleNeon.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PhenomenonPurpleNeon, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gemini AI Analysis & Structuring", color = PhenomenonPurpleNeon, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = aiInsight!!, color = PhenomenonTextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Submit Button with Electric Lime Phenomenon Style
        Button(
            onClick = {
                val finalDesc = if (description.isBlank()) "Citizen reported road depression" else description
                onSubmitReport(severity, finalDesc, roadName, if (hasSimulatedPhoto) "file://pothole_evidence.jpg" else null)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_report_btn"),
            colors = ButtonDefaults.buttonColors(
                containerColor = PhenomenonElectricLime,
                contentColor = PhenomenonCanvas
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "TRANSMIT CITIZEN REPORT",
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
private fun SeveritySelectButton(
    label: String,
    color: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) color.copy(alpha = 0.15f) else PhenomenonSurface)
            .border(
                1.5.dp,
                if (isSelected) color else PhenomenonBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) color else PhenomenonTextSecondary,
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            letterSpacing = 0.5.sp
        )
    }
}
