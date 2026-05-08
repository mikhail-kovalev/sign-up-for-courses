package ru.oborg.courses.server.domain.model

data class CourseReview(
    val id: Int,
    val courseId: Int,
    val userId: Int,
    val authorName: String,
    val rating: Int,
    val text: String,
    val createdAt: String
)
