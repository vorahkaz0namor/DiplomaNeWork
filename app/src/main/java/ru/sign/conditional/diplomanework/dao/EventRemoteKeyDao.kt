package ru.sign.conditional.diplomanework.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import ru.sign.conditional.diplomanework.entity.EventRemoteKeyEntity

@Dao
interface EventRemoteKeyDao {
    @Query("SELECT max(key) FROM EventRemoteKeyEntity")
    fun after(): Int?

    @Query("SELECT min(key) FROM EventRemoteKeyEntity")
    fun before(): Int?

    @Query("SELECT max(key) FROM EventRemoteKeyEntity")
    suspend fun max(): Int?

    @Query("SELECT min(key) FROM EventRemoteKeyEntity")
    suspend fun min(): Int?

    @Insert(onConflict = REPLACE)
    suspend fun saveRemoteKeys(eventRemoteKeyEntities: List<EventRemoteKeyEntity>)

    @Query("DELETE FROM EventRemoteKeyEntity")
    suspend fun clearRemoteKey()
}