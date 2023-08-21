package ru.sign.conditional.diplomanework.api

import retrofit2.Response
import retrofit2.http.*
import ru.sign.conditional.diplomanework.dto.Event

interface EventApiService {
    @GET("events/latest")
    suspend fun getLatestEvents(
        @Query("count") count: Int
    ): Response<List<Event>>

    @GET("events/{event_id}/before")
    suspend fun getEventsBefore(
        @Path("event_id") eventId: Int,
        @Query("count") count: Int
    ): Response<List<Event>>

    @GET("events/{event_id}/after")
    suspend fun getEventsAfter(
        @Path("event_id") eventId: Int,
        @Query("count") count: Int
    ): Response<List<Event>>

    @POST("events")
    suspend fun saveEvent(
        @Body data: Event
    ): Response<Event>

    @DELETE("events/{event_id}")
    suspend fun removeEventById(
        @Path("event_id") eventId: Int
    ): Response<Unit>

    @POST("events/{event_id}/participants")
    suspend fun addParticipant(
        @Path("event_id") eventId: Int
    ): Response<Event>

    @DELETE("events/{event_id}/participants")
    suspend fun removeParticipant(
        @Path("event_id") eventId: Int
    ): Response<Event>
}
