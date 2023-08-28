package ru.netology.neworkapplication.repository


import android.util.Log
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import ru.netology.neworkapplication.api.ApiService
import ru.netology.neworkapplication.auth.AppAuth

import ru.netology.neworkapplication.dto.LoginResponse
import ru.netology.neworkapplication.dto.RegistrationResponse
import ru.netology.neworkapplication.dto.RequestLogin

import javax.inject.Inject


class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val appAuth: AppAuth
) {
    suspend fun register(

        login: RequestBody,
        password: RequestBody,
        name: RequestBody,
        avatar: MultipartBody.Part?,
    ): Response<RegistrationResponse> {
        val response = apiService.register(login, password, name, avatar)
        if (response.isSuccessful) {
            response.body()?.let {
                appAuth.saveTokenAndId(it.token, it.id)


            }
        }
        return response
    }

    suspend fun login(request: RequestLogin): Response<LoginResponse> {
        val response = apiService.login(request)
        if (response.isSuccessful) {
            response.body()?.let {
                appAuth.saveTokenAndId(it.token, it.id)


            }
        }

        return response
    }

}