package ru.netology.neworkapplication.repository

import androidx.paging.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

import retrofit2.HttpException
import ru.netology.neworkapplication.db.AppDb
import ru.netology.neworkapplication.api.ApiService
import ru.netology.neworkapplication.dao.PostDao
import ru.netology.neworkapplication.dto.*

import ru.netology.neworkapplication.entity.PostEntity
import ru.netology.neworkapplication.entity.toEntity
import ru.netology.neworkapplication.enumeration.AttachmentType

import ru.netology.neworkapplication.error.ApiError
import ru.netology.neworkapplication.error.AppError
import ru.netology.neworkapplication.error.UnknownError
import ru.netology.neworkapplication.error.NetworkError
import java.io.File


import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val apiService: ApiService,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb,
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
            val response = apiService.getAll()
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
            val gson = Gson()
            val postJson = gson.toJson(post)
            val postRequestBody = postJson.toRequestBody("application/json".toMediaTypeOrNull())

            val response = apiService.save(postRequestBody)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        val response = apiService.removeById(id)
        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        postDao.removeById(id)
    }

    override suspend fun likeById(post: Post) {

        val likedByMeValue = post.likedByMe
        val postResponse = apiService.let {
            if (likedByMeValue)
                it.dislikeById(post.id)
            else
                it.likeById(post.id)
        }
        if (!postResponse.isSuccessful) {
            throw HttpException(postResponse)

        }
        val updatedPost = postResponse.body() ?: throw HttpException(postResponse)
        val postEntity = PostEntity.fromDto(updatedPost)
        postDao.insert(postEntity)


    }


}
