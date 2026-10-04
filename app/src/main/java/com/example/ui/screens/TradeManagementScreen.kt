package com.example.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Trade
import com.example.ui.components.DateNavigatorBar
import com.example.ui.components.PnlHeroCard
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.ui.components.TimeFilterSegmentedControl
import com.example.ui.components.TradeCardItem
import com.example.ui.components.TradeStatChipsRow
import com.example.ui.theme.ActionButtonGradient
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TradeViewModel
import com.example.util.TimeFilter
import java.time.LocalDate

@Composable
fun TradeManagementScreen(
    viewModel: TradeViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val timeFilter by viewModel.timeFilter.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val selectedMarket by viewModel.tradeMarketSegment.collectAsStateWithLifecycle()
    val trades by viewModel.displayedTrades.collectAsStateWithLifecycle()
    val summary by viewModel.currentSummary.collectAsStateWithLifecycle()

    val pnlTitle = when (timeFilter) {
        TimeFilter.DAY -> "Today's Total P&L"
        TimeFilter.WEEK -> "Weekly Total P&L"
        TimeFilter.MONTH -> "Monthly Total P&L"
    }

    val recentTradesTitle = when (timeFilter) {
        TimeFilter.DAY -> "Recent Trades (Today)"
        TimeFilter.WEEK -> "Recent Trades (This Week)"
        TimeFilter.MONTH -> "Recent Trades (This Month)"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Section: Back button, Title "Trade Management", Calendar Action
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
                        text = "Trade Management",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Calendar icon action
                IconButton(
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                viewModel.setSelectedDate(LocalDate.of(year, month + 1, dayOfMonth))
                            },
                            selectedDate.year,
                            selectedDate.monthValue - 1,
                            selectedDate.dayOfMonth
                        ).show()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Pick Date",
                        tint = NeonCyan
                    )
                }
            }

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("trade_management_list"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp)
            ) {
                // 1. Day / Week / Month Segmented Control
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    TimeFilterSegmentedControl(
                        selectedFilter = timeFilter,
                        onFilterSelected = { viewModel.setTimeFilter(it) }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // 2. Date Navigation Bar
                item {
                    DateNavigatorBar(
                        timeFilter = timeFilter,
                        selectedDate = selectedDate,
                        selectedMonth = selectedMonth,
                        onPrevious = {
                            when (timeFilter) {
                                TimeFilter.DAY -> viewModel.previousDay()
                                TimeFilter.WEEK -> viewModel.previousWeek()
                                TimeFilter.MONTH -> viewModel.previousMonth()
                            }
                        },
                        onNext = {
                            when (timeFilter) {
                                TimeFilter.DAY -> viewModel.nextDay()
                                TimeFilter.WEEK -> viewModel.nextWeek()
                                TimeFilter.MONTH -> viewModel.nextMonth()
                            }
                        },
                        onDatePicked = { viewModel.setSelectedDate(it) },
                        onMonthPicked = { viewModel.setSelectedMonth(it) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // 2.5. Market Segment Filter Pills (Indian Stocks, US Stocks, Forex, All)
                item {
                    val marketOptions = listOf("All", "Indian Stocks", "US Stocks", "Forex")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        marketOptions.forEach { opt ->
                            val isSelected = opt.equals(selectedMarket, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else DarkSurfaceElevated)
                                    .border(1.dp, if (isSelected) NeonCyan else DarkBorderSubtle, RoundedCornerShape(10.dp))
                                    .clickable { viewModel.setTradeMarketSegment(opt) }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = opt,
                                    color = if (isSelected) NeonCyan else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // 3. Performance Hero Card (P&L, Return %, Bars, Win Rate)
                item {
                    PnlHeroCard(
                        title = pnlTitle,
                        totalPnl = summary.totalPnl,
                        pnlPercentage = summary.pnlPercentage,
                        totalTrades = summary.totalTrades,
                        winRate = summary.winRate
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // 4. 4 Statistics Chips Row (Profitable, Loss, Total Charges, Invested Amount)
                item {
                    TradeStatChipsRow(
                        profitableCount = summary.profitableTrades,
                        lossCount = summary.lossTrades,
                        totalCharges = summary.totalCharges,
                        investedAmount = summary.investedAmount
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // 5. Section Header: "Recent Trades (...)"
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = recentTradesTitle,
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${trades.size} Trades",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 6. Trades List or Empty State
                if (trades.isEmpty()) {
                    item {
                        EmptyTradesState(timeFilter = timeFilter)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                } else {
                    items(trades, key = { it.id }) { trade ->
                        TradeCardItem(
                            trade = trade,
                            onClick = { viewModel.openTradeDetails(trade.id) },
                            showDate = timeFilter != TimeFilter.DAY
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                // 7. Informational Tip Card
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceCard)
                            .border(1.dp, DarkBorderSubtle, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Information",
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Trades are grouped by trading date.\nWeek = Monday to Friday. Month = Calendar month.",
                                color = TextMuted,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Floating "+ Add" Button - Strictly only visible on Day view
        if (timeFilter == TimeFilter.DAY) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 24.dp)
            ) {
                FloatingActionButton(
                    onClick = { viewModel.openAddTrade() },
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(30.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(ActionButtonGradient)
                        .testTag("floating_add_trade_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Trade",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyTradesState(
    timeFilter: TimeFilter,
    modifier: Modifier = Modifier
) {
    val message = when (timeFilter) {
        TimeFilter.DAY -> "No trades recorded for this day."
        TimeFilter.WEEK -> "No trades recorded for this trading week."
        TimeFilter.MONTH -> "No trades recorded for this month."
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorderSubtle, RoundedCornerShape(16.dp))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(NeonPurple.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EventNote,
                    contentDescription = null,
                    tint = NeonPurple,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = message,
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap '+ Add' below to log a new position",
                color = TextMuted,
                fontSize = 12.sp
            )
        }
    }
}
