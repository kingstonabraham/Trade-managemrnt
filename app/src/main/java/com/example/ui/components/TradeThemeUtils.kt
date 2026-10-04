package com.example.ui.components

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary

@Composable
fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = DarkSurfaceElevated,
    unfocusedContainerColor = DarkSurfaceElevated,
    disabledContainerColor = DarkSurfaceElevated,
    focusedBorderColor = NeonCyan,
    unfocusedBorderColor = DarkBorderSubtle,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = NeonCyan
)
