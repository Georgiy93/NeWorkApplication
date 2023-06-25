package ru.netology.neworkapplication.repository.job

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.neworkapplication.dto.FeedItem
import ru.netology.neworkapplication.dto.FeedItemJob
import ru.netology.neworkapplication.dto.Job


interface JobRepository {
    val data: Flow<PagingData<FeedItemJob>>
    suspend fun getJobAll(token: String): List<Job>
    suspend fun saveJob(job: Job, token: String)
    suspend fun getJob(id: Int): Job
    suspend fun removeJobById(id: Int, token: String)
}

