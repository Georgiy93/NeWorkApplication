package ru.netology.neworkapplication.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.neworkapplication.dto.Job

import ru.netology.neworkapplication.dto.Post

@Entity
data class JobEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,

    val name: String,
    val position: String,
    val start: String,
    val finish: String,


    ) {
    fun toDto() = Job(
        id,
        name,
        position,
        start,
        finish,

        )

    companion object {
        fun fromDto(dto: Job) =
            JobEntity(
                dto.id,
                dto.name,
                dto.position,
                dto.start,
                dto.finish,

                )

    }
}

fun List<JobEntity>.toDto(): List<Job> = map(JobEntity::toDto)
fun List<Job>.toEntity(): List<JobEntity> = map(JobEntity::fromDto)
