package ru.oborg.courses.server.application

import ru.oborg.courses.server.domain.model.AuthUser
import ru.oborg.courses.server.domain.model.Course
import ru.oborg.courses.server.domain.model.Enrollment
import ru.oborg.courses.server.domain.model.CourseReview
import ru.oborg.courses.server.domain.repository.AuthRepository
import ru.oborg.courses.server.domain.repository.CourseRepository
import ru.oborg.courses.server.domain.repository.CreateReviewResult
import ru.oborg.courses.server.domain.repository.EnrollCourseResult
import ru.oborg.courses.server.security.PasswordHasher

class RegisterUseCase(
    private val repository: AuthRepository
) {
    operator fun invoke(fullName: String, email: String, password: String): AuthUser? {
        return repository.createUser(
            fullName = fullName,
            email = email.lowercase(),
            passwordHash = PasswordHasher.hash(password)
        )
    }
}

class LoginUseCase(
    private val repository: AuthRepository
) {
    operator fun invoke(email: String, password: String): AuthUser? {
        val user = repository.findByEmail(email.lowercase()) ?: return null
        return if (PasswordHasher.verify(password, user.passwordHash)) user else null
    }
}

class GetCurrentUserUseCase(
    private val repository: AuthRepository
) {
    operator fun invoke(userId: Int): AuthUser? = repository.findById(userId)
}

class ChangePasswordUseCase(
    private val repository: AuthRepository
) {
    operator fun invoke(userId: Int, currentPassword: String, newPassword: String): ChangePasswordResult {
        val user = repository.findById(userId) ?: return ChangePasswordResult.UserNotFound
        if (!PasswordHasher.verify(currentPassword, user.passwordHash)) {
            return ChangePasswordResult.InvalidCurrentPassword
        }
        val updated = repository.updatePasswordHash(
            userId = userId,
            passwordHash = PasswordHasher.hash(newPassword)
        )
        return if (updated) ChangePasswordResult.Changed else ChangePasswordResult.UserNotFound
    }
}

class SubmitSupportRequestUseCase(
    private val repository: AuthRepository
) {
    operator fun invoke(userId: Int, message: String): Boolean {
        return repository.createSupportRequest(userId = userId, message = message)
    }
}

sealed interface ChangePasswordResult {
    data object Changed : ChangePasswordResult
    data object InvalidCurrentPassword : ChangePasswordResult
    data object UserNotFound : ChangePasswordResult
}

class GetCoursesUseCase(
    private val repository: CourseRepository
) {
    operator fun invoke(): List<Course> = repository.findAll()
}

class GetCourseUseCase(
    private val repository: CourseRepository
) {
    operator fun invoke(courseId: Int): Course? = repository.findById(courseId)
}

class EnrollCourseUseCase(
    private val repository: CourseRepository
) {
    operator fun invoke(userId: Int, courseId: Int): EnrollCourseResult {
        return repository.enroll(userId = userId, courseId = courseId)
    }
}

class GetMyEnrollmentsUseCase(
    private val repository: CourseRepository
) {
    operator fun invoke(userId: Int): List<Enrollment> = repository.findEnrollmentsByUser(userId)
}

class GetCourseReviewsUseCase(
    private val repository: CourseRepository
) {
    operator fun invoke(courseId: Int): List<CourseReview> = repository.findReviewsByCourse(courseId)
}

class CreateCourseReviewUseCase(
    private val repository: CourseRepository
) {
    operator fun invoke(userId: Int, courseId: Int, rating: Int, text: String): CreateReviewResult {
        return repository.createReview(
            userId = userId,
            courseId = courseId,
            rating = rating,
            text = text
        )
    }
}
