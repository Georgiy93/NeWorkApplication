package ru.netology.neworkapplication.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.neworkapplication.dto.*

interface ApiService {

    @Multipart
    @POST("/api/users/registration/")
    suspend fun register(
        @Part("avatar") avatarUrl: RequestBody?,
        @Part("login") login: RequestBody,
        @Part("password") password: RequestBody,
        @Part("name") name: RequestBody
    ): Response<RegistrationResponse>

    @POST("/api/users/authentication/")
    @Multipart
    suspend fun login(
        @Part("login") login: RequestBody,
        @Part("password") password: RequestBody
    ): Response<LoginResponse>


    @POST("/api/posts")
    @Multipart
    suspend fun save(@Part("post") post: RequestBody): Response<Post>

    @GET("/api/posts/latest")
    suspend fun getLatest(@Query("count") count: Int): Response<List<Post>>

    @GET("/api/posts/{id}/before")
    suspend fun getBefore(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("/api/posts")
    suspend fun getAll(): Response<List<Post>>

    @GET("/api/posts/{id}/newer")
    suspend fun getNewer(@Path("id") id: Long): Response<List<Post>>

    @DELETE("/api/posts/{id}")
    suspend fun removeById(@Path("id") id: Long): Response<Unit>

    @POST("/api/posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Response<Post>

    @DELETE("/api/posts/{id}/likes")
    suspend fun dislikeById(@Path("id") id: Long): Response<Post>

    @GET("/api/posts/{id}/after")
    suspend fun getAfter(@Path("id") id: Long, @Query("count") count: Int): Response<List<Post>>


}
