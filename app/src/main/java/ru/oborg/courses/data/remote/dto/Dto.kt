package ru.oborg.courses.data.remote.dto

import kotlinx.serialization.Serializable
import ru.oborg.courses.domain.model.AuthSession
import ru.oborg.courses.domain.model.Course
import ru.oborg.courses.domain.model.Enrollment
import ru.oborg.courses.domain.model.CourseReview
import ru.oborg.courses.domain.model.User

@Serializable
data class RegisterRequestDto(
    val fullName: String,
    val email: String,
    val password: String
)

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String
)

@Serializable
data class ChangePasswordRequestDto(
    val currentPassword: String,
    val newPassword: String
)

@Serializable
data class SupportRequestDto(
    val message: String
)

@Serializable
data class AuthResponseDto(
    val token: String,
    val user: UserResponseDto,
    val expiresInMinutes: Int
)

@Serializable
data class UserResponseDto(
    val id: Int,
    val fullName: String,
    val email: String,
    val role: String
)

@Serializable
data class CourseResponseDto(
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
    val seatsLeft: Int
)

@Serializable
data class EnrollmentResponseDto(
    val id: Int,
    val course: CourseResponseDto,
    val createdAt: String
)

@Serializable
data class CreateReviewRequestDto(
    val rating: Int,
    val text: String
)

@Serializable
data class CourseReviewResponseDto(
    val id: Int,
    val courseId: Int,
    val userId: Int,
    val authorName: String,
    val rating: Int,
    val text: String,
    val createdAt: String
)

@Serializable
data class ErrorResponseDto(
    val message: String
)

@Serializable
data class MessageResponseDto(
    val message: String
)

fun AuthResponseDto.toDomain(): AuthSession {
    return AuthSession(
        token = token,
        user = user.toDomain(),
        expiresInMinutes = expiresInMinutes
    )
}

fun UserResponseDto.toDomain(): User {
    return User(
        id = id,
        fullName = fullName,
        email = email,
        role = role
    )
}

fun CourseResponseDto.toDomain(): Course {
    return Course(
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
        seatsLeft = seatsLeft
    )
}

fun EnrollmentResponseDto.toDomain(): Enrollment {
    return Enrollment(
        id = id,
        course = course.toDomain(),
        createdAt = createdAt
    )
}

fun CourseReviewResponseDto.toDomain(): CourseReview {
    return CourseReview(
        id = id,
        courseId = courseId,
        userId = userId,
        authorName = authorName,
        rating = rating,
        text = text,
        createdAt = createdAt
    )
}
