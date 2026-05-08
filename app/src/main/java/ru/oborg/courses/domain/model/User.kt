package ru.oborg.courses.domain.model

data class User(
    val id: Int,
    val fullName: String,
    val email: String,
    val role: String
)

