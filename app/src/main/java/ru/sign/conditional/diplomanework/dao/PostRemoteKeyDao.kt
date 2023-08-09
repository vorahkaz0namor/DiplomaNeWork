package ru.sign.conditional.diplomanework.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import ru.sign.conditional.diplomanework.entity.PostRemoteKeyEntity

@Dao
interface PostRemoteKeyDao {
    @Query("SELECT max(key) FROM PostRemoteKeyEntity")
    fun after(): Int?

    @Query("SELECT min(key) FROM PostRemoteKeyEntity")
    fun before(): Int?

    suspend fun max() = after()

    suspend fun min() = before()

    @Insert(onConflict = REPLACE)
    suspend fun saveRemoteKeys(postRemoteKeyEntities: List<PostRemoteKeyEntity>)
}