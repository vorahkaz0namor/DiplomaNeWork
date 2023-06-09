package ru.sign.conditional.diplomanework.dao

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.sign.conditional.diplomanework.db.AppDb
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DaoModule {
    @Singleton
    @Provides
    fun providePostDao(
        appDb: AppDb
    ) : PostDao = appDb.postDao()

    @Singleton
    @Provides
    fun providePostRemoteKeyDao(
        appDb: AppDb
    ) : PostRemoteKeyDao = appDb.postRemoteKeyDao()

    @Singleton
    @Provides
    fun provideAuxDao(
        appDb: AppDb
    ) : AuxDao = appDb.auxDao()
}