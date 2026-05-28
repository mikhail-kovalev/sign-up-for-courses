package ru.oborg.courses.presentation.common

import androidx.annotation.DrawableRes
import java.util.Locale
import ru.oborg.courses.R
import ru.oborg.courses.domain.model.Course

@DrawableRes
fun courseCoverRes(course: Course): Int {
    val name = course.title.lowercase(Locale.getDefault())
    return when {
        "clean" in name || "architecture" in name -> R.drawable.course_cover_clean_architecture
        "compose" in name -> R.drawable.course_cover_compose_new
        "ktor" in name || "back-end" in name || "backend" in name || "api" in name -> R.drawable.course_cover_ktor_backend
        "business" in name || "english" in name -> R.drawable.course_cover_business_english
        "kotlin" in name -> R.drawable.course_cover_kotlin_new
        "android pro" in name -> R.drawable.course_cover_android_network
        "ui/ux" in name || "ui-" in name -> R.drawable.course_cover_uiux
        "sql" in name -> R.drawable.course_cover_sql_analytics
        else -> when (course.id) {
            1 -> R.drawable.course_cover_kotlin_new
            2 -> R.drawable.course_cover_compose_new
            3 -> R.drawable.course_cover_ktor_backend
            4 -> R.drawable.course_cover_clean_architecture
            5 -> R.drawable.course_cover_android_network
            6 -> R.drawable.course_cover_uiux
            7 -> R.drawable.course_cover_sql_analytics
            8 -> R.drawable.course_cover_marketing
            9 -> R.drawable.course_cover_business_english
            10 -> R.drawable.course_cover_management
            else -> when ((course.id - 1).floorMod(10)) {
                0 -> R.drawable.course_cover_kotlin_new
                1 -> R.drawable.course_cover_compose_new
                2 -> R.drawable.course_cover_ktor_backend
                3 -> R.drawable.course_cover_clean_architecture
                4 -> R.drawable.course_cover_android_network
                5 -> R.drawable.course_cover_uiux
                6 -> R.drawable.course_cover_sql_analytics
                7 -> R.drawable.course_cover_marketing
                8 -> R.drawable.course_cover_business_english
                else -> R.drawable.course_cover_management
            }
        }
    }
}

@DrawableRes
fun mentorAvatarRes(course: Course): Int {
    return when ((course.id - 1).floorMod(8)) {
        0 -> R.drawable.mentor_photo_02
        1 -> R.drawable.mentor_photo_04
        2 -> R.drawable.mentor_photo_01
        3 -> R.drawable.mentor_photo_06
        4 -> R.drawable.mentor_photo_08
        5 -> R.drawable.mentor_photo_05
        6 -> R.drawable.mentor_photo_07
        else -> R.drawable.mentor_photo_03
    }
}

fun Course.categoryName(): String {
    val name = title.lowercase(Locale.getDefault())
    return when {
        "compose" in name || "ui" in name || "дизайн" in name -> "UI/UX"
        "ktor" in name || "back-end" in name || "backend" in name || "api" in name -> "Back-end"
        "architecture" in name || "архитект" in name -> "Architecture"
        "данн" in name || "sql" in name || "аналит" in name -> "Data"
        "маркет" in name || "контент" in name -> "Marketing"
        "англий" in name || "english" in name -> "English"
        "управ" in name || "менедж" in name -> "Management"
        else -> "Android"
    }
}

fun Course.learningProgress(): Int {
    val seed = (id * 17 + durationHours + seatsBusy * 3) % 74
    return (18 + seed).coerceIn(18, 92)
}

fun Course.ratingValue(): Float {
    return averageRating?.toFloat() ?: 0f
}

fun Course.ratingText(): String {
    return String.format(Locale.US, "%.1f", ratingValue())
}

fun Course.reviewsCount(): Int {
    return reviewCount
}

fun Course.nextLessonTime(): String {
    return when ((id - 1).floorMod(4)) {
        0 -> "10:00"
        1 -> "13:30"
        2 -> "16:00"
        else -> "18:30"
    }
}

fun Course.reviewHighlights(): List<CourseReview> {
    return when ((id - 1).floorMod(4)) {
        0 -> listOf(
            CourseReview(
                author = "Анна П.",
                role = "Студент",
                text = "Понравилось, что материал сразу закрепляется практикой. После курса стало проще читать Android-код.",
                rating = "5.0"
            ),
            CourseReview(
                author = "Михаил К.",
                role = "Начинающий разработчик",
                text = "Хороший темп и понятные объяснения. Особенно полезными были задания по коллекциям и корутинам.",
                rating = "4.8"
            )
        )
        1 -> listOf(
            CourseReview(
                author = "София Л.",
                role = "UI-дизайнер",
                text = "Compose показали без лишней воды: состояние, навигация и реальные экраны в одном проекте.",
                rating = "4.9"
            ),
            CourseReview(
                author = "Денис Р.",
                role = "Android-разработчик",
                text = "Много внимания к аккуратному UI и компонентам. Отличная база для собственного приложения.",
                rating = "4.8"
            )
        )
        2 -> listOf(
            CourseReview(
                author = "Ирина С.",
                role = "Backend-студент",
                text = "Ktor стал понятнее: маршруты, JWT и PostgreSQL собрали в работающий сервер.",
                rating = "4.9"
            ),
            CourseReview(
                author = "Олег М.",
                role = "Java-разработчик",
                text = "Нравится, что курс не только про API, но и про обработку ошибок и структуру проекта.",
                rating = "4.7"
            )
        )
        else -> listOf(
            CourseReview(
                author = "Кристина В.",
                role = "Mobile developer",
                text = "После курса стало понятнее, зачем разделять data, domain и presentation.",
                rating = "5.0"
            ),
            CourseReview(
                author = "Артем Н.",
                role = "Студент",
                text = "Use case-слой и репозитории разобраны на примерах, которые можно перенести в свой проект.",
                rating = "4.8"
            )
        )
    }
}

data class CourseReview(
    val author: String,
    val role: String,
    val text: String,
    val rating: String
)

private fun Int.floorMod(other: Int): Int = Math.floorMod(this, other)
