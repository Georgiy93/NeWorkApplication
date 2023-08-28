package ru.netology.neworkapplication.auth

import okhttp3.Interceptor
import okhttp3.Response


class AuthInterceptor(private val appAuth: AppAuth) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val token: String = try {
            appAuth.getToken()
        } catch (e: NoTokenException) {
            return chain.proceed(original)
        }

        val requestBuilder = original.newBuilder()
            .header("Authorization", token)
            .method(original.method, original.body)

        return chain.proceed(requestBuilder.build())
    }
}
