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

        @Part("login") login: RequestBody,
        @Part("password") password: RequestBody,
        @Part("name") name: RequestBody,
        @Part avatarUrl: MultipartBody.Part?,
    ): Response<RegistrationResponse>

    @POST("/api/users/authentication/")
    suspend fun login(
        @Body request: RequestLogin
    ): Response<LoginResponse>


    @POST("/api/posts")

    suspend fun save(
        @Header("Authorization") authHeader: String,
        @Body post: Post
    ): Response<Post>

    @GET("/api/posts/latest")
    suspend fun getLatest(
        @Header("Authorization") authHeader: String,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("/api/posts/{id}/")
    suspend fun getPost(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Int
    ): Response<Post>

    @GET("/api/posts/{id}/before")
    suspend fun getBefore(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("/api/posts")
    suspend fun getAll(
        @Header("Authorization") authHeader: String
    ): Response<List<Post>>

    @GET("/api/posts/{id}/newer")
    suspend fun getNewer(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Long
    ): Response<List<Post>>

    @DELETE("/api/posts/{id}")
    suspend fun removeById(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String
    ): Response<Unit>

    @POST("/api/posts/{id}/likes/")
    suspend fun likeById(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String
    ): Response<Post>

    @DELETE("/api/posts/{id}/likes")
    suspend fun dislikeById(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String
    ): Response<Post>


    @GET("/api/posts/{id}/after")
    suspend fun getAfter(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Long, @Query("count") count: Int
    ): Response<List<Post>>

    @Multipart
    @POST("/api/media")
    suspend fun upload(
        @Header("Authorization") authHeader: String,
        @Part media: MultipartBody.Part
    ): Response<Media>


    @POST("/api/my/jobs")
    suspend fun saveJob(
        @Header("Authorization") token: String,
        @Body job: Job,
    ): Response<Job>

    @GET("/api/{id}/jobs")
    suspend fun getJob(

        @Path("id") id: Int
    ): Response<Job>

    @DELETE("/api/my/jobs/{id}")
    suspend fun removeJobById(

        @Path("id") id: Int,
        @Header("Authorization") token: String,
    ): Response<Job>

    @GET("/api/my/jobs")
    suspend fun getJobAll(
        @Header("Authorization") token: String
    ): Response<List<Job>>


    @GET("/api/my/wall/latest/")
    suspend fun getWallLatest(
        @Header("Authorization") authHeader: String,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("/api/my/wall/{id}/after")
    suspend fun getWallAfter(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("/api/my/wall/{id}/before")
    suspend fun getWallBefore(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("/api/my/wall/")
    suspend fun getWallAll(
        @Header("Authorization") authHeader: String
    ): Response<List<Post>>

    @GET("/api/my/wall/{id}/newer")
    suspend fun getWallNewer(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Long
    ): Response<List<Post>>


    @POST("/api/events/")

    suspend fun saveEvent(
        @Header("Authorization") authHeader: String,
        @Body event: Event
    ): Response<Event>

    @GET("/api/events/latest/")
    suspend fun getEventLatest(
        @Header("Authorization") authHeader: String,
        @Query("count") count: Int
    ): Response<List<Event>>

    @GET("/api/events/{id}/after/")
    suspend fun getEvenAfter(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Event>>


    @GET("/api/events/{id}/before")
    suspend fun getEventBefore(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Event>>


    @GET("/api/events/")
    suspend fun getEventsAll(
        @Header("Authorization") authHeader: String
    ): Response<List<Event>>


    @GET("/api/events/{id}/")
    suspend fun getEvent(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Int
    ): Response<Event>


    @GET("/api/events/{id}/newer/")
    suspend fun getEventNewer(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Long
    ): Response<List<Event>>


    @DELETE("/api/events/{id}")
    suspend fun removeEventById(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String
    ): Response<Unit>

    @POST("/api/events/{id}/likes/")
    suspend fun likeEventById(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String
    ): Response<Event>

    @POST("/api/events/{id}/participants/")
    suspend fun addParticipants(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Int
    ): Response<Event>

    @DELETE("/api/events/{id}/likes")
    suspend fun dislikeEventById(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String
    ): Response<Event>


}
