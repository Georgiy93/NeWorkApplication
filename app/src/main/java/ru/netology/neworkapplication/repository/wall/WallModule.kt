package ru.netology.neworkapplication.repository.wall

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface WallModule {
    @Singleton
    @Binds
    fun bindsWallRepository(impl: WallRepositoryImpl): WallRepository
}