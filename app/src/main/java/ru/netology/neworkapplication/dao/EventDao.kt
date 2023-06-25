package ru.netology.neworkapplication.dao

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.netology.neworkapplication.entity.EventEntity
import ru.netology.neworkapplication.entity.PostEntity

@Dao
interface EventDao {
    @Query("SELECT * FROM EventEntity WHERE id = :id") // Update table name to PostEntity
    suspend fun getEvent(id: Int): EventEntity?

    @Update
    suspend fun update(event: EventEntity)

    @Query("SELECT * FROM EventEntity ORDER BY id DESC")
    fun getAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM EventEntity ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int, EventEntity>

    @Query("SELECT COUNT(*) == 0 FROM EventEntity")
    suspend fun isEmpty(): Boolean

    @Query("SELECT COUNT(*) FROM EventEntity")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(events: List<EventEntity>)

    @Query("DELETE FROM EventEntity WHERE id = :id")
    suspend fun removeById(id: Int)

    @Query("DELETE FROM EventEntity")
    suspend fun clear()

}