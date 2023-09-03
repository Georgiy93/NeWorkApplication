package ru.netology.neworkapplication.dao

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.netology.neworkapplication.entity.JobEntity

@Dao
interface JobDao {
    @Query("SELECT * FROM JobEntity WHERE id = :id")
    suspend fun getJob(id: Long): JobEntity?

    @Update
    suspend fun update(job: JobEntity)

    @Query("SELECT * FROM JobEntity ORDER BY id DESC")
    fun getJobAll(): Flow<List<JobEntity>>

    @Query("SELECT * FROM JobEntity ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int, JobEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(job: JobEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(jobs: List<JobEntity>)

    @Query("DELETE FROM JobEntity WHERE id = :id")
    suspend fun removeJobById(id: Long)

    @Query("DELETE FROM JobEntity")
    suspend fun clear()
}