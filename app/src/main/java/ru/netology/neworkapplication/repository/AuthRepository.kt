package ru.netology.neworkapplication.repository


import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import ru.netology.neworkapplication.api.ApiService
import ru.netology.neworkapplication.dto.LoginRequest
import ru.netology.neworkapplication.dto.LoginResponse
import ru.netology.neworkapplication.dto.RegistrationRequest
import ru.netology.neworkapplication.dto.RegistrationResponse
import javax.inject.Inject


class AuthRepository @Inject constructor(private val apiService: ApiService) {
    suspend fun register(
        avatar: RequestBody?,
        login: RequestBody,
        password: RequestBody,
        name: RequestBody
    ): Response<RegistrationResponse> {
        return apiService.register(avatar, login, password, name)
    }

    suspend fun login(login: RequestBody, password: RequestBody): Response<LoginResponse> {
        return apiService.login(login, password)
    }
}