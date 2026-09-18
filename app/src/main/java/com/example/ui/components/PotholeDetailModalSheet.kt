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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Pothole
import com.example.data.models.PotholeStatus
import com.example.data.models.Severity
import com.example.data.models.UserRole
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
            .clip(RoundedCornerShape(24.dp))
            .background(PhenomenonSurface)
            .border(1.dp, PhenomenonBorderActive, RoundedCornerShape(24.dp))
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
                        color = PhenomenonTextPrimary,
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
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = PhenomenonTextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            StatusBadge(status = pothole.status)

            Spacer(modifier = Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = PhenomenonCyanElectric,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = pothole.address,
                        color = PhenomenonTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "GPS: %.5f N, %.5f E".format(pothole.latitude, pothole.longitude),
                        color = PhenomenonCyanElectric,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (pothole.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Notes: ${pothole.notes}",
                    color = PhenomenonTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            // Telemetry stats row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(PhenomenonCanvas)
                    .border(1.dp, PhenomenonBorder, RoundedCornerShape(14.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("CONFIDENCE", color = PhenomenonTextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Text("${(pothole.confidence * 100).toInt()}%", color = PhenomenonTextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("REPORTS", color = PhenomenonTextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Text("${pothole.reportCount}", color = PhenomenonElectricLime, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("VERIFIED", color = PhenomenonTextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Text("${pothole.verificationCount}", color = PhenomenonEmerald, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }
            }

            if (pothole.assignedTo != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Assigned Crew: ${pothole.assignedTo}",
                    color = PhenomenonPurpleNeon,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons: Citizen Verification vs Authority Management
            if (userRole == UserRole.CITIZEN) {
                Button(
                    onClick = onVerifyPothole,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("verify_pothole_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PhenomenonElectricLime,
                        contentColor = PhenomenonCanvas
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "CONFIRM & VERIFY HAZARD",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 0.8.sp
                    )
                }
            } else {
                // Authority Options: Full lifecycle status updates
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "UPDATE CASE STATUS",
                        color = PhenomenonTextTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.6.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onUpdateStatus(PotholeStatus.ASSIGNED, "Sangamner Municipal Ward 4 Quick-Patch Squad")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("assign_crew_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (pothole.status == PotholeStatus.ASSIGNED) PhenomenonPurpleNeon else PhenomenonSurfaceElevated,
                                contentColor = if (pothole.status == PotholeStatus.ASSIGNED) Color.White else PhenomenonTextSecondary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Engineering, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Assign Crew", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                        }

                        Button(
                            onClick = {
                                onUpdateStatus(PotholeStatus.IN_REPAIR, pothole.assignedTo ?: "Ward 4 PWD Squad")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("in_repair_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (pothole.status == PotholeStatus.IN_REPAIR) PhenomenonCyanElectric else PhenomenonSurfaceElevated,
                                contentColor = if (pothole.status == PotholeStatus.IN_REPAIR) PhenomenonCanvas else PhenomenonTextSecondary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("In Repair", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onUpdateStatus(PotholeStatus.RESOLVED, pothole.assignedTo)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("resolve_pothole_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PhenomenonEmerald,
                                contentColor = PhenomenonCanvas
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Mark Resolved", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }

                        if (pothole.status == PotholeStatus.RESOLVED) {
                            Button(
                                onClick = {
                                    onUpdateStatus(PotholeStatus.OPEN, null)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("reopen_case_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PhenomenonCrimson.copy(alpha = 0.2f),
                                    contentColor = PhenomenonCrimson
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Reopen Case", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
