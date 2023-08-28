package ru.netology.neworkapplication.auth

import android.content.Context

import dagger.hilt.android.qualifiers.ApplicationContext

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.neworkapplication.R


import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val idKey = "id"
    private val tokenKey = "token"

    private val _authStateFlow: MutableStateFlow<AuthState>

    init {
        val id = prefs.getLong(idKey, 0L)
        val token = prefs.getString(tokenKey, null)

        if (id == 0L || token == null) {
            _authStateFlow = MutableStateFlow(AuthState())
            clearAuth()
        } else {
            _authStateFlow = MutableStateFlow(AuthState(id.toLong(), token))
        }
    }

    val authStateFlow: StateFlow<AuthState> = _authStateFlow.asStateFlow()

    fun saveTokenAndId(token: String, id: Long) {
        with(prefs.edit()) {
            putString(tokenKey, token)
            putLong(idKey, id.toLong())
            apply()
        }
        _authStateFlow.value = AuthState(id, token)
    }

    fun getToken(): String {
        val token = _authStateFlow.value.token
        if (token != null) {
            return token
        } else {
            throw NoTokenException(context)
        }
    }

    @Throws(NoIdException::class)
    fun getId(): Long {
        val id = _authStateFlow.value.id
        if (id != 0L) {
            return id
        } else {
            throw NoIdException(context)
        }
    }

    fun clearAuth() {
        with(prefs.edit()) {
            clear()
            apply()
        }
        _authStateFlow.value = AuthState()
    }
}

data class AuthState(val id: Long = 0, val token: String? = null)

class NoTokenException(context: Context) :
    RuntimeException(context.getString(R.string.no_token_available))

class NoIdException(context: Context) :
    RuntimeException(context.getString(R.string.no_id_available))
