package ru.sign.conditional.diplomanework.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.sign.conditional.diplomanework.api.PostApiService
import ru.sign.conditional.diplomanework.dao.AuxDao
import ru.sign.conditional.diplomanework.dao.PostDao
import ru.sign.conditional.diplomanework.dao.PostRemoteKeyDao
import ru.sign.conditional.diplomanework.db.AppDb
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
    fun providePostPager(
        postDao: PostDao,
        postRemoteKeyDao: PostRemoteKeyDao,
        postRemoteMediator: PostRemoteMediator
    ) = Pager(
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
}