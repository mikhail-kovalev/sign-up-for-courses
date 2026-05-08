package ru.oborg.courses.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.oborg.courses.domain.model.AuthSession
import ru.oborg.courses.domain.repository.AuthRepository

class LoginUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthSession> {
        return repository.login(email = email, password = password)
    }
}

class RegisterUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(fullName: String, email: String, password: String): Result<AuthSession> {
        return repository.register(fullName = fullName, email = email, password = password)
    }
}

class ObserveAuthStateUseCase(
    private val repository: AuthRepository
) {
    operator fun invoke(): Flow<AuthSession?> = repository.authState
}

class LogoutUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() {
        repository.logout()
    }
}

class ChangePasswordUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(currentPassword: String, newPassword: String): Result<Unit> {
        return repository.changePassword(currentPassword = currentPassword, newPassword = newPassword)
    }
}

class SubmitSupportRequestUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(message: String): Result<Unit> {
        return repository.submitSupportRequest(message = message)
    }
}
