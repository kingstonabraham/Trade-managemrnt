package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TradingTaskEntity
import com.example.ui.components.outlinedFieldColors
import com.example.ui.theme.ActionButtonGradient
import com.example.ui.theme.DarkBackground
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
import com.example.ui.viewmodel.TradeViewModel
import com.example.util.TradingDateUtils
import kotlin.math.roundToInt

@Composable
fun TasksScreen(
    viewModel: TradeViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.currentDayTasks.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }

    val completedCount = tasks.count { it.isCompleted }
    val progress = if (tasks.isNotEmpty()) completedCount.toFloat() / tasks.size else 0f
    val currentDayIso = selectedDate.format(TradingDateUtils.ISO_DATE_FORMATTER)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Trading Disciplines & Tasks",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Swipe Right to Complete • Swipe Left to Delete",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Add Task Button
                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("add_task_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("tasks_screen_list"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp)
            ) {
                // Date Bar
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.previousDay() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.ArrowBackIos, contentDescription = "Prev", tint = TextSecondary, modifier = Modifier.size(12.dp))
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = TradingDateUtils.formatDisplayDate(selectedDate),
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        IconButton(
                            onClick = { viewModel.nextDay() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.ArrowForwardIos, contentDescription = "Next", tint = TextSecondary, modifier = Modifier.size(12.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Progress Banner
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurfaceCard)
                            .border(1.dp, DarkBorderSubtle, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Day's Execution Checklist",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "$completedCount / ${tasks.size} Done",
                                    color = if (progress == 1f && tasks.isNotEmpty()) ProfitGreen else NeonCyan,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = NeonCyan,
                                trackColor = DarkSurfaceElevated
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Uncompleted tasks automatically carry forward to tomorrow.",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Tasks List
                if (tasks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(DarkSurfaceCard)
                                .border(1.dp, DarkBorderSubtle, RoundedCornerShape(16.dp))
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No tasks for this day",
                                    color = TextSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tap '+ Add' above to set a trading discipline task",
                                    color = NeonCyan,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                } else {
                    items(tasks, key = { it.id }) { task ->
                        val isCarriedForward = !task.isCompleted && task.createdDate < currentDayIso
                        SwipeableTaskItem(
                            task = task,
                            isCarriedForward = isCarriedForward,
                            onSwipeComplete = { viewModel.swipeCompleteTask(task) },
                            onSwipeDelete = { viewModel.swipeDeleteTask(task) },
                            onToggle = {
                                if (task.isCompleted) viewModel.swipeUncompleteTask(task)
                                else viewModel.swipeCompleteTask(task)
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }

        // Add Task Dialog
        if (showAddDialog) {
            AddTaskModal(
                onDismiss = { showAddDialog = false },
                onConfirm = { title, category ->
                    viewModel.addNewTask(title, category)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun SwipeableTaskItem(
    task: TradingTaskEntity,
    isCarriedForward: Boolean,
    onSwipeComplete: () -> Unit,
    onSwipeDelete: () -> Unit,
    onToggle: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(targetValue = offsetX, label = "swipe_anim")

    val swipeThreshold = 180f

    // Background reveals on swipe
    val bgActionColor = when {
        offsetX > 40f -> ProfitGreen.copy(alpha = 0.85f)
        offsetX < -40f -> LossRed.copy(alpha = 0.85f)
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceElevated)
    ) {
        // Swipe Background Actions (Green Check on Right, Red Trash on Left)
        Row(
            modifier = Modifier
                .matchParentSize()
                .background(bgActionColor)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left icon (reveals when swiped right -> COMPLETE)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Check, contentDescription = "Complete", tint = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Complete", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            // Right icon (reveals when swiped left -> DELETE)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Delete", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }

        // Foreground Card
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurfaceCard)
                .border(1.dp, DarkBorderSubtle, RoundedCornerShape(14.dp))
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        offsetX += delta
                    },
                    onDragStopped = {
                        if (offsetX > swipeThreshold) {
                            onSwipeComplete()
                        } else if (offsetX < -swipeThreshold) {
                            onSwipeDelete()
                        }
                        offsetX = 0f
                    }
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Checkbox
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(if (task.isCompleted) ProfitGreen else DarkSurfaceElevated)
                        .border(1.5.dp, if (task.isCompleted) ProfitGreen else TextMuted, CircleShape)
                        .clickable(onClick = onToggle),
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        color = if (task.isCompleted) TextMuted else TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Medium,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = task.category,
                            color = NeonPurple,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (isCarriedForward) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFF9100).copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFFFF9100), modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Carried Forward", color = Color(0xFFFF9100), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddTaskModal(
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Routine") }
    val categories = listOf("Routine", "Risk Check", "Discipline", "Psychology", "Review")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Trading Task", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Task Description *", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("e.g. Check max risk before executing", color = TextMuted) },
                    singleLine = true,
                    colors = outlinedFieldColors(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Category", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        val isSelected = cat == category
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonPurple.copy(alpha = 0.25f) else DarkSurfaceElevated)
                                .border(1.dp, if (isSelected) NeonPurple else DarkBorderSubtle, RoundedCornerShape(8.dp))
                                .clickable { category = cat }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(cat, color = if (isSelected) NeonPurple else TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, category)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text("Add Task", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(16.dp)
    )
}
