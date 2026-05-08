package ru.oborg.courses.presentation.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.oborg.courses.R
import ru.oborg.courses.core.UiState
import ru.oborg.courses.domain.model.Course
import ru.oborg.courses.domain.model.CourseReview
import ru.oborg.courses.presentation.common.addDays
import ru.oborg.courses.presentation.common.categoryName
import ru.oborg.courses.presentation.common.courseCoverRes
import ru.oborg.courses.presentation.common.humanCourseDate
import ru.oborg.courses.presentation.common.humanEnrollmentDate
import ru.oborg.courses.presentation.common.mentorAvatarRes
import ru.oborg.courses.presentation.common.AppBackground

@Composable
fun CourseDetailScreen(
    state: CourseDetailUiState,
    onBack: () -> Unit,
    onEnroll: () -> Unit,
    onRetry: () -> Unit,
    onReviewTextChanged: (String) -> Unit,
    onReviewRatingChanged: (Int) -> Unit,
    onSubmitReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = androidx.compose.ui.graphics.Color.Transparent
    ) {
        AppBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Header(onBack = onBack)

                when (state.courseState) {
                    UiState.Loading -> LoadingBlock()
                    is UiState.Error -> ErrorBlock(message = state.courseState.message, onRetry = onRetry)
                    is UiState.Content -> CourseContent(
                        course = state.courseState.value,
                        isEnrolling = state.isEnrolling,
                        isEnrolled = state.isEnrolled,
                        message = state.message,
                        reviewsState = state.reviewsState,
                        reviewText = state.reviewText,
                        reviewRating = state.reviewRating,
                        reviewMessage = state.reviewMessage,
                        isSubmittingReview = state.isSubmittingReview,
                        onReviewTextChanged = onReviewTextChanged,
                        onReviewRatingChanged = onReviewRatingChanged,
                        onSubmitReview = onSubmitReview,
                        onEnroll = onEnroll
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_lucide_arrow_left),
                    contentDescription = "Назад"
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Курс",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun LoadingBlock() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ErrorBlock(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = onRetry) {
                Icon(
                    painter = painterResource(R.drawable.ic_lucide_refresh_cw),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Повторить")
            }
        }
    }
}

@Composable
private fun CourseContent(
    course: Course,
    isEnrolling: Boolean,
    isEnrolled: Boolean,
    message: String?,
    reviewsState: UiState<List<CourseReview>>,
    reviewText: String,
    reviewRating: Int,
    reviewMessage: String?,
    isSubmittingReview: Boolean,
    onReviewTextChanged: (String) -> Unit,
    onReviewRatingChanged: (Int) -> Unit,
    onSubmitReview: () -> Unit,
    onEnroll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CourseHero(course = course)
        RatingSummary(reviewsState = reviewsState)
        InfoGrid(course = course)
        LearningPlanBlock(course = course)
        ProgramBlock(course = course)
        ReviewsBlock(
            reviewsState = reviewsState,
            isEnrolled = isEnrolled,
            reviewText = reviewText,
            reviewRating = reviewRating,
            reviewMessage = reviewMessage,
            isSubmittingReview = isSubmittingReview,
            onReviewTextChanged = onReviewTextChanged,
            onReviewRatingChanged = onReviewRatingChanged,
            onSubmitReview = onSubmitReview
        )
        TeacherBlock(course = course)
        message?.let {
            Text(
                text = it,
                color = if (isEnrolled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Button(
            onClick = onEnroll,
            enabled = !isEnrolling && !isEnrolled,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            contentPadding = PaddingValues(vertical = 15.dp)
        ) {
            if (isEnrolling) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(18.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_lucide_badge_check),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Text(
                when {
                    isEnrolled -> "Уже зарегистрирован"
                    else -> "Записаться"
                }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CourseHero(course: Course) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column {
            Box {
                Image(
                    painter = painterResource(courseCoverRes(course)),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = course.categoryName(),
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.86f),
                    contentColor = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = "Онлайн",
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = course.level,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = course.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RatingSummary(reviewsState: UiState<List<CourseReview>>) {
    val reviews = (reviewsState as? UiState.Content)?.value.orEmpty()
    val average = reviews
        .takeIf { it.isNotEmpty() }
        ?.map { it.rating }
        ?.average()
        ?.let { String.format(java.util.Locale.US, "%.1f", it) }
        ?: "0.0"
    val subtitle = when (reviewsState) {
        UiState.Loading -> "Отзывы загружаются"
        is UiState.Error -> "Отзывы временно недоступны"
        is UiState.Content -> if (reviews.isEmpty()) {
            "Пока нет отзывов"
        } else {
            "${reviews.size} отзывов от студентов"
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = average,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "из 5",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Text(
                    text = "Высокая оценка",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun InfoGrid(course: Course) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            InfoTile(
                icon = { Icon(painterResource(R.drawable.ic_lucide_calendar), contentDescription = null) },
                label = "Старт",
                value = humanCourseDate(course.startsAt),
                modifier = Modifier.weight(1f)
            )
            InfoTile(
                icon = { Icon(painterResource(R.drawable.ic_lucide_clock), contentDescription = null) },
                label = "Длительность",
                value = "${course.durationHours} ч",
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            InfoTile(
                icon = { Icon(painterResource(R.drawable.ic_lucide_banknote), contentDescription = null) },
                label = "Стоимость",
                value = "${course.priceRub} ₽",
                modifier = Modifier.weight(1f)
            )
            InfoTile(
                icon = { Icon(painterResource(R.drawable.ic_lucide_book_open), contentDescription = null) },
                label = "Формат",
                value = "Онлайн",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun InfoTile(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(22.dp), contentAlignment = Alignment.Center) {
                icon()
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun LearningPlanBlock(course: Course) {
    ExpandableCourseSection(
        title = "План обучения",
        subtitle = "Курс идет потоком: видео, практика и итоговый проект",
        iconRes = R.drawable.ic_lucide_calendar,
        initiallyExpanded = true
    ) {
        LearningPlanRow(
            number = "01",
            title = "Старт курса",
            date = humanCourseDate(course.startsAt),
            subtitle = "Открываются вводные уроки и материалы курса"
        )
        LearningPlanRow(
            number = "02",
            title = "Первый дедлайн",
            date = humanCourseDate(addDays(course.startsAt, 7)),
            subtitle = "Практическое задание по базовому модулю"
        )
        LearningPlanRow(
            number = "03",
            title = "Практический блок",
            date = "2-4 неделя",
            subtitle = "Видеоуроки, разбор кейсов и обратная связь преподавателя"
        )
        LearningPlanRow(
            number = "04",
            title = "Итоговый проект",
            date = humanCourseDate(addDays(course.startsAt, 28)),
            subtitle = "Защита работы и подготовка сертификата"
        )
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_lucide_clock),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "После записи доступ к курсу закрепляется за аккаунтом",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ExpandableCourseSection(
    title: String,
    subtitle: String,
    iconRes: Int,
    initiallyExpanded: Boolean,
    content: @Composable () -> Unit
) {
    var expanded by rememberSaveable(title) { mutableStateOf(initiallyExpanded) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                onClick = { expanded = !expanded },
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(iconRes),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(R.drawable.ic_lucide_arrow_right),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(18.dp)
                                    .rotate(if (expanded) 90f else 0f)
                            )
                        }
                    }
                }
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(14.dp))
                content()
            }
        }
    }
}

@Composable
private fun LearningPlanRow(
    number: String,
    title: String,
    date: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.padding(bottom = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(42.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProgramBlock(course: Course) {
    ExpandableCourseSection(
        title = "Программа курса",
        subtitle = "Модули, практика и итоговая работа",
        iconRes = R.drawable.ic_lucide_book_open,
        initiallyExpanded = false
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ProgramStep(number = "01", title = "Вводный модуль", subtitle = "Цели курса и настройка рабочего окружения")
            ProgramStep(number = "02", title = course.categoryName(), subtitle = "Практика по направлению и разбор кейсов")
            ProgramStep(number = "03", title = "Итоговая работа", subtitle = "Мини-проект и рекомендации преподавателя")
         }
    }
}

@Composable
private fun ProgramStep(
    number: String,
    title: String,
    subtitle: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(38.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReviewsBlock(
    reviewsState: UiState<List<CourseReview>>,
    isEnrolled: Boolean,
    reviewText: String,
    reviewRating: Int,
    reviewMessage: String?,
    isSubmittingReview: Boolean,
    onReviewTextChanged: (String) -> Unit,
    onReviewRatingChanged: (Int) -> Unit,
    onSubmitReview: () -> Unit
) {
    val reviews = (reviewsState as? UiState.Content)?.value.orEmpty()
    val averageRating = reviews
        .takeIf { it.isNotEmpty() }
        ?.map { it.rating }
        ?.average()
        ?.let { String.format(java.util.Locale.US, "%.1f", it) }
        ?: "0.0"
    val reviewsText = if (reviews.isEmpty()) {
        "Реальные отзывы появятся после публикации студентами"
    } else {
        "${reviews.size} отзывов от студентов"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Отзывы",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = reviewsText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Text(
                        text = "★ $averageRating",
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            when (reviewsState) {
                UiState.Loading -> Text(
                    text = "Загружаем отзывы",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                is UiState.Error -> Text(
                    text = reviewsState.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )

                is UiState.Content -> {
                    if (reviews.isEmpty()) {
                        Text(
                            text = "Пока никто из записанных студентов не оставил отзыв.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        reviews.forEach { review -> ReviewItem(review = review) }
                    }
                }
            }

            SubmitReviewBlock(
                isEnrolled = isEnrolled,
                reviewText = reviewText,
                reviewRating = reviewRating,
                reviewMessage = reviewMessage,
                isSubmittingReview = isSubmittingReview,
                onReviewTextChanged = onReviewTextChanged,
                onReviewRatingChanged = onReviewRatingChanged,
                onSubmitReview = onSubmitReview
            )
        }
    }
}

@Composable
private fun SubmitReviewBlock(
    isEnrolled: Boolean,
    reviewText: String,
    reviewRating: Int,
    reviewMessage: String?,
    isSubmittingReview: Boolean,
    onReviewTextChanged: (String) -> Unit,
    onReviewRatingChanged: (Int) -> Unit,
    onSubmitReview: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (isEnrolled) "Оставить отзыв" else "Отзывы доступны студентам курса",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            if (isEnrolled) {
                RatingSelector(
                    selected = reviewRating,
                    onSelected = onReviewRatingChanged
                )
                OutlinedTextField(
                    value = reviewText,
                    onValueChange = onReviewTextChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Что понравилось в курсе?") },
                    minLines = 3,
                    maxLines = 5,
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
                Button(
                    onClick = onSubmitReview,
                    enabled = !isSubmittingReview,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(if (isSubmittingReview) "Отправляем" else "Опубликовать")
                }
            } else {
                Text(
                    text = "Запишитесь на курс, чтобы оставить отзыв от своего аккаунта.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            reviewMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = if ("опубликован" in it.lowercase()) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
        }
    }
}

@Composable
private fun RatingSelector(
    selected: Int,
    onSelected: (Int) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        (1..5).forEach { rating ->
            Surface(
                onClick = { onSelected(rating) },
                shape = CircleShape,
                color = if (selected == rating) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface,
                contentColor = if (selected == rating) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text(
                    text = rating.toString(),
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ReviewItem(review: CourseReview) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = review.authorName.firstOrNull()?.uppercase() ?: "С",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = review.authorName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = humanEnrollmentDate(review.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "★ ${review.rating}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = review.text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TeacherBlock(course: Course) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(mentorAvatarRes(course)),
                contentDescription = null,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Преподаватель",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = course.teacher,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Ведет практические занятия и проверяет итоговый проект",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
