package ru.netology.neworkapplication.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.netology.neworkapplication.dao.EventDao
import ru.netology.neworkapplication.dao.JobDao
import ru.netology.neworkapplication.dao.PostDao
import ru.netology.neworkapplication.dto.*
import ru.netology.neworkapplication.entity.*


@Database(
    entities = [PostEntity::class, JobEntity::class, EventEntity::class, JobRemoteKeyEntity::class,
        PostRemoteKeyEntity::class, EventRemoteKeyEntity::class],
    version = 25, // Increment the version number here
    exportSchema = false
)
@TypeConverters(TypeEventConverters::class, UserConvecters::class, ListConverter::class)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
    abstract fun jobDao(): JobDao
    abstract fun jobRemoteKeyDao(): JobRemoteKeyDao
    abstract fun eventDao(): EventDao
    abstract fun eventRemoteKeyDao(): EventRemoteKeyDao

}