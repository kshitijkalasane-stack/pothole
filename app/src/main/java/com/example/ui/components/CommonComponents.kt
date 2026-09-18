package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Pothole
import com.example.data.models.PotholeStatus
import com.example.data.models.Severity
import com.example.ui.theme.SkeuoAmber
import com.example.ui.theme.SkeuoBorderLight
import com.example.ui.theme.SkeuoCanvas
import com.example.ui.theme.SkeuoChromeBezel
import com.example.ui.theme.SkeuoCobalt
import com.example.ui.theme.SkeuoCrimson
import com.example.ui.theme.SkeuoEmerald
import com.example.ui.theme.SkeuoHighlight
import com.example.ui.theme.SkeuoHighlightSoft
import com.example.ui.theme.SkeuoLedGreenGlow
import com.example.ui.theme.SkeuoLedGreenOn
import com.example.ui.theme.SkeuoLedRedGlow
import com.example.ui.theme.SkeuoLedRedOn
import com.example.ui.theme.SkeuoPurple
import com.example.ui.theme.SkeuoShadowDark
import com.example.ui.theme.SkeuoShadowDeep
import com.example.ui.theme.SkeuoSurface
import com.example.ui.theme.SkeuoSurfaceElevated
import com.example.ui.theme.SkeuoSurfacePressed
import com.example.ui.theme.SkeuoTextPrimary
import com.example.ui.theme.SkeuoTextSecondary
import com.example.ui.theme.SkeuoTextTertiary
import com.example.ui.theme.SkeuoWellInset
import kotlin.math.cos
import kotlin.math.sin

/**
 * Skeuomorphic Raised Card Modifier
 * Simulates a tactile extruded physical plastic/aluminum plate with dual light/shadow bevels.
 */
fun Modifier.skeuomorphicCard(
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 5.dp
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = RoundedCornerShape(cornerRadius),
        ambientColor = SkeuoShadowDeep.copy(alpha = 0.25f),
        spotColor = SkeuoShadowDark.copy(alpha = 0.40f)
    )
    .clip(RoundedCornerShape(cornerRadius))
    .background(
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF6F9FD),
                Color(0xFFEDF2F7)
            )
        )
    )
    .border(
        width = 1.dp,
        brush = Brush.verticalGradient(
            colors = listOf(
                SkeuoHighlight,
                SkeuoBorderLight,
                SkeuoShadowDark.copy(alpha = 0.6f)
            )
        ),
        shape = RoundedCornerShape(cornerRadius)
    )

/**
 * Skeuomorphic Inset Well Modifier
 * Simulates a sunken/recessed chamber with dark top inner bevel and bottom specular edge.
 */
fun Modifier.skeuomorphicInset(
    cornerRadius: Dp = 14.dp
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFDFE6F0),
                Color(0xFFE8EEF6),
                Color(0xFFF1F5FA)
            )
        )
    )
    .border(
        width = 1.dp,
        brush = Brush.verticalGradient(
            colors = listOf(
                SkeuoShadowDark,
                SkeuoBorderLight,
                SkeuoHighlight
            )
        ),
        shape = RoundedCornerShape(cornerRadius)
    )

/**
 * Skeuomorphic Tactile Press Feedback Modifier
 * Provides spring-physics scale depression, downward translation, and tactile feel.
 */
@Composable
fun Modifier.tactilePress(
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    enabled: Boolean = true,
    pressedScale: Float = 0.97f,
    pressedTranslationY: Dp = 1.5.dp,
    onClick: (() -> Unit)? = null
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tactile_scale"
    )
    val translationY by animateDpAsState(
        targetValue = if (isPressed && enabled) pressedTranslationY else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tactile_translation"
    )

    val baseModifier = this.graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.translationY = translationY.toPx()
    }

    return if (onClick != null) {
        baseModifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
    } else {
        baseModifier
    }
}

/**
 * Skeuomorphic Physical Push Button
 */
@Composable
fun SkeuomorphicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = SkeuoCobalt,
    cornerRadius: Dp = 14.dp,
    enabled: Boolean = true,
    tag: String = "skeuo_button",
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "button_scale"
    )
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 1.dp else 4.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "button_elevation"
    )
    val translationY by animateDpAsState(
        targetValue = if (isPressed && enabled) 2.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "button_translation"
    )

    val topColor = if (isPressed) backgroundColor.copy(alpha = 0.82f) else backgroundColor
    val bottomColor = if (isPressed) backgroundColor else backgroundColor.copy(alpha = 0.88f)

    Box(
        modifier = modifier
            .testTag(tag)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.translationY = translationY.toPx()
            }
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = SkeuoShadowDeep.copy(alpha = 0.3f),
                spotColor = SkeuoShadowDark.copy(alpha = 0.45f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(
                    colors = if (isPressed) {
                        listOf(bottomColor, topColor)
                    } else {
                        listOf(topColor, bottomColor)
                    }
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    colors = if (isPressed) {
                        listOf(SkeuoShadowDark, SkeuoHighlight.copy(alpha = 0.4f))
                    } else {
                        listOf(SkeuoHighlight.copy(alpha = 0.6f), SkeuoShadowDark.copy(alpha = 0.5f))
                    }
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * Skeuomorphic LED Indicator Diode
 * Realistic illuminated status lens with chrome bezel ring and specular refraction.
 */
@Composable
fun SkeuomorphicLed(
    isOn: Boolean,
    color: Color = SkeuoLedGreenOn,
    label: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE2E8F0),
                            SkeuoChromeBezel,
                            Color(0xFF64748B)
                        )
                    )
                )
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        if (isOn) {
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White,
                                    color,
                                    color.copy(alpha = 0.85f)
                                )
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF94A3B8),
                                    Color(0xFF475569)
                                )
                            )
                        }
                    )
            )
        }

        if (label != null) {
            Text(
                text = label,
                color = SkeuoTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Skeuomorphic Analog Telemetry Gauge (Speed / G-Force Needle Meter)
 */
@Composable
fun SkeuomorphicGauge(
    value: Float,
    maxValue: Float,
    unit: String,
    title: String,
    modifier: Modifier = Modifier,
    needleColor: Color = SkeuoCrimson
) {
    val animatedValue by animateFloatAsState(
        targetValue = value.coerceIn(0f, maxValue),
        label = "gauge_anim"
    )

    Box(
        modifier = modifier
            .skeuomorphicCard(cornerRadius = 18.dp, elevation = 4.dp)
            .padding(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title.uppercase(),
                color = SkeuoTextTertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Gauge Face Box
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .skeuomorphicInset(cornerRadius = 65.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width / 2f - 8f

                    // Dial background track arc (135° to 405°)
                    drawArc(
                        color = Color(0xFFCBD5E1),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = 8f, cap = StrokeCap.Round)
                    )

                    // Active colored arc
                    val sweep = (animatedValue / maxValue) * 270f
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(SkeuoEmerald, SkeuoAmber, SkeuoCrimson)
                        ),
                        startAngle = 135f,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = 8f, cap = StrokeCap.Round)
                    )

                    // Tick marks
                    for (i in 0..10) {
                        val angle = Math.toRadians((135.0 + (i * 27.0)))
                        val tickStart = Offset(
                            (center.x + (radius - 14f) * cos(angle)).toFloat(),
                            (center.y + (radius - 14f) * sin(angle)).toFloat()
                        )
                        val tickEnd = Offset(
                            (center.x + (radius - 6f) * cos(angle)).toFloat(),
                            (center.y + (radius - 6f) * sin(angle)).toFloat()
                        )
                        drawLine(
                            color = Color(0xFF94A3B8),
                            start = tickStart,
                            end = tickEnd,
                            strokeWidth = if (i % 5 == 0) 2.5f else 1.2f
                        )
                    }

                    // Needle
                    val needleAngle = Math.toRadians((135.0 + (animatedValue / maxValue * 270.0)))
                    val needleEnd = Offset(
                        (center.x + (radius - 16f) * cos(needleAngle)).toFloat(),
                        (center.y + (radius - 16f) * sin(needleAngle)).toFloat()
                    )
                    drawLine(
                        color = needleColor,
                        start = center,
                        end = needleEnd,
                        strokeWidth = 3.5f,
                        cap = StrokeCap.Round
                    )

                    // Chrome Center Pivot Cap
                    drawCircle(color = Color(0xFF475569), radius = 9f, center = center)
                    drawCircle(color = Color(0xFFE2E8F0), radius = 5f, center = center)
                    drawCircle(color = Color.White, radius = 2f, center = center)
                }

                // Digital Readout Inset
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "%.1f".format(animatedValue),
                        color = SkeuoTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = unit,
                        color = SkeuoTextTertiary,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Skeuomorphic Severity Badge
 */
@Composable
fun SeverityBadge(severity: Severity, modifier: Modifier = Modifier) {
    val (color, topHighlight) = when (severity) {
        Severity.HIGH -> SkeuoCrimson to Color(0xFFFFB4B4)
        Severity.MEDIUM -> SkeuoAmber to Color(0xFFFFE0B2)
        Severity.LOW -> SkeuoEmerald to Color(0xFFC8E6C9)
    }

    Box(
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(8.dp), ambientColor = color.copy(alpha = 0.3f))
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        topHighlight.copy(alpha = 0.45f),
                        color.copy(alpha = 0.15f)
                    )
                )
            )
            .border(
                1.dp,
                Brush.verticalGradient(listOf(topHighlight, color.copy(alpha = 0.7f))),
                RoundedCornerShape(8.dp)
            )
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

/**
 * Skeuomorphic Status Badge
 */
@Composable
fun StatusBadge(status: PotholeStatus, modifier: Modifier = Modifier) {
    val (color, icon) = when (status) {
        PotholeStatus.OPEN -> SkeuoCrimson to Icons.Default.Warning
        PotholeStatus.UNDER_VERIFICATION -> SkeuoAmber to Icons.Default.PendingActions
        PotholeStatus.ASSIGNED -> SkeuoPurple to Icons.Default.PendingActions
        PotholeStatus.IN_REPAIR -> SkeuoCobalt to Icons.Default.Build
        PotholeStatus.RESOLVED -> SkeuoEmerald to Icons.Default.CheckCircle
    }

    Row(
        modifier = modifier
            .shadow(1.5.dp, RoundedCornerShape(20.dp), ambientColor = color.copy(alpha = 0.25f))
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        color.copy(alpha = 0.12f)
                    )
                )
            )
            .border(
                1.dp,
                Brush.verticalGradient(listOf(SkeuoHighlight, color.copy(alpha = 0.45f))),
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = status.label,
            tint = color,
            modifier = Modifier.size(11.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = status.label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Skeuomorphic Pothole Inspection Card
 */
@Composable
fun PotholeItemCard(
    pothole: Pothole,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) = PotholeListItemCard(pothole = pothole, onClick = onClick, modifier = modifier)

@Composable
fun PotholeListItemCard(
    pothole: Pothole,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.975f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pothole_card_scale"
    )
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 1.5.dp else 4.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "pothole_card_elevation"
    )
    val translationY by animateDpAsState(
        targetValue = if (isPressed) 1.5.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "pothole_card_translation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.translationY = translationY.toPx()
            }
            .skeuomorphicCard(cornerRadius = 16.dp, elevation = elevation)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(14.dp)
            .testTag("pothole_item_${pothole.potholeId}")
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFFFFFF),
                                        Color(0xFFE2E8F0)
                                    )
                                )
                            )
                            .border(1.dp, SkeuoBorderLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = when (pothole.severity) {
                                Severity.HIGH -> SkeuoCrimson
                                Severity.MEDIUM -> SkeuoAmber
                                Severity.LOW -> SkeuoEmerald
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = pothole.potholeId,
                            color = SkeuoTextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                        Text(
                            text = pothole.address,
                            color = SkeuoTextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
                SeverityBadge(severity = pothole.severity)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tactile Bottom Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(status = pothole.status)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Confidence: ${(pothole.confidence * 100).toInt()}%",
                        color = SkeuoTextTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${pothole.reportCount} reports",
                        color = SkeuoCobalt,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Visual Indicator Warning & Status Card for Room Database and Network Sync
 */
@Composable
fun RoomSyncStatusCard(
    isNetworkAvailable: Boolean,
    pendingSyncCount: Int,
    onSyncNow: () -> Unit,
    onToggleSimulation: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val statusColor = when {
        !isNetworkAvailable -> SkeuoCrimson
        pendingSyncCount > 0 -> SkeuoAmber
        else -> SkeuoEmerald
    }

    val icon = when {
        !isNetworkAvailable -> Icons.Default.CloudOff
        pendingSyncCount > 0 -> Icons.Default.SyncProblem
        else -> Icons.Default.CloudDone
    }

    val statusTitle = when {
        !isNetworkAvailable -> "OFFLINE MODE — ROOM DB CACHED"
        pendingSyncCount > 0 -> "ROOM DB OUT OF SYNC ($pendingSyncCount PENDING)"
        else -> "ROOM DB SYNCED & ONLINE"
    }

    val statusSubtitle = when {
        !isNetworkAvailable -> "No network connection. Pothole detections and reports are securely persisted locally in Room SQLite."
        pendingSyncCount > 0 -> "$pendingSyncCount local event(s) recorded offline waiting to upload to municipal cloud server."
        else -> "Local Room database is fully synchronized with central cloud repository."
    }

    val actionText = when {
        !isNetworkAvailable -> "TOGGLE NET"
        pendingSyncCount > 0 -> "SYNC NOW"
        else -> "SYNCED"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        statusColor.copy(alpha = 0.08f)
                    )
                )
            )
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(SkeuoHighlight, statusColor.copy(alpha = 0.45f))
                ),
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
            .testTag("room_sync_status_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(1.dp, statusColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = statusTitle,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SkeuomorphicLed(
                            isOn = isNetworkAvailable && pendingSyncCount == 0,
                            color = statusColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statusTitle,
                            color = statusColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = statusSubtitle,
                        color = SkeuoTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Pill
            if (!isNetworkAvailable || pendingSyncCount > 0) {
                Box(
                    modifier = Modifier
                        .shadow(2.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusColor)
                        .tactilePress(pressedScale = 0.93f) {
                            if (isNetworkAvailable && pendingSyncCount > 0) {
                                onSyncNow()
                            } else {
                                onToggleSimulation?.invoke()
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                        .testTag("sync_action_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (pendingSyncCount > 0) Icons.Default.Sync else Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = actionText,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SkeuoEmerald.copy(alpha = 0.12f))
                        .border(1.dp, SkeuoEmerald.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "SYNCED",
                        color = SkeuoEmerald,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
