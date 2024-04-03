package com.TMDAD_2024

import com.TMDAD_2024.user.User
import com.TMDAD_2024.user.UserRepository
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/login")
class Controller(@Autowired private val userRepository: UserRepository){

    data class Login (
        var username: String,
        var password: String
    )

    //check login for user
    @CrossOrigin(origins = ["http://localhost:3000"])
    @PostMapping("")
    fun login(@RequestBody login: Login): ResponseEntity<User> {
        val user = userRepository.findByLogin(login.username).orElse(null)

        return if (user != null) {
            ResponseEntity(user, HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }
}