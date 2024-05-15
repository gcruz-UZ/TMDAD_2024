package com.TMDAD_2024.user

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.ZoneOffset

@RestController
@RequestMapping("/api/users")
class UserController(@Autowired private val userRepository: UserRepository)
{
    //get all users
    @CrossOrigin(origins = ["http://localhost:3000"], allowCredentials = "true")
    @GetMapping("")
    fun getAllUsers(): List<User>
    {
        //Obtenemos usuarios
        val users = userRepository.findAll().toList()

        //Añadimos a cada una de las rooms de cada usuario los logins de usuario
        //que pertenecen a dicha room
        users.map { user ->
            user.rooms.map { room ->
                room.logins = room.users.map { it.login }
            }
        }

        //Devolvemos
        return users
    }

    //get user by id
    @GetMapping("/{id}")
    fun getUserById(@PathVariable("id") userId: Int): ResponseEntity<User> {
        //Obtenemos usuario
        val user = userRepository.findById(userId).orElse(null)

        //Si existe, informamos lista de logins de las rooms y devolvemos. Si no, devolvemos 404
        if(user != null)
        {
            user.rooms.map { room ->
                room.logins = room.users.map { it.login }
            }
            return ResponseEntity(user, HttpStatus.OK)
        }
        else
        {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

    //create user
    @PostMapping("")
    fun createUser(@RequestBody user: User): ResponseEntity<User> {
        //Guardamos en BBDD y devolvemos
        val savedUser = userRepository.save(user)
        return ResponseEntity(savedUser, HttpStatus.CREATED)
    }

    //update user
    @PutMapping("/{id}")
    fun updateUserById(@PathVariable("id") userId: Int, @RequestBody user: User): ResponseEntity<User> {
        //Buscamos el usuario. Si no existe, devolvemos 404
        val existingUser = userRepository.findById(userId).orElse(null)
            ?: return ResponseEntity(HttpStatus.NOT_FOUND)

        //Modificamos el usuario, lo actualizamos en BBDD y devolvemos OK
        val updatedUser = existingUser.copy(login = user.login, name = user.name, isSuperuser = user.isSuperuser)
        userRepository.save(updatedUser)
        return ResponseEntity(updatedUser, HttpStatus.OK)
    }

    //delete user
    @DeleteMapping("/{id}")
    fun deletedUSerById(@PathVariable("id") userId: Int): ResponseEntity<User> {
        //Si no existe devolvemos 404
        if (!userRepository.existsById(userId)){
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }

        //Lo eliminamos de BBDD y devolvemos OK
        userRepository.deleteById(userId)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}