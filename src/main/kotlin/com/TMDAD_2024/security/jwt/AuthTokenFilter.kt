package com.TMDAD_2024.security.jwt

import com.TMDAD_2024.security.services.UserDetailsServiceImpl
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

class AuthTokenFilter : OncePerRequestFilter() {

    @Autowired
    private val jwtUtils: JwtUtils? = null

    @Autowired
    private val userDetailsService: UserDetailsServiceImpl? = null
    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        try {
            val jwt = parseJwt(request)
            Companion.logger.error("HOLA")
            if (jwt != null && jwtUtils?.validateJwtToken(jwt) == true) {
                val username: String = jwtUtils.getUserNameFromJwtToken(jwt)
                val userDetails = userDetailsService!!.loadUserByUsername(username)

                Companion.logger.error("HOLA 2", userDetails.authorities)
                Companion.logger.error(userDetails.authorities.toString())
                val authentication =
                    UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.authorities
                    )

                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (e: Exception) {
            Companion.logger.error("Cannot set user authentication: {}", e)
        }

        filterChain.doFilter(request, response)
    }

    private fun parseJwt(request: HttpServletRequest): String {
        val jwt: String = jwtUtils?.getJwtFromCookies(request).toString()
        return jwt
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(AuthTokenFilter::class.java)
    }
}