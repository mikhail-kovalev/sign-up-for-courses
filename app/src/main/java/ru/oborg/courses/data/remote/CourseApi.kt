package ru.oborg.courses.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import ru.oborg.courses.data.local.AuthTokenStorage
import ru.oborg.courses.data.remote.dto.AuthResponseDto
import ru.oborg.courses.data.remote.dto.ChangePasswordRequestDto
import ru.oborg.courses.data.remote.dto.CreateReviewRequestDto
import ru.oborg.courses.data.remote.dto.CourseResponseDto
import ru.oborg.courses.data.remote.dto.CourseReviewResponseDto
import ru.oborg.courses.data.remote.dto.EnrollmentResponseDto
import ru.oborg.courses.data.remote.dto.ErrorResponseDto
import ru.oborg.courses.data.remote.dto.LoginRequestDto
import ru.oborg.courses.data.remote.dto.MessageResponseDto
import ru.oborg.courses.data.remote.dto.RegisterRequestDto
import ru.oborg.courses.data.remote.dto.SupportRequestDto

class CourseApi(
    private val client: HttpClient,
    private val baseUrl: String,
    private val tokenStorage: AuthTokenStorage
) {
    suspend fun register(request: RegisterRequestDto): AuthResponseDto {
        return client.post("$baseUrl/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun login(request: LoginRequestDto): AuthResponseDto {
        return client.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun changePassword(request: ChangePasswordRequestDto): MessageResponseDto {
        return client.post("$baseUrl/users/me/password") {
            contentType(ContentType.Application.Json)
            tokenStorage.getToken()?.let { token -> bearerAuth(token) }
            setBody(request)
        }.body()
    }

    suspend fun submitSupportRequest(request: SupportRequestDto): MessageResponseDto {
        return client.post("$baseUrl/support-requests") {
            contentType(ContentType.Application.Json)
            tokenStorage.getToken()?.let { token -> bearerAuth(token) }
            setBody(request)
        }.body()
    }

    suspend fun getCourses(): List<CourseResponseDto> {
        return client.get("$baseUrl/courses").body()
    }

    suspend fun getCourse(id: Int): CourseResponseDto {
        return client.get("$baseUrl/courses/$id").body()
    }

    suspend fun getCourseReviews(courseId: Int): List<CourseReviewResponseDto> {
        return client.get("$baseUrl/courses/$courseId/reviews").body()
    }

    suspend fun enroll(courseId: Int): EnrollmentResponseDto {
        return client.post("$baseUrl/courses/$courseId/enroll") {
            tokenStorage.getToken()?.let { token -> bearerAuth(token) }
        }.body()
    }

    suspend fun createCourseReview(courseId: Int, request: CreateReviewRequestDto): CourseReviewResponseDto {
        return client.post("$baseUrl/courses/$courseId/reviews") {
            contentType(ContentType.Application.Json)
            tokenStorage.getToken()?.let { token -> bearerAuth(token) }
            setBody(request)
        }.body()
    }

    suspend fun getMyEnrollments(): List<EnrollmentResponseDto> {
        return client.get("$baseUrl/enrollments") {
            tokenStorage.getToken()?.let { token -> bearerAuth(token) }
        }.body()
    }
}

suspend fun Throwable.toUserMessage(): String {
    return when (this) {
        is ClientRequestException -> runCatching {
            response.body<ErrorResponseDto>().message
        }.getOrElse {
            if (response.status == HttpStatusCode.Unauthorized) {
                "Сессия истекла. Войдите заново"
            } else {
                "Не удалось выполнить запрос"
            }
        }
        is ServerResponseException -> "Сервер временно недоступен"
        else -> message ?: "Не удалось выполнить запрос"
    }
}

fun Throwable.isUnauthorizedResponse(): Boolean {
    return this is ClientRequestException && response.status == HttpStatusCode.Unauthorized
}
