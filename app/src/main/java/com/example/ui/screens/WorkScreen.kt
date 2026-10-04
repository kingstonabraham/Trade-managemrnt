package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.outlinedFieldColors
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.LossRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.DecimalFormat

@Composable
fun WorkScreen(
    modifier: Modifier = Modifier
) {
    // 1. Fixed Trading Capital
    var capitalText by remember { mutableStateOf("100000") }

    // 2. Risk Percentage (%)
    var riskPctText by remember { mutableStateOf("2.0") }

    // 3. Entry, Stop Loss, Target
    var entryText by remember { mutableStateOf("2450") }
    var slText by remember { mutableStateOf("2400") }
    var targetText by remember { mutableStateOf("2550") }

    val df = DecimalFormat("#,##0.00")
    val dfInt = DecimalFormat("#,##0")

    val riskPresets = listOf("1.0", "1.5", "2.0", "3.0")
    val capitalPresets = listOf("50000", "100000", "200000", "500000")

    // Automatic calculation of Maximum Risk and Position Sizing
    val calcResults by remember {
        derivedStateOf {
            val capital = capitalText.toDoubleOrNull() ?: 0.0
            val riskPct = riskPctText.toDoubleOrNull() ?: 0.0
            val entry = entryText.toDoubleOrNull() ?: 0.0
            val sl = slText.toDoubleOrNull() ?: 0.0
            val target = targetText.toDoubleOrNull() ?: 0.0

            // Maximum Risk = Capital * Risk%
            val maxRiskAmount = (capital * riskPct) / 100.0

            // Stop Loss difference per share
            val slPoints = kotlin.math.abs(entry - sl)

            // Safe quantity to buy so loss never exceeds maxRiskAmount
            val recommendedQty = if (slPoints > 0) (maxRiskAmount / slPoints).toInt() else 0

            // Total investment required
            val totalCapitalNeeded = recommendedQty * entry

            // Target profit difference per share
            val targetPoints = kotlin.math.abs(target - entry)
            val potentialProfit = recommendedQty * targetPoints

            // Risk to Reward Ratio
            val rrRatio = if (slPoints > 0) targetPoints / slPoints else 0.0

            RiskPlan(
                maxRisk = maxRiskAmount,
                slPoints = slPoints,
                recommendedQty = recommendedQty,
                totalCapitalNeeded = totalCapitalNeeded,
                potentialProfit = potentialProfit,
                rrRatio = rrRatio,
                isFavorable = rrRatio >= 1.5
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("work_screen_content"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp)
        ) {
            // Screen Header
            item {
                Text(
                    text = "Risk & Position Size Workspace",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Set your fixed capital, risk percentage, and let the calculator determine your maximum risk and position size.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 1. Fixed Trading Capital Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkSurfaceCard)
                        .border(1.dp, DarkBorderSubtle, RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Fixed Trading Capital",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Fixed Lock Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(NeonPurple.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Account Capital", color = NeonPurple, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = capitalText,
                            onValueChange = { capitalText = it },
                            prefix = { Text("₹ ", color = NeonCyan, fontWeight = FontWeight.Bold) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = outlinedFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("fixed_capital_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Capital Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            capitalPresets.forEach { preset ->
                                val isSelected = capitalText == preset
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) NeonCyan.copy(alpha = 0.15f) else DarkSurfaceElevated)
                                        .border(1.dp, if (isSelected) NeonCyan else DarkBorderSubtle, RoundedCornerShape(8.dp))
                                        .clickable { capitalText = preset }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "₹${dfInt.format(preset.toDouble())}",
                                        color = if (isSelected) NeonCyan else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 2. Risk Percentage (%) Section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkSurfaceCard)
                        .border(1.dp, DarkBorderSubtle, RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFFFF9100), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Risk Percentage per Trade (%)",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "How much percentage of your capital you are willing to risk on this trade.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            riskPresets.forEach { preset ->
                                val isSelected = riskPctText == preset
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Color(0xFFFF9100).copy(alpha = 0.2f) else DarkSurfaceElevated)
                                        .border(1.dp, if (isSelected) Color(0xFFFF9100) else DarkBorderSubtle, RoundedCornerShape(10.dp))
                                        .clickable { riskPctText = preset }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$preset%",
                                        color = if (isSelected) Color(0xFFFF9100) else TextSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 3. Automated "Maximum Risk" Card (Clearly Explained for the user!)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkSurface)
                        .border(
                            1.5.dp,
                            Brush.horizontalGradient(
                                listOf(Color(0xFFFF3366).copy(alpha = 0.6f), Color(0xFF8B5CF6).copy(alpha = 0.4f))
                            ),
                            RoundedCornerShape(18.dp)
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "YOUR MAXIMUM RISK",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "₹ ${df.format(calcResults.maxRisk)}",
                                    color = LossRed,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(LossRed.copy(alpha = 0.12f))
                                    .border(1.dp, LossRed.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Auto Calculated",
                                    color = LossRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkBackground.copy(alpha = 0.6f))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "You don't need to calculate this yourself! Based on your fixed capital of ₹${dfInt.format(capitalText.toDoubleOrNull() ?: 0.0)} and $riskPctText% risk, you will never lose more than ₹${df.format(calcResults.maxRisk)} if the trade hits your stop loss.",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 4. Trade Entry, Stop Loss, and Target Inputs
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkSurfaceCard)
                        .border(1.dp, DarkBorderSubtle, RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Trade Execution Setup",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Entry, Stop Loss, Target
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Entry Price
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Entry Price (₹) *", color = TextMuted, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = entryText,
                                    onValueChange = { entryText = it },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    colors = outlinedFieldColors(),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Stop Loss
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Stop Loss (₹) *", color = LossRed, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = slText,
                                    onValueChange = { slText = it },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    colors = outlinedFieldColors(),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Target
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Target (₹) *", color = ProfitGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = targetText,
                                    onValueChange = { targetText = it },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    colors = outlinedFieldColors(),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 5. Recommended Position Size & Trade Plan Results
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkSurfaceCard)
                        .border(
                            1.dp,
                            if (calcResults.isFavorable) ProfitGreen.copy(alpha = 0.5f) else DarkBorderSubtle,
                            RoundedCornerShape(18.dp)
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recommended Position",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // R:R Badge
                            val rrBadgeBg = if (calcResults.isFavorable) ProfitGreen.copy(alpha = 0.15f) else LossRed.copy(alpha = 0.15f)
                            val rrBadgeColor = if (calcResults.isFavorable) ProfitGreen else LossRed
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(rrBadgeBg)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "R:R 1 : ${String.format(java.util.Locale.US, "%.2f", calcResults.rrRatio)}",
                                    color = rrBadgeColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Large Recommended Quantity Display
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceElevated)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Buy Exactly This Quantity:",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${calcResults.recommendedQty} Shares",
                                    color = NeonCyan,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "SL Distance",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "₹ ${df.format(calcResults.slPoints)} / share",
                                    color = LossRed,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Summary Rows
                        DetailRow(label = "Total Investment Needed", value = "₹ ${df.format(calcResults.totalCapitalNeeded)}")
                        DetailRow(label = "Maximum Possible Loss", value = "- ₹ ${df.format(calcResults.maxRisk)}", valueColor = LossRed)
                        DetailRow(label = "Expected Profit at Target", value = "+ ₹ ${df.format(calcResults.potentialProfit)}", valueColor = ProfitGreen)

                        Spacer(modifier = Modifier.height(8.dp))

                        // Status Note
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (calcResults.isFavorable) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (calcResults.isFavorable) ProfitGreen else LossRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val statusNote = if (calcResults.isFavorable) {
                                "Favorable setup: Reward is at least 1.5x your maximum risk."
                            } else {
                                "Caution: Reward is lower than 1.5x risk. Consider widening target or tightening SL."
                            }
                            Text(
                                text = statusNote,
                                color = if (calcResults.isFavorable) ProfitGreen else LossRed,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

data class RiskPlan(
    val maxRisk: Double,
    val slPoints: Double,
    val recommendedQty: Int,
    val totalCapitalNeeded: Double,
    val potentialProfit: Double,
    val rrRatio: Double,
    val isFavorable: Boolean
)
