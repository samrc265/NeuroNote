package com.example.neuronote

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

// Single source of truth for the info data used by the dialog.
data class InfoContent(val title: String, val text: String)

@Composable
fun InfoDialog(
    info: InfoContent,
    containerColor: Color,
    textColor: Color,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = containerColor,
        title = {
            Text(
                text = info.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        },
        text = {
            Text(
                text = info.text,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK", color = textColor)
            }
        }
    )
}
