package com.varuna.app.model

data class ChatMessage(
    val text: String,
    val isBot: Boolean,
    val timestamp: Long
)