package com.example.neuronote

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.neuronote.ui.components.SelectableText

@Composable
fun SelectableMenuItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Pastel lavender background for selected state
    val selectedBackground = Color(0xFFE9D7FE)
    val backgroundColor = if (isSelected) selectedBackground else Color.Transparent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 12.dp, horizontal = 16.dp)
            .clickable { onClick() }
    ) {
        SelectableText(
            text = text,
            isSelected = isSelected,
            onClick = onClick
        )
    }
}
