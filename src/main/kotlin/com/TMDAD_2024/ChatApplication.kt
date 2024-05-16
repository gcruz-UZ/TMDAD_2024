package com.TMDAD_2024

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@Component
class ConsoleMessagePrinter : CommandLineRunner {

	@Autowired
	private lateinit var connections: AtomicInteger

	@Autowired
	private lateinit var messagingTemplate: SimpMessagingTemplate

	override fun run(vararg args: String?) {
		val scheduler = Executors.newSingleThreadScheduledExecutor()
		val intervalInSeconds = 5L

		scheduler.scheduleAtFixedRate({
//			println("Printing a message every $intervalInSeconds seconds")
			messagingTemplate.convertAndSend("/topic/stats", "Online users: ${connections.get()}")
		}, 0, intervalInSeconds, TimeUnit.SECONDS)
	}
}

@SpringBootApplication
class ChatApplication

fun main(args: Array<String>) {
	runApplication<ChatApplication>(*args)
}
