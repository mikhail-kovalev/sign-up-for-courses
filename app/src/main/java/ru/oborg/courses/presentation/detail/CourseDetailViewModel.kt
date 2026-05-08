package ru.oborg.courses.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.oborg.courses.core.UiState
import ru.oborg.courses.domain.model.Course
import ru.oborg.courses.domain.model.CourseReview
import ru.oborg.courses.domain.usecase.CreateCourseReviewUseCase
import ru.oborg.courses.domain.usecase.EnrollCourseUseCase
import ru.oborg.courses.domain.usecase.GetCourseUseCase
import ru.oborg.courses.domain.usecase.GetCourseReviewsUseCase
import ru.oborg.courses.domain.usecase.GetMyEnrollmentsUseCase

class CourseDetailViewModel(
    private val courseId: Int,
    private val getCourseUseCase: GetCourseUseCase,
    private val enrollCourseUseCase: EnrollCourseUseCase,
    private val getMyEnrollmentsUseCase: GetMyEnrollmentsUseCase,
    private val getCourseReviewsUseCase: GetCourseReviewsUseCase,
    private val createCourseReviewUseCase: CreateCourseReviewUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(CourseDetailUiState())
    val state: StateFlow<CourseDetailUiState> = _state.asStateFlow()

    init {
        loadCourse()
        loadReviews()
        loadEnrollmentStatus()
    }

    fun loadCourse() {
        viewModelScope.launch {
            _state.update { it.copy(courseState = UiState.Loading, message = null) }
            getCourseUseCase(courseId).fold(
                onSuccess = { course ->
                    _state.update { it.copy(courseState = UiState.Content(course)) }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(courseState = UiState.Error(error.message ?: "Не удалось открыть курс"))
                    }
                }
            )
        }
    }

    fun enroll() {
        if (state.value.isEnrolling) return
        viewModelScope.launch {
            _state.update { it.copy(isEnrolling = true, message = null) }
            enrollCourseUseCase(courseId).fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isEnrolling = false,
                            isEnrolled = true,
                            message = "Запись оформлена"
                        )
                    }
                    loadCourse()
                },
                onFailure = { error ->
                    val message = error.message ?: "Не удалось записаться"
                    val alreadyEnrolled = message.contains("уже", ignoreCase = true) ||
                        message.contains("already", ignoreCase = true)
                    _state.update {
                        it.copy(
                            isEnrolling = false,
                            isEnrolled = it.isEnrolled || alreadyEnrolled,
                            message = if (alreadyEnrolled) "Вы уже зарегистрированы на этот курс" else message
                        )
                    }
                }
            )
        }
    }

    fun onReviewTextChanged(value: String) {
        _state.update { it.copy(reviewText = value.take(500), reviewMessage = null) }
    }

    fun onReviewRatingChanged(value: Int) {
        _state.update { it.copy(reviewRating = value.coerceIn(1, 5), reviewMessage = null) }
    }

    fun submitReview() {
        val currentState = state.value
        val text = currentState.reviewText.trim()
        if (currentState.isSubmittingReview) return
        if (!currentState.isEnrolled) {
            _state.update { it.copy(reviewMessage = "Оставить отзыв можно после записи на курс") }
            return
        }
        if (text.length < 12) {
            _state.update { it.copy(reviewMessage = "Напишите отзыв чуть подробнее") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSubmittingReview = true, reviewMessage = null) }
            createCourseReviewUseCase(
                courseId = courseId,
                rating = currentState.reviewRating,
                text = text
            ).fold(
                onSuccess = { review ->
                    val reviews = (state.value.reviewsState as? UiState.Content)?.value.orEmpty()
                    _state.update {
                        it.copy(
                            isSubmittingReview = false,
                            reviewsState = UiState.Content(listOf(review) + reviews.filterNot { item -> item.userId == review.userId }),
                            reviewText = "",
                            reviewMessage = "Отзыв опубликован"
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isSubmittingReview = false,
                            reviewMessage = error.message ?: "Не удалось отправить отзыв"
                        )
                    }
                }
            )
        }
    }

    private fun loadEnrollmentStatus() {
        viewModelScope.launch {
            getMyEnrollmentsUseCase().onSuccess { enrollments ->
                val isAlreadyEnrolled = enrollments.any { enrollment -> enrollment.course.id == courseId }
                if (isAlreadyEnrolled) {
                    _state.update { it.copy(isEnrolled = true, message = null) }
                }
            }
        }
    }

    private fun loadReviews() {
        viewModelScope.launch {
            _state.update { it.copy(reviewsState = UiState.Loading) }
            getCourseReviewsUseCase(courseId).fold(
                onSuccess = { reviews ->
                    _state.update { it.copy(reviewsState = UiState.Content(reviews)) }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(reviewsState = UiState.Error(error.message ?: "Не удалось загрузить отзывы"))
                    }
                }
            )
        }
    }
}

data class CourseDetailUiState(
    val courseState: UiState<Course> = UiState.Loading,
    val reviewsState: UiState<List<CourseReview>> = UiState.Loading,
    val isEnrolling: Boolean = false,
    val isEnrolled: Boolean = false,
    val message: String? = null,
    val reviewText: String = "",
    val reviewRating: Int = 5,
    val isSubmittingReview: Boolean = false,
    val reviewMessage: String? = null
)
