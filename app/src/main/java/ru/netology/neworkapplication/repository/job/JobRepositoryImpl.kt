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



import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JobRepositoryImpl @Inject constructor(
    private val jobDao: JobDao,
    private val apiService: ApiService,




    ) : JobRepository {
    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItemJob>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = { jobDao.getPagingSource() },
    ).flow.map { pagingData ->
        pagingData.map(JobEntity::toDto)
    }

    override suspend fun getJobAll(): List<Job> {
        try {


            val response = apiService.getJobAll()
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


    override suspend fun saveJob(job: Job) {
        try {


            val response = apiService.saveJob(job)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            jobDao.insert(JobEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {

            e.printStackTrace()
            throw UnknownError
        }
    }


    override suspend fun removeJobById(id: Long) {

        val response = apiService.removeJobById(id)
        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        jobDao.removeJobById(id)
    }


    override suspend fun getJob(id: Long): Job {
        try {

            val response = apiService.getJob(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            return body
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }


}
