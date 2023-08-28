package ru.netology.neworkapplication.repository.wall

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.neworkapplication.dto.FeedItem

import ru.netology.neworkapplication.dto.Post


interface WallRepository {
    val data: Flow<PagingData<FeedItem>>
    suspend fun getAll()
    fun getNewerCount(id: Long): Flow<Int>

}

