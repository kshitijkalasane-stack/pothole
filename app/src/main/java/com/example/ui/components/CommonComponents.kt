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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
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
import com.example.ui.theme.PhenomenonBorder
import com.example.ui.theme.PhenomenonBorderActive
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

@Composable
fun SeverityBadge(severity: Severity, modifier: Modifier = Modifier) {
    val (color, bg) = when (severity) {
        Severity.HIGH -> PhenomenonCrimson to PhenomenonCrimson.copy(alpha = 0.15f)
        Severity.MEDIUM -> PhenomenonFlameAmber to PhenomenonFlameAmber.copy(alpha = 0.15f)
        Severity.LOW -> PhenomenonEmerald to PhenomenonEmerald.copy(alpha = 0.15f)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 9.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = severity.label.uppercase(),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
fun StatusBadge(status: PotholeStatus, modifier: Modifier = Modifier) {
    val (color, icon) = when (status) {
        PotholeStatus.OPEN -> PhenomenonCrimson to Icons.Default.Warning
        PotholeStatus.UNDER_VERIFICATION -> PhenomenonFlameAmber to Icons.Default.PendingActions
        PotholeStatus.ASSIGNED -> PhenomenonPurpleNeon to Icons.Default.PendingActions
        PotholeStatus.IN_REPAIR -> PhenomenonCyanElectric to Icons.Default.Build
        PotholeStatus.RESOLVED -> PhenomenonEmerald to Icons.Default.CheckCircle
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = status.label,
            tint = color,
            modifier = Modifier.size(11.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = status.label.uppercase(),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.6.sp
        )
    }
}

@Composable
fun PotholeItemCard(
    pothole: Pothole,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(PhenomenonSurface)
            .border(1.dp, PhenomenonBorder, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("pothole_item_${pothole.potholeId}")
            .padding(16.dp)
    ) {
        Column {
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
                            .background(
                                when (pothole.severity) {
                                    Severity.HIGH -> PhenomenonCrimson
                                    Severity.MEDIUM -> PhenomenonFlameAmber
                                    Severity.LOW -> PhenomenonEmerald
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = pothole.potholeId,
                        fontWeight = FontWeight.Black,
                        color = PhenomenonTextPrimary,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                }
                SeverityBadge(severity = pothole.severity)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = pothole.address,
                color = PhenomenonTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            if (pothole.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = pothole.notes,
                    color = PhenomenonTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 2,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(status = pothole.status)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = PhenomenonCyanElectric,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "%.4f, %.4f".format(pothole.latitude, pothole.longitude),
                        color = PhenomenonTextTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
