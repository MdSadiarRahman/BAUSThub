package com.example.bausthub.models

data class Notification(
    var id: String = "",
    var type: String = "", // "follow", "post", "like"
    var fromId: String = "",
    var fromName: String = "",
    var fromImage: String = "",
    var message: String = "",
    var timestamp: Long = 0,
    var isRead: Boolean = false,
    var postId: String = "" // Optional: if notification is about a post
)
