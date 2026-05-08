package ru.oborg.courses.server.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

object JwtConfig {
    const val issuer = "oborg-courses-api"
    const val audience = "oborg-courses-mobile"
    const val realm = "oborg-courses"
    const val expiresInMinutes = 120

    private val secret = System.getenv("JWT_SECRET") ?: "oborg-courses-local-dev-secret-2026"
    private val algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun generateToken(userId: Int, email: String, role: String): String {
        val expiresAt = Date(System.currentTimeMillis() + expiresInMinutes * 60_000L)
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("userId", userId)
            .withClaim("email", email)
            .withClaim("role", role)
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }
}

