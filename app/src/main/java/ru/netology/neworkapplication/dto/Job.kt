package ru.netology.neworkapplication.dto

sealed interface FeedItemJob {
    val id: Long
}

data class Job(
    override val id: Long,
    val name: String,
    val position: String,
    val start: String,
    val finish: String? = null,
    val link: String? = null,
) : FeedItemJob
