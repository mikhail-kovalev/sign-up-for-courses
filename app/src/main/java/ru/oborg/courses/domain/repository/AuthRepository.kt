package ru.oborg.courses.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.oborg.courses.domain.model.AuthSession

interface AuthRepository {
    val authState: Flow<AuthSession?>

    suspend fun login(email: String, password: String): Result<AuthSession>
    suspend fun register(fullName: String, email: String, password: String): Result<AuthSession>
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>
    suspend fun submitSupportRequest(message: String): Result<Unit>
    suspend fun logout()
}
