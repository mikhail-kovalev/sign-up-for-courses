package ru.oborg.courses.domain.model

data class Enrollment(
    val id: Int,
    val course: Course,
    val createdAt: String
)

