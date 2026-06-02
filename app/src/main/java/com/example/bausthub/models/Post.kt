package com.example.bausthub.models

import com.google.firebase.firestore.Exclude

data class Post(
    @get:Exclude var postId: String = "",
    val userId: String = "",
    val authorName: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    val timestamp: Long = 0
)
