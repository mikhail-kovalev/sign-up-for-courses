package ru.oborg.courses.server.domain.model

data class AuthUser(
    val id: Int,
    val fullName: String,
    val email: String,
    val passwordHash: String,
    val role: String
)

