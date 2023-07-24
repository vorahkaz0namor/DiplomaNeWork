package ru.sign.conditional.diplomanework.repository

import kotlinx.coroutines.flow.Flow
import ru.sign.conditional.diplomanework.dto.Job

interface JobRepository {
    suspend fun getMyJobs(): Flow<List<Job>>
    suspend fun saveJob(job: Job)
    suspend fun removeJobById(job: Job)
}