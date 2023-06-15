package ru.sign.conditional.diplomanework.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.sign.conditional.diplomanework.entity.PostEntity

@Dao
interface PostDao {
    // READ functions

    @Query("SELECT MAX(id) FROM PostEntity")
    suspend fun getLatestPostId(): Int?

    @Query("SELECT * FROM PostEntity ORDER BY idFromServer DESC")
    suspend fun getAll(): List<PostEntity>

    @Query("SELECT * FROM PostEntity WHERE idFromServer BETWEEN :lastId AND :firstId ORDER BY idFromServer DESC")
    fun getPage(lastId: Int, firstId: Int): PagingSource<Int, PostEntity>

    @Query("SELECT * FROM PostEntity WHERE id = :id")
    fun getPostById(id: Int): Flow<PostEntity?>

    // CREATE & UPDATE functions

    @Insert
    suspend fun insertPost(post: PostEntity)

    @Insert(onConflict = REPLACE)
    suspend fun updatePostsByIdFromServer(post: List<PostEntity>)

    suspend fun savePost(post: PostEntity) =
        if (post.id == 0) {
            insertPost(post)
            getLatestPostId()!!
        } else {
            updatePostsByIdFromServer(listOf(post))
            post.id
        }

    // DELETE functions

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Int)
}