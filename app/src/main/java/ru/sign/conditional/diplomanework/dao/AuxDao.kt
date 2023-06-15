package ru.sign.conditional.diplomanework.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import ru.sign.conditional.diplomanework.entity.DraftCopyEntity
import ru.sign.conditional.diplomanework.entity.UserPreviewEntity

@Dao
interface AuxDao {
    // READ functions

    @Query("SELECT * FROM UserPreviewEntity WHERE userId = :userId")
    suspend fun getUserPreview(userId: String): UserPreviewEntity

    @Query("SELECT * FROM DraftCopyEntity")
    suspend fun getDraftCopy(): DraftCopyEntity

    // CREATE & UPDATE functions

    @Insert(onConflict = REPLACE)
    suspend fun saveUserPreview(userPreviews: List<UserPreviewEntity>)

    @Insert(onConflict = REPLACE)
    suspend fun saveDraftCopy(draftCopy: DraftCopyEntity)

    // DELETE functions

    @Query("DELETE FROM DraftCopyEntity")
    suspend fun clearDraftCopy()
}