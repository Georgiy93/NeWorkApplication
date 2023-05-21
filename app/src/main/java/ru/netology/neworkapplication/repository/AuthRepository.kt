package ru.netology.neworkapplication.repository


import retrofit2.Response
import ru.netology.neworkapplication.api.ApiService
import ru.netology.neworkapplication.dto.LoginRequest
import ru.netology.neworkapplication.dto.LoginResponse
import ru.netology.neworkapplication.dto.RegistrationRequest
import ru.netology.neworkapplication.dto.RegistrationResponse
import javax.inject.Inject

class AuthRepository @Inject constructor(private val apiService: ApiService) {

    suspend fun register(registrationRequest: RegistrationRequest): Response<RegistrationResponse> {
        return apiService.register(registrationRequest)
    }

    suspend fun login(loginRequest: LoginRequest): Response<LoginResponse> {
        return apiService.login(loginRequest)
    }
}