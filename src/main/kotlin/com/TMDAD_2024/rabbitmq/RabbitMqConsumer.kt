package com.TMDAD_2024.rabbitmq

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service
import org.springframework.messaging.simp.SimpMessagingTemplate

@Service
class RabbitMqConsumer(private val messagingTemplate: SimpMessagingTemplate) {
    @RabbitListener(queues = ["TRENDING_QUEUE"])
    fun consume(message: String) {
        println("Received message FROM TRENDINGS -> $message")
        // Enviamos por websocket al topic de trendings
        messagingTemplate.convertAndSend("/topic/trendings", message)
    }
}