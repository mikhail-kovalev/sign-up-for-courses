package ru.oborg.courses.presentation.common

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val russianMonths = listOf(
    "января",
    "февраля",
    "марта",
    "апреля",
    "мая",
    "июня",
    "июля",
    "августа",
    "сентября",
    "октября",
    "ноября",
    "декабря"
)

private val russianMonthsTitle = listOf(
    "Январь",
    "Февраль",
    "Март",
    "Апрель",
    "Май",
    "Июнь",
    "Июль",
    "Август",
    "Сентябрь",
    "Октябрь",
    "Ноябрь",
    "Декабрь"
)

private val russianWeekDays = mapOf(
    Calendar.MONDAY to "Пн",
    Calendar.TUESDAY to "Вт",
    Calendar.WEDNESDAY to "Ср",
    Calendar.THURSDAY to "Чт",
    Calendar.FRIDAY to "Пт",
    Calendar.SATURDAY to "Сб",
    Calendar.SUNDAY to "Вс"
)

private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
    isLenient = false
}

fun humanCourseDate(value: String): String {
    val calendar = value.take(10).toCalendar() ?: return value.take(10)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val month = russianMonths[calendar.get(Calendar.MONTH)]
    val year = calendar.get(Calendar.YEAR)
    return "$day $month $year"
}

fun humanEnrollmentDate(value: String): String {
    val date = humanCourseDate(value)
    val time = value.substringAfter("T", missingDelimiterValue = "")
        .take(5)
        .takeIf { it.length == 5 && it[2] == ':' }

    return if (time == null) date else "$date в $time"
}

fun humanMonthYear(value: String): String {
    val calendar = value.take(10).toCalendar() ?: return "Расписание"
    val month = russianMonthsTitle[calendar.get(Calendar.MONTH)]
    return "$month, ${calendar.get(Calendar.YEAR)}"
}

fun dayOfMonth(value: String): String {
    val calendar = value.take(10).toCalendar() ?: return value.takeLast(2)
    return calendar.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
}

fun shortMonth(value: String): String {
    val calendar = value.take(10).toCalendar() ?: return ""
    return russianMonths[calendar.get(Calendar.MONTH)].take(3)
}

fun weekDay(value: String): String {
    val calendar = value.take(10).toCalendar() ?: return ""
    return russianWeekDays[calendar.get(Calendar.DAY_OF_WEEK)].orEmpty()
}

fun addDays(value: String, days: Int): String {
    val calendar = value.take(10).toCalendar() ?: return value
    calendar.add(Calendar.DAY_OF_YEAR, days)
    return apiDateFormat.format(calendar.time)
}

fun monthGridDates(value: String): List<String?> {
    val calendar = value.take(10).toCalendar() ?: return emptyList()
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOffset = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val dates = MutableList<String?>(firstDayOffset) { null }

    repeat(daysInMonth) { index ->
        calendar.set(Calendar.DAY_OF_MONTH, index + 1)
        dates += apiDateFormat.format(calendar.time)
    }

    while (dates.size % 7 != 0) {
        dates += null
    }

    return dates
}

private fun String.toCalendar(): Calendar? {
    return runCatching {
        Calendar.getInstance().apply {
            time = apiDateFormat.parse(this@toCalendar) ?: return null
        }
    }.getOrNull()
}
