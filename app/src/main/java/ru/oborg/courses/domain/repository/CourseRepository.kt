package ru.oborg.courses.domain.repository

import ru.oborg.courses.domain.model.Course
import ru.oborg.courses.domain.model.Enrollment
import ru.oborg.courses.domain.model.CourseReview

interface CourseRepository {
    suspend fun getCourses(): Result<List<Course>>
    suspend fun getCourse(id: Int): Result<Course>
    suspend fun enroll(courseId: Int): Result<Enrollment>
    suspend fun getMyEnrollments(): Result<List<Enrollment>>
    suspend fun getCourseReviews(courseId: Int): Result<List<CourseReview>>
    suspend fun createCourseReview(courseId: Int, rating: Int, text: String): Result<CourseReview>
}
