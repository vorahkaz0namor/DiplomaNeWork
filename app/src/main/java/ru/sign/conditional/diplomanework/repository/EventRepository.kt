package ru.sign.conditional.diplomanework.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.sign.conditional.diplomanework.dto.DraftCopy
import ru.sign.conditional.diplomanework.dto.FeedItem
import ru.sign.conditional.diplomanework.dto.Event
import ru.sign.conditional.diplomanework.model.MediaModel

interface EventRepository {
    val data: Flow<PagingData<FeedItem>>
    suspend fun getLatestEventId(): Int
    suspend fun getEventById(id: Int): Flow<Event?>
    suspend fun saveEvent(event: Event, media: MediaModel?)
    suspend fun editParticipantsInEventById(event: Event, ownerId: Int)
    suspend fun removeEventById(event: Event)
    suspend fun getDraftCopy(): DraftCopy
    suspend fun saveDraftCopy(draftCopy: DraftCopy)
}