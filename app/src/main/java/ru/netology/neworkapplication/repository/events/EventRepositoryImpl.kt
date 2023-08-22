package ru.netology.neworkapplication.repository.events

import android.util.Log
import androidx.paging.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import ru.netology.neworkapplication.api.ApiService
import ru.netology.neworkapplication.dao.EventDao
import ru.netology.neworkapplication.db.AppDb
import ru.netology.neworkapplication.dto.*
import ru.netology.neworkapplication.entity.EventEntity
import ru.netology.neworkapplication.entity.toEntity
import ru.netology.neworkapplication.enumeration.AttachmentType
import ru.netology.neworkapplication.error.ApiError
import ru.netology.neworkapplication.error.AppError


import ru.netology.neworkapplication.error.UnknownError
import ru.netology.neworkapplication.error.NetworkError
import ru.netology.neworkapplication.model.MediaModel
import ru.netology.neworkapplication.util.TokenManager
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao,
    private val apiService: ApiService,
    private val eventRemoteKeyDao: EventRemoteKeyDao,
    private val appDb: AppDb,
    private val tokenManager: TokenManager,

    ) : EventRepository {
    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItemEvent>> =
        Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = { eventDao.getPagingSource() },
            remoteMediator = EventRemoteMediator(
                service = apiService,
                eventDao = eventDao,
                eventRemoteKeyDao = eventRemoteKeyDao,
                appDb = appDb,
                tokenManager = tokenManager
            )
        ).flow
            .map { pagingData ->
                pagingData.map(EventEntity::toDto)

            }


    override suspend fun getEventsAll() {
        try {
            val token = tokenManager.getToken() // Get the token

            val response = apiService.getEventsAll(token)
            Log.d("apiService.getEventsAll", "Response: ${response.code()} - ${response.message()}")
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())


            eventDao.insert(body.toEntity())

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }


    override fun getEventNewer(id: Long): Flow<Int> = flow {
        while (true) {
            delay(120_000L)
            val token = tokenManager.getToken() // Get the token
            val authHeader = token
            val response = apiService.getEventNewer(authHeader, id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            eventDao.insert(body.toEntity())
            emit(body.size)
        }
    }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

    override suspend fun saveEvent(event: Event): Event {
        try {
            val token = tokenManager.getToken() // Get the token
            val authHeader = token
            val response = apiService.saveEvent(authHeader, event)
            Log.d("saveEvent", "Response: ${response.code()} - ${response.message()}")
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            eventDao.insert(EventEntity.fromDto(body))
            return body
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }

    override suspend fun saveEventWithAttachment(event: Event, upload: MediaModel): Event {
        try {
            val token = tokenManager.getToken() // Get the token
            val authHeader = token

            val media = upload(upload)


            val eventWithAttachment =
                event.copy(attachment = Attachment(media.url, AttachmentType.IMAGE))

            val response = apiService.saveEvent(authHeader, eventWithAttachment ?: event)
            Log.d("saveEventWithAttachment", "Response: ${response.code()} - ${response.message()}")
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            eventDao.insert(EventEntity.fromDto(body))
            return body
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }


    override suspend fun removeEventById(id: Int) {
        val token = tokenManager.getToken()
        val response = apiService.removeEventById(token, id.toString())
        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        eventDao.removeById(id)
    }

    override suspend fun likeEventById(event: Event) {
        val token = tokenManager.getToken()
        val likedByMeValue = event.likedByMe
        val eventResponse = apiService.let {
            if (likedByMeValue)
                it.dislikeEventById(token, event.id.toString())
            else
                it.likeEventById(token, event.id.toString())

        }
        if (!eventResponse.isSuccessful) {
            throw HttpException(eventResponse)

        }
        val updatedEvent = eventResponse.body() ?: throw HttpException(eventResponse)

        val eventEntity = EventEntity.fromDto(updatedEvent)
        eventDao.insert(eventEntity)


    }

    override suspend fun getEvent(id: Int): Event {
        try {
            val token = tokenManager.getToken() // Get the token
            val authHeader = token
            val response = apiService.getEvent(authHeader, id)
            Log.d("EventRepository", "Response: ${response.code()} - ${response.message()}")
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            return body
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }

    override suspend fun userAll(): List<LoginRequest> {
        try {


            val response = apiService.userAll()
            Log.d("apiService.getEventsAll", "Response: ${response.code()} - ${response.message()}")
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())


            return body

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }


    override suspend fun removeParticipantById(id: Int): Event {
        try {
            val token = tokenManager.getToken() // Get the token
            val authHeader = token
            val response = apiService.removeParticipantById(authHeader, id)
            Log.d("EventRepository", "Response: ${response.code()} - ${response.message()}")
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            eventDao.insert(EventEntity.fromDto(body))
            return body
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }

    private suspend fun upload(media: MediaModel): Media {
        try {
            val token = tokenManager.getToken()

            val part = MultipartBody.Part.createFormData(
                "file",
                media.file.name,
                media.file.asRequestBody()
            )
            val response = apiService.upload(token, part)
            Log.d("media", "Response: ${response.code()} - ${response.message()}")
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
