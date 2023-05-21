package ru.netology.neworkapplication.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.neworkapplication.dto.*

interface ApiService {

    @POST("/api/users/registration/")
    suspend fun register(@Body registration: RegistrationRequest): Response<RegistrationResponse>

    @POST("/api/users/authentication/")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("/api/posts")
    suspend fun save(@Body post: Post): Response<Post>

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
//    @Multipart
//    @POST("media")
//    suspend fun upload(@Part media: MultipartBody.Part): Response<Media>
}