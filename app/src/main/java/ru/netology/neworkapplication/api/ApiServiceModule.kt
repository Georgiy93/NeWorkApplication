package ru.netology.neworkapplication.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.netology.neworkapplication.BuildConfig
import ru.netology.neworkapplication.auth.AppAuth
import ru.netology.neworkapplication.auth.AuthInterceptor

import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApiServiceModule {
    companion object {
        private const val BASE_URL = "${BuildConfig.BASE_URL}"
    }

    @Provides
    @Singleton
    fun provideRetrofit(appAuth: AppAuth): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(AuthInterceptor(appAuth))
                    .addInterceptor(
                        HttpLoggingInterceptor()
                            .apply {
                                level = HttpLoggingInterceptor.Level.BODY
                            }
                    ).build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
}