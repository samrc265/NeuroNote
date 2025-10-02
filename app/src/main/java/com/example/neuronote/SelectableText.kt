package com.example.neuronote.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun SelectableText(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 18.sp,
    onClick: () -> Unit
) {
    val textColor = if (isSelected) Color(0xFF1A1A1A) else Color(0xFFE3E3E3)
    Text(
        text = text,
        color = textColor,
        fontSize = fontSize,
        modifier = modifier.clickable { onClick() }
    )
}
