package com.example.bausthub.models

import com.google.firebase.firestore.Exclude

data class Post(
    @get:Exclude var postId: String = "",
    val userId: String = "",
    val authorName: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    val timestamp: Long = 0,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    @get:Exclude var isLiked: Boolean = false,
    @get:Exclude var isBookmarked: Boolean = false
)
