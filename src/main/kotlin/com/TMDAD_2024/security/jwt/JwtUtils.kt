package com.TMDAD_2024.security.jwt

import com.TMDAD_2024.security.services.UserDetailsImpl
import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import org.springframework.web.util.WebUtils
import java.security.Key
import java.util.*

@Component
class JwtUtils {
    @Value("===========================SECRET=TMDAD=2024===========================")
    private val jwtSecret: String = ""

    @Value("86400000")
    private val jwtExpirationMs = 0

    @Value("jwt-tmdad-2024")
    private val jwtCookie: String = ""

    fun getJwtFromCookies(request: HttpServletRequest): String? {
        val cookie = WebUtils.getCookie(request, jwtCookie)
        return cookie?.value
    }

    fun generateJwtCookie(userPrincipal: UserDetailsImpl): ResponseCookie {
        val jwt = generateTokenFromUsername(userPrincipal.username)
        val cookie =
            ResponseCookie.from(jwtCookie, jwt).path("/api").maxAge((24 * 60 * 60).toLong()).httpOnly(true).build()
        return cookie
    }

    val cleanJwtCookie: ResponseCookie
        get() {
            val cookie = ResponseCookie.from(jwtCookie, null.toString()).path("/api").build()
            return cookie
        }

    fun getUserNameFromJwtToken(token: String): String {
        return Jwts.parserBuilder().setSigningKey(key()).build()
            .parseClaimsJws(token).body.subject
    }

    private fun key(): Key {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret))
    }

    fun generateTokenFromUsername(username: String): String {
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(Date())
            .setExpiration(Date(Date().time + jwtExpirationMs))
            .signWith(key(), SignatureAlgorithm.HS256)
            .compact()
    }

    fun validateJwtToken(authToken: String?): Boolean {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken)
            return true
        } catch (e: MalformedJwtException) {
            logger.error("Invalid JWT token: {}", e.message)
        } catch (e: ExpiredJwtException) {
            logger.error("JWT token is expired: {}", e.message)
        } catch (e: UnsupportedJwtException) {
            logger.error("JWT token is unsupported: {}", e.message)
        } catch (e: IllegalArgumentException) {
            logger.error("JWT claims string is empty: {}", e.message)
        }

        return false
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(JwtUtils::class.java)
    }
}