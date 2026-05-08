package ru.oborg.courses.presentation.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.oborg.courses.core.UiState
import ru.oborg.courses.domain.model.Course
import ru.oborg.courses.domain.usecase.GetCoursesUseCase

class CourseListViewModel(
    private val getCoursesUseCase: GetCoursesUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<Course>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Course>>> = _state.asStateFlow()

    init {
        loadCourses()
    }

    fun loadCourses() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            getCoursesUseCase().fold(
                onSuccess = { courses -> _state.value = UiState.Content(courses) },
                onFailure = { error ->
                    _state.value = UiState.Error(error.message ?: "Не удалось загрузить курсы")
                }
            )
        }
    }
}

