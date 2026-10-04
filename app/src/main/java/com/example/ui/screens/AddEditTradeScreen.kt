package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Trade
import com.example.ui.components.outlinedFieldColors
import com.example.ui.theme.ActionButtonGradient
import com.example.ui.theme.BuyButtonGradient
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
import com.example.ui.theme.SellButtonGradient
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TradeViewModel
import com.example.util.TradingDateUtils
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTradeScreen(
    viewModel: TradeViewModel,
    tradeToEdit: Trade? = null,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Market Segment
    var marketSegment by remember { mutableStateOf(tradeToEdit?.marketSegment ?: "Indian Stocks") }
    val marketSegments = listOf("Indian Stocks", "US Stocks", "Forex", "Crypto", "Commodity")

    // Form fields state
    var symbol by remember { mutableStateOf(tradeToEdit?.symbol ?: "") }
    var exchange by remember { mutableStateOf(tradeToEdit?.exchange ?: "NSE") }
    var tradeType by remember { mutableStateOf(tradeToEdit?.tradeType ?: "Equity") }
    var buySell by remember { mutableStateOf(tradeToEdit?.buySell ?: "BUY") }

    // Leverage (1x to 100x slider and presets)
    var leverage by remember { mutableIntStateOf(tradeToEdit?.leverage ?: 1) }

    var entryPriceText by remember { mutableStateOf(tradeToEdit?.let { String.format(Locale.US, "%.2f", it.entryPrice) } ?: "") }
    var quantityText by remember { mutableStateOf(tradeToEdit?.quantity?.toString() ?: "") }

    // Date & Time
    var entryDate by remember { mutableStateOf(tradeToEdit?.entryDate ?: "2026-10-04") }
    var entryTime by remember { mutableStateOf(tradeToEdit?.entryTime ?: "10:15 AM") }

    var exitPriceText by remember { mutableStateOf(tradeToEdit?.exitPrice?.let { String.format(Locale.US, "%.2f", it) } ?: "") }
    var exitDate by remember { mutableStateOf(tradeToEdit?.exitDate ?: "") }
    var exitTime by remember { mutableStateOf(tradeToEdit?.exitTime ?: "") }

    var brokerageText by remember { mutableStateOf(tradeToEdit?.let { String.format(Locale.US, "%.2f", it.brokerage) } ?: "20.00") }
    var taxesText by remember { mutableStateOf(tradeToEdit?.let { String.format(Locale.US, "%.2f", it.taxesAndCharges) } ?: "60.00") }

    var stopLossText by remember { mutableStateOf(tradeToEdit?.stopLoss?.let { String.format(Locale.US, "%.2f", it) } ?: "") }
    var targetText by remember { mutableStateOf(tradeToEdit?.target?.let { String.format(Locale.US, "%.2f", it) } ?: "") }
    var notes by remember { mutableStateOf(tradeToEdit?.notes ?: "") }

    var validationError by remember { mutableStateOf<String?>(null) }

    // Automatic Calculations with Leverage
    val totalExposure by remember {
        derivedStateOf {
            val price = entryPriceText.toDoubleOrNull() ?: 0.0
            val qty = quantityText.toIntOrNull() ?: 0
            price * qty
        }
    }

    val marginRequiredWithLeverage by remember {
        derivedStateOf {
            val lev = leverage.coerceAtLeast(1)
            totalExposure / lev
        }
    }

    val calculatedTotalCharges by remember {
        derivedStateOf {
            val brokerage = brokerageText.toDoubleOrNull() ?: 0.0
            val taxes = taxesText.toDoubleOrNull() ?: 0.0
            brokerage + taxes
        }
    }

    // Dropdowns
    var exchangeExpanded by remember { mutableStateOf(false) }
    val exchangeOptions = when (marketSegment) {
        "Indian Stocks" -> listOf("NSE", "BSE", "MCX")
        "US Stocks" -> listOf("NASDAQ", "NYSE", "AMEX")
        "Forex" -> listOf("FOREX", "OANDA", "FXCM")
        "Crypto" -> listOf("BINANCE", "COINBASE", "BYBIT")
        "Commodity" -> listOf("MCX", "COMEX", "NYMEX")
        else -> listOf("NSE", "BSE", "MCX", "NASDAQ", "NYSE", "FOREX", "CRYPTO")
    }

    var tradeTypeExpanded by remember { mutableStateOf(false) }
    val tradeTypeOptions = listOf("Equity", "Options", "Futures", "Commodity", "Currencies", "Crypto", "Forex")

    val popularSymbols = when (marketSegment) {
        "Indian Stocks" -> listOf("RELIANCE", "NIFTY 19500 CE", "TCS", "INFY", "BANKNIFTY", "HDFCBANK", "TATAMOTORS")
        "US Stocks" -> listOf("AAPL", "TSLA", "NVDA", "MSFT", "AMZN", "GOOGL", "META")
        "Forex" -> listOf("EUR/USD", "GBP/USD", "USD/JPY", "AUD/USD", "USD/INR")
        "Crypto" -> listOf("BTC/USDT", "ETH/USDT", "SOL/USDT", "BNB/USDT", "XRP/USDT")
        else -> listOf("CRUDEOIL", "GOLD", "SILVER", "NATURALGAS", "COPPER")
    }

    val leveragePresets = listOf(1, 5, 10, 20, 50, 100)

    val df = DecimalFormat("#,##0.00")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .imePadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (tradeToEdit == null) "Add New Trade" else "Edit Trade",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Form
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("add_trade_form"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // 1. Market Segment Selection (Indian Stocks, US Stocks, Forex, Crypto, Commodity)
                item {
                    Text(
                        text = "Market / Asset Class *",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        marketSegments.forEach { seg ->
                            val isSelected = seg.equals(marketSegment, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else DarkSurfaceElevated)
                                    .border(1.dp, if (isSelected) NeonCyan else DarkBorderSubtle, RoundedCornerShape(10.dp))
                                    .clickable {
                                        marketSegment = seg
                                        // Update default exchange if segment changes
                                        if (seg == "Indian Stocks") exchange = "NSE"
                                        else if (seg == "US Stocks") exchange = "NASDAQ"
                                        else if (seg == "Forex") exchange = "FOREX"
                                        else if (seg == "Crypto") exchange = "BINANCE"
                                        else if (seg == "Commodity") exchange = "MCX"
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = seg,
                                    color = if (isSelected) NeonCyan else TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 2. Symbol Field
                item {
                    Text(
                        text = "Symbol *",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = symbol,
                        onValueChange = { symbol = it.uppercase() },
                        placeholder = { Text("Search symbol (e.g., RELIANCE, AAPL)", color = TextMuted) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = NeonCyan)
                        },
                        trailingIcon = {
                            if (symbol.isNotEmpty()) {
                                IconButton(onClick = { symbol = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                                }
                            }
                        },
                        singleLine = true,
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("trade_symbol_input")
                    )

                    // Symbol Suggestion Chips
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        popularSymbols.forEach { sym ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated)
                                    .border(1.dp, DarkBorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable { symbol = sym }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = sym,
                                    color = if (symbol == sym) NeonCyan else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (symbol == sym) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 3 & 4. Exchange & Trade Type
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Exchange Dropdown
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Exchange *",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box {
                                OutlinedTextField(
                                    value = exchange,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = {
                                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextSecondary)
                                    },
                                    colors = outlinedFieldColors(),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { exchangeExpanded = true }
                                        .testTag("exchange_dropdown")
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable { exchangeExpanded = true }
                                )
                                DropdownMenu(
                                    expanded = exchangeExpanded,
                                    onDismissRequest = { exchangeExpanded = false },
                                    modifier = Modifier.background(DarkSurface)
                                ) {
                                    exchangeOptions.forEach { opt ->
                                        DropdownMenuItem(
                                            text = { Text(opt, color = TextPrimary) },
                                            onClick = {
                                                exchange = opt
                                                exchangeExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Trade Type Dropdown
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Trade Type *",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box {
                                OutlinedTextField(
                                    value = tradeType,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = {
                                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextSecondary)
                                    },
                                    colors = outlinedFieldColors(),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { tradeTypeExpanded = true }
                                        .testTag("trade_type_dropdown")
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable { tradeTypeExpanded = true }
                                )
                                DropdownMenu(
                                    expanded = tradeTypeExpanded,
                                    onDismissRequest = { tradeTypeExpanded = false },
                                    modifier = Modifier.background(DarkSurface)
                                ) {
                                    tradeTypeOptions.forEach { opt ->
                                        DropdownMenuItem(
                                            text = { Text(opt, color = TextPrimary) },
                                            onClick = {
                                                tradeType = opt
                                                tradeTypeExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 5. Buy / Sell Selectable Buttons
                item {
                    Text(
                        text = "Buy / Sell *",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val isBuy = buySell.equals("BUY", ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isBuy) BuyButtonGradient else androidx.compose.ui.graphics.Brush.linearGradient(listOf(DarkSurfaceElevated, DarkSurfaceElevated)))
                                .border(1.dp, if (isBuy) Color.Transparent else DarkBorder, RoundedCornerShape(12.dp))
                                .clickable { buySell = "BUY" }
                                .testTag("buy_selector_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Buy",
                                color = if (isBuy) Color.White else TextSecondary,
                                fontSize = 15.sp,
                                fontWeight = if (isBuy) FontWeight.Bold else FontWeight.Medium
                            )
                        }

                        val isSell = buySell.equals("SELL", ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSell) SellButtonGradient else androidx.compose.ui.graphics.Brush.linearGradient(listOf(DarkSurfaceElevated, DarkSurfaceElevated)))
                                .border(1.dp, if (isSell) Color.Transparent else DarkBorder, RoundedCornerShape(12.dp))
                                .clickable { buySell = "SELL" }
                                .testTag("sell_selector_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sell",
                                color = if (isSell) Color.White else TextSecondary,
                                fontSize = 15.sp,
                                fontWeight = if (isSell) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 6 & 7. Entry Price & Quantity
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(
                                text = "Entry Price *",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = entryPriceText,
                                onValueChange = { entryPriceText = it },
                                placeholder = { Text("0.00", color = TextMuted) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                colors = outlinedFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("entry_price_input")
                            )
                        }

                        Column(modifier = Modifier.weight(0.8f)) {
                            Text(
                                text = "Quantity *",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = quantityText,
                                onValueChange = { quantityText = it },
                                placeholder = { Text("0", color = TextMuted) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = outlinedFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("quantity_input")
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 8. Leverage Selector (1x to 100x Slider + Presets)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkSurfaceCard)
                            .border(1.dp, DarkBorderSubtle, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Speed, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Trade Leverage",
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = "${leverage}x Leverage",
                                    color = NeonCyan,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Leverage Slider (1x to 100x)
                            Slider(
                                value = leverage.toFloat(),
                                onValueChange = { leverage = it.toInt() },
                                valueRange = 1f..100f,
                                steps = 99,
                                colors = SliderDefaults.colors(
                                    thumbColor = NeonCyan,
                                    activeTrackColor = NeonCyan,
                                    inactiveTrackColor = DarkSurfaceElevated
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Quick Leverage Presets
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                leveragePresets.forEach { p ->
                                    val isSelected = leverage == p
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else DarkSurfaceElevated)
                                            .border(1.dp, if (isSelected) NeonCyan else DarkBorderSubtle, RoundedCornerShape(8.dp))
                                            .clickable { leverage = p }
                                            .padding(vertical = 5.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${p}x",
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

                // 9. Investment Amount Display (With Leverage & Without Leverage)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, DarkBorderSubtle, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            // Margin Required (With Leverage)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Margin Required (With ${leverage}x)",
                                        color = TextSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Info, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(13.dp))
                                }
                                Text(
                                    text = "₹ ${df.format(marginRequiredWithLeverage)}",
                                    color = NeonCyan,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Total Position Exposure (Without Leverage)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Position Exposure (1x Original)",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "₹ ${df.format(totalExposure)}",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 10. Entry Date & Time Pickers
                item {
                    Text(
                        text = "Entry Date & Time *",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, DarkBorderSubtle, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Date selector
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    val currentLocal = TradingDateUtils.parseIsoDate(entryDate) ?: LocalDate.of(2026, 10, 4)
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            val picked = LocalDate.of(year, month + 1, dayOfMonth)
                                            entryDate = picked.format(TradingDateUtils.ISO_DATE_FORMATTER)
                                        },
                                        currentLocal.year,
                                        currentLocal.monthValue - 1,
                                        currentLocal.dayOfMonth
                                    ).show()
                                }
                                .padding(4.dp)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            val displayD = TradingDateUtils.parseIsoDate(entryDate)?.let { TradingDateUtils.formatDisplayDate(it) } ?: entryDate
                            Text(displayD, color = TextPrimary, fontSize = 14.sp)
                        }

                        // Time selector
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            val time = LocalTime.of(hourOfDay, minute)
                                            entryTime = time.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US))
                                        },
                                        10, 15, false
                                    ).show()
                                }
                                .padding(4.dp)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(entryTime, color = TextPrimary, fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 11 & 12. Exit Price & Exit Date/Time
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Exit Price (Optional)",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = exitPriceText,
                                onValueChange = { exitPriceText = it },
                                placeholder = { Text("0.00", color = TextMuted) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                colors = outlinedFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("exit_price_input")
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Exit Date & Time",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkSurfaceElevated)
                                    .border(1.dp, DarkBorderSubtle, RoundedCornerShape(12.dp))
                                    .clickable {
                                        val currentLocal = TradingDateUtils.parseIsoDate(exitDate.ifEmpty { entryDate }) ?: LocalDate.of(2026, 10, 4)
                                        DatePickerDialog(
                                            context,
                                            { _, year, month, dayOfMonth ->
                                                val picked = LocalDate.of(year, month + 1, dayOfMonth)
                                                exitDate = picked.format(TradingDateUtils.ISO_DATE_FORMATTER)
                                                if (exitTime.isEmpty()) exitTime = "03:15 PM"
                                            },
                                            currentLocal.year,
                                            currentLocal.monthValue - 1,
                                            currentLocal.dayOfMonth
                                        ).show()
                                    }
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val exitLabel = if (exitDate.isNotEmpty()) {
                                        val display = TradingDateUtils.parseIsoDate(exitDate)?.let { TradingDateUtils.formatDisplayDate(it) } ?: exitDate
                                        display
                                    } else "Select"

                                    Text(
                                        text = exitLabel,
                                        color = if (exitDate.isNotEmpty()) TextPrimary else TextMuted,
                                        fontSize = 13.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 13. Brokerage & Taxes
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Brokerage", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = brokerageText,
                                onValueChange = { brokerageText = it },
                                placeholder = { Text("0.00", color = TextMuted) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                colors = outlinedFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Taxes & Charges", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = taxesText,
                                onValueChange = { taxesText = it },
                                placeholder = { Text("0.00", color = TextMuted) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                colors = outlinedFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Column {
                        Text("Total Charges", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, DarkBorderSubtle, RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Text(
                                text = "₹ ${df.format(calculatedTotalCharges)}",
                                color = NeonPurple,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 14 & 15. Stop Loss & Target
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Stop Loss", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = stopLossText,
                                onValueChange = { stopLossText = it },
                                placeholder = { Text("0.00", color = TextMuted) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                colors = outlinedFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Target", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = targetText,
                                onValueChange = { targetText = it },
                                placeholder = { Text("0.00", color = TextMuted) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                colors = outlinedFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 16. Notes
                item {
                    Text("Notes (Optional)", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("Enter your notes (e.g. Good momentum breakout)...", color = TextMuted) },
                        minLines = 3,
                        maxLines = 4,
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("trade_notes_input")
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                if (validationError != null) {
                    item {
                        Text(
                            text = validationError ?: "",
                            color = LossRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }
            }

            // Save Trade Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        val cleanSymbol = symbol.trim()
                        if (cleanSymbol.isEmpty()) {
                            validationError = "Please enter a stock or contract symbol."
                            return@Button
                        }
                        val entryPrice = entryPriceText.toDoubleOrNull()
                        if (entryPrice == null || entryPrice <= 0.0) {
                            validationError = "Please enter a valid entry price."
                            return@Button
                        }
                        val qty = quantityText.toIntOrNull()
                        if (qty == null || qty <= 0) {
                            validationError = "Please enter a valid quantity."
                            return@Button
                        }

                        val exitPrice = exitPriceText.toDoubleOrNull()
                        val brokerage = brokerageText.toDoubleOrNull() ?: 0.0
                        val taxes = taxesText.toDoubleOrNull() ?: 0.0
                        val totalCharges = brokerage + taxes

                        val (gross, net, status) = Trade.calculatePnL(
                            buySell = buySell,
                            entryPrice = entryPrice,
                            exitPrice = exitPrice,
                            quantity = qty,
                            totalCharges = totalCharges
                        )

                        val tradeToSave = Trade(
                            id = tradeToEdit?.id ?: 0,
                            symbol = cleanSymbol,
                            exchange = exchange,
                            tradeType = tradeType,
                            marketSegment = marketSegment,
                            buySell = buySell.uppercase(),
                            leverage = leverage,
                            entryPrice = entryPrice,
                            exitPrice = exitPrice,
                            quantity = qty,
                            investmentAmount = marginRequiredWithLeverage,
                            totalExposure = totalExposure,
                            entryDate = entryDate,
                            entryTime = entryTime,
                            exitDate = exitDate.ifEmpty { if (exitPrice != null) entryDate else null },
                            exitTime = exitTime.ifEmpty { if (exitPrice != null) "03:15 PM" else null },
                            brokerage = brokerage,
                            taxesAndCharges = taxes,
                            totalCharges = totalCharges,
                            stopLoss = stopLossText.toDoubleOrNull(),
                            target = targetText.toDoubleOrNull(),
                            notes = notes.trim(),
                            grossPnl = gross,
                            netPnl = net,
                            status = status
                        )

                        viewModel.saveTrade(tradeToSave) {
                            onBackClick()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(ActionButtonGradient)
                        .testTag("save_trade_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Text(
                        text = "Save Trade",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}
