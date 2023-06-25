package ru.netology.neworkapplication.repository.job

import android.util.Log
import androidx.paging.*

import retrofit2.HttpException
import ru.netology.neworkapplication.db.AppDb
import ru.netology.neworkapplication.api.ApiService
import ru.netology.neworkapplication.dao.JobDao
import ru.netology.neworkapplication.dto.*
import ru.netology.neworkapplication.entity.JobEntity
import kotlinx.coroutines.flow.*
import ru.netology.neworkapplication.entity.PostEntity
import ru.netology.neworkapplication.entity.toEntity

import ru.netology.neworkapplication.error.ApiError
import ru.netology.neworkapplication.error.UnknownError
import ru.netology.neworkapplication.error.NetworkError
import ru.netology.neworkapplication.util.TokenManager


import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JobRepositoryImpl @Inject constructor(
    private val jobDao: JobDao,
    private val apiService: ApiService,


    private val tokenManager: TokenManager,

    ) : JobRepository {
    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItemJob>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = { jobDao.getPagingSource() },
    ).flow.map { pagingData ->
        pagingData.map(JobEntity::toDto)
    }

    override suspend fun getJobAll(token: String): List<Job> {
        try {
            val token = tokenManager.getToken() // Get the token

            val response = apiService.getJobAll(token)
            Log.d("apiService.getAll", "Response: ${response.code()} - ${response.message()}")
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())


            jobDao.insert(body.toEntity())
            return body
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }


    override suspend fun saveJob(job: Job, token: String) {
        try {
            val authToken = tokenManager.getToken()

            val response = apiService.saveJob(authToken, job)
            Log.d("JobRepository", "Response: ${response.code()} - ${response.message()}")
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            jobDao.insert(JobEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            Log.e(
                "JobRepository",
                "Error in saveJob: ",
                e
            ) // Logs the exception message and stack trace
            e.printStackTrace()
            throw UnknownError
        }
    }


    override suspend fun removeJobById(id: Int, token: String) {
        val token = tokenManager.getToken()
        val response = apiService.removeJobById(id, token)
        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        jobDao.removeJobById(id)
    }


    override suspend fun getJob(id: Int): Job {
        try {

            val response = apiService.getJob(id)
            Log.d("JobRepository", "Response: ${response.code()} - ${response.message()}")
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            return body // Returns the post as DTO
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }


}
