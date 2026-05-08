package ru.oborg.courses.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.oborg.courses.core.UiState
import ru.oborg.courses.domain.model.Enrollment
import ru.oborg.courses.domain.usecase.ChangePasswordUseCase
import ru.oborg.courses.domain.usecase.GetMyEnrollmentsUseCase
import ru.oborg.courses.domain.usecase.SubmitSupportRequestUseCase

class ProfileViewModel(
    private val getMyEnrollmentsUseCase: GetMyEnrollmentsUseCase,
    private val submitSupportRequestUseCase: SubmitSupportRequestUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<Enrollment>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Enrollment>>> = _state.asStateFlow()
    private val _formState = MutableStateFlow(ProfileFormState())
    val formState: StateFlow<ProfileFormState> = _formState.asStateFlow()

    init {
        loadEnrollments()
    }

    fun loadEnrollments() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            getMyEnrollmentsUseCase().fold(
                onSuccess = { enrollments -> _state.value = UiState.Content(enrollments) },
                onFailure = { error ->
                    _state.value = UiState.Error(error.message ?: "Не удалось загрузить записи")
                }
            )
        }
    }

    fun onSupportMessageChanged(value: String) {
        _formState.update { it.copy(supportMessage = value.take(1000), supportStatus = null) }
    }

    fun submitSupportRequest() {
        val message = formState.value.supportMessage.trim()
        if (formState.value.isSubmittingSupport) return
        if (message.length < 10) {
            _formState.update { it.copy(supportStatus = "Напишите вопрос или предложение чуть подробнее") }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isSubmittingSupport = true, supportStatus = null) }
            submitSupportRequestUseCase(message).fold(
                onSuccess = {
                    _formState.update {
                        it.copy(
                            isSubmittingSupport = false,
                            supportMessage = "",
                            supportStatus = "Обращение отправлено"
                        )
                    }
                },
                onFailure = { error ->
                    _formState.update {
                        it.copy(
                            isSubmittingSupport = false,
                            supportStatus = error.message ?: "Не удалось отправить обращение"
                        )
                    }
                }
            )
        }
    }

    fun onCurrentPasswordChanged(value: String) {
        _formState.update { it.copy(currentPassword = value, passwordStatus = null) }
    }

    fun onNewPasswordChanged(value: String) {
        _formState.update { it.copy(newPassword = value, passwordStatus = null) }
    }

    fun onRepeatPasswordChanged(value: String) {
        _formState.update { it.copy(repeatPassword = value, passwordStatus = null) }
    }

    fun changePassword() {
        val current = formState.value.currentPassword
        val new = formState.value.newPassword
        val repeat = formState.value.repeatPassword
        if (formState.value.isChangingPassword) return
        if (current.isBlank() || new.length < 6) {
            _formState.update { it.copy(passwordStatus = "Новый пароль должен быть не короче 6 символов") }
            return
        }
        if (new != repeat) {
            _formState.update { it.copy(passwordStatus = "Новые пароли не совпадают") }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isChangingPassword = true, passwordStatus = null) }
            changePasswordUseCase(currentPassword = current, newPassword = new).fold(
                onSuccess = {
                    _formState.update {
                        it.copy(
                            isChangingPassword = false,
                            currentPassword = "",
                            newPassword = "",
                            repeatPassword = "",
                            passwordStatus = "Пароль изменен"
                        )
                    }
                },
                onFailure = { error ->
                    _formState.update {
                        it.copy(
                            isChangingPassword = false,
                            passwordStatus = error.message ?: "Не удалось изменить пароль"
                        )
                    }
                }
            )
        }
    }
}

data class ProfileFormState(
    val supportMessage: String = "",
    val supportStatus: String? = null,
    val isSubmittingSupport: Boolean = false,
    val currentPassword: String = "",
    val newPassword: String = "",
    val repeatPassword: String = "",
    val passwordStatus: String? = null,
    val isChangingPassword: Boolean = false
)
