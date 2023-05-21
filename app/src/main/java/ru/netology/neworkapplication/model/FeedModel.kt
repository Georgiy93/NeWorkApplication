package ru.netology.neworkapplication.model

import ru.netology.neworkapplication.dto.Post


data class FeedModel(
    val posts: List<Post> = emptyList(),
    val empty: Boolean = false,
)
