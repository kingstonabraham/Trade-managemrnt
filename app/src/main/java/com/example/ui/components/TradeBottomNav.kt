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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.MainTab

@Composable
fun TradeBottomNav(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .border(1.dp, DarkBorderSubtle)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                label = "Home",
                icon = Icons.Default.Home,
                isSelected = selectedTab == MainTab.HOME,
                onClick = { onTabSelected(MainTab.HOME) },
                testTag = "nav_home"
            )

            BottomNavItem(
                label = "Trade",
                icon = Icons.AutoMirrored.Filled.ShowChart,
                isSelected = selectedTab == MainTab.TRADE,
                onClick = { onTabSelected(MainTab.TRADE) },
                testTag = "nav_trade"
            )

            BottomNavItem(
                label = "Tasks",
                icon = Icons.Default.AssignmentTurnedIn,
                isSelected = selectedTab == MainTab.TASKS,
                onClick = { onTabSelected(MainTab.TASKS) },
                testTag = "nav_tasks"
            )

            BottomNavItem(
                label = "Work",
                icon = Icons.Default.BusinessCenter,
                isSelected = selectedTab == MainTab.WORK,
                onClick = { onTabSelected(MainTab.WORK) },
                testTag = "nav_work"
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val activeColor = NeonCyan
    val inactiveColor = TextMuted

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        // Glowing cyan pill indicator when selected
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(activeColor)
            )
            Spacer(modifier = Modifier.height(4.dp))
        } else {
            Spacer(modifier = Modifier.height(7.dp))
        }

        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            color = if (isSelected) activeColor else inactiveColor,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
