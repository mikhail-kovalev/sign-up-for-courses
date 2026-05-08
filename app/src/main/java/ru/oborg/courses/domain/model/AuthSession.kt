package ru.oborg.courses.domain.model

data class AuthSession(
    val token: String,
    val user: User,
    val expiresInMinutes: Int
)

