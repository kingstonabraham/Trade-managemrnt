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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Trade
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.DecimalFormat

@Composable
fun TradeCardItem(
    trade: Trade,
    onClick: () -> Unit,
    showDate: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Generate avatar color based on symbol
    val avatarBg = when {
        trade.symbol.startsWith("R", ignoreCase = true) -> Color(0xFF059669) // Emerald
        trade.symbol.startsWith("P", ignoreCase = true) || trade.symbol.startsWith("N", ignoreCase = true) -> Color(0xFFE11D48) // Rose/Pink
        trade.symbol.startsWith("T", ignoreCase = true) -> Color(0xFF7C3AED) // Purple
        trade.symbol.startsWith("I", ignoreCase = true) -> Color(0xFF2563EB) // Blue
        trade.symbol.startsWith("H", ignoreCase = true) -> Color(0xFF0284C7) // Sky
        trade.symbol.startsWith("B", ignoreCase = true) -> Color(0xFF9333EA) // Magenta
        else -> Color(0xFF4F46E5) // Indigo
    }

    val symbolInitial = trade.symbol.firstOrNull()?.uppercase() ?: "T"

    val isProfit = trade.netPnl > 0
    val isLoss = trade.netPnl < 0
    val pnlColor = when {
        isProfit -> ProfitGreen
        isLoss -> LossRed
        else -> TextSecondary
    }

    val df = DecimalFormat("#,##0")
    val pnlText = when {
        trade.isOpen -> "OPEN"
        isProfit -> "+₹${df.format(trade.netPnl)}"
        isLoss -> "-₹${df.format(kotlin.math.abs(trade.netPnl))}"
        else -> "₹0"
    }

    val pnlPctText = when {
        trade.isOpen -> "Pending"
        else -> {
            val prefix = if (trade.pnlPercentage >= 0) "+" else ""
            String.format("P&L %s%.2f%%", prefix, trade.pnlPercentage)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceCard)
            .border(1.dp, DarkBorderSubtle, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("trade_card_${trade.id}")
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Badge + Symbol & details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Initial Badge (Rounded square like screenshot)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(avatarBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = symbolInitial,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = trade.symbol,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = trade.tradeType,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        if (trade.leverage > 1) {
                            Text(
                                text = " • ${trade.leverage}x",
                                color = Color(0xFFFF9100),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = " • ",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        val timeDisplay = if (showDate) {
                            "${trade.entryDate}, ${trade.entryTime}"
                        } else {
                            trade.entryTime
                        }
                        Text(
                            text = timeDisplay,
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right: P&L and % Return
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = pnlText,
                    color = pnlColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = pnlPctText,
                    color = pnlColor.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
