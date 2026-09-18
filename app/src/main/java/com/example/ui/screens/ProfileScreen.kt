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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.UserProfile
import com.example.data.models.UserRole
import com.example.ui.components.SkeuomorphicButton
import com.example.ui.components.SkeuomorphicLed
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
import com.example.ui.theme.SkeuoShadowDark
import com.example.ui.theme.SkeuoSurface
import com.example.ui.theme.SkeuoSurfaceElevated
import com.example.ui.theme.SkeuoTextInverse
import com.example.ui.theme.SkeuoTextPrimary
import com.example.ui.theme.SkeuoTextSecondary
import com.example.ui.theme.SkeuoTextTertiary
import com.example.ui.theme.SkeuoWellInset

@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    onLogout: () -> Unit
) {
    var adaptiveGpsEnabled by remember { mutableStateOf(true) }
    var backgroundSensorsEnabled by remember { mutableStateOf(true) }
    var anonymousDataContribution by remember { mutableStateOf(true) }

    val accentColor = if (userProfile.role == UserRole.AUTHORITY) SkeuoPurple else SkeuoCobalt

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkeuoCanvas)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Skeuomorphic Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .skeuomorphicCard(cornerRadius = 18.dp, elevation = 4.dp)
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SkeuomorphicLed(isOn = true, color = accentColor)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SYSTEM PREFERENCES & HARDWARE",
                            color = accentColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp
                        )
                    }
                    Text(
                        text = "User Profile & Diagnostics",
                        color = SkeuoTextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SkeuoWellInset)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = userProfile.role.name,
                        color = accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Tactile ID Badge Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .skeuomorphicCard(cornerRadius = 20.dp, elevation = 5.dp)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (userProfile.role == UserRole.AUTHORITY) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = userProfile.name,
                        color = SkeuoTextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    Text(
                        text = userProfile.email,
                        color = SkeuoTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = userProfile.jurisdictionArea,
                        color = accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Hardware Sensor Engine Config
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .skeuomorphicCard(cornerRadius = 18.dp, elevation = 4.dp)
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "HARDWARE SENSOR CONFIGURATION",
                    color = SkeuoTextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Background Telemetry Service",
                            color = SkeuoTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Continuous 50Hz high-pass anomaly detector",
                            color = SkeuoTextTertiary,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = backgroundSensorsEnabled,
                        onCheckedChange = { backgroundSensorsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = accentColor,
                            uncheckedThumbColor = SkeuoTextTertiary,
                            uncheckedTrackColor = SkeuoWellInset
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Adaptive Battery Saver GPS",
                            color = SkeuoTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Throttles GPS polling when vehicle is stationary",
                            color = SkeuoTextTertiary,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = adaptiveGpsEnabled,
                        onCheckedChange = { adaptiveGpsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = accentColor,
                            uncheckedThumbColor = SkeuoTextTertiary,
                            uncheckedTrackColor = SkeuoWellInset
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Crowdsourced Telemetry Sharing",
                            color = SkeuoTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Anonymously upload road defect bumps to civic grid",
                            color = SkeuoTextTertiary,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = anonymousDataContribution,
                        onCheckedChange = { anonymousDataContribution = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = accentColor,
                            uncheckedThumbColor = SkeuoTextTertiary,
                            uncheckedTrackColor = SkeuoWellInset
                        )
                    )
                }
            }
        }

        // Hardware Diagnostics Inset Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .skeuomorphicInset(cornerRadius = 16.dp)
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "DEVICE TELEMETRY SPECS",
                    color = SkeuoTextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Text(text = "Sensor: 3-Axis MEMS Linear Accelerometer", color = SkeuoTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(text = "Sampling Rate: 50 Samples/Sec (High-pass filtered)", color = SkeuoTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(text = "AI Model: Gemini Flash Road Analysis API", color = SkeuoTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(text = "Mapping Engine: Google Maps Android SDK", color = SkeuoTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Logout Button
        SkeuomorphicButton(
            onClick = onLogout,
            backgroundColor = SkeuoCrimson,
            cornerRadius = 16.dp,
            tag = "profile_logout_btn",
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Log Out",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DISCONNECT & LOGOUT",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
