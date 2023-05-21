package ru.netology.neworkapplication.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.netology.neworkapplication.dao.PostDao
import ru.netology.neworkapplication.dto.PostRemoteKeyDao
import ru.netology.neworkapplication.entity.PostEntity
import ru.netology.neworkapplication.entity.PostRemoteKeyEntity


@Database(
    entities = [PostEntity::class, PostRemoteKeyEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao

}