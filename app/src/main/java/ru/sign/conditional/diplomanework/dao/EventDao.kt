package ru.sign.conditional.diplomanework.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.sign.conditional.diplomanework.entity.EventEntity

@Dao
interface EventDao {
    // READ functions

    @Query("SELECT MAX(id) FROM EventEntity")
    suspend fun getLatestEventId(): Int?

    @Query("SELECT * FROM EventEntity ORDER BY idFromServer DESC")
    suspend fun getAll(): List<EventEntity>

    @Query("SELECT * FROM EventEntity WHERE idFromServer BETWEEN :lastId AND :firstId ORDER BY idFromServer DESC")
    fun getPage(lastId: Int, firstId: Int): PagingSource<Int, EventEntity>

    @Query("SELECT * FROM EventEntity WHERE id = :id")
    fun getEventById(id: Int): Flow<EventEntity?>

    // CREATE & UPDATE functions

    @Insert
    suspend fun insertEvent(event: EventEntity)

    @Insert(onConflict = REPLACE)
    suspend fun updateEventsByIdFromServer(events: List<EventEntity>)

    suspend fun saveEvent(event: EventEntity) =
        if (event.id == 0) {
            insertEvent(event)
            getLatestEventId()!!
        } else {
            updateEventsByIdFromServer(listOf(event))
            event.id
        }

    // DELETE functions

    @Query("DELETE FROM EventEntity WHERE id = :id")
    suspend fun removeById(id: Int)
}