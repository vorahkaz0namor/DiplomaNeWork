package ru.sign.conditional.diplomanework.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import ru.sign.conditional.diplomanework.dto.UserPreview
import ru.sign.conditional.diplomanework.model.AuthModel

interface UserApiService {
    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun login(
        @Field("login") login: String,
        @Field("password") password: String
    ) : Response<AuthModel>

    @Multipart
    @POST("users/registration")
    suspend fun register(
        @Part("login") login: RequestBody,
        @Part("password") password: RequestBody,
        @Part("name") name: RequestBody,
        @Part media: MultipartBody.Part?
    ) : Response<AuthModel>

    @GET("users/{user_id}")
    suspend fun getUserById(
        @Path("user_id") userId: Int
    ) : Response<UserPreview>
}