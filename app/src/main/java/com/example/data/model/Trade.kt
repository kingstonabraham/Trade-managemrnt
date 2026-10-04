package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.DecimalFormat

@Entity(tableName = "trades")
data class Trade(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val symbol: String,
    val exchange: String, // e.g., "NSE", "BSE", "MCX", "NASDAQ", "NYSE", "FOREX", "BINANCE"
    val tradeType: String, // e.g., "Equity", "Options", "Futures", "Commodity", "Currencies", "Crypto", "Forex"
    val marketSegment: String = "Indian Stocks", // "Indian Stocks", "US Stocks", "Forex", "Crypto", "Commodity"
    val buySell: String, // "BUY" or "SELL"
    val leverage: Int = 1, // 1x to 100x leverage
    val entryPrice: Double,
    val exitPrice: Double? = null,
    val quantity: Int,
    val investmentAmount: Double, // Margin required with leverage: (Entry Price * Qty) / Leverage
    val totalExposure: Double = 0.0, // Full contract/position value: Entry Price * Qty
    val entryDate: String, // "YYYY-MM-DD" e.g. "2026-10-03"
    val entryTime: String, // "HH:mm" or "hh:mm a" e.g. "10:15 AM"
    val exitDate: String? = null,
    val exitTime: String? = null,
    val brokerage: Double = 0.0,
    val taxesAndCharges: Double = 0.0,
    val totalCharges: Double = 0.0,
    val stopLoss: Double? = null,
    val target: Double? = null,
    val notes: String = "",
    val grossPnl: Double = 0.0,
    val netPnl: Double = 0.0,
    val status: String = "OPEN" // "PROFIT", "LOSS", "OPEN"
) {
    val isProfit: Boolean get() = netPnl > 0
    val isLoss: Boolean get() = netPnl < 0
    val isOpen: Boolean get() = status == "OPEN" || exitPrice == null

    val pnlPercentage: Double
        get() {
            return if (investmentAmount > 0) {
                (netPnl / investmentAmount) * 100
            } else 0.0
        }

    fun formattedNetPnl(): String {
        val df = DecimalFormat("#,##0.00")
        val prefix = if (netPnl >= 0) "+ ₹" else "- ₹"
        return prefix + df.format(kotlin.math.abs(netPnl))
    }

    fun formattedGrossPnl(): String {
        val df = DecimalFormat("#,##0.00")
        val prefix = if (grossPnl >= 0) "+ ₹" else "- ₹"
        return prefix + df.format(kotlin.math.abs(grossPnl))
    }

    fun formattedPnlPercentage(): String {
        val prefix = if (pnlPercentage >= 0) "+" else ""
        return String.format("%s%.2f%%", prefix, pnlPercentage)
    }

    companion object {
        fun calculatePnL(
            buySell: String,
            entryPrice: Double,
            exitPrice: Double?,
            quantity: Int,
            totalCharges: Double
        ): Triple<Double, Double, String> {
            if (exitPrice == null || exitPrice == 0.0) {
                return Triple(0.0, -totalCharges, "OPEN")
            }
            val gross = if (buySell.uppercase() == "BUY") {
                (exitPrice - entryPrice) * quantity
            } else {
                (entryPrice - exitPrice) * quantity
            }
            val net = gross - totalCharges
            val status = if (net >= 0) "PROFIT" else "LOSS"
            return Triple(gross, net, status)
        }
    }
}
