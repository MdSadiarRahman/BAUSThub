package com.example.bausthub.models

data class Comment(
    val commentId: String = "",
    val userId: String = "",
    val userName: String = "",
    val text: String = "",
    val timestamp: Long = 0
)
