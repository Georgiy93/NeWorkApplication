package ru.netology.neworkapplication.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.neworkapplication.dto.*
import ru.netology.neworkapplication.model.MediaModel


interface PostRepository {
    val data: Flow<PagingData<FeedItem>>
    suspend fun getAll()
    fun getNewerCount(id: Long): Flow<Int>
    suspend fun save(post: Post)
    suspend fun saveWithAttachment(post: Post, mediaParam: MediaModel)

    suspend fun getPost(id: Long): Post
    suspend fun removeById(id: Long)
    suspend fun likeById(post: Post)


}

