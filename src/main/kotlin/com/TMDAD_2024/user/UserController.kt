package com.TMDAD_2024.user

import com.TMDAD_2024.room.RoomRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.sql.Timestamp

@RestController
@RequestMapping("/api/users")
class UserController(@Autowired private val userRepository: UserRepository,
                     @Autowired private val roomRepository: RoomRepository,
                     @Autowired private val userRoomRepository: UserRoomRepository)
{
    //get all users
    @CrossOrigin(origins = ["http://localhost:3000", "https://tmdad2024front-6457f4860338.herokuapp.com"], allowCredentials = "true")
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

    @CrossOrigin(origins = ["http://localhost:3000", "https://tmdad2024front-6457f4860338.herokuapp.com"], allowCredentials = "true")
    @PutMapping("/{userId}/room/{roomId}/lastAccess")
    fun updateUserLastAccessToRoom(@PathVariable("userId") userId: Int, @PathVariable("roomId") roomId: Int): ResponseEntity<User> {
        println("UPDATING LAST ACCESS")
        println("userId: ${userId}, roomId: ${roomId}")
        //Buscamos el usuario. Si no existe, devolvemos 404
        val existingUser = userRepository.findById(userId).orElse(null)
            ?: return ResponseEntity(HttpStatus.NOT_FOUND)

        //Buscamos la room. Si no existe, devolvemos 404
        val existingRoom = roomRepository.findById(roomId).orElse(null)
            ?: return ResponseEntity(HttpStatus.NOT_FOUND)

        //Buscamos la user-room. Si no existe, devolvemos 404
        val existingUserRoom = userRoomRepository.findById(UserRoomKey(userId, roomId)).orElse(null)
            ?: return ResponseEntity(HttpStatus.NOT_FOUND)

        //Modificamos el usuario, lo actualizamos en BBDD y devolvemos OK
        val updatedUserRoom = existingUserRoom.copy(lastAccess = Timestamp(System.currentTimeMillis()))
        userRoomRepository.save(updatedUserRoom)
        return ResponseEntity(existingUser, HttpStatus.OK)
    }

    //delete user
    @DeleteMapping("/{id}")
    fun deletedUSerById(@PathVariable("id") userId: Int): ResponseEntity<*> {
        //Si no existe devolvemos 404
        if (!userRepository.existsById(userId)){
            return ResponseEntity("User with ID ${userId} not found", HttpStatus.NOT_FOUND)
        }

        //Comprobamos que no tenga rooms
        if(userRoomRepository.existsByUserId(userId))
        {
            return ResponseEntity("User with ID ${userId} still has active rooms", HttpStatus.BAD_REQUEST)
        }

        //Lo eliminamos de BBDD y devolvemos OK
        userRepository.deleteById(userId)
        return ResponseEntity("User with ID ${userId} deleted", HttpStatus.OK)
    }
}