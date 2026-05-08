package ru.oborg.courses.server.domain.model

data class Enrollment(
    val id: Int,
    val course: Course,
    val createdAt: String
)

