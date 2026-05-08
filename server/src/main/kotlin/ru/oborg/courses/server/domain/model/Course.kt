package ru.oborg.courses.server.domain.model

import kotlin.math.max

data class Course(
    val id: Int,
    val title: String,
    val description: String,
    val teacher: String,
    val durationHours: Int,
    val priceRub: Int,
    val level: String,
    val startsAt: String,
    val seatsTotal: Int,
    val seatsBusy: Int
) {
    val seatsLeft: Int
        get() = max(0, seatsTotal - seatsBusy)
}

