package ru.sign.conditional.diplomanework.repository

import androidx.paging.*
import androidx.paging.LoadType.*
import androidx.room.withTransaction
import retrofit2.HttpException
import ru.sign.conditional.diplomanework.api.PostApiService
import ru.sign.conditional.diplomanework.dao.AuxDao
import ru.sign.conditional.diplomanework.dao.PostDao
import ru.sign.conditional.diplomanework.dao.PostRemoteKeyDao
import ru.sign.conditional.diplomanework.db.AppDb
import ru.sign.conditional.diplomanework.dto.Post
import ru.sign.conditional.diplomanework.entity.PostEntity
import ru.sign.conditional.diplomanework.entity.PostRemoteKeyEntity
import ru.sign.conditional.diplomanework.entity.UserPreviewEntity

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val appDb: AppDb,
    private val postApiService: PostApiService,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val auxDao: AuxDao
) : RemoteMediator<Int, PostEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val latestIdInLocal = postRemoteKeyDao.max()
            val earliestIdInLocal = postRemoteKeyDao.min()
            val response = when (loadType) {
                REFRESH -> {
                    postApiService.getLatestPosts(state.config.pageSize)
                }
                PREPEND -> {
                    latestIdInLocal ?: return MediatorResult.Success(false)
                    postApiService.getPostsAfter(
                        latestIdInLocal,
                        state.config.pageSize
                    )
                }
                APPEND -> {
                    earliestIdInLocal ?: return MediatorResult.Success(false)
                    postApiService.getPostsBefore(
                        earliestIdInLocal,
                        state.config.pageSize
                    )
                }
            }
            if (response.isSuccessful) {
                val body = response.body()
                    ?: throw HttpException(response)
                if (body.isNotEmpty())
                    appDb.withTransaction {
                        val firstId = body.first().id
                        val lastId = body.last().id
                        when (loadType) {
                            REFRESH -> {
                                postRemoteKeyDao.saveRemoteKeys(
                                    listOf(
                                        PostRemoteKeyEntity(
                                            PostRemoteKeyEntity.KeyType.AFTER,
                                            firstId
                                        ),
                                        PostRemoteKeyEntity(
                                            PostRemoteKeyEntity.KeyType.BEFORE,
                                            lastId
                                        )
                                    )
                                )
                            }
                            PREPEND -> {
                                postRemoteKeyDao.saveRemoteKeys(
                                    listOf(
                                        PostRemoteKeyEntity(
                                            PostRemoteKeyEntity.KeyType.AFTER,
                                            firstId
                                        )
                                    )
                                )
                            }
                            APPEND -> {
                                postRemoteKeyDao.saveRemoteKeys(
                                    listOf(
                                        PostRemoteKeyEntity(
                                            PostRemoteKeyEntity.KeyType.BEFORE,
                                            lastId
                                        )
                                    )
                                )
                            }
                        }
                        updatePostsByIdFormServer(body)
                    }
                return MediatorResult.Success(endOfPaginationReached = body.isEmpty())
            } else
                throw HttpException(response)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }

    private suspend fun updatePostsByIdFormServer(posts: List<Post>) {
        var loadedPosts: List<PostEntity> = emptyList()
        val allExistingPosts = postDao.getAll()
        posts.map { singlePost ->
            val existingPost =
                allExistingPosts.find { it.idFromServer == singlePost.id }
            loadedPosts = loadedPosts.plus(PostEntity.fromDto(
                if (existingPost != null)
                    singlePost.copy(
                        id = existingPost.id,
                        idFromServer = existingPost.idFromServer
                    )
                else
                    singlePost.copy(
                        id = 0,
                        idFromServer = singlePost.id
                    )
            ))
            auxDao.saveUserPreview(
                UserPreviewEntity.fromDto(singlePost.users)
            )
        }
        postDao.updatePostsByIdFromServer(loadedPosts)
    }
}