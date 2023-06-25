package ru.netology.neworkapplication.dto

import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

sealed interface FeedItemJob {
    val id: Int
}

data class Job(
    override val id: Int,
    val name: String,
    val position: String,
    val start: String,
    val finish: String? = null,
    val link: String? = null,
) : FeedItemJob
