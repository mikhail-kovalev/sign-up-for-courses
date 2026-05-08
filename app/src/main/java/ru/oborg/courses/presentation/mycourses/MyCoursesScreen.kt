package ru.oborg.courses.presentation.mycourses

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.oborg.courses.R
import ru.oborg.courses.core.UiState
import ru.oborg.courses.domain.model.Enrollment
import ru.oborg.courses.presentation.common.addDays
import ru.oborg.courses.presentation.common.categoryName
import ru.oborg.courses.presentation.common.courseCoverRes
import ru.oborg.courses.presentation.common.dayOfMonth
import ru.oborg.courses.presentation.common.humanCourseDate
import ru.oborg.courses.presentation.common.humanEnrollmentDate
import ru.oborg.courses.presentation.common.humanMonthYear
import ru.oborg.courses.presentation.common.learningProgress
import ru.oborg.courses.presentation.common.monthGridDates
import ru.oborg.courses.presentation.common.AppBackground
import ru.oborg.courses.presentation.common.shortMonth
import ru.oborg.courses.presentation.common.weekDay

@Composable
fun MyCoursesScreen(
    state: UiState<List<Enrollment>>,
    onRefresh: () -> Unit,
    onCourseClick: (Int) -> Unit,
    onOpenCatalog: () -> Unit,
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
                Header(onRefresh = onRefresh)
                when (state) {
                    UiState.Loading -> LoadingBlock()
                    is UiState.Error -> ErrorBlock(message = state.message, onRefresh = onRefresh)
                    is UiState.Content -> EnrollmentList(
                        enrollments = state.value,
                        onCourseClick = onCourseClick,
                        onOpenCatalog = onOpenCatalog
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(onRefresh: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 22.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Мои курсы",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Записи, прогресс и сертификаты",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onRefresh) {
            Icon(
                painter = painterResource(R.drawable.ic_lucide_refresh_cw),
                contentDescription = "Обновить",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun LoadingBlock() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ErrorBlock(
    message: String,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.error
            )
            IconButton(onClick = onRefresh) {
                Icon(
                    painter = painterResource(R.drawable.ic_lucide_refresh_cw),
                    contentDescription = "Повторить"
                )
            }
        }
    }
}

@Composable
private fun EnrollmentList(
    enrollments: List<Enrollment>,
    onCourseClick: (Int) -> Unit,
    onOpenCatalog: () -> Unit
) {
    if (enrollments.isEmpty()) {
        EmptyState(onOpenCatalog = onOpenCatalog)
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            MyCourseEventsBlock(enrollments = enrollments)
        }
        items(enrollments, key = { enrollment -> enrollment.id }) { enrollment ->
            MyCourseCard(
                enrollment = enrollment,
                onClick = { onCourseClick(enrollment.course.id) }
            )
        }
    }
}

@Composable
private fun EmptyState(onOpenCatalog: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_lucide_circle_play),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(34.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Вы пока не записаны на курсы",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Откройте каталог и выберите подходящую программу обучения.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
                onClick = onOpenCatalog,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Перейти в каталог")
            }
        }
    }
}

@Composable
private fun MyCourseEventsBlock(enrollments: List<Enrollment>) {
    val events = enrollments
        .flatMap { it.learningEvents() }
        .sortedBy { it.date }
        .take(6)
    val calendarMonth = events.firstOrNull()?.date.orEmpty()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "События моих курсов",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Дедлайны, проекты и старты потоков",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    painter = painterResource(R.drawable.ic_lucide_calendar),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (calendarMonth.isNotBlank()) {
                EventsCalendarGrid(
                    month = calendarMonth,
                    events = events
                )
                Spacer(modifier = Modifier.height(14.dp))
            }
            events.forEachIndexed { index, event ->
                MyCourseEventRow(
                    event = event,
                    highlighted = index == 0
                )
                if (index < events.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun EventsCalendarGrid(
    month: String,
    events: List<MyCourseEvent>
) {
    val eventLabels = events.associate { it.date to it.label }
    val gridDates = monthGridDates(month)
    val weeks = gridDates.chunked(7)
    val weekLabels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = humanMonthYear(month),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CalendarLegendDot(
                        color = MaterialTheme.colorScheme.secondary,
                        text = "дедлайн"
                    )
                    CalendarLegendDot(
                        color = MaterialTheme.colorScheme.primary,
                        text = "старт"
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                weekLabels.forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            weeks.forEach { week ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    week.forEach { date ->
                        CalendarDayDot(
                            date = date,
                            eventLabel = date?.let { eventLabels[it] },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarLegendDot(
    color: androidx.compose.ui.graphics.Color,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(9.dp),
            shape = CircleShape,
            color = color
        ) {}
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CalendarDayDot(
    date: String?,
    eventLabel: String?,
    modifier: Modifier = Modifier
) {
    val marked = eventLabel != null
    val eventColor = if (eventLabel == "Старт курса") {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondary
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (date == null) {
            Spacer(modifier = Modifier.size(32.dp))
        } else {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = if (marked) eventColor else MaterialTheme.colorScheme.surface,
                contentColor = if (marked) {
                    if (eventLabel == "Старт курса") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                border = if (marked) {
                    null
                } else {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = dayOfMonth(date).trimStart('0').ifBlank { "0" },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun MyCourseEventRow(
    event: MyCourseEvent,
    highlighted: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (highlighted) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (highlighted) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.74f),
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = weekDay(event.date),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = dayOfMonth(event.date),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = shortMonth(event.date),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = event.courseTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${humanCourseDate(event.date)} · ${event.description}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private data class MyCourseEvent(
    val date: String,
    val label: String,
    val courseTitle: String,
    val description: String
)

private fun Enrollment.learningEvents(): List<MyCourseEvent> {
    return listOf(
        MyCourseEvent(
            date = course.startsAt,
            label = "Старт курса",
            courseTitle = course.title,
            description = "${course.categoryName()} · открывается доступ"
        ),
        MyCourseEvent(
            date = addDays(course.startsAt, 7),
            label = "Первый дедлайн",
            courseTitle = course.title,
            description = "практическое задание"
        ),
        MyCourseEvent(
            date = addDays(course.startsAt, 28),
            label = "Итоговый проект",
            courseTitle = course.title,
            description = "финальная работа и сертификат"
        )
    )
}

@Composable
private fun MyCourseCard(
    enrollment: Enrollment,
    onClick: () -> Unit
) {
    val course = enrollment.course
    val progress = course.learningProgress()

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column {
            Image(
                painter = painterResource(courseCoverRes(course)),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Запись от ${humanEnrollmentDate(enrollment.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Прогресс",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$progress%",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_lucide_badge_check),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Сертификат будет доступен после завершения",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
