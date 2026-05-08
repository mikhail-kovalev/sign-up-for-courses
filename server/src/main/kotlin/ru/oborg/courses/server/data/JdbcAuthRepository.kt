package ru.oborg.courses.server.data

import ru.oborg.courses.server.domain.model.AuthUser
import ru.oborg.courses.server.domain.repository.AuthRepository
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import javax.sql.DataSource

class JdbcAuthRepository(
    private val dataSource: DataSource
) : AuthRepository {
    override fun createUser(fullName: String, email: String, passwordHash: String, role: String): AuthUser? {
        return try {
            withSqlRetry(operation = "create user") {
                dataSource.connection.use { connection ->
                    connection.prepareStatement(
                        """
                        INSERT INTO users (full_name, email, password_hash, role)
                        VALUES (?, ?, ?, ?)
                        """.trimIndent(),
                        Statement.RETURN_GENERATED_KEYS
                    ).use { statement ->
                        statement.setString(1, fullName)
                        statement.setString(2, email)
                        statement.setString(3, passwordHash)
                        statement.setString(4, role)
                        statement.executeUpdate()

                        statement.generatedKeys.use { keys ->
                            if (keys.next()) {
                                AuthUser(
                                    id = keys.getInt(1),
                                    fullName = fullName,
                                    email = email,
                                    passwordHash = passwordHash,
                                    role = role
                                )
                            } else {
                                null
                            }
                        }
                    }
                }
            }
        } catch (error: SQLException) {
            if (error.sqlState == UNIQUE_VIOLATION) null else throw error
        }
    }

    override fun findByEmail(email: String): AuthUser? {
        return withSqlRetry(operation = "find user by email") {
            dataSource.connection.use { connection ->
                connection.prepareStatement(
                    """
                    SELECT id, full_name, email, password_hash, role
                    FROM users
                    WHERE email = ?
                    """.trimIndent()
                ).use { statement ->
                    statement.setString(1, email)
                    statement.executeQuery().use { resultSet ->
                        if (resultSet.next()) resultSet.toAuthUser() else null
                    }
                }
            }
        }
    }

    override fun findById(id: Int): AuthUser? {
        return withSqlRetry(operation = "find user by id") {
            dataSource.connection.use { connection ->
                connection.prepareStatement(
                    """
                    SELECT id, full_name, email, password_hash, role
                    FROM users
                    WHERE id = ?
                    """.trimIndent()
                ).use { statement ->
                    statement.setInt(1, id)
                    statement.executeQuery().use { resultSet ->
                        if (resultSet.next()) resultSet.toAuthUser() else null
                    }
                }
            }
        }
    }

    override fun updatePasswordHash(userId: Int, passwordHash: String): Boolean {
        return withSqlRetry(operation = "update user password") {
            dataSource.connection.use { connection ->
                connection.prepareStatement(
                    """
                    UPDATE users
                    SET password_hash = ?
                    WHERE id = ?
                    """.trimIndent()
                ).use { statement ->
                    statement.setString(1, passwordHash)
                    statement.setInt(2, userId)
                    statement.executeUpdate() == 1
                }
            }
        }
    }

    override fun createSupportRequest(userId: Int, message: String): Boolean {
        return withSqlRetry(operation = "create support request") {
            dataSource.connection.use { connection ->
                connection.prepareStatement(
                    """
                    INSERT INTO support_requests (user_id, message)
                    VALUES (?, ?)
                    """.trimIndent()
                ).use { statement ->
                    statement.setInt(1, userId)
                    statement.setString(2, message)
                    statement.executeUpdate() == 1
                }
            }
        }
    }

    private fun ResultSet.toAuthUser(): AuthUser {
        return AuthUser(
            id = getInt("id"),
            fullName = getString("full_name"),
            email = getString("email"),
            passwordHash = getString("password_hash"),
            role = getString("role")
        )
    }

    private companion object {
        const val UNIQUE_VIOLATION = "23505"
    }
}
