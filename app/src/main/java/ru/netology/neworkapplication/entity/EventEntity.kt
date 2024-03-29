package ru.netology.neworkapplication.entity

import androidx.room.*
import ru.netology.neworkapplication.dto.*

@Entity

data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val datetime: String,
    val published: String,

    val type: String,


    val likeOwnerIds: List<Long>? = emptyList(),
    val likedByMe: Boolean,


    val speakerIds: List<Long>? = emptyList(),


    val participantsIds: List<Long>? = emptyList(),

    val participatedByMe: Boolean,

    val link: String?,
    val ownedByMe: Boolean,
    val users: Map<Long, UserPreview> = emptyMap(),

//    @Embedded
//    var users: UserPreview?,
    @Embedded(prefix = "attachment_")
    var attachment: Attachment?,

    ) {

    fun toDto() = Event(
        id,
        authorId,
        author,
        authorAvatar,
        authorJob,
        content,
        datetime,
        published,
        type,
        likeOwnerIds,
        likedByMe,
        speakerIds,
        participantsIds,
        participatedByMe,
        attachment,
        link,
        ownedByMe,
        users
    )

    companion object {
        fun fromDto(dto: Event) =
            EventEntity(
                dto.id,
                dto.authorId,
                dto.author,
                dto.authorAvatar,
                dto.authorJob,
                dto.content,
                dto.datetime,
                dto.published,
                dto.type,
                dto.likeOwnerIds,
                dto.likedByMe,
                dto.speakerIds,
                dto.participantsIds,
                dto.participatedByMe,

                dto.link,
                dto.ownedByMe,
                dto.users,
                dto.attachment,

                )
    }


}


fun List<Event>.toEntity(): List<EventEntity> = map(EventEntity::fromDto)
