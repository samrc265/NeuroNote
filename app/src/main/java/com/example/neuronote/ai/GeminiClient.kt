package com.example.neuronote.ai

import com.example.neuronote.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel

object GeminiClient {
    // Tip: use a fast model for chat UIs; switch to Pro if you need deeper reasoning
    val model: GenerativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }
}
