package com.TMDAD_2024.security.controllers

import com.TMDAD_2024.message.MessageRepository
import com.TMDAD_2024.room.Room
import com.TMDAD_2024.security.jwt.JwtUtils
import com.TMDAD_2024.security.services.UserDetailsImpl
import com.TMDAD_2024.user.*
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
import java.sql.Timestamp
import java.util.stream.Collectors

@CrossOrigin(origins = ["http://localhost:3000", "https://tmdad2024front-6457f4860338.herokuapp.com"], maxAge = 3600, allowCredentials = "true")
@RestController
@RequestMapping("/api/auth")
class AuthController(
    @Autowired private val authenticationManager: AuthenticationManager,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val messageRepository: MessageRepository,
    @Autowired private val userRoomRepository: UserRoomRepository,
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
                           var lastSignIn: Timestamp?, var rooms: List<Room>, var token: String)

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

        //Añadimos a cada una de las rooms del user, el ultimo mensaje escrito en esa room
        for(r in userDetails.rooms)
        {
            r.lastMessage = r.id?.let { messageRepository.findLastMessageByRoomId(it) }
            if(r.lastMessage != null)
                r.lastMessageTime = r.lastMessage!!.timeSent!!
            else
                r.lastMessageTime = r.createdAt

            //Traer el ultimpo tiempo de acceso a esta room por el user
            val userRoom = userRoomRepository.findById(UserRoomKey(userDetails.id, r.id))
            r.userLastAccess = userRoom.get().lastAccess
        }

        //Actualizamos ultima hora de signin del user
        userRepository.updateLastSignInById(userDetails.id, Timestamp(System.currentTimeMillis()))

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
            .body(
                UserInfoResponse(
                    userDetails.id,
                    userDetails.username,
                    userDetails.name,
                    userDetails.isSuperuser,
                    userDetails.lastSignIn,
                    userDetails.rooms,
                    jwtCookie.toString()
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