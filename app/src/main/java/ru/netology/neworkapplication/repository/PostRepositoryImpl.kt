package ru.netology.neworkapplication.repository

import android.util.Log
import androidx.paging.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

import retrofit2.HttpException
import ru.netology.neworkapplication.db.AppDb
import ru.netology.neworkapplication.api.ApiService
import ru.netology.neworkapplication.dao.PostDao
import ru.netology.neworkapplication.dto.*

import ru.netology.neworkapplication.entity.PostEntity
import ru.netology.neworkapplication.entity.toEntity

import ru.netology.neworkapplication.error.ApiError
import ru.netology.neworkapplication.error.AppError
import ru.netology.neworkapplication.error.UnknownError
import ru.netology.neworkapplication.error.NetworkError
import ru.netology.neworkapplication.util.TokenManager


import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val apiService: ApiService,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb,
    private val tokenManager: TokenManager,

    ) : PostRepository {
    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItem>> =
        Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = { postDao.getPagingSource() },
            remoteMediator = PostRemoteMediator(
                service = apiService,
                postDao = postDao,
                postRemoteKeyDao = postRemoteKeyDao,
                appDb = appDb,
            )
        ).flow
            .map { pagingData ->
                pagingData.map(PostEntity::toDto)

            }


    override suspend fun getAll() {
        try {
            val token = tokenManager.getToken() // Get the token

            val response = apiService.getAll(token)
            Log.d("apiService.getAll", "Response: ${response.code()} - ${response.message()}")
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())


            postDao.insert(body.toEntity())

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override fun getNewerCount(id: Long): Flow<Int> = flow {
        while (true) {
            delay(120_000L)
            val response = apiService.getNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(body.toEntity())
            emit(body.size)
        }
    }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

    override suspend fun save(post: Post) {
        try {
            val token = tokenManager.getToken() // Get the token
            val authHeader = token
            val response = apiService.save(authHeader, post)
            Log.d("PostRepository", "Response: ${response.code()} - ${response.message()}")
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }


    override suspend fun removeById(id: Int) {
        val token = tokenManager.getToken()
        val response = apiService.removeById(token, id.toString())
        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        postDao.removeById(id)
    }

    override suspend fun likeById(post: Post) {
        val token = tokenManager.getToken()
        val likedByMeValue = post.likedByMe
        val postResponse = apiService.let {
            if (likedByMeValue)
                it.dislikeById(token, post.id.toString())
            else
                it.likeById(token, post.id.toString())

        }
        if (!postResponse.isSuccessful) {
            throw HttpException(postResponse)

        }
        val updatedPost = postResponse.body() ?: throw HttpException(postResponse)

        val postEntity = PostEntity.fromDto(updatedPost)
        postDao.insert(postEntity)


    }

    override suspend fun getPost(id: Int): Post {
        try {
            val token = tokenManager.getToken() // Get the token
            val authHeader = token
            val response = apiService.getPost(authHeader, id)
            Log.d("PostRepository", "Response: ${response.code()} - ${response.message()}")
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            return body // Returns the post as DTO
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }

    override suspend fun upload(upload: MediaUpload): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", upload.file.name, upload.file.asRequestBody()
            )

            val response = apiService.upload(media)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

}
