package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Dark Fintech Palette
val DarkBackground = Color(0xFF090D16)
val DarkSurface = Color(0xFF111726)
val DarkSurfaceElevated = Color(0xFF172033)
val DarkSurfaceCard = Color(0xFF131A2B)
val DarkBorder = Color(0xFF1F2B42)
val DarkBorderSubtle = Color(0xFF192235)

// Accent Neons
val NeonCyan = Color(0xFF00E5FF)
val NeonCyanLight = Color(0xFF70F3FF)
val ProfitGreen = Color(0xFF00E676)
val ProfitGreenDark = Color(0xFF052E16)
val ProfitGreenBadgeBg = Color(0xFF063A24)
val LossRed = Color(0xFFFF3366)
val LossRedDark = Color(0xFF38101A)
val LossRedBadgeBg = Color(0xFF3B121F)
val NeonPurple = Color(0xFF8B5CF6)
val NeonMagenta = Color(0xFFE040FB)
val AccentPink = Color(0xFFEC4899)
val AccentOrange = Color(0xFFFF9100)
val AccentGold = Color(0xFFFFD600)

// Text Colors
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

// Gradients
val PrimaryGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF8B5CF6), Color(0xFFD946EF))
)

val CyanGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF00C6FF), Color(0xFF0072FF))
)

val ActionButtonGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF00E5FF), Color(0xFF9B51E0), Color(0xFFE040FB))
)

val CardGlowGradient = Brush.verticalGradient(
    colors = listOf(Color(0x228B5CF6), Color(0x0500E5FF), Color(0x00000000))
)

val BuyButtonGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF00C6FF), Color(0xFF8B5CF6))
)

val SellButtonGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFEC4899), Color(0xFFFF3366))
)
