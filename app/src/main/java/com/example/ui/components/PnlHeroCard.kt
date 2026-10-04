package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.LossRed
import com.example.ui.theme.LossRedBadgeBg
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.ProfitGreenBadgeBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.DecimalFormat

@Composable
fun PnlHeroCard(
    title: String,
    totalPnl: Double,
    pnlPercentage: Double,
    totalTrades: Int,
    winRate: Int,
    modifier: Modifier = Modifier
) {
    val isProfit = totalPnl >= 0
    val pnlColor = if (isProfit) NeonCyan else LossRed
    val badgeTextColor = if (isProfit) ProfitGreen else LossRed
    val badgeBgColor = if (isProfit) ProfitGreenBadgeBg else LossRedBadgeBg

    val df = DecimalFormat("#,##0")
    val pnlPrefix = if (isProfit) "+ ₹" else "- ₹"
    val pnlFormatted = "$pnlPrefix${df.format(kotlin.math.abs(totalPnl))}"

    val pctPrefix = if (pnlPercentage >= 0) "+" else ""
    val pctFormatted = String.format("%s%.2f%%", pctPrefix, pnlPercentage)

    // Visual mini performance graph bars (7 bars)
    val barRatios = listOf(0.45f, 0.65f, 0.35f, 0.85f, 0.55f, 0.95f, 0.75f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(DarkSurface)
            .border(
                1.5.dp,
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF38235C),
                        Color(0xFF1E2B48),
                        Color(0xFF131D33)
                    )
                ),
                RoundedCornerShape(22.dp)
            )
            .testTag("pnl_hero_card")
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Row: Title + Return Percentage Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                // Percentage Badge Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(badgeBgColor)
                        .border(1.dp, badgeTextColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = pctFormatted,
                        color = badgeTextColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Middle Row: Big P&L text + Mini Performance Graph
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large P&L
                Text(
                    text = pnlFormatted,
                    color = pnlColor,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )

                // 7 mini vertical performance bars with purple-pink gradient
                MiniPerformanceBars(
                    barRatios = barRatios,
                    isProfit = isProfit
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom row: Total Trades & Win Rate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Trades ",
                    color = TextMuted,
                    fontSize = 13.sp
                )
                Text(
                    text = "$totalTrades",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "   |   ",
                    color = TextMuted.copy(alpha = 0.5f),
                    fontSize = 13.sp
                )

                Text(
                    text = "Win Rate ",
                    color = TextMuted,
                    fontSize = 13.sp
                )
                Text(
                    text = "$winRate%",
                    color = if (winRate >= 50) ProfitGreen else LossRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MiniPerformanceBars(
    barRatios: List<Float>,
    isProfit: Boolean,
    modifier: Modifier = Modifier
) {
    val barBrush = if (isProfit) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFE040FB), // Magenta
                Color(0xFF8B5CF6)  // Violet
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFF3366),
                Color(0xFF991B1B)
            )
        )
    }

    Row(
        modifier = modifier
            .height(36.dp)
            .padding(end = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        barRatios.forEach { ratio ->
            var startAnim by remember { mutableStateOf(false) }
            val heightFraction by animateFloatAsState(
                targetValue = if (startAnim) ratio else 0.1f,
                animationSpec = tween(durationMillis = 600),
                label = "bar_anim"
            )

            LaunchedEffect(Unit) {
                startAnim = true
            }

            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height((36 * heightFraction).dp.coerceAtLeast(6.dp))
                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp, bottomStart = 1.dp, bottomEnd = 1.dp))
                    .background(barBrush)
            )
        }
    }
}
