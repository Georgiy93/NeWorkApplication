package ru.netology.neworkapplication.dto


data class RegistrationResponse(
    val id: Long,
    val token: String
)

data class LoginRequest(
    val id: Long,
    val login: String,
    val name: String,
    val avatar: String?,
)

data class LoginResponse(
    val id: Long,
    val token: String

)