package ru.sign.conditional.diplomanework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.sign.conditional.diplomanework.dto.Job

@Entity
data class JobEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val idFromServer: Int,
    val name: String,
    val position: String,
    val start: String,
    val finish: String?,
    val link: String?
) {
    fun toDto() = Job(
        id = id,
        idFromServer = idFromServer,
        name = name,
        position = position,
        start = start,
        finish = finish,
        link = link
    )

    companion object {
        fun fromDto(dtoJob: Job) =
            JobEntity(
                id = dtoJob.id,
                idFromServer = dtoJob.idFromServer,
                name = dtoJob.name,
                position = dtoJob.position,
                start = dtoJob.start,
                finish = dtoJob.finish,
                link = dtoJob.link
            )
    }
}
