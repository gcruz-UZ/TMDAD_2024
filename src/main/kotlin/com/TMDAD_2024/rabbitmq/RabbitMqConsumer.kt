package com.TMDAD_2024.rabbitmq

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service
import org.springframework.messaging.simp.SimpMessagingTemplate

@Service
class RabbitMqConsumer(private val messagingTemplate: SimpMessagingTemplate) {
    @RabbitListener(queues = ["MESSAGE_QUEUE"])
    fun consume(message: String) {
        println("Received message -> $message")

        // Send the received message down the WebSocket
        messagingTemplate.convertAndSend("/topic/trendings", message)
    }
}