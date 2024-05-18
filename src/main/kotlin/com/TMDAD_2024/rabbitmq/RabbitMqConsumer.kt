package com.TMDAD_2024.rabbitmq

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service
import org.springframework.messaging.simp.SimpMessagingTemplate

@Service
class RabbitMqConsumer(private val messagingTemplate: SimpMessagingTemplate) {
    @RabbitListener(queues = ["SECOND_MESSAGE_QUEUE"])
    fun consume(message: String) {
        println("Received message FROM TRENDINGS -> $message")

        val trends = message.split(",")
        val builder = StringBuilder()

        builder.append("*** TRENDING TOPICS ***").append('\n')
        for(t in trends)
        {
            builder.append(t.trim()).append('\n')
        }

        println("Parsed message:")
        println(builder.toString())

        // Enviamos por websocket al topic de trendings
        messagingTemplate.convertAndSend("/topic/trendings", builder.toString())
    }
}