package ru.oborg.courses.data.repository

import ru.oborg.courses.data.local.AuthTokenStorage
import ru.oborg.courses.data.remote.CourseApi
import ru.oborg.courses.data.remote.dto.CreateReviewRequestDto
import ru.oborg.courses.data.remote.dto.toDomain
import ru.oborg.courses.data.remote.isUnauthorizedResponse
import ru.oborg.courses.data.remote.toUserMessage
import ru.oborg.courses.domain.model.Course
import ru.oborg.courses.domain.model.Enrollment
import ru.oborg.courses.domain.model.CourseReview
import ru.oborg.courses.domain.repository.CourseRepository

class CourseRepositoryImpl(
    private val api: CourseApi,
    private val tokenStorage: AuthTokenStorage
) : CourseRepository {
    override suspend fun getCourses(): Result<List<Course>> {
        return safeCall { api.getCourses().map { course -> course.toDomain() } }
    }

    override suspend fun getCourse(id: Int): Result<Course> {
        return safeCall { api.getCourse(id).toDomain() }
    }

    override suspend fun enroll(courseId: Int): Result<Enrollment> {
        return safeCall { api.enroll(courseId).toDomain() }
    }

    override suspend fun getMyEnrollments(): Result<List<Enrollment>> {
        return safeCall { api.getMyEnrollments().map { enrollment -> enrollment.toDomain() } }
    }

    override suspend fun getCourseReviews(courseId: Int): Result<List<CourseReview>> {
        return safeCall { api.getCourseReviews(courseId).map { review -> review.toDomain() } }
    }

    override suspend fun createCourseReview(courseId: Int, rating: Int, text: String): Result<CourseReview> {
        return safeCall {
            api.createCourseReview(
                courseId = courseId,
                request = CreateReviewRequestDto(rating = rating, text = text)
            ).toDomain()
        }
    }

    private suspend fun <T> safeCall(block: suspend () -> T): Result<T> {
        return runCatching { block() }.fold(
            onSuccess = { Result.success(it) },
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
