package ru.netology.neworkapplication.util

import javax.inject.Singleton


import javax.inject.Inject


@Singleton
class TokenManager @Inject constructor() {
    private var token: String? = null
    private var id: Int? = null
    fun saveTokenAndId(token: String, id: Int) {
        this.token = token
        this.id = id
    }

    fun getToken(): String {
        if (token != null) {
            return token!!
        } else {
            throw NoTokenException()
        }
    }

    fun getId(): Int {
        if (id != null) {
            return id!!
        } else {
            throw NoIdException()
        }
    }

    fun clearToken() {
        token = null
    }
}

class NoTokenException : RuntimeException("No token available")
class NoIdException : RuntimeException("No id available")
