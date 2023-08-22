package ru.netology.neworkapplication.dto

enum class EventType {
    OFFLINE, ONLINE
}

sealed interface FeedItemEvent {
    val id: Int
}

data class Event(
    override val id: Int,
    val authorId: Int,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val datetime: String,
    val published: String,

    val type: String,
    val likeOwnerIds: List<Int>? = emptyList(),
    val likedByMe: Boolean,
    val speakerIds: List<Int>? = emptyList(),
    val participantsIds: List<Int>? = emptyList(),
    val participatedByMe: Boolean,
    val attachment: Attachment? = null,
    val link: String?,
    val ownedByMe: Boolean = false,
    val users: Map<Int, UserPreview> = emptyMap()
) : FeedItemEvent

