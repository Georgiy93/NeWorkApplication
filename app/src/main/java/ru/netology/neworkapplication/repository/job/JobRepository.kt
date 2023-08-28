package ru.netology.neworkapplication.repository.job

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.neworkapplication.dto.FeedItem
import ru.netology.neworkapplication.dto.FeedItemJob
import ru.netology.neworkapplication.dto.Job


interface JobRepository {
    val data: Flow<PagingData<FeedItemJob>>
    suspend fun getJobAll(): List<Job>
    suspend fun saveJob(job: Job)
    suspend fun getJob(id: Long): Job
    suspend fun removeJobById(id: Long)
}

