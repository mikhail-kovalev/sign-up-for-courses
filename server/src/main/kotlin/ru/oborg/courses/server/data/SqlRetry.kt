package ru.oborg.courses.server.data

import java.sql.SQLException

fun <T> withSqlRetry(operation: String, attempts: Int = 3, block: () -> T): T {
    var lastError: SQLException? = null
    repeat(attempts) { index ->
        try {
            return block()
        } catch (error: SQLException) {
            lastError = error
            if (index < attempts - 1) {
                Thread.sleep(300L * (index + 1))
            }
        }
    }

    throw lastError ?: SQLException("SQL operation failed: $operation")
}

