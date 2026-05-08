package ru.oborg.courses.server.domain.repository

import ru.oborg.courses.server.domain.model.Course
import ru.oborg.courses.server.domain.model.Enrollment
import ru.oborg.courses.server.domain.model.CourseReview

interface CourseRepository {
    fun findAll(): List<Course>
    fun findById(id: Int): Course?
    fun findEnrollmentsByUser(userId: Int): List<Enrollment>
    fun enroll(userId: Int, courseId: Int): EnrollCourseResult
    fun findReviewsByCourse(courseId: Int): List<CourseReview>
    fun createReview(userId: Int, courseId: Int, rating: Int, text: String): CreateReviewResult
}

sealed interface EnrollCourseResult {
    data class Enrolled(val enrollment: Enrollment) : EnrollCourseResult
    data object AlreadyEnrolled : EnrollCourseResult
    data object CourseNotFound : EnrollCourseResult
    data object NoSeats : EnrollCourseResult
}

sealed interface CreateReviewResult {
    data class Created(val review: CourseReview) : CreateReviewResult
    data object CourseNotFound : CreateReviewResult
    data object NotEnrolled : CreateReviewResult
    data object AlreadyReviewed : CreateReviewResult
}
