package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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
fun LoginScreen(
    onCitizenLogin: (identifier: String, name: String) -> Unit,
    onAuthorityLogin: (officerId: String, passkey: String, dept: String) -> Unit
) {
    var selectedPortalTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PhenomenonCanvas)
            .verticalScroll(rememberScrollState())
            .testTag("login_screen")
    ) {
        // Phenomenon Studio Style Hero Section with Frosted Glass & Neon Energy Accents
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_road_banner),
                contentDescription = "Road Infrastructure Visual",
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
                                PhenomenonCanvas.copy(alpha = 0.65f),
                                PhenomenonCanvas
                            )
                        )
                    )
            )

            // Top Status Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(PhenomenonCanvas.copy(alpha = 0.85f))
                        .border(1.dp, PhenomenonBorderActive, RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(PhenomenonEmerald)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PHENOMENON STUDIO // SYSTEM v2.6",
                            color = PhenomenonTextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(PhenomenonPurpleNeon.copy(alpha = 0.2f))
                        .border(1.dp, PhenomenonPurpleNeon.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "EDGE AI RADAR",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            // Hero Brand Title with Distinctive Modernist Type
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(PhenomenonElectricLime, PhenomenonCyanElectric)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = PhenomenonCanvas,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "SMART POTHOLE",
                            color = PhenomenonTextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            letterSpacing = (-0.5).sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "MUNICIPAL TELEMETRY // ",
                                color = PhenomenonTextTertiary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "SAN-PWD",
                                color = PhenomenonElectricLime,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Portal Selector Segment with Phenomenon Capsule Design
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PhenomenonSurface)
                    .border(1.dp, PhenomenonBorder, RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Citizen Portal Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selectedPortalTab == 0) PhenomenonElectricLime
                                else Color.Transparent
                            )
                            .clickable { selectedPortalTab = 0 }
                            .padding(vertical = 12.dp)
                            .testTag("tab_citizen_login"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = if (selectedPortalTab == 0) PhenomenonCanvas else PhenomenonTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Citizen Access",
                                fontWeight = FontWeight.ExtraBold,
                                color = if (selectedPortalTab == 0) PhenomenonCanvas else PhenomenonTextSecondary,
                                fontSize = 13.sp,
                                letterSpacing = 0.2.sp
                            )
                        }
                    }

                    // Authority Portal Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selectedPortalTab == 1) PhenomenonPurpleNeon
                                else Color.Transparent
                            )
                            .clickable { selectedPortalTab = 1 }
                            .padding(vertical = 12.dp)
                            .testTag("tab_authority_login"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = if (selectedPortalTab == 1) Color.White else PhenomenonTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Authority Portal",
                                fontWeight = FontWeight.ExtraBold,
                                color = if (selectedPortalTab == 1) Color.White else PhenomenonTextSecondary,
                                fontSize = 13.sp,
                                letterSpacing = 0.2.sp
                            )
                        }
                    }
                }
            }

            if (selectedPortalTab == 0) {
                CitizenLoginForm(onLogin = onCitizenLogin)
            } else {
                AuthorityLoginForm(onLogin = onAuthorityLogin)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Phenomenon Studio Style Refined Micro-Footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(PhenomenonSurfaceElevated.copy(alpha = 0.4f))
                    .border(1.dp, PhenomenonBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AMRUTVAHINI COLLEGE OF ENGINEERING",
                            color = PhenomenonTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Dept. of Information Technology • Class of 2026",
                            color = PhenomenonTextTertiary,
                            fontSize = 9.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PhenomenonCanvas)
                            .border(1.dp, PhenomenonBorderActive, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "STABLE",
                            color = PhenomenonEmerald,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun CitizenLoginForm(
    onLogin: (identifier: String, name: String) -> Unit
) {
    var identifier by remember { mutableStateOf("9823012345") }
    var commuterName by remember { mutableStateOf("Samyak Gawale") }
    var vehicleType by remember { mutableStateOf("Two-Wheeler / Bike") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PhenomenonSurface)
            .border(1.dp, PhenomenonBorderActive, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(PhenomenonElectricLime)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "COMMUTER IDENTITY",
                        color = PhenomenonElectricLime,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "PASSIVE DETECTION",
                    color = PhenomenonTextTertiary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    letterSpacing = 0.5.sp
                )
            }

            Text(
                text = "Continuous accelerometer sensor processing running on-device to log road hazards without human intervention.",
                color = PhenomenonTextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            // Full Name Input
            OutlinedTextField(
                value = commuterName,
                onValueChange = {
                    commuterName = it
                    errorMsg = null
                },
                label = { Text("Commuter Full Name", color = PhenomenonTextTertiary, fontSize = 12.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = PhenomenonElectricLime, modifier = Modifier.size(20.dp))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("citizen_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PhenomenonTextPrimary,
                    unfocusedTextColor = PhenomenonTextPrimary,
                    focusedBorderColor = PhenomenonElectricLime,
                    unfocusedBorderColor = PhenomenonBorder,
                    focusedContainerColor = PhenomenonCanvas,
                    unfocusedContainerColor = PhenomenonCanvas
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Mobile / Email Input
            OutlinedTextField(
                value = identifier,
                onValueChange = {
                    identifier = it
                    errorMsg = null
                },
                label = { Text("Mobile or Sangamner ID", color = PhenomenonTextTertiary, fontSize = 12.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = PhenomenonElectricLime, modifier = Modifier.size(20.dp))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("citizen_identifier_input"),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PhenomenonTextPrimary,
                    unfocusedTextColor = PhenomenonTextPrimary,
                    focusedBorderColor = PhenomenonElectricLime,
                    unfocusedBorderColor = PhenomenonBorder,
                    focusedContainerColor = PhenomenonCanvas,
                    unfocusedContainerColor = PhenomenonCanvas
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Vehicle Category Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PhenomenonSurfaceHover)
                    .border(1.dp, PhenomenonBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mode: $vehicleType",
                    color = PhenomenonTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PhenomenonElectricLime.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "EDGE ML ACTIVE",
                        color = PhenomenonElectricLime,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            if (errorMsg != null) {
                Text(
                    text = errorMsg!!,
                    color = PhenomenonCrimson,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Phenomenon Signature Pill Action Button
            Button(
                onClick = {
                    if (commuterName.isBlank()) {
                        errorMsg = "Please enter your name"
                        return@Button
                    }
                    if (identifier.isBlank()) {
                        errorMsg = "Please enter your mobile number"
                        return@Button
                    }
                    onLogin(identifier, commuterName)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("citizen_login_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PhenomenonElectricLime,
                    contentColor = PhenomenonCanvas
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ENTER CITIZEN SYSTEM",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Quick Preset Demo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Select: ",
                    color = PhenomenonTextTertiary,
                    fontSize = 11.sp
                )
                Text(
                    text = "Samyak Gawale (Demo Citizen)",
                    color = PhenomenonElectricLime,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        commuterName = "Samyak Gawale"
                        identifier = "9823012345"
                    }
                )
            }
        }
    }
}

@Composable
private fun AuthorityLoginForm(
    onLogin: (officerId: String, passkey: String, dept: String) -> Unit
) {
    var officerId by remember { mutableStateOf("PWD-MH-1701") }
    var actualPassword by remember { mutableStateOf("admin123") }
    var passwordVisible by remember { mutableStateOf(false) }
    var department by remember { mutableStateOf("Public Works Dept (Sangamner Municipal)") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PhenomenonSurface)
            .border(1.dp, PhenomenonPurpleNeon.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(PhenomenonPurpleNeon)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MUNICIPAL COMMAND",
                        color = PhenomenonPurpleNeon,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "RESTRICTED",
                    color = PhenomenonTextTertiary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    letterSpacing = 0.5.sp
                )
            }

            Text(
                text = "Authorized console for Ward Engineers, Highway PWD Squads, and automated crew dispatchers.",
                color = PhenomenonTextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            // Officer ID
            OutlinedTextField(
                value = officerId,
                onValueChange = {
                    officerId = it
                    errorMsg = null
                },
                label = { Text("Officer ID / Badge Code", color = PhenomenonTextTertiary, fontSize = 12.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Badge, contentDescription = null, tint = PhenomenonPurpleNeon, modifier = Modifier.size(20.dp))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("authority_id_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PhenomenonTextPrimary,
                    unfocusedTextColor = PhenomenonTextPrimary,
                    focusedBorderColor = PhenomenonPurpleNeon,
                    unfocusedBorderColor = PhenomenonBorder,
                    focusedContainerColor = PhenomenonCanvas,
                    unfocusedContainerColor = PhenomenonCanvas
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Department
            OutlinedTextField(
                value = department,
                onValueChange = {
                    department = it
                    errorMsg = null
                },
                label = { Text("Department / Municipal Ward", color = PhenomenonTextTertiary, fontSize = 12.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Engineering, contentDescription = null, tint = PhenomenonPurpleNeon, modifier = Modifier.size(20.dp))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("authority_dept_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PhenomenonTextPrimary,
                    unfocusedTextColor = PhenomenonTextPrimary,
                    focusedBorderColor = PhenomenonPurpleNeon,
                    unfocusedBorderColor = PhenomenonBorder,
                    focusedContainerColor = PhenomenonCanvas,
                    unfocusedContainerColor = PhenomenonCanvas
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Passkey
            OutlinedTextField(
                value = actualPassword,
                onValueChange = {
                    actualPassword = it
                    errorMsg = null
                },
                label = { Text("Authority Passkey", color = PhenomenonTextTertiary, fontSize = 12.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = PhenomenonPurpleNeon, modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password",
                            tint = PhenomenonTextSecondary
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("authority_passkey_input"),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PhenomenonTextPrimary,
                    unfocusedTextColor = PhenomenonTextPrimary,
                    focusedBorderColor = PhenomenonPurpleNeon,
                    unfocusedBorderColor = PhenomenonBorder,
                    focusedContainerColor = PhenomenonCanvas,
                    unfocusedContainerColor = PhenomenonCanvas
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (errorMsg != null) {
                Text(
                    text = errorMsg!!,
                    color = PhenomenonCrimson,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Authority Login Button
            Button(
                onClick = {
                    if (officerId.isBlank()) {
                        errorMsg = "Please enter Officer ID"
                        return@Button
                    }
                    if (actualPassword.isBlank()) {
                        errorMsg = "Please enter passkey"
                        return@Button
                    }
                    onLogin(officerId, actualPassword, department)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("authority_login_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PhenomenonPurpleNeon,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACCESS COMMAND CONSOLE",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Quick Preset Demo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Select: ",
                    color = PhenomenonTextTertiary,
                    fontSize = 11.sp
                )
                Text(
                    text = "Officer Sharma (PWD Ward 4)",
                    color = PhenomenonCyanElectric,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        officerId = "PWD-MH-1701"
                        department = "Sangamner Municipal PWD Div"
                        actualPassword = "admin"
                    }
                )
            }
        }
    }
}
