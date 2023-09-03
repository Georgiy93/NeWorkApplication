package ru.netology.neworkapplication.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.neworkapplication.dto.Job

@Entity
data class JobEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    val name: String,
    val position: String,
    val start: String,
    val finish: String?,
    val link: String?,


    ) {
    fun toDto() = Job(
        id,
        name,
        position,
        start,
        finish,
        link,
    )

    companion object {
        fun fromDto(dto: Job) =
            JobEntity(
                dto.id,
                dto.name,
                dto.position,
                dto.start,
                dto.finish,
                dto.link,
            )

    }
}

fun List<Job>.toEntity(): List<JobEntity> = map(JobEntity::fromDto)
