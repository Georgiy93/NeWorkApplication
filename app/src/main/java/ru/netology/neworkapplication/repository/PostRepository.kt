package ru.netology.neworkapplication.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import ru.netology.neworkapplication.dto.*
import ru.netology.neworkapplication.model.MediaModel


interface PostRepository {
    val data: Flow<PagingData<FeedItem>>
    suspend fun getAll()
    fun getNewerCount(id: Long): Flow<Int>
    suspend fun save(post: Post)
    suspend fun saveWithAttachment(post: Post, media: MediaModel)

    suspend fun getPost(id: Int): Post
    suspend fun removeById(id: Int)
    suspend fun likeById(post: Post)


}

