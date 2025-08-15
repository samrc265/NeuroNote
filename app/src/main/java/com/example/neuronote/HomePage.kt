package com.example.neuronote

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomePage(
    darkGreen: Color,
    lightGreen: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Welcome to NeuroNote!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = darkGreen
        )

        Text(
            text = "Your all-in-one mental wellness companion.",
            fontSize = 16.sp,
            color = Color.DarkGray
        )

        Button(
            onClick = { /* Navigate to Calendar */ },
            colors = ButtonDefaults.buttonColors(containerColor = darkGreen),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Open Calendar", color = Color.White, fontSize = 16.sp)
        }

        Button(
            onClick = { /* Navigate to Mood Tracker */ },
            colors = ButtonDefaults.buttonColors(containerColor = lightGreen),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Mood Tracking", color = darkGreen, fontSize = 16.sp)
        }
    }
}
