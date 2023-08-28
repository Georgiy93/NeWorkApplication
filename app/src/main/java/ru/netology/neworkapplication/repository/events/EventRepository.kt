package ru.netology.neworkapplication.repository.events

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.neworkapplication.dto.*
import ru.netology.neworkapplication.model.MediaModel


interface EventRepository {
    val data: Flow<PagingData<FeedItemEvent>>
    suspend fun getEventsAll()

    fun getEventNewer(id: Long): Flow<Int>
    suspend fun saveEvent(event: Event): Event
    suspend fun saveEventWithAttachment(event: Event, media: MediaModel): Event

    suspend fun getEvent(id: Long): Event
    suspend fun removeEventById(id: Long)
    suspend fun likeEventById(event: Event)

    suspend fun userAll(): List<LoginRequest>
    suspend fun removeParticipantById(id: Long): Event

}

