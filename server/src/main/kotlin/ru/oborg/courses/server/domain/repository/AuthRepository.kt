package ru.oborg.courses.server.domain.repository

import ru.oborg.courses.server.domain.model.AuthUser

interface AuthRepository {
    fun createUser(fullName: String, email: String, passwordHash: String, role: String = "student"): AuthUser?
    fun findByEmail(email: String): AuthUser?
    fun findById(id: Int): AuthUser?
    fun updatePasswordHash(userId: Int, passwordHash: String): Boolean
    fun createSupportRequest(userId: Int, message: String): Boolean
}
