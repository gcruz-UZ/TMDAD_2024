package com.TMDAD_2024.websocket

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import org.springframework.web.util.HtmlUtils


@Controller
class GreetingController(@Autowired private val messagingTemplate: SimpMessagingTemplate) {
//    @MessageMapping("/hello")
//    @SendTo("/topic/greetings")
//    @Throws(Exception::class)
//    fun greeting(message: HelloMessage): Greeting {
//        Thread.sleep(1000) // simulated delay
//        return Greeting("Hello, " + HtmlUtils.htmlEscape(message.name) + "!")

    @MessageMapping("/hello")
    @Throws(Exception::class)
    fun greeting(message: HelloMessage) {
        Thread.sleep(1000) // simulated delay
        println("EY")
        val fruits = listOf("juan", "federico")
//        messagingTemplate.convertAndSendToUser("greetings", "/topic", Greeting("Hello, " + HtmlUtils.htmlEscape(message.name) + "!"))
        for (fruit in fruits) {
            messagingTemplate.convertAndSend("/topic/greetings/$fruit", Greeting("Hello, " + HtmlUtils.htmlEscape(fruit) + "!"))
        }
    }
}