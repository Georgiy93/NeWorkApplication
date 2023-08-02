package ru.netology.neworkapplication.dto

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.neworkapplication.entity.EventRemoteKeyEntity

@Dao
interface EventRemoteKeyDao {
    @Query("SELECT max(`key`) FROM EventRemoteKeyEntity")
    suspend fun max(): Long?

    @Query("SELECT min(`key`) FROM EventRemoteKeyEntity")
    suspend fun min(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(EventRemoteKeyEntity: EventRemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(EventRemoteKeyEntity: List<EventRemoteKeyEntity>)

    @Query("DELETE FROM EventRemoteKeyEntity")
    suspend fun clear()
}