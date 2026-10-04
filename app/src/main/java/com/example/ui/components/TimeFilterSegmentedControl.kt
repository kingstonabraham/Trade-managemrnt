package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.PrimaryGradient
import com.example.ui.theme.TextMuted
import com.example.util.TimeFilter

@Composable
fun TimeFilterSegmentedControl(
    selectedFilter: TimeFilter,
    onFilterSelected: (TimeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    // Strictly only Day, Week, Month. NO CUSTOM.
    val filters = listOf(TimeFilter.DAY, TimeFilter.WEEK, TimeFilter.MONTH)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(DarkSurfaceElevated)
            .padding(4.dp)
            .testTag("time_filter_segmented_control")
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            filters.forEach { filter ->
                val isSelected = filter == selectedFilter
                val pillModifier = if (isSelected) {
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(26.dp))
                        .background(PrimaryGradient)
                } else {
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color.Transparent)
                }

                Box(
                    modifier = pillModifier
                        .clickable { onFilterSelected(filter) }
                        .padding(vertical = 10.dp)
                        .testTag("filter_tab_${filter.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter.label,
                        color = if (isSelected) Color.White else TextMuted,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
