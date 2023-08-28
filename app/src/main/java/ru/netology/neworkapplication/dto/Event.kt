package ru.netology.neworkapplication.dto

enum class EventType {
    OFFLINE, ONLINE
}

sealed interface FeedItemEvent {
    val id: Long
}

data class Event(
    override val id: Long,
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
    val attachment: Attachment? = null,
    val link: String?,
    val ownedByMe: Boolean = false,
    val users: Map<Long, UserPreview> = emptyMap()
) : FeedItemEvent

