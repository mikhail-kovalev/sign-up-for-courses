package ru.oborg.courses.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.oborg.courses.domain.model.AuthSession
import ru.oborg.courses.domain.usecase.LoginUseCase
import ru.oborg.courses.domain.usecase.LogoutUseCase
import ru.oborg.courses.domain.usecase.ObserveAuthStateUseCase
import ru.oborg.courses.domain.usecase.RegisterUseCase

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeAuthStateUseCase().collect { session ->
                _state.update { current ->
                    current.copy(
                        session = session,
                        isAuthenticated = session != null
                    )
                }
            }
        }
    }

    fun onFullNameChanged(value: String) {
        _state.update { it.copy(fullName = value, errorMessage = null) }
    }

    fun onEmailChanged(value: String) {
        _state.update { it.copy(email = value, errorMessage = null) }
    }

    fun onPasswordChanged(value: String) {
        _state.update { it.copy(password = value, errorMessage = null) }
    }

    fun toggleMode() {
        _state.update {
            it.copy(
                isRegisterMode = !it.isRegisterMode,
                errorMessage = null
            )
        }
    }

    fun submit() {
        val snapshot = state.value
        if (!snapshot.isFormValid) {
            _state.update { it.copy(errorMessage = "Заполните поля, пароль не короче 6 символов") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = if (snapshot.isRegisterMode) {
                registerUseCase(
                    fullName = snapshot.fullName.trim(),
                    email = snapshot.email.trim(),
                    password = snapshot.password
                )
            } else {
                loginUseCase(
                    email = snapshot.email.trim(),
                    password = snapshot.password
                )
            }

            result.fold(
                onSuccess = {
                    _state.update { current ->
                        current.copy(
                            isLoading = false,
                            password = "",
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Не удалось выполнить вход"
                        )
                    }
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _state.value = AuthUiState()
        }
    }
}

data class AuthUiState(
    val session: AuthSession? = null,
    val fullName: String = "",
    val email: String = "student@oborg.ru",
    val password: String = "student123",
    val isRegisterMode: Boolean = false,
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val errorMessage: String? = null
) {
    val isFormValid: Boolean
        get() {
            val hasCredentials = email.isNotBlank() && password.length >= 6
            return if (isRegisterMode) hasCredentials && fullName.isNotBlank() else hasCredentials
        }
}

