package com.example.newsbackend

import com.example.newsbackend.security.JwtService
import com.auth0.jwt.exceptions.JWTVerificationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JwtServiceTest {

    private val jwtService = JwtService(
        secret = "test-secret",
        issuer = "test-issuer",
        audience = "test-audience",
        expiresInMs = 60_000
    )

    @Test
    fun generatedTokenCarriesUsernameAndVerifies() {
        val token = jwtService.generateToken("scut2026")
        val decoded = jwtService.verifier.verify(token)
        assertEquals("scut2026", decoded.getClaim(JwtService.CLAIM_USERNAME).asString())
    }

    @Test
    fun tokenSignedWithDifferentSecretFailsVerification() {
        val other = JwtService("another-secret", "test-issuer", "test-audience", 60_000)
        val token = other.generateToken("scut2026")
        assertFailsWith<JWTVerificationException> {
            jwtService.verifier.verify(token)
        }
    }
}
