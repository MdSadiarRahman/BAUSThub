package com.example.bausthub.models

data class User(
    var uid: String = "",
    var name: String = "",
    var email: String = "",
    var bio: String = "",
    var profileImage: String = "",
    var department: String = "",
    var batch: String = ""
)
