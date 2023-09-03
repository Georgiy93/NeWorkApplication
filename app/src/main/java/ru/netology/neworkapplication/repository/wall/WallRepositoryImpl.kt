package ru.netology.neworkapplication.repository.wall

import androidx.paging.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

import ru.netology.neworkapplication.db.AppDb
import ru.netology.neworkapplication.api.ApiService
import ru.netology.neworkapplication.dao.PostDao
import ru.netology.neworkapplication.dto.*

import ru.netology.neworkapplication.entity.PostEntity
import ru.netology.neworkapplication.entity.toEntity

import ru.netology.neworkapplication.error.ApiError
import ru.netology.neworkapplication.error.AppError
import ru.netology.neworkapplication.error.UnknownError
import ru.netology.neworkapplication.error.NetworkError



import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("CanBeParameter")
@Singleton
class WallRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val apiService: ApiService,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb,


    ) : WallRepository {
    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItem>> =
        Pager(
            config = PagingConfig(pageSize = 4, enablePlaceholders = false),
            pagingSourceFactory = { postDao.getPagingSource() },
            remoteMediator = WallRemoteMediator(
                service = apiService,
                postDao = postDao,
                postRemoteKeyDao = postRemoteKeyDao,
                appDb = appDb,

                )
        ).flow
            .map { pagingData ->
                pagingData.map(PostEntity::toDto)

            }


    override suspend fun getAll() {
        try {


            val response = apiService.getWallAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code())
            }

            val body = response.body()?.filter { post ->
                post.content.isNotEmpty() && post.content.isNotBlank()
            } ?: throw ApiError(response.code())

            postDao.insert(body.toEntity())


        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override fun getNewerCount(id: Long): Flow<Int> = flow {
        while (true) {
            delay(120_000L)
            val response = apiService.getWallNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code())
            }

            val body = response.body()?.filter { post ->
                post.content.isNotEmpty() && post.content.isNotBlank()
            } ?: throw ApiError(response.code())

            postDao.insert(body.toEntity())

            emit(body.size)
        }
    }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)


}
