package ru.oborg.courses.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.oborg.courses.R
import ru.oborg.courses.core.AppContainer
import ru.oborg.courses.presentation.auth.AuthScreen
import ru.oborg.courses.presentation.auth.AuthViewModel
import ru.oborg.courses.presentation.catalog.CourseListScreen
import ru.oborg.courses.presentation.catalog.CourseListViewModel
import ru.oborg.courses.presentation.detail.CourseDetailScreen
import ru.oborg.courses.presentation.detail.CourseDetailViewModel
import ru.oborg.courses.presentation.home.HomeScreen
import ru.oborg.courses.presentation.mycourses.MyCoursesScreen
import ru.oborg.courses.presentation.profile.ProfileScreen
import ru.oborg.courses.presentation.profile.ProfileViewModel
import ru.oborg.courses.data.local.UiSettings
import ru.oborg.courses.core.UiState

@Composable
fun CourseApp(
    container: AppContainer,
    uiSettings: UiSettings,
    onDarkThemeChanged: (Boolean) -> Unit,
    onNotificationsChanged: (Boolean) -> Unit,
    onEnrollmentNotification: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val authViewModel: AuthViewModel = viewModel(factory = container.authViewModelFactory)
    val authState by authViewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current

    if (!authState.isAuthenticated) {
        AuthScreen(
            state = authState,
            onFullNameChanged = authViewModel::onFullNameChanged,
            onEmailChanged = authViewModel::onEmailChanged,
            onPasswordChanged = authViewModel::onPasswordChanged,
            onToggleMode = authViewModel::toggleMode,
            onSubmit = {
                focusManager.clearFocus()
                authViewModel.submit()
            },
            modifier = modifier
        )
    } else {
        MainArea(
            container = container,
            authViewModel = authViewModel,
            uiSettings = uiSettings,
            onDarkThemeChanged = onDarkThemeChanged,
            onNotificationsChanged = onNotificationsChanged,
            onEnrollmentNotification = onEnrollmentNotification,
            modifier = modifier
        )
    }
}

@Composable
private fun MainArea(
    container: AppContainer,
    authViewModel: AuthViewModel,
    uiSettings: UiSettings,
    onDarkThemeChanged: (Boolean) -> Unit,
    onNotificationsChanged: (Boolean) -> Unit,
    onEnrollmentNotification: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val authState by authViewModel.state.collectAsState()
    val bottomItems = listOf(
        BottomItem.Home,
        BottomItem.Catalog,
        BottomItem.MyCourses,
        BottomItem.Profile
    )

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        bottomBar = {
            if (currentRoute != Screen.Detail.route) {
                val navShape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(navShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.86f))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.38f),
                            shape = navShape
                        )
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp
                    ) {
                        bottomItems.forEach { item ->
                            NavigationBarItem(
                                selected = currentRoute == item.route,
                                onClick = {
                                    if (item.route == Screen.Home.route) {
                                        val returnedHome = navController.popBackStack(Screen.Home.route, false)
                                        if (!returnedHome) {
                                            navController.navigate(Screen.Home.route) {
                                                launchSingleTop = true
                                            }
                                        }
                                    } else {
                                        navController.navigate(item.route) {
                                            popUpTo(Screen.Home.route) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        painter = painterResource(item.iconRes),
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route
        ) {
            composable(Screen.Home.route) {
                val courseViewModel: CourseListViewModel = viewModel(factory = container.courseListViewModelFactory)
                val profileViewModel: ProfileViewModel = viewModel(factory = container.profileViewModelFactory)
                val coursesState by courseViewModel.state.collectAsState()
                val enrollmentsState by profileViewModel.state.collectAsState()
                HomeScreen(
                    user = authState.session?.user,
                    coursesState = coursesState,
                    enrollmentsState = enrollmentsState,
                    onCourseClick = { courseId -> navController.navigate(Screen.Detail.createRoute(courseId)) },
                    onOpenCatalog = { navController.navigate(Screen.Catalog.route) },
                    onOpenMyCourses = { navController.navigate(Screen.MyCourses.route) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            composable(Screen.Catalog.route) {
                val viewModel: CourseListViewModel = viewModel(factory = container.courseListViewModelFactory)
                val state by viewModel.state.collectAsState()
                CourseListScreen(
                    state = state,
                    onRefresh = viewModel::loadCourses,
                    onCourseClick = { courseId -> navController.navigate(Screen.Detail.createRoute(courseId)) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            composable(Screen.MyCourses.route) {
                val viewModel: ProfileViewModel = viewModel(factory = container.profileViewModelFactory)
                val state by viewModel.state.collectAsState()
                MyCoursesScreen(
                    state = state,
                    onRefresh = viewModel::loadEnrollments,
                    onCourseClick = { courseId -> navController.navigate(Screen.Detail.createRoute(courseId)) },
                    onOpenCatalog = { navController.navigate(Screen.Catalog.route) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            composable(Screen.Profile.route) {
                val viewModel: ProfileViewModel = viewModel(factory = container.profileViewModelFactory)
                val state by viewModel.state.collectAsState()
                val formState by viewModel.formState.collectAsState()
                ProfileScreen(
                    user = authState.session?.user,
                    enrollmentsState = state,
                    formState = formState,
                    uiSettings = uiSettings,
                    onDarkThemeChanged = onDarkThemeChanged,
                    onNotificationsChanged = onNotificationsChanged,
                    onSupportMessageChanged = viewModel::onSupportMessageChanged,
                    onSubmitSupportRequest = viewModel::submitSupportRequest,
                    onCurrentPasswordChanged = viewModel::onCurrentPasswordChanged,
                    onNewPasswordChanged = viewModel::onNewPasswordChanged,
                    onRepeatPasswordChanged = viewModel::onRepeatPasswordChanged,
                    onChangePassword = viewModel::changePassword,
                    onRefresh = viewModel::loadEnrollments,
                    onLogout = authViewModel::logout,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument(Screen.Detail.ARG_COURSE_ID) { type = NavType.IntType })
            ) { entry ->
                val courseId = entry.arguments?.getInt(Screen.Detail.ARG_COURSE_ID) ?: return@composable
                val viewModel: CourseDetailViewModel = viewModel(
                    key = "course-detail-$courseId",
                    factory = container.courseDetailViewModelFactory(courseId)
                )
                val state by viewModel.state.collectAsState()
                val notificationWasSent = remember(courseId) { mutableStateOf(false) }
                LaunchedEffect(state.isEnrolled, state.courseState) {
                    val courseState = state.courseState
                    if (state.isEnrolled && !notificationWasSent.value && courseState is UiState.Content) {
                        notificationWasSent.value = true
                        onEnrollmentNotification(courseState.value.title)
                    }
                }
                CourseDetailScreen(
                    state = state,
                    onBack = { navController.popBackStack() },
                    onEnroll = viewModel::enroll,
                    onRetry = viewModel::loadCourse,
                    onReviewTextChanged = viewModel::onReviewTextChanged,
                    onReviewRatingChanged = viewModel::onReviewRatingChanged,
                    onSubmitReview = viewModel::submitReview,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

private sealed interface Screen {
    val route: String

    data object Home : Screen {
        override val route: String = "home"
    }

    data object Catalog : Screen {
        override val route: String = "catalog"
    }

    data object MyCourses : Screen {
        override val route: String = "my-courses"
    }

    data object Profile : Screen {
        override val route: String = "profile"
    }

    data object Detail : Screen {
        const val ARG_COURSE_ID = "courseId"
        override val route: String = "detail/{$ARG_COURSE_ID}"

        fun createRoute(courseId: Int): String = "detail/$courseId"
    }
}

private sealed class BottomItem(
    val route: String,
    val label: String,
    val iconRes: Int
) {
    data object Home : BottomItem(
        route = Screen.Home.route,
        label = "Главная",
        iconRes = R.drawable.ic_lucide_house
    )

    data object Catalog : BottomItem(
        route = Screen.Catalog.route,
        label = "Курсы",
        iconRes = R.drawable.ic_lucide_library_big
    )

    data object MyCourses : BottomItem(
        route = Screen.MyCourses.route,
        label = "Мои",
        iconRes = R.drawable.ic_lucide_bookmark_check
    )

    data object Profile : BottomItem(
        route = Screen.Profile.route,
        label = "Профиль",
        iconRes = R.drawable.ic_lucide_user_round
    )
}
