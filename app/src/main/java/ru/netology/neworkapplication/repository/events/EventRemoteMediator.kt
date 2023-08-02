package ru.netology.neworkapplication.repository.events

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import retrofit2.HttpException
import ru.netology.neworkapplication.api.ApiService
import ru.netology.neworkapplication.dao.EventDao
import ru.netology.neworkapplication.db.AppDb
import ru.netology.neworkapplication.dto.EventRemoteKeyDao
import ru.netology.neworkapplication.entity.EventEntity
import ru.netology.neworkapplication.entity.EventRemoteKeyEntity
import ru.netology.neworkapplication.error.ApiError
import ru.netology.neworkapplication.util.TokenManager
import java.io.IOException


@OptIn(ExperimentalPagingApi::class)
class EventRemoteMediator(
    private val eventDao: EventDao,
    private val service: ApiService,
    private val eventRemoteKeyDao: EventRemoteKeyDao,
    private val appDb: AppDb,
    private val tokenManager: TokenManager,
) : RemoteMediator<Int, EventEntity>() {


    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, EventEntity>
    ): MediatorResult {
        val token = tokenManager.getToken()
        try {


            val result = when (loadType) {

                LoadType.REFRESH -> {


                    val id = eventRemoteKeyDao.max()

                    if (id == null) {
                        val response = service.getEventLatest(token, state.config.pageSize)
                        Log.d(
                            "service.getEventLatest",
                            "Response: ${response.code()} - ${response.message()}"
                        )
                        response
                    } else {
                        val response = service.getEvenAfter(token, id, state.config.pageSize)
                        Log.d(
                            "service.getEvenAfter",
                            "Response: ${response.code()} - ${response.message()}"
                        )
                        response
                    }
                }


                LoadType.APPEND -> {

                    val id = eventRemoteKeyDao.min() ?: return MediatorResult.Success(false)
                    val response = service.getEventBefore(token, id, state.config.pageSize)
                    Log.d(
                        "service.getEventBefore",
                        "Response: ${response.code()} - ${response.message()}"
                    )
                    response

                }
                else -> {
                    return MediatorResult.Success(false)
                }
            }

            if (!result.isSuccessful) {
                throw HttpException(result)
            }
            val data = result.body() ?: throw ApiError(
                result.code(), result.message()
            )
            appDb.withTransaction {

                when (loadType) {
                    LoadType.REFRESH -> {
                        // postDao.clear()

                        if (!data.isNullOrEmpty()) {

                            if (eventDao.isEmpty()) {
                                eventRemoteKeyDao.insert(
                                    listOf(
                                        EventRemoteKeyEntity(
                                            EventRemoteKeyEntity.KeyType.AFTER,
                                            data.first().id,
                                        ),
                                        EventRemoteKeyEntity(
                                            EventRemoteKeyEntity.KeyType.BEFORE,
                                            data.last().id,
                                        ),
                                    )
                                )
                            } else {
                                eventRemoteKeyDao.insert(
                                    EventRemoteKeyEntity(
                                        EventRemoteKeyEntity.KeyType.AFTER,
                                        data.first().id
                                    )
                                )
                            }
                        }

                    }

                    LoadType.APPEND -> {
                        if (!data.isNullOrEmpty()) {
                            eventRemoteKeyDao.insert(
                                listOf(

                                    EventRemoteKeyEntity(
                                        EventRemoteKeyEntity.KeyType.BEFORE,
                                        data.last().id,
                                    ),
                                )
                            )
                        }
                    }
                    else -> {

                    }
                }


                eventDao.insert(data.map(EventEntity.Companion::fromDto))
            }
            return MediatorResult.Success(
                data.isEmpty()
            )
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }
    }
}