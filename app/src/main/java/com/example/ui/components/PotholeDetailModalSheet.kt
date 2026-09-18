package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Pothole
import com.example.data.models.PotholeStatus
import com.example.data.models.Severity
import com.example.data.models.UserRole
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PotholeDetailModalSheet(
    pothole: Pothole,
    userRole: UserRole,
    onDismiss: () -> Unit,
    onVerifyPothole: () -> Unit,
    onUpdateStatus: (PotholeStatus, String?) -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .skeuomorphicCard(cornerRadius = 24.dp, elevation = 10.dp)
            .testTag("pothole_detail_modal")
            .padding(20.dp)
    ) {
        Column {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = pothole.potholeId,
                        color = SkeuoTextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = (-0.2).sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    SeverityBadge(severity = pothole.severity)
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = SkeuoTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Address & GPS Strip
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = SkeuoCobalt,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = pothole.address,
                    color = SkeuoTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "GPS Coordinates: %.5f° N, %.5f° E".format(pothole.latitude, pothole.longitude),
                color = SkeuoTextTertiary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Inset Sensor Telemetry Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .skeuomorphicInset(cornerRadius = 14.dp)
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "INCIDENT TELEMETRY & CONFIDENCE",
                        color = SkeuoTextTertiary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "AI Confidence: ${(pothole.confidence * 100).toInt()}%",
                            color = SkeuoCobalt,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${pothole.reportCount} Commuter Confirmations",
                            color = SkeuoTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (pothole.notes.isNotBlank()) {
                        Text(text = "Notes: ${pothole.notes}", color = SkeuoTextPrimary, fontSize = 11.sp)
                    }
                    Text(
                        text = "First Detected: ${dateFormat.format(Date(pothole.firstDetectedAt))}",
                        color = SkeuoTextTertiary,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Current Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(status = pothole.status)
                pothole.assignedTo?.let { crew ->
                    Text(
                        text = "Assigned: $crew",
                        color = SkeuoPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            if (userRole == UserRole.CITIZEN) {
                SkeuomorphicButton(
                    onClick = onVerifyPothole,
                    backgroundColor = SkeuoEmerald,
                    cornerRadius = 14.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CONFIRM DEFECT AT THIS LOCATION",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            } else {
                // Authority Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (pothole.status != PotholeStatus.ASSIGNED && pothole.status != PotholeStatus.IN_REPAIR && pothole.status != PotholeStatus.RESOLVED) {
                        SkeuomorphicButton(
                            onClick = { onUpdateStatus(PotholeStatus.ASSIGNED, "Sangamner PWD Squad #1") },
                            backgroundColor = SkeuoPurple,
                            cornerRadius = 12.dp,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("ASSIGN CREW", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    if (pothole.status == PotholeStatus.ASSIGNED) {
                        SkeuomorphicButton(
                            onClick = { onUpdateStatus(PotholeStatus.IN_REPAIR, pothole.assignedTo) },
                            backgroundColor = SkeuoCobalt,
                            cornerRadius = 12.dp,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("START REPAIR", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    if (pothole.status == PotholeStatus.IN_REPAIR) {
                        SkeuomorphicButton(
                            onClick = { onUpdateStatus(PotholeStatus.RESOLVED, pothole.assignedTo) },
                            backgroundColor = SkeuoEmerald,
                            cornerRadius = 12.dp,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("MARK RESOLVED", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}
