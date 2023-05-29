package ru.netology.neworkapplication.repository.job

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.neworkapplication.dto.FeedItem
import ru.netology.neworkapplication.dto.FeedItemJob
import ru.netology.neworkapplication.dto.Job


interface JobRepository {
    val data: Flow<PagingData<FeedItemJob>>
    suspend fun getJobAll()


    suspend fun saveJob(job: Job)
    suspend fun getJob(id: Int): Job
    suspend fun removeJobById(id: Int)


}

