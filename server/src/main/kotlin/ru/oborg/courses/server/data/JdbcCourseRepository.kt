package ru.oborg.courses.server.data

import ru.oborg.courses.server.domain.model.Course
import ru.oborg.courses.server.domain.model.Enrollment
import ru.oborg.courses.server.domain.model.CourseReview
import ru.oborg.courses.server.domain.repository.CourseRepository
import ru.oborg.courses.server.domain.repository.CreateReviewResult
import ru.oborg.courses.server.domain.repository.EnrollCourseResult
import java.sql.Connection
import java.sql.Date
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.time.LocalDate
import javax.sql.DataSource

class JdbcCourseRepository(
    private val dataSource: DataSource
) : CourseRepository {
    override fun findAll(): List<Course> {
        return withSqlRetry(operation = "find all courses") {
            dataSource.connection.use { connection ->
                connection.createStatement().use { statement ->
                    statement.executeQuery(COURSE_SELECT + " ORDER BY c.starts_at, c.title").use { resultSet ->
                        buildList {
                            while (resultSet.next()) {
                                add(resultSet.toCourse())
                            }
                        }
                    }
                }
            }
        }
    }

    override fun findById(id: Int): Course? {
        return withSqlRetry(operation = "find course by id") {
            dataSource.connection.use { connection ->
                connection.prepareStatement(COURSE_SELECT + " HAVING c.id = ?").use { statement ->
                    statement.setInt(1, id)
                    statement.executeQuery().use { resultSet ->
                        if (resultSet.next()) resultSet.toCourse() else null
                    }
                }
            }
        }
    }

    override fun findEnrollmentsByUser(userId: Int): List<Enrollment> {
        return withSqlRetry(operation = "find enrollments by user") {
            dataSource.connection.use { connection ->
                connection.prepareStatement(
                    """
                    SELECT e.id AS enrollment_id,
                           e.created_at AS enrollment_created_at,
                           c.id,
                           c.title,
                           c.description,
                           c.teacher,
                           c.duration_hours,
                           c.price_rub,
                           c.level,
                           c.starts_at,
                           c.seats_total,
                           COUNT(all_e.id) AS seats_busy
                    FROM enrollments e
                    JOIN courses c ON c.id = e.course_id
                    LEFT JOIN enrollments all_e ON all_e.course_id = c.id
                    WHERE e.user_id = ?
                    GROUP BY e.id, e.created_at, c.id, c.title, c.description, c.teacher,
                             c.duration_hours, c.price_rub, c.level, c.starts_at, c.seats_total
                    ORDER BY e.created_at DESC
                    """.trimIndent()
                ).use { statement ->
                    statement.setInt(1, userId)
                    statement.executeQuery().use { resultSet ->
                        buildList {
                            while (resultSet.next()) {
                                add(resultSet.toEnrollment())
                            }
                        }
                    }
                }
            }
        }
    }

    override fun enroll(userId: Int, courseId: Int): EnrollCourseResult {
        return withSqlRetry(operation = "enroll user") {
            dataSource.connection.use { connection ->
                connection.autoCommit = false
                try {
                    if (connection.hasEnrollment(userId, courseId)) {
                        connection.rollback()
                        return@withSqlRetry EnrollCourseResult.AlreadyEnrolled
                    }

                    val seatInfo = connection.findSeatInfo(courseId)
                    if (seatInfo == null) {
                        connection.rollback()
                        return@withSqlRetry EnrollCourseResult.CourseNotFound
                    }

                    if (seatInfo.busy >= seatInfo.total) {
                        connection.rollback()
                        return@withSqlRetry EnrollCourseResult.NoSeats
                    }

                    val enrollmentId = connection.prepareStatement(
                        """
                        INSERT INTO enrollments (user_id, course_id)
                        VALUES (?, ?)
                        """.trimIndent(),
                        Statement.RETURN_GENERATED_KEYS
                    ).use { statement ->
                        statement.setInt(1, userId)
                        statement.setInt(2, courseId)
                        statement.executeUpdate()
                        statement.generatedKeys.use { keys ->
                            check(keys.next()) { "Enrollment id was not generated" }
                            keys.getInt(1)
                        }
                    }

                    connection.commit()
                    val enrollment = findEnrollmentById(enrollmentId)
                        ?: error("Created enrollment was not found")
                    EnrollCourseResult.Enrolled(enrollment)
                } catch (error: Throwable) {
                    connection.rollback()
                    throw error
                } finally {
                    connection.autoCommit = true
                }
            }
        }
    }

    override fun findReviewsByCourse(courseId: Int): List<CourseReview> {
        return withSqlRetry(operation = "find course reviews") {
            dataSource.connection.use { connection ->
                connection.prepareStatement(REVIEW_SELECT + " WHERE r.course_id = ? ORDER BY r.created_at DESC").use { statement ->
                    statement.setInt(1, courseId)
                    statement.executeQuery().use { resultSet ->
                        buildList {
                            while (resultSet.next()) {
                                add(resultSet.toCourseReview())
                            }
                        }
                    }
                }
            }
        }
    }

    override fun createReview(userId: Int, courseId: Int, rating: Int, text: String): CreateReviewResult {
        return withSqlRetry(operation = "create course review") {
            dataSource.connection.use { connection ->
                connection.autoCommit = false
                try {
                    if (!connection.courseExists(courseId)) {
                        connection.rollback()
                        return@withSqlRetry CreateReviewResult.CourseNotFound
                    }

                    if (!connection.hasEnrollment(userId, courseId)) {
                        connection.rollback()
                        return@withSqlRetry CreateReviewResult.NotEnrolled
                    }

                    if (connection.hasReview(userId, courseId)) {
                        connection.rollback()
                        return@withSqlRetry CreateReviewResult.AlreadyReviewed
                    }

                    val reviewId = connection.prepareStatement(
                        """
                        INSERT INTO course_reviews (course_id, user_id, rating, text)
                        VALUES (?, ?, ?, ?)
                        """.trimIndent(),
                        Statement.RETURN_GENERATED_KEYS
                    ).use { statement ->
                        statement.setInt(1, courseId)
                        statement.setInt(2, userId)
                        statement.setInt(3, rating)
                        statement.setString(4, text)
                        statement.executeUpdate()
                        statement.generatedKeys.use { keys ->
                            check(keys.next()) { "Review id was not generated" }
                            keys.getInt(1)
                        }
                    }

                    connection.commit()
                    CreateReviewResult.Created(
                        findReviewById(reviewId) ?: error("Created review was not found")
                    )
                } catch (error: Throwable) {
                    connection.rollback()
                    if (error is SQLException && error.sqlState == UNIQUE_VIOLATION) {
                        CreateReviewResult.AlreadyReviewed
                    } else {
                        throw error
                    }
                } finally {
                    connection.autoCommit = true
                }
            }
        }
    }

    fun insertSeedCourse(seedCourse: SeedCourse) {
        withSqlRetry(operation = "insert seed course") {
            dataSource.connection.use { connection ->
                val existingCourseId = connection.findCourseIdByTitle(seedCourse.title)
                if (existingCourseId != null) {
                    connection.prepareStatement(
                        """
                        UPDATE courses
                        SET description = ?,
                            teacher = ?,
                            duration_hours = ?,
                            price_rub = ?,
                            level = ?,
                            starts_at = ?,
                            seats_total = ?
                        WHERE id = ?
                        """.trimIndent()
                    ).use { statement ->
                        statement.setString(1, seedCourse.description)
                        statement.setString(2, seedCourse.teacher)
                        statement.setInt(3, seedCourse.durationHours)
                        statement.setInt(4, seedCourse.priceRub)
                        statement.setString(5, seedCourse.level)
                        statement.setDate(6, Date.valueOf(seedCourse.startsAt))
                        statement.setInt(7, seedCourse.seatsTotal)
                        statement.setInt(8, existingCourseId)
                        statement.executeUpdate()
                    }
                    return@use
                }

                connection.prepareStatement(
                    """
                    INSERT INTO courses (
                        title, description, teacher, duration_hours, price_rub,
                        level, starts_at, seats_total
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent()
                ).use { statement ->
                    statement.setString(1, seedCourse.title)
                    statement.setString(2, seedCourse.description)
                    statement.setString(3, seedCourse.teacher)
                    statement.setInt(4, seedCourse.durationHours)
                    statement.setInt(5, seedCourse.priceRub)
                    statement.setString(6, seedCourse.level)
                    statement.setDate(7, Date.valueOf(seedCourse.startsAt))
                    statement.setInt(8, seedCourse.seatsTotal)
                    statement.executeUpdate()
                }
            }
        }
    }

    fun insertSeedReview(userId: Int, courseTitle: String, rating: Int, text: String) {
        withSqlRetry(operation = "insert seed review") {
            dataSource.connection.use { connection ->
                val courseId = connection.findCourseIdByTitle(courseTitle) ?: return@use

                connection.prepareStatement(
                    """
                    INSERT INTO enrollments (user_id, course_id)
                    VALUES (?, ?)
                    ON CONFLICT (user_id, course_id) DO NOTHING
                    """.trimIndent()
                ).use { statement ->
                    statement.setInt(1, userId)
                    statement.setInt(2, courseId)
                    statement.executeUpdate()
                }

                connection.prepareStatement(
                    """
                    INSERT INTO course_reviews (course_id, user_id, rating, text)
                    VALUES (?, ?, ?, ?)
                    ON CONFLICT (user_id, course_id) DO NOTHING
                    """.trimIndent()
                ).use { statement ->
                    statement.setInt(1, courseId)
                    statement.setInt(2, userId)
                    statement.setInt(3, rating)
                    statement.setString(4, text)
                    statement.executeUpdate()
                }
            }
        }
    }

    private fun Connection.findCourseIdByTitle(title: String): Int? {
        return prepareStatement(
            """
            SELECT id
            FROM courses
            WHERE title = ?
            """.trimIndent()
        ).use { statement ->
            statement.setString(1, title)
            statement.executeQuery().use { resultSet ->
                if (resultSet.next()) resultSet.getInt("id") else null
            }
        }
    }

    fun countCourses(): Int {
        return withSqlRetry(operation = "count courses") {
            dataSource.connection.use { connection ->
                connection.createStatement().use { statement ->
                    statement.executeQuery("SELECT COUNT(*) FROM courses").use { resultSet ->
                        resultSet.next()
                        resultSet.getInt(1)
                    }
                }
            }
        }
    }

    private fun findEnrollmentById(enrollmentId: Int): Enrollment? {
        return dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT e.id AS enrollment_id,
                       e.created_at AS enrollment_created_at,
                       c.id,
                       c.title,
                       c.description,
                       c.teacher,
                       c.duration_hours,
                       c.price_rub,
                       c.level,
                       c.starts_at,
                       c.seats_total,
                       COUNT(all_e.id) AS seats_busy
                FROM enrollments e
                JOIN courses c ON c.id = e.course_id
                LEFT JOIN enrollments all_e ON all_e.course_id = c.id
                WHERE e.id = ?
                GROUP BY e.id, e.created_at, c.id, c.title, c.description, c.teacher,
                         c.duration_hours, c.price_rub, c.level, c.starts_at, c.seats_total
                """.trimIndent()
            ).use { statement ->
                statement.setInt(1, enrollmentId)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) resultSet.toEnrollment() else null
                }
            }
        }
    }

    private fun findReviewById(reviewId: Int): CourseReview? {
        return dataSource.connection.use { connection ->
            connection.prepareStatement(REVIEW_SELECT + " WHERE r.id = ?").use { statement ->
                statement.setInt(1, reviewId)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) resultSet.toCourseReview() else null
                }
            }
        }
    }

    private fun Connection.courseExists(courseId: Int): Boolean {
        return prepareStatement(
            """
            SELECT 1
            FROM courses
            WHERE id = ?
            """.trimIndent()
        ).use { statement ->
            statement.setInt(1, courseId)
            statement.executeQuery().use { resultSet -> resultSet.next() }
        }
    }

    private fun Connection.hasEnrollment(userId: Int, courseId: Int): Boolean {
        return prepareStatement(
            """
            SELECT 1
            FROM enrollments
            WHERE user_id = ? AND course_id = ?
            """.trimIndent()
        ).use { statement ->
            statement.setInt(1, userId)
            statement.setInt(2, courseId)
            statement.executeQuery().use { resultSet -> resultSet.next() }
        }
    }

    private fun Connection.hasReview(userId: Int, courseId: Int): Boolean {
        return prepareStatement(
            """
            SELECT 1
            FROM course_reviews
            WHERE user_id = ? AND course_id = ?
            """.trimIndent()
        ).use { statement ->
            statement.setInt(1, userId)
            statement.setInt(2, courseId)
            statement.executeQuery().use { resultSet -> resultSet.next() }
        }
    }

    private fun Connection.findSeatInfo(courseId: Int): SeatInfo? {
        return prepareStatement(
            """
            SELECT c.seats_total, COUNT(e.id) AS seats_busy
            FROM courses c
            LEFT JOIN enrollments e ON e.course_id = c.id
            WHERE c.id = ?
            GROUP BY c.id, c.seats_total
            """.trimIndent()
        ).use { statement ->
            statement.setInt(1, courseId)
            statement.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    SeatInfo(
                        total = resultSet.getInt("seats_total"),
                        busy = resultSet.getInt("seats_busy")
                    )
                } else {
                    null
                }
            }
        }
    }

    private fun ResultSet.toEnrollment(): Enrollment {
        return Enrollment(
            id = getInt("enrollment_id"),
            course = toCourse(),
            createdAt = getTimestamp("enrollment_created_at").toInstant().toString()
        )
    }

    private fun ResultSet.toCourse(): Course {
        return Course(
            id = getInt("id"),
            title = getString("title"),
            description = getString("description"),
            teacher = getString("teacher"),
            durationHours = getInt("duration_hours"),
            priceRub = getInt("price_rub"),
            level = getString("level"),
            startsAt = getDate("starts_at").toLocalDate().toString(),
            seatsTotal = getInt("seats_total"),
            seatsBusy = getInt("seats_busy")
        )
    }

    private fun ResultSet.toCourseReview(): CourseReview {
        return CourseReview(
            id = getInt("review_id"),
            courseId = getInt("course_id"),
            userId = getInt("user_id"),
            authorName = getString("author_name"),
            rating = getInt("rating"),
            text = getString("review_text"),
            createdAt = getTimestamp("review_created_at").toInstant().toString()
        )
    }

    private data class SeatInfo(
        val total: Int,
        val busy: Int
    )

    private companion object {
        val COURSE_SELECT = """
            SELECT c.id,
                   c.title,
                   c.description,
                   c.teacher,
                   c.duration_hours,
                   c.price_rub,
                   c.level,
                   c.starts_at,
                   c.seats_total,
                   COUNT(e.id) AS seats_busy
            FROM courses c
            LEFT JOIN enrollments e ON e.course_id = c.id
            GROUP BY c.id, c.title, c.description, c.teacher,
                     c.duration_hours, c.price_rub, c.level, c.starts_at, c.seats_total
        """.trimIndent()
        val REVIEW_SELECT = """
            SELECT r.id AS review_id,
                   r.course_id,
                   r.user_id,
                   u.full_name AS author_name,
                   r.rating,
                   r.text AS review_text,
                   r.created_at AS review_created_at
            FROM course_reviews r
            JOIN users u ON u.id = r.user_id
        """.trimIndent()
        const val UNIQUE_VIOLATION = "23505"
    }
}

data class SeedCourse(
    val title: String,
    val description: String,
    val teacher: String,
    val durationHours: Int,
    val priceRub: Int,
    val level: String,
    val startsAt: LocalDate,
    val seatsTotal: Int
)
