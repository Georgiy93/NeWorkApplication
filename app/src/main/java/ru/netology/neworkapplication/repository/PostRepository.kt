package ru.netology.neworkapplication.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.neworkapplication.dto.FeedItem
import ru.netology.neworkapplication.dto.Media
import ru.netology.neworkapplication.dto.MediaUpload

import ru.netology.neworkapplication.dto.Post


interface PostRepository {
    val data: Flow<PagingData<FeedItem>>
    suspend fun getAll()
    fun getNewerCount(id: Long): Flow<Int>

    suspend fun save(post: Post)
    suspend fun getPost(id: Int): Post
    suspend fun removeById(id: Int)
    suspend fun likeById(post: Post)
    suspend fun upload(upload: MediaUpload): Media

}

