package ru.netology.neworkapplication.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.neworkapplication.dto.FeedItem

import ru.netology.neworkapplication.dto.Post


interface PostRepository {
    val data: Flow<PagingData<FeedItem>>
    suspend fun getAll()
    fun getNewerCount(id: Long): Flow<Int>
    suspend fun save(post: Post)
    suspend fun removeById(id: Long)
    suspend fun likeById(post: Post)
    //  suspend fun upload(upload: MediaUpload): Media
}

