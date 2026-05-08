package ru.oborg.courses.server

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import org.slf4j.event.Level
import ru.oborg.courses.server.application.EnrollCourseUseCase
import ru.oborg.courses.server.application.ChangePasswordResult
import ru.oborg.courses.server.application.ChangePasswordUseCase
import ru.oborg.courses.server.application.CreateCourseReviewUseCase
import ru.oborg.courses.server.application.GetCourseUseCase
import ru.oborg.courses.server.application.GetCourseReviewsUseCase
import ru.oborg.courses.server.application.GetCoursesUseCase
import ru.oborg.courses.server.application.GetCurrentUserUseCase
import ru.oborg.courses.server.application.GetMyEnrollmentsUseCase
import ru.oborg.courses.server.application.LoginUseCase
import ru.oborg.courses.server.application.RegisterUseCase
import ru.oborg.courses.server.application.SubmitSupportRequestUseCase
import ru.oborg.courses.server.data.Database
import ru.oborg.courses.server.data.DatabaseMigrator
import ru.oborg.courses.server.data.JdbcAuthRepository
import ru.oborg.courses.server.data.JdbcCourseRepository
import ru.oborg.courses.server.data.ServerSeeder
import ru.oborg.courses.server.domain.repository.CreateReviewResult
import ru.oborg.courses.server.domain.repository.EnrollCourseResult
import ru.oborg.courses.server.presentation.AuthResponse
import ru.oborg.courses.server.presentation.ChangePasswordRequest
import ru.oborg.courses.server.presentation.CreateReviewRequest
import ru.oborg.courses.server.presentation.ErrorResponse
import ru.oborg.courses.server.presentation.LoginRequest
import ru.oborg.courses.server.presentation.MessageResponse
import ru.oborg.courses.server.presentation.RegisterRequest
import ru.oborg.courses.server.presentation.SupportRequest
import ru.oborg.courses.server.presentation.toResponse
import ru.oborg.courses.server.security.JwtConfig
import java.sql.SQLException

fun main() {
    embeddedServer(
        factory = Netty,
        host = "0.0.0.0",
        port = 8080,
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    val applicationLog = environment.log

    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                encodeDefaults = true
                ignoreUnknownKeys = true
            }
        )
    }

    install(CallLogging) {
        level = Level.INFO
    }

    install(StatusPages) {
        exception<SQLException> { call, cause ->
            applicationLog.error("Database request failed", cause)
            call.respond(HttpStatusCode.ServiceUnavailable, ErrorResponse("Database is temporarily unavailable"))
        }
        exception<Throwable> { call, cause ->
            applicationLog.error("Unexpected server error", cause)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Unexpected server error"))
        }
    }

    install(Authentication) {
        jwt("auth-jwt") {
            realm = JwtConfig.realm
            verifier(JwtConfig.verifier)
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asInt()
                val email = credential.payload.getClaim("email").asString()
                if (userId != null && !email.isNullOrBlank()) JWTPrincipal(credential.payload) else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Token is missing or invalid"))
            }
        }
    }

    val dataSource = Database.createDataSource()
    val authRepository = JdbcAuthRepository(dataSource)
    val courseRepository = JdbcCourseRepository(dataSource)

    DatabaseMigrator(dataSource).migrate()
    ServerSeeder(
        authRepository = authRepository,
        courseRepository = courseRepository
    ).seed()

    val registerUseCase = RegisterUseCase(authRepository)
    val loginUseCase = LoginUseCase(authRepository)
    val getCurrentUserUseCase = GetCurrentUserUseCase(authRepository)
    val changePasswordUseCase = ChangePasswordUseCase(authRepository)
    val submitSupportRequestUseCase = SubmitSupportRequestUseCase(authRepository)
    val getCoursesUseCase = GetCoursesUseCase(courseRepository)
    val getCourseUseCase = GetCourseUseCase(courseRepository)
    val enrollCourseUseCase = EnrollCourseUseCase(courseRepository)
    val getMyEnrollmentsUseCase = GetMyEnrollmentsUseCase(courseRepository)
    val getCourseReviewsUseCase = GetCourseReviewsUseCase(courseRepository)
    val createCourseReviewUseCase = CreateCourseReviewUseCase(courseRepository)

    routing {
        get("/") {
            call.respondRedirect("/health")
        }

        get("/health") {
            call.respond(mapOf("status" to "ok", "service" to "oborg-courses-api"))
        }

        route("/auth") {
            post("/register") {
                val request = call.receive<RegisterRequest>()
                if (!request.isValid()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Name, email and password are required"))
                    return@post
                }

                val user = registerUseCase(
                    fullName = request.fullName.trim(),
                    email = request.email.trim(),
                    password = request.password
                )

                if (user == null) {
                    call.respond(HttpStatusCode.Conflict, ErrorResponse("User with this email already exists"))
                    return@post
                }

                call.respond(HttpStatusCode.Created, user.toAuthResponse())
            }

            post("/login") {
                val request = call.receive<LoginRequest>()
                val user = loginUseCase(
                    email = request.email.trim(),
                    password = request.password
                )

                if (user == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid email or password"))
                    return@post
                }

                call.respond(user.toAuthResponse())
            }
        }

        get("/courses") {
            call.respond(getCoursesUseCase().map { course -> course.toResponse() })
        }

        get("/courses/{id}") {
            val courseId = call.parameters["id"]?.toIntOrNull()
            if (courseId == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Course id is required"))
                return@get
            }

            val course = getCourseUseCase(courseId)
            if (course == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Course not found"))
            } else {
                call.respond(course.toResponse())
            }
        }

        get("/courses/{id}/reviews") {
            val courseId = call.parameters["id"]?.toIntOrNull()
            if (courseId == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Course id is required"))
                return@get
            }

            if (getCourseUseCase(courseId) == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Course not found"))
                return@get
            }

            call.respond(getCourseReviewsUseCase(courseId).map { review -> review.toResponse() })
        }

        authenticate("auth-jwt") {
            get("/users/me") {
                val user = getCurrentUserUseCase(call.requireUserId())
                if (user == null) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("User not found"))
                } else {
                    call.respond(user.toResponse())
                }
            }

            post("/users/me/password") {
                val request = call.receive<ChangePasswordRequest>()
                if (request.currentPassword.isBlank() || request.newPassword.length < 6) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Current password and new password are required"))
                    return@post
                }

                when (
                    changePasswordUseCase(
                        userId = call.requireUserId(),
                        currentPassword = request.currentPassword,
                        newPassword = request.newPassword
                    )
                ) {
                    ChangePasswordResult.Changed -> {
                        call.respond(MessageResponse("Password changed"))
                    }

                    ChangePasswordResult.InvalidCurrentPassword -> {
                        call.respond(HttpStatusCode.Forbidden, ErrorResponse("Current password is incorrect"))
                    }

                    ChangePasswordResult.UserNotFound -> {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("User not found"))
                    }
                }
            }

            post("/support-requests") {
                val request = call.receive<SupportRequest>()
                val message = request.message.trim()
                if (message.length < 10) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Message is too short"))
                    return@post
                }

                submitSupportRequestUseCase(
                    userId = call.requireUserId(),
                    message = message.take(1000)
                )
                call.respond(HttpStatusCode.Created, MessageResponse("Support request created"))
            }

            get("/enrollments") {
                call.respond(
                    getMyEnrollmentsUseCase(call.requireUserId())
                        .map { enrollment -> enrollment.toResponse() }
                )
            }

            post("/courses/{id}/enroll") {
                val courseId = call.parameters["id"]?.toIntOrNull()
                if (courseId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Course id is required"))
                    return@post
                }

                when (val result = enrollCourseUseCase(userId = call.requireUserId(), courseId = courseId)) {
                    EnrollCourseResult.AlreadyEnrolled -> {
                        call.respond(HttpStatusCode.Conflict, ErrorResponse("User is already enrolled in this course"))
                    }

                    EnrollCourseResult.CourseNotFound -> {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("Course not found"))
                    }

                    EnrollCourseResult.NoSeats -> {
                        call.respond(HttpStatusCode.Conflict, ErrorResponse("No seats left for this course"))
                    }

                    is EnrollCourseResult.Enrolled -> {
                        call.respond(HttpStatusCode.Created, result.enrollment.toResponse())
                    }
                }
            }

            post("/courses/{id}/reviews") {
                val courseId = call.parameters["id"]?.toIntOrNull()
                if (courseId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Course id is required"))
                    return@post
                }

                val request = call.receive<CreateReviewRequest>()
                val text = request.text.trim()
                if (request.rating !in 1..5 || text.length < 12) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Rating from 1 to 5 and review text are required"))
                    return@post
                }

                when (
                    val result = createCourseReviewUseCase(
                        userId = call.requireUserId(),
                        courseId = courseId,
                        rating = request.rating,
                        text = text.take(500)
                    )
                ) {
                    CreateReviewResult.CourseNotFound -> {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("Course not found"))
                    }

                    CreateReviewResult.NotEnrolled -> {
                        call.respond(HttpStatusCode.Forbidden, ErrorResponse("Only enrolled students can review this course"))
                    }

                    CreateReviewResult.AlreadyReviewed -> {
                        call.respond(HttpStatusCode.Conflict, ErrorResponse("You have already reviewed this course"))
                    }

                    is CreateReviewResult.Created -> {
                        call.respond(HttpStatusCode.Created, result.review.toResponse())
                    }
                }
            }
        }
    }
}

private fun RegisterRequest.isValid(): Boolean {
    return fullName.isNotBlank() && email.isNotBlank() && password.length >= 6
}

private fun ru.oborg.courses.server.domain.model.AuthUser.toAuthResponse(): AuthResponse {
    return AuthResponse(
        token = JwtConfig.generateToken(
            userId = id,
            email = email,
            role = role
        ),
        user = toResponse(),
        expiresInMinutes = JwtConfig.expiresInMinutes
    )
}

private fun ApplicationCall.requireUserId(): Int {
    return principal<JWTPrincipal>()
        ?.payload
        ?.getClaim("userId")
        ?.asInt()
        ?: error("Missing authenticated user")
}
