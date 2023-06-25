package ru.netology.neworkapplication.repository


import android.util.Log
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import ru.netology.neworkapplication.api.ApiService

import ru.netology.neworkapplication.dto.LoginResponse
import ru.netology.neworkapplication.dto.RegistrationResponse
import ru.netology.neworkapplication.dto.RequestLogin
import ru.netology.neworkapplication.util.TokenManager
import javax.inject.Inject


class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    suspend fun register(

        login: RequestBody,
        password: RequestBody,
        name: RequestBody,
        avatar: MultipartBody.Part?,
    ): Response<RegistrationResponse> {
        val response = apiService.register(login, password, name, avatar)
        if (response.isSuccessful) {
            response.body()?.let { registrationResponse ->
                registrationResponse.token?.let { token ->
                    registrationResponse.id?.let { id ->
                        tokenManager.saveTokenAndId(token, id)
                    }
                }
            }
        }
        return response
    }

    suspend fun login(request: RequestLogin): Response<LoginResponse> {
        val response = apiService.login(request)
        if (response.isSuccessful) {
            response.body()?.let { loginResponse ->
                loginResponse.token?.let { token ->
                    loginResponse.id?.let { id ->
                        tokenManager.saveTokenAndId(token, id)
                    }
                }
            }
        }
        Log.d("login", "Response: ${response.code()} - ${response.message()}")
        return response
    }

}