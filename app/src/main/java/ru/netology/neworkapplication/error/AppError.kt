
package ru.netology.neworkapplication.error

import android.database.SQLException
import java.io.IOException

sealed class AppError : RuntimeException() {
    companion object {
        fun from(e: Throwable): AppError = when (e) {
            is AppError -> e
            is SQLException -> DbError
            is IOException -> NetworkError
            else -> UnknownError
        }
    }
}


class ApiError(@Suppress("unused") val status: Int) : AppError()
object NetworkError : AppError()
object DbError : AppError()
object UnknownError : AppError()