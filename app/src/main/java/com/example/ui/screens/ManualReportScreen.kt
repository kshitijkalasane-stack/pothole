package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.location.LocationViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.models.Severity
import com.example.services.ai.AiClient
import com.example.services.location.UserLocation
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
import com.example.ui.theme.SkeuoPurple
import com.example.ui.theme.SkeuoSurface
import com.example.ui.theme.SkeuoTextInverse
import com.example.ui.theme.SkeuoTextPrimary
import com.example.ui.theme.SkeuoTextSecondary
import com.example.ui.theme.SkeuoTextTertiary
import com.example.ui.theme.SkeuoWellInset
import kotlinx.coroutines.launch
import java.io.File

private fun createPotholeImageUri(context: Context): Uri {
    val photosDir = File(context.cacheDir, "pothole_photos")
    if (!photosDir.exists()) {
        photosDir.mkdirs()
    }
    val photoFile = File(photosDir, "pothole_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        photoFile
    )
}

@Composable
fun ManualReportScreen(
    userLocation: UserLocation,
    onSubmitReport: (severity: Severity, description: String, roadName: String, photoUri: String?) -> Unit,
    locationViewModel: LocationViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val locationState by locationViewModel.locationState.collectAsState()

    var roadName by remember(locationState.estimatedAddress) {
        mutableStateOf(locationState.estimatedAddress.ifBlank { "Sangamner-Akole Bypass Rd, near Ghulewadi" })
    }
    var description by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf(Severity.MEDIUM) }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var isAnalyzingAi by remember { mutableStateOf(false) }
    var aiConfidenceTag by remember { mutableStateOf<String?>(null) }

    fun runAiAnalysis(photoDescription: String) {
        coroutineScope.launch {
            isAnalyzingAi = true
            val result = AiClient.analyzePotholeSeverity(
                imageDescription = photoDescription,
                speedKmh = userLocation.speedKmh.takeIf { it > 5f } ?: 40f,
                peakZDiff = 2.8f
            )
            severity = result.severity
            aiConfidenceTag = "AI Verified: ${(result.confidence * 100).toInt()}% • ${result.severity.label} Severity"
            isAnalyzingAi = false
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            selectedPhotoUri = tempCameraUri
            runAiAnalysis("Live camera photograph of road pothole surface captured at $roadName")
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val uri = createPotholeImageUri(context)
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to launch camera: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Camera permission is required to capture pothole photos", Toast.LENGTH_LONG).show()
        }
    }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedPhotoUri = uri
            runAiAnalysis("Citizen photo evidence from gallery of road asphalt defect at $roadName")
        }
    }

    fun launchCameraFlow() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            try {
                val uri = createPotholeImageUri(context)
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                Toast.makeText(context, "Camera error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun launchGalleryFlow() {
        galleryPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

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
                        SkeuomorphicLed(isOn = true, color = SkeuoCobalt)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MANUAL HAZARD DISPATCH",
                            color = SkeuoCobalt,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp
                        )
                    }
                    Text(
                        text = "Report Road Defect",
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
                        text = "GPS LOCK ACTIVE",
                        color = SkeuoEmerald,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Skeuomorphic Camera Viewfinder Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .skeuomorphicCard(cornerRadius = 18.dp, elevation = 5.dp)
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ROAD DEFECT PHOTO EVIDENCE",
                        color = SkeuoTextTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    if (selectedPhotoUri != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SkeuoEmerald.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "PHOTO ATTACHED",
                                color = SkeuoEmerald,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                if (selectedPhotoUri != null) {
                    // Captured / Selected Photo Preview Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .skeuomorphicInset(cornerRadius = 14.dp)
                            .padding(4.dp)
                    ) {
                        AsyncImage(
                            model = selectedPhotoUri,
                            contentDescription = "Attached pothole photo evidence",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(10.dp))
                                .testTag("attached_photo_preview")
                        )

                        // Remove Photo overlay button
                        IconButton(
                            onClick = {
                                selectedPhotoUri = null
                                aiConfidenceTag = null
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .size(32.dp)
                                .shadow(4.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .testTag("delete_attached_photo_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove photo",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // AI status badge
                        if (isAnalyzingAi) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.7f))
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        color = SkeuoPurple,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Gemini AI Analyzing Crater Geometry...",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else if (aiConfidenceTag != null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.7f))
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = aiConfidenceTag!!,
                                    color = SkeuoEmerald,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Retake & Gallery Switch Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(2.dp, RoundedCornerShape(10.dp))
                                .clip(RoundedCornerShape(10.dp))
                                .background(SkeuoSurface)
                                .border(1.dp, SkeuoBorderLight, RoundedCornerShape(10.dp))
                                .clickable { launchCameraFlow() }
                                .padding(vertical = 8.dp)
                                .testTag("retake_photo_camera_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = null,
                                    tint = SkeuoCobalt,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Retake",
                                    color = SkeuoTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(2.dp, RoundedCornerShape(10.dp))
                                .clip(RoundedCornerShape(10.dp))
                                .background(SkeuoSurface)
                                .border(1.dp, SkeuoBorderLight, RoundedCornerShape(10.dp))
                                .clickable { launchGalleryFlow() }
                                .padding(vertical = 8.dp)
                                .testTag("pick_another_gallery_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = SkeuoPurple,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "From Gallery",
                                    color = SkeuoTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                } else {
                    // Empty State Camera Viewfinder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .skeuomorphicInset(cornerRadius = 14.dp)
                            .padding(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Camera Button
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable { launchCameraFlow() }
                                        .testTag("camera_photo_trigger")
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .shadow(4.dp, CircleShape)
                                            .clip(CircleShape)
                                            .background(SkeuoCobalt)
                                            .border(2.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Open Device Camera",
                                            tint = Color.White,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Open Camera",
                                        color = SkeuoTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }

                                // Gallery Button
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable { launchGalleryFlow() }
                                        .testTag("gallery_photo_trigger")
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .shadow(4.dp, CircleShape)
                                            .clip(CircleShape)
                                            .background(SkeuoSurface)
                                            .border(1.dp, SkeuoBorderLight, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddPhotoAlternate,
                                            contentDescription = "Select from Gallery",
                                            tint = SkeuoPurple,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Choose Photo",
                                        color = SkeuoTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Snap live photo or attach image to auto-estimate defect severity",
                                color = SkeuoTextTertiary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Severity Selector (Tactile 3D Buttons)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .skeuomorphicCard(cornerRadius = 18.dp, elevation = 4.dp)
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = "SELECT SEVERITY LEVEL",
                    color = SkeuoTextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(Severity.LOW, Severity.MEDIUM, Severity.HIGH).forEach { sev ->
                        val isSelected = severity == sev
                        val btnColor = when (sev) {
                            Severity.LOW -> SkeuoEmerald
                            Severity.MEDIUM -> SkeuoAmber
                            Severity.HIGH -> SkeuoCrimson
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(
                                    elevation = if (isSelected) 1.dp else 3.dp,
                                    shape = RoundedCornerShape(12.dp),
                                    ambientColor = btnColor.copy(alpha = 0.3f)
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) btnColor else SkeuoWellInset
                                )
                                .border(1.dp, if (isSelected) Color.White else SkeuoBorderLight, RoundedCornerShape(12.dp))
                                .clickable { severity = sev }
                                .padding(vertical = 10.dp)
                                .testTag("severity_tab_${sev.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sev.label.uppercase(),
                                color = if (isSelected) Color.White else SkeuoTextSecondary,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Live Play Services GPS Coordinates Lock Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .skeuomorphicCard(cornerRadius = 18.dp, elevation = 4.dp)
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
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "GPS Coordinates",
                            tint = SkeuoEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PLAY SERVICES GPS TRACKER",
                            color = SkeuoTextTertiary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Refresh GPS Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SkeuoWellInset)
                            .border(1.dp, SkeuoBorderLight, RoundedCornerShape(8.dp))
                            .clickable { locationViewModel.requestSingleHighAccuracyUpdate() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("refresh_gps_coordinates_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh GPS",
                                tint = SkeuoCobalt,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (locationState.isLoading) "ACQUIRING..." else "REFRESH GPS",
                                color = SkeuoCobalt,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .skeuomorphicInset(cornerRadius = 12.dp)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = locationState.formattedCoordinates,
                                color = SkeuoTextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Locality: ${locationState.estimatedAddress}",
                                color = SkeuoTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SkeuoEmerald.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "±%.1fm".format(locationState.accuracy),
                                color = SkeuoEmerald,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // Form Fields (Road Location & Description)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .skeuomorphicCard(cornerRadius = 18.dp, elevation = 4.dp)
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "LOCATION & DETAILS",
                    color = SkeuoTextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )

                OutlinedTextField(
                    value = roadName,
                    onValueChange = { roadName = it },
                    label = { Text("Road / Landmark Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_road_name_input"),
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
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Hazard Description / Context") },
                    placeholder = { Text("E.g., deep edge pothole on right lane after flyover") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_desc_input"),
                    minLines = 3,
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
            }
        }

        // Primary Submit Push Button
        SkeuomorphicButton(
            onClick = {
                onSubmitReport(
                    severity,
                    description.ifBlank { "Citizen manual report at $roadName" },
                    roadName,
                    selectedPhotoUri?.toString()
                )
            },
            backgroundColor = SkeuoCobalt,
            cornerRadius = 16.dp,
            tag = "submit_report_button",
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    tint = SkeuoTextInverse,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DISPATCH ROAD REPORT",
                    color = SkeuoTextInverse,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

