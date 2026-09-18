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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.models.UserProfile
import com.example.data.models.UserRole
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
import com.example.ui.theme.PhenomenonTextPrimary
import com.example.ui.theme.PhenomenonTextSecondary
import com.example.ui.theme.PhenomenonTextTertiary

@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    onLogout: () -> Unit
) {
    var adaptiveGpsEnabled by remember { mutableStateOf(true) }
    var backgroundSensorsEnabled by remember { mutableStateOf(true) }
    var anonymousDataContribution by remember { mutableStateOf(true) }

    val accentColor = if (userProfile.role == UserRole.AUTHORITY) PhenomenonPurpleNeon else PhenomenonElectricLime

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PhenomenonCanvas)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Column {
            Text(
                text = "System Profile & Sensors",
                color = PhenomenonTextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                letterSpacing = (-0.3).sp
            )
            Text(
                text = "COMMUTER TELEMETRY // PREFERENCES",
                color = PhenomenonTextTertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        }

        // Phenomenon User Identity Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(PhenomenonSurface)
                .border(1.dp, PhenomenonBorder, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f))
                        .border(1.5.dp, accentColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (userProfile.role == UserRole.AUTHORITY) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = userProfile.name,
                        color = PhenomenonTextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                    Text(
                        text = userProfile.email,
                        color = PhenomenonTextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PORTAL: ${userProfile.role.name} SESSION",
                            color = accentColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // Dedicated Logout / Switch Portal Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(PhenomenonSurface)
                .border(1.dp, PhenomenonCrimson.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .clickable { onLogout() }
                .padding(16.dp)
                .testTag("logout_card_btn")
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
                            .background(PhenomenonCrimson.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = PhenomenonCrimson,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Sign Out / Switch Portal",
                            color = PhenomenonTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Return to Citizen / Authority Portal login screen",
                            color = PhenomenonTextTertiary,
                            fontSize = 11.sp
                        )
                    }
                }
                Text(
                    "DISCONNECT",
                    color = PhenomenonCrimson,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp
                )
            }
        }

        // Edge Detection & Sensor Tuning Preferences
        Text(
            "Edge Engine & Telemetry Tuning",
            color = PhenomenonTextPrimary,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            letterSpacing = (-0.2).sp
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(PhenomenonSurface)
                .border(1.dp, PhenomenonBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SettingToggleRow(
                    icon = Icons.Default.BatteryChargingFull,
                    title = "Adaptive GPS Sampling",
                    subtitle = "Reduces polling rate when vehicle is decelerated",
                    checked = adaptiveGpsEnabled,
                    accentColor = accentColor,
                    onCheckedChange = { adaptiveGpsEnabled = it }
                )
                SettingToggleRow(
                    icon = Icons.Default.Sensors,
                    title = "Z-THRESH Edge Engine",
                    subtitle = "Filter standard engine vibrations locally",
                    checked = backgroundSensorsEnabled,
                    accentColor = accentColor,
                    onCheckedChange = { backgroundSensorsEnabled = it }
                )
                SettingToggleRow(
                    icon = Icons.Default.Security,
                    title = "Anonymous Road Telemetry",
                    subtitle = "Strip personal identifier before batch sync",
                    checked = anonymousDataContribution,
                    accentColor = accentColor,
                    onCheckedChange = { anonymousDataContribution = it }
                )
            }
        }

        // Academic Project Credits with Phenomenon Subtle Styling
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(PhenomenonSurfaceElevated.copy(alpha = 0.5f))
                .border(1.dp, PhenomenonBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = PhenomenonCyanElectric,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "PROJECT CREDITS",
                        color = PhenomenonCyanElectric,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Smart Pothole Locator & Management System\nDepartment of Information Technology\nAmrutvahini College of Engineering, Sangamner\nClass of 2026-27",
                    color = PhenomenonTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SettingToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    accentColor: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, color = PhenomenonTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(subtitle, color = PhenomenonTextTertiary, fontSize = 11.sp)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PhenomenonCanvas,
                checkedTrackColor = accentColor,
                uncheckedThumbColor = PhenomenonTextTertiary,
                uncheckedTrackColor = PhenomenonCanvas
            )
        )
    }
}
