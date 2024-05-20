package com.TMDAD_2024.room

import com.TMDAD_2024.message.MessageRepository
import com.TMDAD_2024.message.Message
import com.TMDAD_2024.user.User
import com.TMDAD_2024.user.UserRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import com.TMDAD_2024.security.jwt.JwtUtils
import com.TMDAD_2024.security.services.UserDetailsImpl
import com.TMDAD_2024.security.services.UserDetailsServiceImpl
import jakarta.transaction.Transactional
import org.springframework.messaging.simp.SimpMessagingTemplate
import java.sql.Timestamp

@RestController
@RequestMapping("/api/rooms")
class RoomController(
    @Autowired private val roomRepository: RoomRepository,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val messageRepository: MessageRepository,
    @Autowired private val jwtUtils: JwtUtils,
    @Autowired private val userDetailsService: UserDetailsServiceImpl,
    @Autowired private val messagingTemplate: SimpMessagingTemplate,
){
    //Modelo de datos para crear una nueva room
    data class PostRoom (
        var name: String,
        var users: List<String>
    )

    //get all rooms
    @GetMapping("")
    fun getAllRooms(): List<Room>
    {
        //Obtenemos rooms e informamos la lista de logins de cada room
        val rooms = roomRepository.findAll().toList()
        rooms.map { room ->
            room.logins = room.users.map { user ->
                user.login
            }
        }

        //Devolvemos
        return rooms
    }

    @GetMapping("/publish")
    fun sendMessage(@RequestParam("message") message: String): ResponseEntity<String> {
        messagingTemplate.convertAndSend("/topic/stats", message)
        return ResponseEntity.ok("Message sent to STATS ...")
    }

    //get room by id
    @GetMapping("/{id}")
    fun getRoomById(@PathVariable("id") roomId: Int): ResponseEntity<Room> {
        //Obtenemos room
        val room = roomRepository.findById(roomId).orElse(null)

        //Si existe, informamos lista de logins y devolvemos. Si no, devolvemos 404
        if(room != null)
        {
            room.logins = room.users.map { it.login }
            return ResponseEntity(room, HttpStatus.OK)
        }
        else
        {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

    //create room
    @CrossOrigin(origins = ["http://localhost:3000", "https://tmdad2024front-6457f4860338.herokuapp.com"], allowCredentials = "true")
    @PostMapping("")
    fun createRoom(request: HttpServletRequest, @RequestBody room: PostRoom): ResponseEntity<Room> {
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

        //El moderador de la room es el usuario que realiza la llamada (JWT)
        val jwt = jwtUtils.getJwtFromCookies(request)
        val userName = jwtUtils.getUserNameFromJwtToken(jwt.toString())
        val userId = (userDetailsService.loadUserByUsername(userName) as UserDetailsImpl).id

        //Guardamos en BBDD la nueva room
        val savedRoom = roomRepository.save(Room(room.name, Timestamp(System.currentTimeMillis()),
            userId, users.map { it.login }))

        //Le asignamos la nueva room a los usuarios
        for (user in users)
        {
            userRepository.save(
                user.copy(rooms = user.rooms.plus(savedRoom))
            )

            //Enviamos nueva room a los users
            messagingTemplate.convertAndSend("/topic/rooms/${user.login}", savedRoom)
        }

        //Devolvemos
        return ResponseEntity(savedRoom, HttpStatus.CREATED)
    }

    @CrossOrigin(origins = ["http://localhost:3000", "https://tmdad2024front-6457f4860338.herokuapp.com"], allowCredentials = "true")
    @Throws(ResponseStatusException::class)
    @PutMapping("/{id}/user/{login}")
    fun addUserToRoom(request: HttpServletRequest,
                      @PathVariable("id") roomId: Int,
                      @PathVariable("login") login: String): ResponseEntity<*> {
        //Traemos la room. Si no existe, devolvemos 404
        val existingRoom = roomRepository.findById(roomId).orElse(null)
            ?: return ResponseEntity("Room $roomId not found", HttpStatus.NOT_FOUND)

        //Comprobamos que quien realiza la llamada es el moderador
        val jwt = jwtUtils.getJwtFromCookies(request)
        val userName = jwtUtils.getUserNameFromJwtToken(jwt.toString())
        val userId = (userDetailsService.loadUserByUsername(userName) as UserDetailsImpl).id
        if(existingRoom.moderatorId != userId)
            return ResponseEntity("You are not the moderator of this room", HttpStatus.BAD_REQUEST)

        //Traemos el user. Si no existe, devolvemos 404
        val user = userRepository.findByLogin(login).orElse(null)
            ?: return ResponseEntity("User $login not found", HttpStatus.NOT_FOUND)

        //Checkeamos si el usuario ya pertenece a la room
        user.rooms.map {
            if(it.id == roomId)
            {
                return ResponseEntity("User $login already belongs to room $roomId", HttpStatus.BAD_REQUEST)
            }
        }

        //Añadimos el usuario a la room
        userRepository.save(
            user.copy(rooms = user.rooms.plus(existingRoom))
        )

        existingRoom.logins = userRepository.findByRooms(listOf(existingRoom)).map {
//            println("Sending to users: ${it.login}")
//            messagingTemplate.convertAndSend("/topic/messages/${it.login}", msg)
            it.login
        }

        //Buscamos el ultimo mensaje de la room
        existingRoom.lastMessage = existingRoom.id?.let { messageRepository.findLastMessageByRoomId(it) }
        if(existingRoom.lastMessage != null)
            existingRoom.lastMessageTime = existingRoom.lastMessage!!.timeSent!!
        else
            existingRoom.lastMessageTime = existingRoom.createdAt

        //Enviamos nueva room al user
        messagingTemplate.convertAndSend("/topic/rooms/${user.login}", existingRoom)

        //Devolvemos ok
        return ResponseEntity(existingRoom, HttpStatus.OK)
    }

    @CrossOrigin(origins = ["http://localhost:3000", "https://tmdad2024front-6457f4860338.herokuapp.com"], allowCredentials = "true")
    @Throws(ResponseStatusException::class)
    @DeleteMapping("/{id}/user/{login}")
    fun removeUserFromRoom(request: HttpServletRequest,
                      @PathVariable("id") roomId: Int,
                      @PathVariable("login") login: String): ResponseEntity<*> {
        //Traemos la room. Si no existe, devolvemos 404
        val existingRoom = roomRepository.findById(roomId).orElse(null)
            ?: return ResponseEntity("Room $roomId not found", HttpStatus.NOT_FOUND)

        //Comprobamos que quien realiza la llamada es el moderador
        val jwt = jwtUtils.getJwtFromCookies(request)
        val userName = jwtUtils.getUserNameFromJwtToken(jwt.toString())
        val userId = (userDetailsService.loadUserByUsername(userName) as UserDetailsImpl).id
        if(existingRoom.moderatorId != userId)
            return ResponseEntity("You are not the moderator of this room", HttpStatus.BAD_REQUEST)

        //Traemos el user. Si no existe, devolvemos 404
        val user = userRepository.findByLogin(login).orElse(null)
            ?: return ResponseEntity("User $login not found", HttpStatus.NOT_FOUND)

        //Checkeamos si el usuario pertenece a la room
        var exists = false
        user.rooms.map {
            if(it.id == roomId)
                exists = true
        }

        if(!exists)
            return ResponseEntity("User $login does not belong to room $roomId", HttpStatus.BAD_REQUEST)

        //Quitamos el usuario de la room
        userRepository.save(
            user.copy(rooms = user.rooms.filter { it.id != roomId })
        )

        existingRoom.logins = userRepository.findByRooms(listOf(existingRoom)).map {
//            println("Sending to users: ${it.login}")
//            messagingTemplate.convertAndSend("/topic/messages/${it.login}", msg)
            it.login
        }

        //Enviamos informacion al user
        messagingTemplate.convertAndSend("/topic/removedFromRoom/${user.login}", existingRoom)

        //Devolvemos ok
        return ResponseEntity(existingRoom, HttpStatus.OK)
    }

    @CrossOrigin(origins = ["http://localhost:3000", "https://tmdad2024front-6457f4860338.herokuapp.com"], allowCredentials = "true")
    @Throws(ResponseStatusException::class)
    @Transactional
    @DeleteMapping("/{id}")
    fun removeRoom(request: HttpServletRequest,
                           @PathVariable("id") roomId: Int): ResponseEntity<*> {
        //Traemos la room. Si no existe, devolvemos 404
        val existingRoom = roomRepository.findById(roomId).orElse(null)
            ?: return ResponseEntity("Room $roomId not found", HttpStatus.NOT_FOUND)

        //Comprobamos que quien realiza la llamada es el moderador
        val jwt = jwtUtils.getJwtFromCookies(request)
        val userName = jwtUtils.getUserNameFromJwtToken(jwt.toString())
        val userId = (userDetailsService.loadUserByUsername(userName) as UserDetailsImpl).id
        if(existingRoom.moderatorId != userId)
            return ResponseEntity("You are not the moderator of this room", HttpStatus.BAD_REQUEST)

        //Traemos los usuarios de esta room y se la eliminamos
        val users = userRepository.findByRooms(listOf(existingRoom))
        for(user in users)
        {
            userRepository.save(
                user.copy(rooms = user.rooms.filter { it.id != roomId })
            )

            //Avisamos por el websocket
            messagingTemplate.convertAndSend("/topic/removedFromRoom/${user.login}", existingRoom)
        }

        //Borramos los mensajes de esta room
        messageRepository.deleteByRoomId(roomId)

        //Borramos la room
        roomRepository.deleteById(roomId)

        //Devolvemos ok
        return ResponseEntity("Room con ID ${roomId} borrada", HttpStatus.OK)
    }

    //update room
    @PutMapping("/{id}")
    fun updateRoomById(@PathVariable("id") roomId: Int, @RequestBody room: Room): ResponseEntity<Room> {
        //Traemos la room. Si no existe, devolvemos 404
        val existingRoom = roomRepository.findById(roomId).orElse(null) ?: return ResponseEntity(HttpStatus.NOT_FOUND)

        //Modificamos, guardamos en BBDD y devolvemos OK
        val updatedRoom = existingRoom.copy(name = room.name)
        roomRepository.save(updatedRoom)
        return ResponseEntity(updatedRoom, HttpStatus.OK)
    }

    //get messages by room id
    @CrossOrigin(origins = ["http://localhost:3000", "https://tmdad2024front-6457f4860338.herokuapp.com"], allowCredentials = "true",)
    @GetMapping("/{id}/messages")
    fun getMessagesByRoomId(@PathVariable("id") roomId: Int): List<Message> {
        //Comprobamos que la room existe
        val room = roomRepository.findById(roomId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND)
        }

        //Obtenemos los mensajes de la room
        return messageRepository.findByRoomId(roomId)
    }

    //get users by room id
    @CrossOrigin(origins = ["http://localhost:3000", "https://tmdad2024front-6457f4860338.herokuapp.com"], allowCredentials = "true")
    @GetMapping("/{id}/users")
    fun getUsersByRoomId(@PathVariable("id") roomId: Int): List<User> {
        //Comprobamos que la room existe
        val room = roomRepository.findById(roomId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND)
        }

        //Obtenemos los users de la room
        return userRepository.findByRooms(listOf(room))
    }

    @CrossOrigin(origins = ["http://localhost:3000", "https://tmdad2024front-6457f4860338.herokuapp.com"], allowCredentials = "true")
    @GetMapping("/{id}/candidate-users")
    fun getCandidateUsersByRoomId(@PathVariable("id") roomId: Int): List<User> {
        //Comprobamos que la room existe
        val room = roomRepository.findById(roomId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND)
        }

        //Obtenemos users
        val users = userRepository.findAll().toList()

        //Obtenemos users de la room
        val usersInRoom = userRepository.findByRooms(listOf(room))

        //Devolvemos la dierencia
        return users.filter { obj1 -> usersInRoom.none { obj2 -> obj1.id == obj2.id } }
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