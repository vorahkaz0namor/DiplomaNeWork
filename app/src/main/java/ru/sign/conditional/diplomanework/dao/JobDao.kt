package ru.sign.conditional.diplomanework.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.sign.conditional.diplomanework.entity.JobEntity

@Dao
interface JobDao {
    // READ functions

    @Query("SELECT MAX(id) FROM JobEntity")
    suspend fun getLatestJobId(): Int?

    @Query("SELECT * FROM JobEntity ORDER BY start DESC")
    fun getMyJobs(): Flow<List<JobEntity>>

    // CREATE & UPDATE functions

    @Insert
    suspend fun insertJob(job: JobEntity)

    @Insert(onConflict = REPLACE)
    suspend fun updateJobsByIdFromServer(jobs: List<JobEntity>)

    suspend fun saveJob(job: JobEntity) =
        if (job.id == 0) {
            insertJob(job)
            getLatestJobId()!!
        } else {
            updateJobsByIdFromServer(listOf(job))
            job.id
        }

    // DELETE functions

    @Query("DELETE FROM JobEntity WHERE id = :id")
    suspend fun removeById(id: Int)
}