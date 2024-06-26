package com.TMDAD_2024.websocket

import com.TMDAD_2024.Metrics
import com.TMDAD_2024.message.Message
import com.TMDAD_2024.message.MessageRepository
import com.TMDAD_2024.room.RoomRepository
import com.TMDAD_2024.user.UserRepository
import org.springframework.amqp.rabbit.core.RabbitTemplate
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
    @Autowired private val userRepository: UserRepository,
    @Autowired private val rabbitTemplate: RabbitTemplate
)
{
    //Recibimos mensaje
    @MessageMapping("/message")
    @Throws(Exception::class)
    fun message(msg: Message) {
        println("Received message: $msg")

        if(msg.body.length > 500)
        {
            println("Not allowed messages larger than 500 characters")
            return
        }

        val user = userRepository.findById(msg.userId).orElse(null)
        if(user == null)
        {
            println("user not found")
            return
        }

        if(msg.isAd && !user.isSuperuser)
        {
            println("ad is only for superusers")
            return
        }

        //Añadimos el timestamp y lo almacenamos en BBDD
        val time = Timestamp(System.currentTimeMillis())
        msg.timeSent = time
        messageRepository.save(msg)

        //Si es AD, enviamos y ya
        if(msg.isAd)
        {
            println("Sending AD to topic")
            messagingTemplate.convertAndSend("/topic/ad", msg)
            return
        }

        //Ahora, obtenemos la room a la que pertenece el mensaje, y lo reenviamos a los users de dicha room
        val roomId = msg.roomId ?: -1
        val room = roomRepository.findById(roomId).orElse(null)
        userRepository.findByRooms(listOf(room)).map {
            println("Sending to users: ${it.login}")
            messagingTemplate.convertAndSend("/topic/messages/${it.login}", msg)
        }

        //Añadimos el mensaje a la estructura de metricas
        Metrics.addMessage(Metrics.MetricsMessage(time, msg.body.length.toLong()))

        //Lo enviamos al analisis de palabras
//        rabbitTemplate.convertAndSend("MESSAGE_EXCHANGE", "MESSAGE_ROUTING_KEY", msg.body)
        rabbitTemplate.convertAndSend("MESSAGE_QUEUE", msg.body)
    }
}