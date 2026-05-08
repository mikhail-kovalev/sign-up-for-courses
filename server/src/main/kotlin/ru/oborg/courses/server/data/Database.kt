package ru.oborg.courses.server.data

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

object Database {
    fun createDataSource(): HikariDataSource {
        val config = HikariConfig().apply {
            jdbcUrl = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/oborg_courses"
            username = System.getenv("DB_USER") ?: "oborg"
            password = System.getenv("DB_PASSWORD") ?: "oborg"
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 5
            minimumIdle = 1
            connectionTimeout = 10_000
            validationTimeout = 3_000
            idleTimeout = 120_000
            maxLifetime = 300_000
            connectionTestQuery = "SELECT 1"
            poolName = "oborg-courses-pool"
        }

        return HikariDataSource(config)
    }
}

class DatabaseMigrator(
    private val dataSource: DataSource
) {
    fun migrate() {
        listOf(
            """
            CREATE TABLE IF NOT EXISTS users (
                id SERIAL PRIMARY KEY,
                full_name TEXT NOT NULL,
                email TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                role TEXT NOT NULL DEFAULT 'student',
                created_at TIMESTAMPTZ NOT NULL DEFAULT now()
            )
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS courses (
                id SERIAL PRIMARY KEY,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                teacher TEXT NOT NULL,
                duration_hours INTEGER NOT NULL,
                price_rub INTEGER NOT NULL,
                level TEXT NOT NULL,
                starts_at DATE NOT NULL,
                seats_total INTEGER NOT NULL,
                created_at TIMESTAMPTZ NOT NULL DEFAULT now()
            )
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS enrollments (
                id SERIAL PRIMARY KEY,
                user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                course_id INTEGER NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
                created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                UNIQUE (user_id, course_id)
            )
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS course_reviews (
                id SERIAL PRIMARY KEY,
                course_id INTEGER NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
                user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
                text TEXT NOT NULL,
                created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                UNIQUE (user_id, course_id)
            )
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS support_requests (
                id SERIAL PRIMARY KEY,
                user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                message TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'new',
                created_at TIMESTAMPTZ NOT NULL DEFAULT now()
            )
            """.trimIndent()
        ).forEach { sql ->
            withSqlRetry(operation = "migration") {
                dataSource.connection.use { connection ->
                    connection.createStatement().use { statement ->
                        statement.execute(sql)
                    }
                }
            }
        }
    }
}
