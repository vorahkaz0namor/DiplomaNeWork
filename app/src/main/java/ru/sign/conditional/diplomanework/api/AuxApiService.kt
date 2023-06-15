package ru.sign.conditional.diplomanework.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import ru.sign.conditional.diplomanework.dto.Media

interface AuxApiService {
    @Multipart
    @POST("media")
    suspend fun uploadMedia(
        @Part file: MultipartBody.Part
    ): Response<Media>
}