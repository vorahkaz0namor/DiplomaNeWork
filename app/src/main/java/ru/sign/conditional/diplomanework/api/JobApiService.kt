package ru.sign.conditional.diplomanework.api

import retrofit2.Response
import retrofit2.http.*
import ru.sign.conditional.diplomanework.dto.Job

interface JobApiService {
    @GET("my/jobs")
    suspend fun getMyJobs(): Response<List<Job>>

    @POST("my/jobs")
    suspend fun saveMyJob(
        @Body data: Job
    ): Response<Job>

    @DELETE("my/jobs/{job_id}")
    suspend fun removeJobById(
        @Path("job_id") jobId: Int
    ): Response<Unit>
}