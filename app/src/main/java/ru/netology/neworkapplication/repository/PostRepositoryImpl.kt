package ru.netology.neworkapplication.repository

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
import ru.netology.neworkapplication.enumeration.AttachmentType

import ru.netology.neworkapplication.error.ApiError
import ru.netology.neworkapplication.error.AppError
import ru.netology.neworkapplication.error.UnknownError
import ru.netology.neworkapplication.error.NetworkError
import ru.netology.neworkapplication.model.MediaModel



import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val apiService: ApiService,
    postRemoteKeyDao: PostRemoteKeyDao,
    appDb: AppDb,


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
                throw ApiError(response.code())
            }

            val body = response.body() ?: throw ApiError(response.code())


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
                throw ApiError(response.code())
            }

            val body = response.body() ?: throw ApiError(response.code())
            postDao.insert(body.toEntity())
            emit(body.size)
        }
    }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

    override suspend fun save(post: Post) {
        try {

            val response = apiService.save(post)

            if (!response.isSuccessful) {
                throw ApiError(response.code())
            }

            val body = response.body() ?: throw ApiError(response.code())
            postDao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }

    override suspend fun saveWithAttachment(post: Post, mediaParam: MediaModel) {
        try {


            val media = upload(mediaParam)


            val postWithAttachment =
                post.copy(attachment = Attachment(media.url, AttachmentType.IMAGE))

            val response = apiService.save(postWithAttachment)

            if (!response.isSuccessful) {
                throw ApiError(response.code())
            }

            val body = response.body() ?: throw ApiError(response.code())
            postDao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }


    override suspend fun removeById(id: Long) {

        val response = apiService.removeById(id.toString())
        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        postDao.removeById(id)
    }

    override suspend fun likeById(post: Post) {

        val likedByMeValue = post.likedByMe
        val postResponse = apiService.let {
            if (likedByMeValue)
                it.dislikeById(post.id.toString())
            else
                it.likeById(post.id.toString())

        }
        if (!postResponse.isSuccessful) {
            throw HttpException(postResponse)

        }
        val updatedPost = postResponse.body() ?: throw HttpException(postResponse)

        val postEntity = PostEntity.fromDto(updatedPost)
        postDao.insert(postEntity)


    }

    override suspend fun getPost(id: Long): Post {
        try {

            val response = apiService.getPost(id)

            if (!response.isSuccessful) {
                throw ApiError(response.code())
            }
            return response.body() ?: throw ApiError(
                response.code()
            )
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }

    private suspend fun upload(media: MediaModel): Media {
        try {


            val part = MultipartBody.Part.createFormData(
                "file",
                media.file.name,
                media.file.asRequestBody()
            )
            val response = apiService.upload(part)
            if (!response.isSuccessful) {
                throw ApiError(response.code())
            }

            return response.body() ?: throw ApiError(response.code())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

}
