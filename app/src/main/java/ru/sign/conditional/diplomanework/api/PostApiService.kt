package ru.sign.conditional.diplomanework.api

import retrofit2.Response
import retrofit2.http.*
import ru.sign.conditional.diplomanework.dto.Post

interface PostApiService {
    @GET("posts/latest")
    suspend fun getLatestPosts(
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("posts/{post_id}/before")
    suspend fun getPostsBefore(
        @Path("post_id") postId: Int,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("posts/{post_id}/after")
    suspend fun getPostsAfter(
        @Path("post_id") postId: Int,
        @Query("count") count: Int
    ): Response<List<Post>>

    @POST("posts")
    suspend fun savePost(
        @Body data: Post
    ): Response<Post>

    @POST("posts/{post_id}/likes")
    suspend fun likePostById(
        @Path("post_id") postId: Int
    ): Response<Post>

    @DELETE("posts/{post_id}/likes")
    suspend fun unlikePostById(
        @Path("post_id") postId: Int
    ): Response<Post>

    @DELETE("posts/{post_id}")
    suspend fun removePostById(
        @Path("post_id") postId: Int
    ): Response<Unit>
}