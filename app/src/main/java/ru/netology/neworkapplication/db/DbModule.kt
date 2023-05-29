package ru.netology.neworkapplication.db

import ru.netology.neworkapplication.dao.PostDao
import ru.netology.neworkapplication.dto.PostRemoteKeyDao


import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.netology.neworkapplication.dao.JobDao
import ru.netology.neworkapplication.dto.JobRemoteKeyDao

import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DbModule {
    @Singleton
    @Provides
    fun provideDb(@ApplicationContext context: Context): AppDb =
        Room.databaseBuilder(context, AppDb::class.java, "app.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun providePostDao(appDb: AppDb): PostDao = appDb.postDao()

    @Provides
    fun providePostRemoteKeyDao(db: AppDb): PostRemoteKeyDao = db.postRemoteKeyDao()

    @Provides
    fun provideJobDao(appDb: AppDb): JobDao = appDb.jobDao()

    @Provides
    fun provideJobRemoteKeyDao(db: AppDb): JobRemoteKeyDao = db.jobRemoteKeyDao()
}