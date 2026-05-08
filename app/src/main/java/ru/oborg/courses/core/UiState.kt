package ru.oborg.courses.core

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Content<T>(val value: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

