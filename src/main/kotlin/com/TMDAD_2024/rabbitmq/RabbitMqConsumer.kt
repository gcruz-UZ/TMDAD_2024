package com.TMDAD_2024.rabbitmq

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service
import org.springframework.messaging.simp.SimpMessagingTemplate

@Service
class RabbitMqConsumer(private val messagingTemplate: SimpMessagingTemplate) {
//    @RabbitListener(queues = ["MESSAGE_QUEUE"])
//    fun consume(message: String) {
//        println("Received message -> $message")
//
//        // Send the received message down the WebSocket
//        messagingTemplate.convertAndSend("/topic/trendings", message)
//    }

//    @RabbitListener(queues = ["SECOND_MESSAGE_QUEUE"])
//    fun consumeTrendings(message: String) {
//        println("Received message -> $message")
//
//        // Send the received message down the WebSocket
////        messagingTemplate.convertAndSend("/topic/trendings", message)
//    }
}

@Service
class SecondRabbit(private val messagingTemplate: SimpMessagingTemplate) {
    @RabbitListener(queues = ["SECOND_MESSAGE_QUEUE"])
    fun consume(message: String) {
        println("Received message FROM DANIELLA -> $message")

//        val input = "Hello (world) and (universe)"

        // Split the input string by elements within parentheses
//        val result = message.split("\\([^()]*\\)".toRegex())
//
//        // Print the result
//        result.forEach { println(it.trim()) }

        val result = message.split(",")
        val builder = StringBuilder()
//
        for(r in result)
        {
            val isNumber: Boolean = r.trim().toDoubleOrNull() != null
//            builder.append(r.trim())


            if(isNumber)
                builder.append(r.trim() + '\n')
            else
                builder.append(r.trim() + ": ")
        }

        println(builder.toString())
        // Send the received message down the WebSocket
        messagingTemplate.convertAndSend("/topic/trendings", message)
    }
}