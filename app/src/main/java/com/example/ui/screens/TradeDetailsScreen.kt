package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Trade
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.LossRed
import com.example.ui.theme.LossRedBadgeBg
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.ProfitGreenBadgeBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TradeViewModel
import com.example.util.TradingDateUtils
import java.text.DecimalFormat

@Composable
fun TradeDetailsScreen(
    tradeId: Long,
    viewModel: TradeViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    val allTrades by viewModel.allTrades.collectAsStateWithLifecycle()
    val trade = allTrades.find { it.id == tradeId }

    var showDeleteDialog by remember { mutableStateOf(false) }

    val df = DecimalFormat("#,##0.00")

    if (trade == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            Text("Trade not found", color = TextSecondary)
        }
        return
    }

    val isProfit = trade.netPnl > 0
    val isLoss = trade.netPnl < 0
    val statusText = when {
        trade.isOpen -> "OPEN"
        isProfit -> "PROFIT"
        else -> "LOSS"
    }

    val statusTextColor = when {
        trade.isOpen -> NeonCyan
        isProfit -> ProfitGreen
        else -> LossRed
    }

    val statusBgColor = when {
        trade.isOpen -> NeonCyan.copy(alpha = 0.15f)
        isProfit -> ProfitGreenBadgeBg
        else -> LossRedBadgeBg
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Section: Back, Title, Edit, Delete
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Trade Details",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    // Edit Icon
                    IconButton(
                        onClick = { viewModel.openAddTrade(trade) },
                        modifier = Modifier.testTag("edit_trade_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Trade",
                            tint = NeonCyan
                        )
                    }

                    // Delete Icon
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.testTag("delete_trade_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Trade",
                            tint = LossRed
                        )
                    }
                }
            }

            // Scrollable Trade Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("trade_details_content"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // 1. Trade Header (Symbol, Status Badge, Tags)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = trade.symbol,
                            color = TextPrimary,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        // Status Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(statusBgColor)
                                .border(1.dp, statusTextColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = statusText,
                                color = statusTextColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Badges row: Market Segment, Equity/Type, Exchange, Leverage
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(androidx.compose.foundation.rememberScrollState())
                    ) {
                        BadgeTag(text = trade.marketSegment, tint = NeonPurple)
                        BadgeTag(text = trade.tradeType, tint = NeonCyan)
                        BadgeTag(text = trade.exchange, tint = TextSecondary)
                        BadgeTag(text = "${trade.leverage}x Leverage", tint = Color(0xFFFF9100))
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // 2. Trade Execution Section (Buy/Sell, entry/exit date & time, prices, quantity)
                item {
                    val isBuy = trade.buySell.equals("BUY", ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(DarkSurfaceCard)
                            .border(1.dp, DarkBorderSubtle, RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            // Buy vs Sell header row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = if (isBuy) "Buy" else "Sell (Short)",
                                        color = if (isBuy) NeonCyan else LossRed,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${trade.entryDate}, ${trade.entryTime}",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                }

                                if (trade.exitPrice != null && trade.exitPrice > 0) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = if (isBuy) "Sell (Exit)" else "Buy (Cover)",
                                            color = if (isBuy) LossRed else NeonCyan,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        val exitDateStr = trade.exitDate ?: trade.entryDate
                                        val exitTimeStr = trade.exitTime ?: "Market Close"
                                        Text(
                                            text = "$exitDateStr, $exitTimeStr",
                                            color = TextMuted,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Entry Price, Qty vs Exit Price, Qty
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Column {
                                            Text("Entry Price", color = TextMuted, fontSize = 12.sp)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text("₹ ${df.format(trade.entryPrice)}", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                        Column {
                                            Text("Qty", color = TextMuted, fontSize = 12.sp)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text("${trade.quantity}", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }

                                if (trade.exitPrice != null && trade.exitPrice > 0) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("Exit Price", color = TextMuted, fontSize = 12.sp)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text("₹ ${df.format(trade.exitPrice)}", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("Qty", color = TextMuted, fontSize = 12.sp)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text("${trade.quantity}", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 3. P&L Section Card (Gross P&L & Net P&L with cyan/green glowing border)
                item {
                    val grossColor = if (trade.grossPnl >= 0) NeonCyan else LossRed
                    val netColor = if (trade.netPnl >= 0) NeonCyan else LossRed
                    val grossSign = if (trade.grossPnl >= 0) "+ ₹" else "- ₹"
                    val netSign = if (trade.netPnl >= 0) "+ ₹" else "- ₹"

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(DarkSurfaceCard)
                            .border(
                                1.5.dp,
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF00E5FF).copy(alpha = 0.5f),
                                        Color(0xFF00E676).copy(alpha = 0.3f),
                                        Color(0xFF8B5CF6).copy(alpha = 0.2f)
                                    )
                                ),
                                RoundedCornerShape(18.dp)
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Gross P&L",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$grossSign${df.format(kotlin.math.abs(trade.grossPnl))}",
                                    color = grossColor,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(40.dp)
                                    .background(DarkBorder)
                            )

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Net P&L",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$netSign${df.format(kotlin.math.abs(trade.netPnl))}",
                                    color = netColor,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 4. Financial Details Card (Invested Amount, Brokerage, Taxes & Charges, Total Charges)
                item {
                    SectionContainer(title = "Financial Details") {
                        DetailRow(label = "Margin Used (Invested)", value = "₹ ${df.format(trade.investmentAmount)}")
                        DetailRow(label = "Leverage", value = "${trade.leverage}x")
                        val exposure = if (trade.totalExposure > 0) trade.totalExposure else (trade.entryPrice * trade.quantity)
                        DetailRow(label = "Total Position Exposure (1x)", value = "₹ ${df.format(exposure)}")
                        DetailRow(label = "Brokerage", value = "₹ ${df.format(trade.brokerage)}")
                        DetailRow(label = "Taxes & Charges", value = "₹ ${df.format(trade.taxesAndCharges)}")
                        DetailRow(label = "Total Charges", value = "₹ ${df.format(trade.totalCharges)}", isEmphasized = true)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 5. Risk-Management Details (Stop Loss, Target)
                item {
                    SectionContainer(title = "Risk Management") {
                        val slText = trade.stopLoss?.let { "₹ ${df.format(it)}" } ?: "Not Set"
                        val targetText = trade.target?.let { "₹ ${df.format(it)}" } ?: "Not Set"
                        DetailRow(label = "Stop Loss", value = slText, valueColor = LossRed)
                        DetailRow(label = "Target", value = targetText, valueColor = ProfitGreen)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 6. Notes
                item {
                    if (trade.notes.isNotEmpty()) {
                        SectionContainer(title = "Notes") {
                            Text(
                                text = trade.notes,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // 7. Other Details (Trade Date & Time, Exchange, Trade Type)
                item {
                    SectionContainer(title = "System Details") {
                        DetailRow(label = "Trade Date & Time", value = "${trade.entryDate}, ${trade.entryTime}")
                        DetailRow(label = "Exchange", value = trade.exchange)
                        DetailRow(label = "Trade Type", value = trade.tradeType)
                        DetailRow(label = "Order Direction", value = trade.buySell)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Trade", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Are you sure you want to delete this trade (${trade.symbol})? This action cannot be undone.",
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            viewModel.deleteTrade(trade.id) {
                                onBackClick()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LossRed)
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = DarkSurface,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun BadgeTag(text: String, tint: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkBorderSubtle, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = tint,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SectionContainer(
    title: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(DarkSurfaceCard)
            .border(1.dp, DarkBorderSubtle, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary,
    isEmphasized: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = if (isEmphasized) TextPrimary else TextMuted,
            fontSize = 13.sp,
            fontWeight = if (isEmphasized) FontWeight.Medium else FontWeight.Normal
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 14.sp,
            fontWeight = if (isEmphasized) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}
