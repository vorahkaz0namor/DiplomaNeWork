package ru.sign.conditional.diplomanework.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import ru.sign.conditional.diplomanework.api.AuxApiService
import ru.sign.conditional.diplomanework.api.EventApiService
import ru.sign.conditional.diplomanework.dao.AuxDao
import ru.sign.conditional.diplomanework.dao.EventDao
import ru.sign.conditional.diplomanework.dao.EventRemoteKeyDao
import ru.sign.conditional.diplomanework.dto.*
import ru.sign.conditional.diplomanework.entity.DraftCopyEntity
import ru.sign.conditional.diplomanework.entity.EventEntity
import ru.sign.conditional.diplomanework.model.MediaModel
import ru.sign.conditional.diplomanework.util.AndroidUtils.defaultDispatcher
import ru.sign.conditional.diplomanework.util.NeWorkHelper.customLog
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao,
    private val eventRemoteKeyDao: EventRemoteKeyDao,
    private val auxDao: AuxDao,
    private val eventApiService: EventApiService,
    private val auxApiService: AuxApiService,
    private val eventPager: Pager<Int, EventEntity>
): EventRepository {
    override val data: Flow<PagingData<FeedItem>>
        get() = eventPager.flow
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

    override suspend fun getLatestEventId(): Int =
        eventRemoteKeyDao.max() ?: 0

    override suspend fun getEventById(id: Int): Flow<Event?> =
        eventDao.getEventById(id)
            .let {
                flow {
                    emit(it?.toDto()?.copy(users = getUsers(it.usersIds)))
                }
                    .flowOn(defaultDispatcher)
            }
                .flowOn(defaultDispatcher)

    override suspend fun saveEvent(event: Event, media: MediaModel?) {
        try {
            val uploadedMedia =
                if (media != null)
                    Attachment(
                        url = uploadMedia(media).url,
                        type = AttachmentType.IMAGE
                    )
                else
                    event.attachment
            val eventWithMedia = event.copy(
                attachment = uploadedMedia
            )
            val localSavedEventId =
                eventDao.saveEvent(EventEntity.fromDto(eventWithMedia))
            val eventResponse = eventApiService.saveEvent(
                eventWithMedia.copy(
                    id = event.idFromServer
                )
            )
            if (eventResponse.isSuccessful) {
                val savedEvent = eventResponse.body()
                    ?: throw HttpException(eventResponse)
                eventDao.saveEvent(EventEntity.fromDto(
                    savedEvent.copy(
                        id = localSavedEventId,
                        idFromServer = savedEvent.id
                    )
                ))
            } else
                throw HttpException(eventResponse)
        } catch (e: Exception) {
            customLog("SAVING EVENT BY REPO", e)
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

    override suspend fun editParticipantsInEventById(event: Event, ownerId: Int) {
        val newEvent =
            if (event.participatedByMe) {
                event.copy(
                    participantsIds = event.participantsIds.minus(ownerId),
                    participatedByMe = false,
                    users = event.users.minus(ownerId.toString())
                )
            } else {
                event.copy(
                    participantsIds = event.participantsIds.plus(ownerId),
                    participatedByMe = true,
                    users = event.users.plus(getUsers(listOf(ownerId.toString())))
                )
            }
        eventDao.saveEvent(EventEntity.fromDto(newEvent))
        val eventResponse = eventApiService.let {
            if (event.participatedByMe)
                it.removeParticipant(event.idFromServer)
            else
                it.addParticipant(event.idFromServer)
        }
        if (eventResponse.isSuccessful) {
            val loadedEvent = eventResponse.body()
                ?: throw HttpException(eventResponse)
            eventDao.saveEvent(EventEntity.fromDto(
                loadedEvent.copy(
                    id = event.id,
                    idFromServer = loadedEvent.id
                )
            ))
        } else
            throw HttpException(eventResponse)
    }

    override suspend fun removeEventById(event: Event) {
        eventDao.removeById(event.id)
        if (event.idFromServer != 0)
            eventApiService.removeEventById(event.idFromServer)
                .also { response ->
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