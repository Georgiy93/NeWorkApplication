
package ru.netology.neworkapplication.repository.events

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

import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao,
    private val apiService: ApiService,
    eventRemoteKeyDao: EventRemoteKeyDao,
    appDb: AppDb,


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

            )
        ).flow
            .map { pagingData ->
                pagingData.map(EventEntity::toDto)

            }


    override suspend fun getEventsAll() {
        try {


            val response = apiService.getEventsAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code())
            }

            val body = response.body() ?: throw ApiError(response.code())


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

            val response = apiService.getEventNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code())
            }

            val body = response.body() ?: throw ApiError(response.code())
            eventDao.insert(body.toEntity())
            emit(body.size)
        }
    }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

    override suspend fun saveEvent(event: Event): Event {
        try {

            val response = apiService.saveEvent(event)
            if (!response.isSuccessful) {
                throw ApiError(response.code())
            }

            val body = response.body() ?: throw ApiError(response.code())
            eventDao.insert(EventEntity.fromDto(body))
            return body
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }

    override suspend fun saveEventWithAttachment(event: Event, mediaParam: MediaModel): Event {
        try {


            val media = upload(mediaParam)


            val eventWithAttachment =
                event.copy(attachment = Attachment(media.url, AttachmentType.IMAGE))

            val response = apiService.saveEvent(eventWithAttachment)
            if (!response.isSuccessful) {
                throw ApiError(response.code())
            }

            val body = response.body() ?: throw ApiError(response.code())
            eventDao.insert(EventEntity.fromDto(body))
            return body
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }


    override suspend fun removeEventById(id: Long) {

        val response = apiService.removeEventById(id.toString())
        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        eventDao.removeById(id)
    }

    override suspend fun likeEventById(event: Event) {

        val likedByMeValue = event.likedByMe
        val eventResponse = apiService.let {
            if (likedByMeValue)
                it.dislikeEventById(event.id.toString())
            else
                it.likeEventById(event.id.toString())

        }
        if (!eventResponse.isSuccessful) {
            throw HttpException(eventResponse)

        }
        val updatedEvent = eventResponse.body() ?: throw HttpException(eventResponse)

        val eventEntity = EventEntity.fromDto(updatedEvent)
        eventDao.insert(eventEntity)


    }

    override suspend fun getEvent(id: Long): Event {
        try {

            val response = apiService.getEvent(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code())
            }

            return response.body() ?: throw ApiError(response.code())
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


    override suspend fun removeParticipantById(id: Long): Event {
        try {

            val response = apiService.removeParticipantById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code())
            }
            val body = response.body() ?: throw ApiError(response.code())
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
