package ru.netology.neworkapplication.dto

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.selects.select
import ru.netology.neworkapplication.entity.JobRemoteKeyEntity
import ru.netology.neworkapplication.entity.PostRemoteKeyEntity


@Dao
interface JobRemoteKeyDao {
    @Query("SELECT max(`key`) FROM JobRemoteKeyEntity")
    suspend fun max(): Long?

    @Query("SELECT min(`key`) FROM JobRemoteKeyEntity")
    suspend fun min(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(jobRemoteKeyEntity: JobRemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(jobRemoteKeyEntity: List<JobRemoteKeyEntity>)

    @Query("DELETE FROM JobRemoteKeyEntity")
    suspend fun clear()
}