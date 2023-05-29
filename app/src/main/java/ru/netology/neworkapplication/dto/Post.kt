package ru.netology.neworkapplication.dto

import ru.netology.neworkapplication.enumeration.AttachmentType

sealed interface FeedItem {
    val id: Int
}

data class Post(
    override val id: Int,
    val authorId: Int,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String? = "",
    val content: String = "",
    val published: String,
    val likedByMe: Boolean,

    val attachment: Attachment? = null,
    val ownedByMe: Boolean = false,
    val mentionedMe: Boolean = false,
) : FeedItem


data class Attachment(
    val url: String,
    val type: AttachmentType?,
)
