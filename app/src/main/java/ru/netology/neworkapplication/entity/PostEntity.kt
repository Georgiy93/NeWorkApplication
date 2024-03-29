package ru.netology.neworkapplication.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.neworkapplication.dto.Attachment

import ru.netology.neworkapplication.dto.Post


@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val published: String,

    val likedByMe: Boolean,

    val ownedByMe: Boolean,
    val mentionedMe: Boolean,

    @Embedded
    var attachment: Attachment?,

    ) {
    fun toDto() = Post(
        id,
        authorId,
        author,
        authorAvatar,
        authorJob,
        content,
        published,
        likedByMe,

        attachment,
        ownedByMe,
        mentionedMe,

        )

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                dto.id,
                dto.authorId,
                dto.author,
                dto.authorAvatar,
                dto.authorJob,
                dto.content,
                dto.published,
                dto.likedByMe,

                dto.ownedByMe,
                dto.mentionedMe,

                dto.attachment,

                )

    }
}

fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)
