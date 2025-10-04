package com.example.neuronote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.neuronote.ai.GeminiClient
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val role: String,   // "user" or "assistant"
    val text: String
)

@Composable
fun ChatbotPage(
    darkColor: Color,
    lightColor: Color,
    textColor: Color
) {
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var input by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    //  ✅  Scroll state for simple, reliable scrolling even with giant messages
    val scrollState = rememberScrollState()
    //  ✅  Auto-scroll whenever message count changes
    LaunchedEffect(messages.size) {
        // jump first to avoid long animation on huge diffs
        scrollState.scrollTo(scrollState.maxValue)
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .imePadding(), // keep input visible above keyboard
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Ask anything about mood tracking, sleep, journaling—or anything you like.",
            style = MaterialTheme.typography.bodyMedium,
            color = textColor.copy(alpha = 0.8f)
        )
        Divider(color = textColor.copy(alpha = 0.2f))
        //  ✅  Scrollable chat area
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (messages.isEmpty() && !isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                ) {
                    Text(
                        "Start the conversation below 👇",
                        modifier = Modifier.align(Alignment.Center),
                        color = textColor.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                messages.forEach { msg ->
                    val isUser = msg.role == "user"
                    Surface(
                        // FIX: Added elevation and uniform rounded shape for a professional chat UI
                        tonalElevation = 2.dp,
                        shadowElevation = 1.dp,
                        color = if (isUser) darkColor else lightColor,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = msg.text.trim(),
                            color = if (isUser) Color.White else textColor,
                            modifier = Modifier.padding(12.dp),
                            fontWeight = if (isUser) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
                if (isLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = darkColor, strokeWidth = 2.dp)
                    }
                }
            }
        }
        // Input row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type your message…") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedBorderColor = darkColor,
                    unfocusedBorderColor = textColor.copy(alpha = 0.4f),
                    cursorColor = darkColor
                ),
                maxLines = 4
            )
            FilledIconButton(
                onClick = {
                    if (input.isBlank() || isLoading) return@FilledIconButton
                    val userText = input
                    input = ""
                    messages.add(ChatMessage(role = "user", text = userText))
                    scope.launch {
                        isLoading = true
                        try {
                            val prompt = content { text(userText) }
                            val response = GeminiClient.model.generateContent(prompt)
                            val reply = response.text?.ifBlank { "I couldn’t generate a response." }
                                ?: "I couldn’t generate a response."
                            messages.add(ChatMessage(role = "assistant", text = reply))
                        } catch (e: Exception) {
                            messages.add(
                                ChatMessage(
                                    role = "assistant",
                                    text = " ⚠️ Error: ${e.message ?: "Something went wrong."}"
                                )
                            )
                        } finally {
                            isLoading = false
                        }
                    }
                },
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = darkColor)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}