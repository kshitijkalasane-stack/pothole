package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ========================================================================
// SKEUOMORPHIC LIGHT PALETTE & TACTILE TOKENS
// ========================================================================
// Tactile light surfaces, specular highlights, deep cast shadows, 
// physical metallic accents, chrome bezels, and illuminated LED diodes.

// Base Canvas & Neutral Surfaces
val SkeuoCanvas = Color(0xFFEFF3F8)          // Soft tactile platinum/warm ceramic canvas
val SkeuoSurface = Color(0xFFF7FAFD)         // Raised card surface
val SkeuoSurfaceElevated = Color(0xFFFFFFFF) // High-elevation card / active button surface
val SkeuoSurfacePressed = Color(0xFFE2E8F0)  // Depressed / pressed tactile state
val SkeuoWellInset = Color(0xFFE5ECF4)       // Sunken well for LCD meters & inputs

// Tactile Highlights & Shadows
val SkeuoHighlight = Color(0xFFFFFFFF)       // Top/Left 3D specular light edge (100% white)
val SkeuoHighlightSoft = Color(0x99FFFFFF)   // 60% white gloss
val SkeuoShadowDark = Color(0xFFCBD5E1)      // Bottom/Right soft ambient drop shadow
val SkeuoShadowDeep = Color(0xFF94A3B8)      // Deep cast shadow
val SkeuoBorderLight = Color(0xFFD8E2ED)     // Bevel divider line
val SkeuoChromeBezel = Color(0xFF94A3B8)     // Brushed metallic trim
val SkeuoChromeBezelLight = Color(0xFFE2E8F0)// Chrome highlight

// Vibrant Skeuomorphic Accents (Rich & High-Contrast on light canvas)
val SkeuoCobalt = Color(0xFF2563EB)          // Precision Instrument Blue
val SkeuoCyan = Color(0xFF0284C7)            // Telemetry Radar Sky Blue
val SkeuoCrimson = Color(0xFFDC2626)         // Warning / High Severity Beacon Red
val SkeuoAmber = Color(0xFFD97706)           // Hazard Warning Light Amber
val SkeuoEmerald = Color(0xFF059669)         // Active System / Verified Emerald Green
val SkeuoPurple = Color(0xFF7C3AED)          // AI Intelligence Royal Purple
val SkeuoElectricLime = Color(0xFF65A30D)    // High-visibility sensor lime

// Physical LED Status Lights
val SkeuoLedGreenOn = Color(0xFF10B981)
val SkeuoLedGreenGlow = Color(0x6610B981)
val SkeuoLedRedOn = Color(0xFFEF4444)
val SkeuoLedRedGlow = Color(0x66EF4444)
val SkeuoLedAmberOn = Color(0xFFF59E0B)
val SkeuoLedAmberGlow = Color(0x66F59E0B)
val SkeuoLedBlueOn = Color(0xFF3B82F6)
val SkeuoLedBlueGlow = Color(0x663B82F6)

// Typography (High Contrast, Deep Slate Tones)
val SkeuoTextPrimary = Color(0xFF0F172A)     // Dark charcoal slate (primary)
val SkeuoTextSecondary = Color(0xFF475569)   // Medium slate (subtitles, metrics)
val SkeuoTextTertiary = Color(0xFF64748B)    // Muted slate (timestamps, labels)
val SkeuoTextInverse = Color(0xFFFFFFFF)     // Crisp white text on colored buttons

// Backwards compatibility aliases (maps old Phenomenon names to light Skeuo tokens)
val PhenomenonCanvas = SkeuoCanvas
val PhenomenonSurface = SkeuoSurface
val PhenomenonSurfaceElevated = SkeuoSurfaceElevated
val PhenomenonSurfaceHover = SkeuoSurfacePressed
val PhenomenonBorder = SkeuoBorderLight
val PhenomenonBorderActive = SkeuoCobalt
val PhenomenonElectricLime = SkeuoElectricLime
val PhenomenonPurpleNeon = SkeuoPurple
val PhenomenonCyanElectric = SkeuoCobalt
val PhenomenonFlameAmber = SkeuoAmber
val PhenomenonCrimson = SkeuoCrimson
val PhenomenonEmerald = SkeuoEmerald
val PhenomenonTextPrimary = SkeuoTextPrimary
val PhenomenonTextSecondary = SkeuoTextSecondary
val PhenomenonTextTertiary = SkeuoTextTertiary
val PhenomenonGlassBackground = Color(0xE6F7FAFD)
val PhenomenonGlassBorder = SkeuoHighlight
