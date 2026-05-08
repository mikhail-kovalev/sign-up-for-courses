package ru.oborg.courses.domain.model

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
    val seatsBusy: Int,
    val seatsLeft: Int
)

