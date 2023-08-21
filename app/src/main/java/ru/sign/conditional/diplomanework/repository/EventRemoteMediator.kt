package ru.sign.conditional.diplomanework.repository

import androidx.paging.*
import androidx.paging.LoadType.*
import androidx.room.withTransaction
import retrofit2.HttpException
import ru.sign.conditional.diplomanework.api.EventApiService
import ru.sign.conditional.diplomanework.dao.*
import ru.sign.conditional.diplomanework.db.AppDb
import ru.sign.conditional.diplomanework.dto.Event
import ru.sign.conditional.diplomanework.entity.*

@OptIn(ExperimentalPagingApi::class)
class EventRemoteMediator(
    private val appDb: AppDb,
    private val eventApiService: EventApiService,
    private val eventDao: EventDao,
    private val eventRemoteKeyDao: EventRemoteKeyDao,
    private val auxDao: AuxDao
) : RemoteMediator<Int, EventEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, EventEntity>
    ): MediatorResult {
        try {
            val latestIdInLocal = eventRemoteKeyDao.max()
            val earliestIdInLocal = eventRemoteKeyDao.min()
            val response = when (loadType) {
                REFRESH -> {
                    eventApiService.getLatestEvents(state.config.pageSize)
                }
                PREPEND -> {
                    latestIdInLocal ?: return MediatorResult.Success(false)
                    eventApiService.getEventsAfter(
                        latestIdInLocal,
                        state.config.pageSize
                    )
                }
                APPEND -> {
                    earliestIdInLocal ?: return MediatorResult.Success(false)
                    eventApiService.getEventsBefore(
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
                                eventRemoteKeyDao.saveRemoteKeys(
                                    listOf(
                                        EventRemoteKeyEntity(
                                            RemoteKeyType.AFTER,
                                            firstId
                                        ),
                                        EventRemoteKeyEntity(
                                            RemoteKeyType.BEFORE,
                                            lastId
                                        )
                                    )
                                )
                            }
                            PREPEND -> {
                                eventRemoteKeyDao.saveRemoteKeys(
                                    listOf(
                                        EventRemoteKeyEntity(
                                            RemoteKeyType.AFTER,
                                            firstId
                                        )
                                    )
                                )
                            }
                            APPEND -> {
                                eventRemoteKeyDao.saveRemoteKeys(
                                    listOf(
                                        EventRemoteKeyEntity(
                                            RemoteKeyType.BEFORE,
                                            lastId
                                        )
                                    )
                                )
                            }
                        }
                        updateEventsByIdFormServer(body)
                    }
                return MediatorResult.Success(endOfPaginationReached = body.isEmpty())
            } else
                throw HttpException(response)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }

    private suspend fun updateEventsByIdFormServer(events: List<Event>) {
        var loadedEvents: List<EventEntity> = emptyList()
        val allExistingEvents = eventDao.getAll()
        events.map { singleEvent ->
            val existingEvent =
                allExistingEvents.find { it.idFromServer == singleEvent.id }
            loadedEvents = loadedEvents.plus(EventEntity.fromDto(
                if (existingEvent != null)
                    singleEvent.copy(
                        id = existingEvent.id,
                        idFromServer = existingEvent.idFromServer
                    )
                else
                    singleEvent.copy(
                        id = 0,
                        idFromServer = singleEvent.id
                    )
            ))
            auxDao.saveUserPreview(
                UserPreviewEntity.fromDto(singleEvent.users)
            )
        }
        eventDao.updateEventsByIdFromServer(loadedEvents)
    }
}