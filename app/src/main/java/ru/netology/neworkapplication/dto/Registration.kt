package ru.netology.neworkapplication.dto

data class RegistrationRequest(
    val login: String,
    val password: String,
    val name: String,
    val avatar: String? = null,
)

data class RegistrationResponse(
    val id: Int,
    val token: String
)

data class LoginRequest(
    val id: Long,
    val login: String,
    val name: String,
    val avatar: String?,
)

data class LoginResponse(
    val id: Int,
    val token: String

)