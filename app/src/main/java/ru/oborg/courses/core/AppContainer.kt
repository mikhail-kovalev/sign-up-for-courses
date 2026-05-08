package ru.oborg.courses.core

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import ru.oborg.courses.BuildConfig
import ru.oborg.courses.data.local.AuthTokenStorage
import ru.oborg.courses.data.local.UiSettingsStorage
import ru.oborg.courses.data.remote.CourseApi
import ru.oborg.courses.data.repository.AuthRepositoryImpl
import ru.oborg.courses.data.repository.CourseRepositoryImpl
import ru.oborg.courses.domain.usecase.EnrollCourseUseCase
import ru.oborg.courses.domain.usecase.ChangePasswordUseCase
import ru.oborg.courses.domain.usecase.CreateCourseReviewUseCase
import ru.oborg.courses.domain.usecase.GetCourseUseCase
import ru.oborg.courses.domain.usecase.GetCourseReviewsUseCase
import ru.oborg.courses.domain.usecase.GetCoursesUseCase
import ru.oborg.courses.domain.usecase.GetMyEnrollmentsUseCase
import ru.oborg.courses.domain.usecase.LoginUseCase
import ru.oborg.courses.domain.usecase.LogoutUseCase
import ru.oborg.courses.domain.usecase.ObserveAuthStateUseCase
import ru.oborg.courses.domain.usecase.RegisterUseCase
import ru.oborg.courses.domain.usecase.SubmitSupportRequestUseCase
import ru.oborg.courses.presentation.auth.AuthViewModel
import ru.oborg.courses.presentation.catalog.CourseListViewModel
import ru.oborg.courses.presentation.detail.CourseDetailViewModel
import ru.oborg.courses.presentation.profile.ProfileViewModel

class AppContainer(context: Context) {
    private val tokenStorage = AuthTokenStorage(context.applicationContext)
    val uiSettingsStorage = UiSettingsStorage(context.applicationContext)

    private val httpClient = HttpClient(OkHttp) {
        expectSuccess = true

        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }
            )
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 15_000
        }
    }

    private val api = CourseApi(
        client = httpClient,
        baseUrl = BuildConfig.API_BASE_URL,
        tokenStorage = tokenStorage
    )

    private val authRepository = AuthRepositoryImpl(
        api = api,
        tokenStorage = tokenStorage
    )
    private val courseRepository = CourseRepositoryImpl(
        api = api,
        tokenStorage = tokenStorage
    )

    val authViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        AuthViewModel(
            loginUseCase = LoginUseCase(authRepository),
            registerUseCase = RegisterUseCase(authRepository),
            observeAuthStateUseCase = ObserveAuthStateUseCase(authRepository),
            logoutUseCase = LogoutUseCase(authRepository)
        )
    }

    val courseListViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        CourseListViewModel(
            getCoursesUseCase = GetCoursesUseCase(courseRepository)
        )
    }

    fun courseDetailViewModelFactory(courseId: Int): ViewModelProvider.Factory = viewModelFactory {
        CourseDetailViewModel(
            courseId = courseId,
            getCourseUseCase = GetCourseUseCase(courseRepository),
            enrollCourseUseCase = EnrollCourseUseCase(courseRepository),
            getMyEnrollmentsUseCase = GetMyEnrollmentsUseCase(courseRepository),
            getCourseReviewsUseCase = GetCourseReviewsUseCase(courseRepository),
            createCourseReviewUseCase = CreateCourseReviewUseCase(courseRepository)
        )
    }

    val profileViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        ProfileViewModel(
            getMyEnrollmentsUseCase = GetMyEnrollmentsUseCase(courseRepository),
            submitSupportRequestUseCase = SubmitSupportRequestUseCase(authRepository),
            changePasswordUseCase = ChangePasswordUseCase(authRepository)
        )
    }
}

private fun <VM : ViewModel> viewModelFactory(create: () -> VM): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = create() as T
    }
}
