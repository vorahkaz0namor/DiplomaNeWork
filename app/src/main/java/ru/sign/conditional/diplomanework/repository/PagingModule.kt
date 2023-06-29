package ru.sign.conditional.diplomanework.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.*
import ru.sign.conditional.diplomanework.api.EventApiService
import ru.sign.conditional.diplomanework.api.PostApiService
import ru.sign.conditional.diplomanework.dao.*
import ru.sign.conditional.diplomanework.db.AppDb
import ru.sign.conditional.diplomanework.entity.EventEntity
import ru.sign.conditional.diplomanework.entity.PostEntity
import ru.sign.conditional.diplomanework.util.AndroidUtils.defaultDispatcher
import javax.inject.Singleton

@OptIn(ExperimentalPagingApi::class)
@InstallIn(SingletonComponent::class)
@Module
class PagingModule {
    @Singleton
    @Provides
    fun providePostRemoteMediator(
        appDb: AppDb,
        postApiService: PostApiService,
        postDao: PostDao,
        postRemoteKeyDao: PostRemoteKeyDao,
        auxDao: AuxDao
    ) : PostRemoteMediator = PostRemoteMediator(
        appDb = appDb,
        postApiService = postApiService,
        postDao = postDao,
        postRemoteKeyDao = postRemoteKeyDao,
        auxDao = auxDao
    )

    @Singleton
    @Provides
    fun provideEventRemoteMediator(
        appDb: AppDb,
        eventApiService: EventApiService,
        eventDao: EventDao,
        eventRemoteKeyDao: EventRemoteKeyDao,
        auxDao: AuxDao
    ) : EventRemoteMediator = EventRemoteMediator(
        appDb = appDb,
        eventApiService = eventApiService,
        eventDao = eventDao,
        eventRemoteKeyDao = eventRemoteKeyDao,
        auxDao = auxDao
    )

    @Singleton
    @Provides
    fun providePostPager(
        postDao: PostDao,
        postRemoteKeyDao: PostRemoteKeyDao,
        postRemoteMediator: PostRemoteMediator
    ) : Pager<Int, PostEntity> = Pager(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            postDao.getPage(
                lastId = postRemoteKeyDao.before() ?: 0,
                firstId = postRemoteKeyDao.after() ?: 0
            )
        },
        remoteMediator = postRemoteMediator
    )

    @Singleton
    @Provides
    fun provideEventPager(
        eventDao: EventDao,
        eventRemoteKeyDao: EventRemoteKeyDao,
        eventRemoteMediator: EventRemoteMediator
    ) : Pager<Int, EventEntity> = Pager(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            eventDao.getPage(
                    lastId = eventRemoteKeyDao.before() ?: 0,
                    firstId = eventRemoteKeyDao.after() ?: 0
                )
        },
        remoteMediator = eventRemoteMediator
    )
}