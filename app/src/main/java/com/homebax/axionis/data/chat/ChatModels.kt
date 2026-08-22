package com.homebax.axionis.data.chat

import java.util.UUID

enum class MessageRole {
    USER, AI, SYSTEM
}

sealed class ToolWidget {
    data class Weather(
        val temp: String,
        val humidity: String,
        val wind: String,
        val pressure: String,
        val hourlyForecast: List<ForecastItem>
    ) : ToolWidget()

    data class Maps(
        val destination: String,
        val routeInfo: String,
        val travelMode: String = "driving"
    ) : ToolWidget()

    data class Time(
        val currentTime: String,
        val currentDate: String
    ) : ToolWidget()

    data class Location(
        val latitude: Double,
        val longitude: Double,
        val address: String? = null
    ) : ToolWidget()

    data class Compass(
        val heading: Float
    ) : ToolWidget()
    
    data class ImageGeneration(
        val imageUri: String,
        val prompt: String
    ) : ToolWidget()
}

data class ForecastItem(
    val time: String,
    val temp: String,
    val iconRes: Int // Using Int for icon resources
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val role: MessageRole,
    val timestamp: Long = System.currentTimeMillis(),
    val processingTimeMs: Long? = null,
    val tool: ToolWidget? = null,
    val attachments: List<String> = emptyList()
)

enum class AIMode(val label: String) {
    KREATIVNI("Kreativní"),
    PREMYSLET("Přemýšlet"),
    CODE("code"),
    ROLEPLAY("RolePlay"),
    NORMAL("Normal"),
    LOW("Low"),
    HIGH("High")
}
