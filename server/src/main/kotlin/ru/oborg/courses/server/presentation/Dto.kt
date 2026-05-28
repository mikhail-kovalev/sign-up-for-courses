package ru.oborg.courses.server.presentation

import kotlinx.serialization.Serializable
import ru.oborg.courses.server.domain.model.AuthUser
import ru.oborg.courses.server.domain.model.Course
import ru.oborg.courses.server.domain.model.Enrollment
import ru.oborg.courses.server.domain.model.CourseReview

@Serializable
data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

@Serializable
data class SupportRequest(
    val message: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserResponse,
    val expiresInMinutes: Int
)

@Serializable
data class UserResponse(
    val id: Int,
    val fullName: String,
    val email: String,
    val role: String
)

@Serializable
data class CourseResponse(
    val id: Int,
    val title: String,
    val description: String,
    val teacher: String,
    val durationHours: Int,
    val priceRub: Int,
    val level: String,
    val startsAt: String,
    val seatsTotal: Int,
    val seatsBusy: Int,
    val seatsLeft: Int,
    val averageRating: Double?,
    val reviewCount: Int
)

@Serializable
data class EnrollmentResponse(
    val id: Int,
    val course: CourseResponse,
    val createdAt: String
)

@Serializable
data class CreateReviewRequest(
    val rating: Int,
    val text: String
)

@Serializable
data class CourseReviewResponse(
    val id: Int,
    val courseId: Int,
    val userId: Int,
    val authorName: String,
    val rating: Int,
    val text: String,
    val createdAt: String
)

@Serializable
data class ErrorResponse(
    val message: String
)

@Serializable
data class MessageResponse(
    val message: String
)

fun AuthUser.toResponse(): UserResponse {
    return UserResponse(
        id = id,
        fullName = fullName,
        email = email,
        role = role
    )
}

fun Course.toResponse(): CourseResponse {
    return CourseResponse(
        id = id,
        title = title,
        description = description,
        teacher = teacher,
        durationHours = durationHours,
        priceRub = priceRub,
        level = level,
        startsAt = startsAt,
        seatsTotal = seatsTotal,
        seatsBusy = seatsBusy,
        seatsLeft = seatsLeft,
        averageRating = averageRating,
        reviewCount = reviewCount
    )
}

fun Enrollment.toResponse(): EnrollmentResponse {
    return EnrollmentResponse(
        id = id,
        course = course.toResponse(),
        createdAt = createdAt
    )
}

fun CourseReview.toResponse(): CourseReviewResponse {
    return CourseReviewResponse(
        id = id,
        courseId = courseId,
        userId = userId,
        authorName = authorName,
        rating = rating,
        text = text,
        createdAt = createdAt
    )
}
