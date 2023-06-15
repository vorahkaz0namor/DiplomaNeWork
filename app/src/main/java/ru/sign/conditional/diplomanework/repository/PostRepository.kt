package ru.sign.conditional.diplomanework.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.sign.conditional.diplomanework.dto.DraftCopy
import ru.sign.conditional.diplomanework.dto.FeedItem
import ru.sign.conditional.diplomanework.dto.Post
import ru.sign.conditional.diplomanework.model.MediaModel

interface PostRepository {
    val data: Flow<PagingData<FeedItem>>
    suspend fun getLatestPostId(): Int
    suspend fun getPostById(id: Int): Flow<Post?>
    suspend fun savePost(post: Post, media: MediaModel?)
    suspend fun likePostById(post: Post, ownerId: Int)
    suspend fun removePostById(post: Post)
    suspend fun getDraftCopy(): DraftCopy
    suspend fun saveDraftCopy(draftCopy: DraftCopy)
}