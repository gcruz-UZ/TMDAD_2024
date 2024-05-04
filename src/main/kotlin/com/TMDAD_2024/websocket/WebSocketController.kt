package com.TMDAD_2024.websocket

import com.TMDAD_2024.message.Message
import com.TMDAD_2024.message.MessageRepository
import com.TMDAD_2024.room.RoomRepository
import com.TMDAD_2024.user.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import java.sql.Timestamp

@Controller
class WebSocketController(
    @Autowired private val messagingTemplate: SimpMessagingTemplate,
    @Autowired private val messageRepository: MessageRepository,
    @Autowired private val roomRepository: RoomRepository,
    @Autowired private val userRepository: UserRepository
)
{
    //Recibimos mensaje
    @MessageMapping("/message")
    @Throws(Exception::class)
    fun message(msg: Message) {
        println("Received message: $msg")

        //Añadimos el timestamp y lo almacenamos en BBDD
        msg.timeSent = Timestamp(System.currentTimeMillis())
        messageRepository.save(msg)

        //Ahora, obtenemos la room a la que pertenece el mensaje, y lo reenviamos a los users de dicha room
        val room = roomRepository.findById(msg.roomId).orElse(null)
        userRepository.findByRooms(listOf(room)).map {
            println("Sending to users: ${it.login}")
            messagingTemplate.convertAndSend("/topic/messages/${it.login}", msg)
        }
    }
}