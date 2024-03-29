package ru.sign.conditional.diplomanework.repository

import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import ru.sign.conditional.diplomanework.api.AuxApiService
import ru.sign.conditional.diplomanework.api.PostApiService
import ru.sign.conditional.diplomanework.dao.AuxDao
import ru.sign.conditional.diplomanework.dao.PostDao
import ru.sign.conditional.diplomanework.dao.PostRemoteKeyDao
import ru.sign.conditional.diplomanework.dto.*
import ru.sign.conditional.diplomanework.entity.DraftCopyEntity
import ru.sign.conditional.diplomanework.entity.PostEntity
import ru.sign.conditional.diplomanework.model.MediaModel
import ru.sign.conditional.diplomanework.util.NeWorkHelper.customLog
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val auxDao: AuxDao,
    private val postApiService: PostApiService,
    private val auxApiService: AuxApiService,
    private val postPager: Pager<Int, PostEntity>
): PostRepository {
    override val data: Flow<PagingData<FeedItem>>
        get() = postPager.flow
            .map {
                it.map { entity ->
                    entity.toDto().copy(
                        users = getUsers(entity.usersIds)
                    )
                }
            }

    private suspend fun getUsers(userIds: List<String>): Map<String, UserPreview> =
        buildMap {
            userIds.map { id ->
                put(
                    id,
                    auxDao.getUserPreview(id).toDto()
                )
            }
        }

    override suspend fun getLatestPostId(): Int =
        postRemoteKeyDao.max() ?: 0

    override suspend fun savePost(post: Post, media: MediaModel?) {
        try {
            val uploadedMedia =
                if (media != null)
                    Attachment(
                        url = uploadMedia(media).url,
                        type = AttachmentType.IMAGE
                    )
                else
                    post.attachment
            val postWithMedia = post.copy(
                attachment = uploadedMedia
            )
            val localSavedPostId =
                postDao.savePost(PostEntity.fromDto(postWithMedia))
            val postResponse = postApiService.savePost(
                postWithMedia.copy(
                    id = post.idFromServer
                )
            )
            if (postResponse.isSuccessful) {
                val savedPost = postResponse.body()
                    ?: throw HttpException(postResponse)
                postDao.savePost(PostEntity.fromDto(
                    savedPost.copy(
                        id = localSavedPostId,
                        idFromServer = savedPost.id
                    )
                ))
            } else
                throw HttpException(postResponse)
        } catch (e: Exception) {
            customLog("SAVING POST BY REPO", e)
        }
    }

    private suspend fun uploadMedia(media: MediaModel): Media {
        val part = MultipartBody.Part.createFormData(
            "file",
            media.file.name,
            media.file.asRequestBody()
        )
        val mediaResponse = auxApiService.uploadMedia(part)
        if (mediaResponse.isSuccessful)
            return requireNotNull(mediaResponse.body())
        else
            throw HttpException(mediaResponse)
    }

    override suspend fun likePostById(post: Post, ownerId: Int) {
        val postToUpdate =
            if (post.likedByMe) {
                post.copy(
                    likeOwnerIds = post.likeOwnerIds.minus(ownerId),
                    likedByMe = false,
                    users = post.users.minus(ownerId.toString())
                )
            } else {
                post.copy(
                    likeOwnerIds = post.likeOwnerIds.plus(ownerId),
                    likedByMe = true,
                    users = post.users.plus(getUsers(listOf(ownerId.toString())))
                )
            }
        postDao.savePost(PostEntity.fromDto(postToUpdate))
        val postResponse = postApiService.let {
            if (post.likedByMe)
                it.unlikePostById(post.idFromServer)
            else
                it.likePostById(post.idFromServer)
        }
        if (postResponse.isSuccessful) {
            val loadedPost = postResponse.body()
                ?: throw HttpException(postResponse)
            postDao.savePost(PostEntity.fromDto(
                loadedPost.copy(
                    id = post.id,
                    idFromServer = loadedPost.id
                )
            ))
        } else
            throw HttpException(postResponse)
    }

    override suspend fun removePostById(post: Post) {
        postDao.removeById(post.id)
        if (post.idFromServer != 0)
            postApiService.removePostById(post.idFromServer).also { response ->
                if (!response.isSuccessful)
                    throw HttpException(response)
            }
    }

    override suspend fun getDraftCopy(): DraftCopy =
        auxDao.getDraftCopy().toDto()

    override suspend fun saveDraftCopy(draftCopy: DraftCopy) {
        auxDao.clearDraftCopy()
        auxDao.saveDraftCopy(DraftCopyEntity.fromDto(draftCopy))
    }
}