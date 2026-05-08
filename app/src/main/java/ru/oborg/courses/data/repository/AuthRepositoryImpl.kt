package ru.oborg.courses.data.repository

import kotlinx.coroutines.flow.Flow
import ru.oborg.courses.data.local.AuthTokenStorage
import ru.oborg.courses.data.remote.CourseApi
import ru.oborg.courses.data.remote.dto.ChangePasswordRequestDto
import ru.oborg.courses.data.remote.dto.LoginRequestDto
import ru.oborg.courses.data.remote.dto.RegisterRequestDto
import ru.oborg.courses.data.remote.dto.SupportRequestDto
import ru.oborg.courses.data.remote.isUnauthorizedResponse
import ru.oborg.courses.data.remote.dto.toDomain
import ru.oborg.courses.data.remote.toUserMessage
import ru.oborg.courses.domain.model.AuthSession
import ru.oborg.courses.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val api: CourseApi,
    private val tokenStorage: AuthTokenStorage
) : AuthRepository {
    override val authState: Flow<AuthSession?> = tokenStorage.authState

    override suspend fun login(email: String, password: String): Result<AuthSession> {
        return runCatching {
            api.login(LoginRequestDto(email = email, password = password)).toDomain()
        }.fold(
            onSuccess = { session ->
                tokenStorage.saveSession(session)
                Result.success(session)
            },
            onFailure = { error ->
                Result.failure(IllegalStateException(error.toUserMessage()))
            }
        )
    }

    override suspend fun register(fullName: String, email: String, password: String): Result<AuthSession> {
        return runCatching {
            api.register(RegisterRequestDto(fullName = fullName, email = email, password = password)).toDomain()
        }.fold(
            onSuccess = { session ->
                tokenStorage.saveSession(session)
                Result.success(session)
            },
            onFailure = { error ->
                Result.failure(IllegalStateException(error.toUserMessage()))
            }
        )
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return safeAuthorizedCall {
            api.changePassword(
                ChangePasswordRequestDto(
                    currentPassword = currentPassword,
                    newPassword = newPassword
                )
            )
        }
    }

    override suspend fun submitSupportRequest(message: String): Result<Unit> {
        return safeAuthorizedCall {
            api.submitSupportRequest(SupportRequestDto(message = message))
        }
    }

    override suspend fun logout() {
        tokenStorage.clear()
    }

    private suspend fun safeAuthorizedCall(block: suspend () -> Any): Result<Unit> {
        return runCatching { block() }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { error ->
                if (error.isUnauthorizedResponse()) {
                    tokenStorage.clear()
                    Result.failure(IllegalStateException("Сессия истекла. Войдите заново"))
                } else {
                    Result.failure(IllegalStateException(error.toUserMessage()))
                }
            }
        )
    }
}
