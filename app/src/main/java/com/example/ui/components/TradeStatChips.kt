package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.LossRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.TextMuted
import java.text.DecimalFormat

@Composable
fun TradeStatChipsRow(
    profitableCount: Int,
    lossCount: Int,
    totalCharges: Double,
    investedAmount: Double,
    modifier: Modifier = Modifier
) {
    val df = DecimalFormat("#,##0")

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Profitable
        StatChipItem(
            label = "Profitable",
            value = profitableCount.toString(),
            valueColor = ProfitGreen,
            modifier = Modifier.weight(1f)
        )

        // Loss
        StatChipItem(
            label = "Loss",
            value = lossCount.toString(),
            valueColor = LossRed,
            modifier = Modifier.weight(0.9f)
        )

        // Total Charges
        StatChipItem(
            label = "Total Charges",
            value = "₹ ${df.format(totalCharges)}",
            valueColor = NeonPurple,
            modifier = Modifier.weight(1.2f)
        )

        // Invested Amount
        StatChipItem(
            label = "Invested Amount",
            value = "₹ ${df.format(investedAmount)}",
            valueColor = NeonCyan,
            modifier = Modifier.weight(1.35f)
        )
    }
}

@Composable
fun StatChipItem(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceCard)
            .border(1.dp, DarkBorderSubtle, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                color = valueColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
