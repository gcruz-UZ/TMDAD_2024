package com.TMDAD_2024.room

import com.TMDAD_2024.message.MessageRepository
import com.TMDAD_2024.message.Message
import com.TMDAD_2024.user.User
import com.TMDAD_2024.user.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import kotlin.jvm.optionals.toList

@RestController
@RequestMapping("/api/rooms")
class RoomController(
    @Autowired private val roomRepository: RoomRepository,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val messageRepository: MessageRepository
){
    data class PostRoom (
        var name: String,
        var users: List<String>
    )

    //get all rooms
    @GetMapping("")
    fun getAllRooms(): List<Room> =
        roomRepository.findAll().toList()

    //create room
    @PostMapping("")
    fun createRoom(@RequestBody room: PostRoom): ResponseEntity<Room> {
        //Controlar que al menos llega un usuario
        if(room.users.isEmpty())
            return ResponseEntity(HttpStatus.BAD_REQUEST)

        //Comprobar que los usuarios que nos llegan, existen
        val users: MutableList<User> = mutableListOf()
        for (login in room.users)
        {
            val user = userRepository.findByLogin(login).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND)
            }
            users.add(user)
        }

        //Guardamos en BBDD la nueva room
        val savedRoom = roomRepository.save(Room(room.name))

        //Le asignamos la nueva room a los usuarios
        for (user in users)
        {
            userRepository.save(
                user.copy(rooms = user.rooms.plus(savedRoom))
            )
        }

        //Devolvemos
        return ResponseEntity(savedRoom, HttpStatus.CREATED)
    }

    //get room by id
    @GetMapping("/{id}")
    fun getRoomById(@PathVariable("id") roomId: Int): ResponseEntity<Room> {
        val room = roomRepository.findById(roomId).orElse(null)
        return if (room != null) {
            ResponseEntity(room, HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

    //update room
    @PutMapping("/{id}")
    fun updateRoomById(@PathVariable("id") roomId: Int, @RequestBody room: Room): ResponseEntity<Room> {
        val existingRoom = roomRepository.findById(roomId).orElse(null)

        if (existingRoom == null){
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }

        val updatedRoom = existingRoom.copy(name = room.name)
        roomRepository.save(updatedRoom)
        return ResponseEntity(updatedRoom, HttpStatus.OK)
    }

    //get messages by room id
    @CrossOrigin(origins = ["http://localhost:3000"])
    @GetMapping("/{id}/messages")
    fun getMessagesByRoomId(@PathVariable("id") roomId: Int): List<Message> {
        val room = roomRepository.findById(roomId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND)
        }

        return messageRepository.findByRoomId(roomId)
    }

//    //delete user
//    @DeleteMapping("/{id}")
//    fun deletedUSerById(@PathVariable("id") userId: Int): ResponseEntity<User> {
//        if (!userRepository.existsById(userId)){
//            return ResponseEntity(HttpStatus.NOT_FOUND)
//        }
//
//        userRepository.deleteById(userId)
//        return ResponseEntity(HttpStatus.NO_CONTENT)
//    }
}