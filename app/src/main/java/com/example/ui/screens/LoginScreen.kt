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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.shadow
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
fun LoginScreen(
    onCitizenLogin: (identifier: String, name: String) -> Unit,
    onAuthorityLogin: (officerId: String, passkey: String, dept: String) -> Unit
) {
    var selectedPortalTab by remember { mutableIntStateOf(0) } // 0: Citizen, 1: Authority

    // Citizen Form State
    var citizenPhone by remember { mutableStateOf("+91 98765 43210") }
    var citizenName by remember { mutableStateOf("Rajesh Sharma") }
    var citizenVehicle by remember { mutableStateOf("Hyundai Creta (MH-17-AB-1234)") }

    // Authority Form State
    var officerId by remember { mutableStateOf("PWD-OFFICER-704") }
    var authorityPasskey by remember { mutableStateOf("sangamner2026") }
    var department by remember { mutableStateOf("Road Maintenance & Infrastructure") }
    var showPasskey by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkeuoCanvas)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Skeuomorphic Hero Card
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
                        contentDescription = "Smart Pothole System",
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
                                        Color(0x88FFFFFF),
                                        Color.White
                                    )
                                )
                            )
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SkeuomorphicLed(isOn = true, color = SkeuoEmerald)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SMART MUNICIPAL ROAD TELEMETRY",
                            color = SkeuoCobalt,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sangamner Smart Road",
                        color = SkeuoTextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    )
                    Text(
                        text = "Real-time AI Pothole Detection & Rapid Civic Dispatch",
                        color = SkeuoTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Skeuomorphic Portal Switcher
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .skeuomorphicInset(cornerRadius = 16.dp)
                .padding(4.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(
                            elevation = if (selectedPortalTab == 0) 3.dp else 0.dp,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedPortalTab == 0) Color.White else Color.Transparent)
                        .clickable { selectedPortalTab = 0; errorMessage = null }
                        .padding(vertical = 10.dp)
                        .testTag("tab_citizen_portal"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = if (selectedPortalTab == 0) SkeuoCobalt else SkeuoTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Citizen Commuter",
                            color = if (selectedPortalTab == 0) SkeuoCobalt else SkeuoTextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(
                            elevation = if (selectedPortalTab == 1) 3.dp else 0.dp,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedPortalTab == 1) Color.White else Color.Transparent)
                        .clickable { selectedPortalTab = 1; errorMessage = null }
                        .padding(vertical = 10.dp)
                        .testTag("tab_authority_portal"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = if (selectedPortalTab == 1) SkeuoPurple else SkeuoTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PWD Authority",
                            color = if (selectedPortalTab == 1) SkeuoPurple else SkeuoTextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Credentials Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .skeuomorphicCard(cornerRadius = 18.dp, elevation = 4.dp)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (selectedPortalTab == 0) {
                    // Citizen Fields
                    OutlinedTextField(
                        value = citizenName,
                        onValueChange = { citizenName = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SkeuoCobalt) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_citizen_name_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SkeuoWellInset,
                            unfocusedContainerColor = SkeuoWellInset,
                            focusedBorderColor = SkeuoCobalt,
                            unfocusedBorderColor = SkeuoBorderLight,
                            focusedTextColor = SkeuoTextPrimary,
                            unfocusedTextColor = SkeuoTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = citizenPhone,
                        onValueChange = { citizenPhone = it },
                        label = { Text("Mobile Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = SkeuoCobalt) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_citizen_phone_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SkeuoWellInset,
                            unfocusedContainerColor = SkeuoWellInset,
                            focusedBorderColor = SkeuoCobalt,
                            unfocusedBorderColor = SkeuoBorderLight,
                            focusedTextColor = SkeuoTextPrimary,
                            unfocusedTextColor = SkeuoTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = citizenVehicle,
                        onValueChange = { citizenVehicle = it },
                        label = { Text("Vehicle Descriptor") },
                        leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = SkeuoCobalt) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_citizen_vehicle_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SkeuoWellInset,
                            unfocusedContainerColor = SkeuoWellInset,
                            focusedBorderColor = SkeuoCobalt,
                            unfocusedBorderColor = SkeuoBorderLight,
                            focusedTextColor = SkeuoTextPrimary,
                            unfocusedTextColor = SkeuoTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    // Authority Fields
                    OutlinedTextField(
                        value = officerId,
                        onValueChange = { officerId = it },
                        label = { Text("Officer / Staff Badge ID") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = SkeuoPurple) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_officer_id_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SkeuoWellInset,
                            unfocusedContainerColor = SkeuoWellInset,
                            focusedBorderColor = SkeuoPurple,
                            unfocusedBorderColor = SkeuoBorderLight,
                            focusedTextColor = SkeuoTextPrimary,
                            unfocusedTextColor = SkeuoTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = authorityPasskey,
                        onValueChange = { authorityPasskey = it },
                        label = { Text("Security Passkey") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SkeuoPurple) },
                        trailingIcon = {
                            IconButton(onClick = { showPasskey = !showPasskey }) {
                                Icon(
                                    imageVector = if (showPasskey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Passkey",
                                    tint = SkeuoTextTertiary
                                )
                            }
                        },
                        visualTransformation = if (showPasskey) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_authority_passkey_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SkeuoWellInset,
                            unfocusedContainerColor = SkeuoWellInset,
                            focusedBorderColor = SkeuoPurple,
                            unfocusedBorderColor = SkeuoBorderLight,
                            focusedTextColor = SkeuoTextPrimary,
                            unfocusedTextColor = SkeuoTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("Municipal Department") },
                        leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = SkeuoPurple) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_department_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SkeuoWellInset,
                            unfocusedContainerColor = SkeuoWellInset,
                            focusedBorderColor = SkeuoPurple,
                            unfocusedBorderColor = SkeuoBorderLight,
                            focusedTextColor = SkeuoTextPrimary,
                            unfocusedTextColor = SkeuoTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Error message
                AnimatedVisibility(visible = errorMessage != null) {
                    errorMessage?.let { msg ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SkeuoCrimson.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = SkeuoCrimson, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = msg, color = SkeuoCrimson, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Primary Action Button
        val isCitizen = selectedPortalTab == 0
        SkeuomorphicButton(
            onClick = {
                if (isCitizen) {
                    if (citizenName.isBlank() || citizenPhone.isBlank()) {
                        errorMessage = "Please provide your name and phone number."
                    } else {
                        onCitizenLogin(citizenPhone, citizenName)
                    }
                } else {
                    if (officerId.isBlank() || authorityPasskey.isBlank()) {
                        errorMessage = "Please enter both Officer ID and Passkey."
                    } else {
                        onAuthorityLogin(officerId, authorityPasskey, department)
                    }
                }
            },
            backgroundColor = if (isCitizen) SkeuoCobalt else SkeuoPurple,
            cornerRadius = 16.dp,
            tag = if (isCitizen) "login_citizen_submit" else "login_authority_submit",
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isCitizen) "ENTER COMMUTER RADAR" else "ACCESS PWD CONSOLE",
                    color = SkeuoTextInverse,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = SkeuoTextInverse,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Quick Preset Badges for Demo Testing
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Demo Mode: ",
                color = SkeuoTextTertiary,
                fontSize = 11.sp
            )
            Text(
                text = if (isCitizen) "Quick Login as Rajesh" else "Quick Login as PWD Officer",
                color = if (isCitizen) SkeuoCobalt else SkeuoPurple,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    if (isCitizen) {
                        onCitizenLogin(citizenPhone, citizenName)
                    } else {
                        onAuthorityLogin(officerId, authorityPasskey, department)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
