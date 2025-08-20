package com.example.neuronote

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

data class InfoContent(val title: String, val text: String)

@Composable
fun InfoDialog(info: InfoContent, darkColor: Color, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = info.title,
                fontWeight = FontWeight.Bold,
                color = darkColor
            )
        },
        text = {
            Text(text = info.text, color = darkColor)
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK", color = darkColor)
            }
        }
    )
}