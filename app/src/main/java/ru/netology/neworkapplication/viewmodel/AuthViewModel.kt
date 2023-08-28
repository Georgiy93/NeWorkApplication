package ru.netology.neworkapplication.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.neworkapplication.dto.RegistrationResponse
import ru.netology.neworkapplication.dto.RequestLogin
import ru.netology.neworkapplication.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: AuthRepository,
) : ViewModel() {

    private val _registrationLoading = MutableLiveData(false)
    val registrationLoading: LiveData<Boolean> = _registrationLoading

    private val _registrationResult = MutableLiveData<ApiResponse<RegistrationResponse>>()
    val registrationResult: LiveData<ApiResponse<RegistrationResponse>> = _registrationResult

    private val _registrationError = MutableLiveData<String>()
    val registrationError: LiveData<String> = _registrationError

    private val _loginLoading = MutableLiveData(false)
    val loginLoading: LiveData<Boolean> = _loginLoading

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult

    private val _loginError = MutableLiveData<String>()
    val loginError: LiveData<String> = _loginError


    fun register(

        login: String,
        password: String,
        name: String,
        avatar: ByteArray?,

        ) {
        _registrationLoading.value = true
        viewModelScope.launch {
            try {
                val avatarRequestBody = avatar?.toRequestBody("image/*".toMediaTypeOrNull())
                    ?.let { MultipartBody.Part.createFormData("file", "file", it) }
                val loginRequestBody = login.toRequestBody("text/plain".toMediaTypeOrNull())
                val passwordRequestBody = password.toRequestBody("text/plain".toMediaTypeOrNull())
                val nameRequestBody = name.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = userRepository.register(

                    loginRequestBody,
                    passwordRequestBody,
                    nameRequestBody,
                    avatarRequestBody,
                )
                _registrationLoading.value = false
                if (response.isSuccessful) {
                    _registrationResult.value = ApiResponse(response.body(), response.code(), null)
                } else {
                    _registrationResult.value =
                        ApiResponse(null, response.code(), response.message())
                }
            } catch (e: Exception) {
                _registrationLoading.value = false
                _registrationError.value = e.localizedMessage
            }
        }
    }

    fun login(request: RequestLogin) {
        _loginLoading.value = true
        viewModelScope.launch {
            try {
                val response = userRepository.login(request)
                _loginLoading.value = false
                if (response.isSuccessful) {
                    _loginResult.value = true
                } else {
                    _loginResult.value = false
                    _loginError.value = response.message()
                }
            } catch (e: Exception) {
                _loginLoading.value = false
                _loginError.value = e.localizedMessage
            }
        }
    }

}

data class ApiResponse<T>(
    val responseBody: T?,
    val statusCode: Int,
    val errorMessage: String?
)
