package ru.sign.conditional.diplomanework.repository

import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import ru.sign.conditional.diplomanework.api.JobApiService
import ru.sign.conditional.diplomanework.dao.JobDao
import ru.sign.conditional.diplomanework.dto.Job
import ru.sign.conditional.diplomanework.entity.JobEntity
import ru.sign.conditional.diplomanework.util.AndroidUtils.defaultDispatcher
import ru.sign.conditional.diplomanework.util.NeWorkHelper.customLog
import javax.inject.Inject

class JobRepositoryImpl @Inject constructor(
    private val jobDao: JobDao,
    private val jobApiService: JobApiService
): JobRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getMyJobs(): Flow<List<Job>> =
        jobDao.getMyJobs().mapLatest {
            val jobsFromDao = it.map(JobEntity::toDto)
            try {
                val jobResponse = jobApiService.getMyJobs()
                if (jobResponse.isSuccessful) {
                    val jobsFromServer = jobResponse.body()
                        ?: throw HttpException(jobResponse)
                    val unloadedJobs =
                        if (jobsFromDao.isEmpty() && jobsFromServer.isNotEmpty()) {
                            jobsFromServer
                        } else {
                            val jobsIdsFromDao = jobsFromDao.map { it.idFromServer }
                            jobsFromServer.filterNot { job ->
                                jobsIdsFromDao.contains(job.id)
                            }
                        }
                    if (unloadedJobs.isNotEmpty()) {
                        jobDao.updateJobsByIdFromServer(
                            unloadedJobs.map {
                                JobEntity.fromDto(
                                    it.copy(
                                        id = 0,
                                        idFromServer = it.id
                                    )
                                )
                            }
                        )
                    }
                } else
                    throw HttpException(jobResponse)
            } catch (e: Exception) {
                customLog("LOADING JOBS", e)
            }
            jobsFromDao
        }
            .flowOn(defaultDispatcher)

    override suspend fun saveJob(job: Job) {
        try {
            val localSavedJobId =
                jobDao.saveJob(JobEntity.fromDto(job))
            val jobResponse = jobApiService.saveMyJob(
                job.copy(id = job.idFromServer)
            )
            if (jobResponse.isSuccessful) {
                val savedJob = jobResponse.body()
                    ?: throw HttpException(jobResponse)
                jobDao.saveJob(
                    JobEntity.fromDto(
                        savedJob.copy(
                            id = localSavedJobId,
                            idFromServer = savedJob.id
                        )
                    )
                )
            } else
                throw HttpException(jobResponse)
        } catch (e: Exception) {
            customLog("ADDING JOB BY REPO", e)
        }
    }

    override suspend fun removeJobById(job: Job) {
        if (job.idFromServer != 0) {
            jobApiService.removeJobById(job.idFromServer)
                .also { response ->
                    if (!response.isSuccessful)
                        throw  HttpException(response)
                }
        }
        jobDao.removeById(job.id)
    }
}