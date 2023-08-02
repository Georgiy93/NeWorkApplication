package ru.netology.neworkapplication.dto

data class Users(

    val user: Map<String, UserPreview>
)

data class UserPreview(
    val name: String,
    val avatar: String?,
)
