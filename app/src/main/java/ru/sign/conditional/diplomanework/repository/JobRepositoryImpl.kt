package ru.sign.conditional.diplomanework.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import ru.sign.conditional.diplomanework.api.JobApiService
import ru.sign.conditional.diplomanework.dao.JobDao
import ru.sign.conditional.diplomanework.dto.Job
import ru.sign.conditional.diplomanework.entity.JobEntity
import ru.sign.conditional.diplomanework.util.AndroidUtils.defaultDispatcher
import javax.inject.Inject

class JobRepositoryImpl @Inject constructor(
    private val jobDao: JobDao,
    private val jobApiService: JobApiService
): JobRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getMyJobs(): Flow<List<Job>> =
        jobDao.getMyJobs().mapLatest {
            val jobsFromDao = it.map(JobEntity::toDto)
            val jobResponse = jobApiService.getMyJobs()
            if (jobResponse.isSuccessful) {
                val jobsFromServer = jobResponse.body()
                    ?: throw HttpException(jobResponse)
                if (jobsFromDao.isEmpty() && jobsFromServer.isNotEmpty()) {
                    jobDao.updateJobsByIdFromServer(
                        jobsFromServer.map(JobEntity.Companion::fromDto)
                    )
                } else {
                    jobsFromServer.filterNot { job ->
                        jobsFromDao.contains(job)
                    }.also { filteredJobs ->
                        if (filteredJobs.isNotEmpty()) {
                            jobDao.updateJobsByIdFromServer(
                                filteredJobs.map(JobEntity.Companion::fromDto)
                            )
                        }
                    }
                }
                jobsFromDao
            } else
                throw HttpException(jobResponse)
        }
            .flowOn(defaultDispatcher)

    override suspend fun saveJob(job: Job) {
        val localSavedJobId =
            jobDao.saveJob(JobEntity.fromDto(job))
        val jobResponse = jobApiService.saveMyJob(
            job.copy(id = job.idFromServer)
        )
        if (jobResponse.isSuccessful) {
            val savedJob = jobResponse.body()
                ?: throw HttpException(jobResponse)
            jobDao.saveJob(JobEntity.fromDto(
                savedJob.copy(
                    id = localSavedJobId,
                    idFromServer = savedJob.id
                )
            ))
        } else
            throw HttpException(jobResponse)
    }

    override suspend fun removeJobById(job: Job) {
        jobDao.removeById(job.id)
        if (job.idFromServer != 0) {
            jobApiService.removeJobById(job.idFromServer)
                .also { response ->
                    if (!response.isSuccessful)
                        throw  HttpException(response)
                }
        }
    }
}