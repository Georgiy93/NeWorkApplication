package ru.netology.neworkapplication.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.neworkapplication.dto.*

interface ApiService {
    @POST("users/push-tokens")
    suspend fun save(@Body pushToken: PushToken): Response<Unit>

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

    @GET("/api/users/")
    suspend fun userAll(): Response<List<LoginRequest>>

    @POST("/api/posts")

    suspend fun save(

        @Body post: Post
    ): Response<Post>

    @GET("/api/posts/latest")
    suspend fun getLatest(

        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("/api/posts/{id}/")
    suspend fun getPost(

        @Path("id") id: Long
    ): Response<Post>

    @GET("/api/posts/{id}/before")
    suspend fun getBefore(

        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("/api/posts")
    suspend fun getAll(

    ): Response<List<Post>>

    @GET("/api/posts/{id}/newer")
    suspend fun getNewer(

        @Path("id") id: Long
    ): Response<List<Post>>

    @DELETE("/api/posts/{id}")
    suspend fun removeById(

        @Path("id") id: String
    ): Response<Unit>

    @POST("/api/posts/{id}/likes/")
    suspend fun likeById(

        @Path("id") id: String
    ): Response<Post>

    @DELETE("/api/posts/{id}/likes")
    suspend fun dislikeById(

        @Path("id") id: String
    ): Response<Post>


    @GET("/api/posts/{id}/after")
    suspend fun getAfter(

        @Path("id") id: Long, @Query("count") count: Int
    ): Response<List<Post>>

    @Multipart
    @POST("/api/media")
    suspend fun upload(

        @Part media: MultipartBody.Part
    ): Response<Media>


    @POST("/api/my/jobs")
    suspend fun saveJob(

        @Body job: Job,
    ): Response<Job>

    @GET("/api/{id}/jobs")
    suspend fun getJob(

        @Path("id") id: Long
    ): Response<Job>

    @DELETE("/api/my/jobs/{id}")
    suspend fun removeJobById(

        @Path("id") id: Long,

        ): Response<Job>

    @GET("/api/my/jobs")
    suspend fun getJobAll(

    ): Response<List<Job>>


    @GET("/api/my/wall/latest/")
    suspend fun getWallLatest(

        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("/api/my/wall/{id}/after")
    suspend fun getWallAfter(

        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("/api/my/wall/{id}/before")
    suspend fun getWallBefore(

        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("/api/my/wall/")
    suspend fun getWallAll(

    ): Response<List<Post>>

    @GET("/api/my/wall/{id}/newer")
    suspend fun getWallNewer(

        @Path("id") id: Long
    ): Response<List<Post>>


    @POST("/api/events/")

    suspend fun saveEvent(

        @Body event: Event
    ): Response<Event>

    @GET("/api/events/latest/")
    suspend fun getEventLatest(

        @Query("count") count: Int
    ): Response<List<Event>>

    @GET("/api/events/{id}/after/")
    suspend fun getEvenAfter(

        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Event>>


    @GET("/api/events/{id}/before")
    suspend fun getEventBefore(

        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Event>>


    @GET("/api/events/")
    suspend fun getEventsAll(

    ): Response<List<Event>>


    @GET("/api/events/{id}/")
    suspend fun getEvent(

        @Path("id") id: Long
    ): Response<Event>


    @GET("/api/events/{id}/newer/")
    suspend fun getEventNewer(

        @Path("id") id: Long
    ): Response<List<Event>>


    @DELETE("/api/events/{id}")
    suspend fun removeEventById(

        @Path("id") id: String
    ): Response<Unit>

    @POST("/api/events/{id}/likes/")
    suspend fun likeEventById(

        @Path("id") id: String
    ): Response<Event>

    @POST("/api/events/{id}/participants/")
    suspend fun addParticipants(

        @Path("id") id: Long
    ): Response<Event>

    @DELETE("/api/events/{id}/participants/")
    suspend fun removeParticipantById(

        @Path("id") id: Long
    ): Response<Event>

    @DELETE("/api/events/{id}/likes")
    suspend fun dislikeEventById(

        @Path("id") id: String
    ): Response<Event>


}
