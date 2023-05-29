package ru.netology.neworkapplication.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.netology.neworkapplication.dao.JobDao
import ru.netology.neworkapplication.dao.PostDao
import ru.netology.neworkapplication.dto.JobRemoteKeyDao
import ru.netology.neworkapplication.dto.PostRemoteKeyDao
import ru.netology.neworkapplication.entity.JobEntity
import ru.netology.neworkapplication.entity.JobRemoteKeyEntity

import ru.netology.neworkapplication.entity.PostEntity
import ru.netology.neworkapplication.entity.PostRemoteKeyEntity


@Database(
    entities = [PostEntity::class, JobEntity::class, JobRemoteKeyEntity::class, PostRemoteKeyEntity::class],
    version = 3, // Increment the version number here
    exportSchema = false
)

abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
    abstract fun jobDao(): JobDao
    abstract fun jobRemoteKeyDao(): JobRemoteKeyDao
}