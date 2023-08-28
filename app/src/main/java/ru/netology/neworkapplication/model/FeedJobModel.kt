package ru.netology.neworkapplication.model

import ru.netology.neworkapplication.dto.Job


class FeedJobModel(
    val jobs: List<Job> = emptyList(),
    val empty: Boolean = false,
)
