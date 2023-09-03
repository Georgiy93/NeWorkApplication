package ru.netology.neworkapplication.repository.events

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
interface EventRepositoryModule {
    @Singleton
    @Binds
    fun bindsEventRepository(impl: EventRepositoryImpl): EventRepository
}