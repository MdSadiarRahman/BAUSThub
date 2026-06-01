package com.example.bausthub.models

data class Post(
    val userId: String = "",
    val authorName: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    val timestamp: Long = 0
)
