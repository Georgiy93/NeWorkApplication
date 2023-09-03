package ru.netology.neworkapplication.repository.wall

import androidx.paging.*
import androidx.room.withTransaction
import retrofit2.HttpException
import ru.netology.neworkapplication.db.AppDb
import ru.netology.neworkapplication.api.ApiService
import ru.netology.neworkapplication.dao.PostDao
import ru.netology.neworkapplication.dto.PostRemoteKeyDao
import ru.netology.neworkapplication.entity.PostEntity
import ru.netology.neworkapplication.entity.PostRemoteKeyEntity
import ru.netology.neworkapplication.error.ApiError



import java.io.IOException


@OptIn(ExperimentalPagingApi::class)
class WallRemoteMediator(
    private val postDao: PostDao,
    private val service: ApiService,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb,


    ) : RemoteMediator<Int, PostEntity>() {


    override suspend fun load(
        loadType: LoadType,

        state: PagingState<Int, PostEntity>
    ): MediatorResult {


        try {


            val result = when (loadType) {

                LoadType.REFRESH -> {


                    val id = postRemoteKeyDao.max()

                    if (id == null) {
                        service.getWallLatest(state.config.pageSize)
                    } else {
                        service.getWallAfter(id, state.config.pageSize)
                    }
                }


                LoadType.APPEND -> {

                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(false)
                    service.getWallBefore(id, state.config.pageSize)

                }
                else -> {
                    return MediatorResult.Success(false)
                }
            }

            if (!result.isSuccessful) {
                throw HttpException(result)
            }
            val data = result.body() ?: throw ApiError(
                result.code()
            )
            appDb.withTransaction {

                when (loadType) {
                    LoadType.REFRESH -> {


                        if (data.isNotEmpty()) {

                            if (postDao.isEmpty()) {
                                postRemoteKeyDao.insert(
                                    listOf(
                                        PostRemoteKeyEntity(
                                            PostRemoteKeyEntity.KeyType.AFTER,
                                            data.first().id,
                                        ),
                                        PostRemoteKeyEntity(
                                            PostRemoteKeyEntity.KeyType.BEFORE,
                                            data.last().id,
                                        ),
                                    )
                                )
                            } else {
                                postRemoteKeyDao.insert(
                                    PostRemoteKeyEntity(
                                        PostRemoteKeyEntity.KeyType.AFTER,
                                        data.first().id
                                    )
                                )
                            }
                        }

                    }

                    LoadType.APPEND -> {
                        if (data.isNotEmpty()) {
                            postRemoteKeyDao.insert(
                                listOf(

                                    PostRemoteKeyEntity(
                                        PostRemoteKeyEntity.KeyType.BEFORE,
                                        data.last().id,
                                    ),
                                )
                            )
                        }
                    }
                    else -> {

                    }
                }


                postDao.insert(data.map(PostEntity.Companion::fromDto))
            }
            return MediatorResult.Success(
                data.isEmpty()
            )
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }
    }
}