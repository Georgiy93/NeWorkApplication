package ru.netology.neworkapplication.dto

import ru.netology.neworkapplication.enumeration.AttachmentType

sealed interface FeedItem {
    val id: Long
}

data class Post(
    override val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val attachment: Attachment? = null,
    val ownedByMe: Boolean = false,
) : FeedItem


data class Attachment(
    val url: String,
    val type: AttachmentType?,
)
