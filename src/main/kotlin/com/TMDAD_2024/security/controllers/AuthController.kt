package com.TMDAD_2024.security.controllers

import com.TMDAD_2024.room.Room
import com.TMDAD_2024.security.jwt.JwtUtils
import com.TMDAD_2024.security.services.UserDetailsImpl
import com.TMDAD_2024.user.User
import com.TMDAD_2024.user.UserRepository
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import java.util.stream.Collectors

@CrossOrigin(origins = ["http://localhost:3000"], maxAge = 3600, allowCredentials = "true")
@RestController
@RequestMapping("/api/auth")
class AuthController(
    @Autowired private val authenticationManager: AuthenticationManager,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val encoder: PasswordEncoder,
    @Autowired private val jwtUtils: JwtUtils
) {
    class SignUpRequest {
        @NotBlank
        @Size(min = 3, max = 20)
        lateinit var username: String

        @NotBlank
        @Size(min = 6, max = 40)
        lateinit var password: String
    }

    class LoginRequest {
        @NotBlank
        lateinit var username: String

        @NotBlank
        lateinit var password: String
    }

    class MessageResponse(var message: String)

    class UserInfoResponse(var id: Int?, var login: String, var name: String, var isSuperUser: Boolean,
        var rooms: List<Room>)

    @PostMapping("/signup")
    fun registerUser(@RequestBody signUpRequest: @Valid SignUpRequest): ResponseEntity<*> {
        if (userRepository.existsByLogin(signUpRequest.username)) {
            return ResponseEntity.badRequest().body(MessageResponse("Error: Username is already taken!"))
        }

        // Create new user's account
        val user = User(
            signUpRequest.username,
            encoder.encode(signUpRequest.password)
        )
        userRepository.save(user)
        return ResponseEntity.ok(MessageResponse("User registered successfully!"))
    }

    @PostMapping("/signin")
    fun authenticateUser(@RequestBody loginRequest: @Valid LoginRequest): ResponseEntity<*> {
        val authentication = authenticationManager
            .authenticate(UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password))

        SecurityContextHolder.getContext().authentication = authentication

        val userDetails = authentication.principal as UserDetailsImpl
        val jwtCookie = jwtUtils.generateJwtCookie(userDetails)

//        val roles = userDetails.authorities.stream()
//            .map { item: GrantedAuthority -> item.authority }
//            .collect(Collectors.toList())
//
//        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
//            .body(
//                UserInfoResponse(
//                    userDetails.id,
//                    userDetails.username,
//                    roles
//                )
//            )

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
            .body(
                UserInfoResponse(
                    userDetails.id,
                    userDetails.username,
                    userDetails.name,
                    userDetails.isSuperuser,
                    userDetails.rooms
                )
            )
    }

    @PostMapping("/signout")
    fun logoutUser(): ResponseEntity<*> {
        val cookie = jwtUtils.cleanJwtCookie
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(MessageResponse("You've been signed out!"))
    }
}