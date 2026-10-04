package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.TimeFilter
import com.example.util.TradingDateUtils
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun DateNavigatorBar(
    timeFilter: TimeFilter,
    selectedDate: LocalDate,
    selectedMonth: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onDatePicked: (LocalDate) -> Unit,
    onMonthPicked: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMonthDialog by remember { mutableStateOf(false) }

    val displayText = when (timeFilter) {
        TimeFilter.DAY -> TradingDateUtils.formatDisplayDate(selectedDate)
        TimeFilter.WEEK -> {
            val (mon, fri) = TradingDateUtils.getTradingWeek(selectedDate)
            TradingDateUtils.formatWeekRange(mon, fri)
        }
        TimeFilter.MONTH -> TradingDateUtils.formatMonthYear(selectedMonth)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous Button
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(DarkSurfaceElevated)
                .border(1.dp, DarkBorderSubtle, CircleShape)
                .clickable(onClick = onPrevious)
                .testTag("date_nav_prev"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                contentDescription = "Previous",
                tint = TextSecondary,
                modifier = Modifier.size(14.dp)
            )
        }

        // Center Pill (Clickable date/week/month selector)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(DarkSurfaceElevated)
                .border(1.dp, DarkBorderSubtle, RoundedCornerShape(20.dp))
                .clickable {
                    when (timeFilter) {
                        TimeFilter.DAY -> {
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    onDatePicked(LocalDate.of(year, month + 1, dayOfMonth))
                                },
                                selectedDate.year,
                                selectedDate.monthValue - 1,
                                selectedDate.dayOfMonth
                            ).show()
                        }
                        TimeFilter.WEEK -> {
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    onDatePicked(LocalDate.of(year, month + 1, dayOfMonth))
                                },
                                selectedDate.year,
                                selectedDate.monthValue - 1,
                                selectedDate.dayOfMonth
                            ).show()
                        }
                        TimeFilter.MONTH -> {
                            showMonthDialog = true
                        }
                    }
                }
                .testTag("date_nav_center")
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Select Date",
                    tint = NeonCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = displayText,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Next Button
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(DarkSurfaceElevated)
                .border(1.dp, DarkBorderSubtle, CircleShape)
                .clickable(onClick = onNext)
                .testTag("date_nav_next"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Next",
                tint = TextSecondary,
                modifier = Modifier.size(14.dp)
            )
        }
    }

    if (showMonthDialog) {
        MonthPickerModal(
            currentMonth = selectedMonth,
            onDismiss = { showMonthDialog = false },
            onSelect = {
                onMonthPicked(it)
                showMonthDialog = false
            }
        )
    }
}

@Composable
fun MonthPickerModal(
    currentMonth: YearMonth,
    onDismiss: () -> Unit,
    onSelect: (YearMonth) -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Trading Month",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            val months = remember {
                (0..11).map { i ->
                    YearMonth.of(2026, 10).minusMonths(i.toLong())
                }
            }
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(months.size) { index ->
                    val ym = months[index]
                    val isSelected = ym == currentMonth
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) NeonCyan.copy(alpha = 0.15f) else androidx.compose.ui.graphics.Color.Transparent)
                            .clickable { onSelect(ym) }
                            .padding(vertical = 12.dp, horizontal = 16.dp)
                    ) {
                        Text(
                            text = TradingDateUtils.formatMonthYear(ym),
                            color = if (isSelected) NeonCyan else TextPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Close", color = NeonCyan)
            }
        },
        containerColor = com.example.ui.theme.DarkSurface,
        shape = RoundedCornerShape(16.dp)
    )
}
