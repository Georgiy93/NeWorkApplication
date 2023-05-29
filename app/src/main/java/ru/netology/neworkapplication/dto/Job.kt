package ru.netology.neworkapplication.dto

sealed interface FeedItemJob {
    val id: Int
}

data class Job(
    override val id: Int,

    val name: String,
    val position: String,
    val start: String,
    val finish: String,


    ) : FeedItemJob