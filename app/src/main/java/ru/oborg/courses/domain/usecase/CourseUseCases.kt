package ru.oborg.courses.domain.usecase

import ru.oborg.courses.domain.model.Course
import ru.oborg.courses.domain.model.Enrollment
import ru.oborg.courses.domain.model.CourseReview
import ru.oborg.courses.domain.repository.CourseRepository

class GetCoursesUseCase(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(): Result<List<Course>> = repository.getCourses()
}

class GetCourseUseCase(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(id: Int): Result<Course> = repository.getCourse(id)
}

class EnrollCourseUseCase(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(courseId: Int): Result<Enrollment> = repository.enroll(courseId)
}

class GetMyEnrollmentsUseCase(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(): Result<List<Enrollment>> = repository.getMyEnrollments()
}

class GetCourseReviewsUseCase(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(courseId: Int): Result<List<CourseReview>> = repository.getCourseReviews(courseId)
}

class CreateCourseReviewUseCase(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(courseId: Int, rating: Int, text: String): Result<CourseReview> {
        return repository.createCourseReview(courseId = courseId, rating = rating, text = text)
    }
}
