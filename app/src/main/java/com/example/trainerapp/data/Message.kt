package com.example.trainerapp.data


data class Message(
    var senderId: String = "",
    var text: String = "",
    var imageUrl: String = "",
    var videoUrl: String = "",
    var timestamp: Long = 0,
    var participants: Map<String, Boolean> = emptyMap() // Add participants field
)
