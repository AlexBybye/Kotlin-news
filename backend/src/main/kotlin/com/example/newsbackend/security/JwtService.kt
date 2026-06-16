package com.example.newsbackend.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

/**
 * JWT 令牌签发与校验。
 */
class JwtService(
    private val secret: String,
    private val issuer: String,
    private val audience: String,
    private val expiresInMs: Long
) {
    private val algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun generateToken(username: String): String =
        JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim(CLAIM_USERNAME, username)
            .withExpiresAt(Date(System.currentTimeMillis() + expiresInMs))
            .sign(algorithm)

    companion object {
        const val CLAIM_USERNAME = "username"
    }
}
