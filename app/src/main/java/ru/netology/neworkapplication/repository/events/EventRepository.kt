package ru.netology.neworkapplication.repository.events

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.neworkapplication.dto.*
import ru.netology.neworkapplication.model.MediaModel


interface EventRepository {
    val data: Flow<PagingData<FeedItemEvent>>
    suspend fun getEventsAll()
    fun getEventNewer(id: Long): Flow<Int>
    suspend fun saveEvent(event: Event)
    suspend fun saveEventWithAttachment(event: Event, media: MediaModel)

    suspend fun getEvent(id: Int): Event
    suspend fun removeEventById(id: Int)
    suspend fun likeEventById(event: Event)
    //suspend fun  addParticipants(id: Int):Event


}

