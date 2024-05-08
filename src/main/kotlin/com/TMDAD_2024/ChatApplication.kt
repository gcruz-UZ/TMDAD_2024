package com.TMDAD_2024

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

//@Component
//class ConsoleMessagePrinter : CommandLineRunner {
//
//	override fun run(vararg args: String?) {
//		val scheduler = Executors.newSingleThreadScheduledExecutor()
//		val intervalInSeconds = 30L
//
//		scheduler.scheduleAtFixedRate({
//			println("Printing a message every $intervalInSeconds seconds")
//		}, 0, intervalInSeconds, TimeUnit.SECONDS)
//	}
//}

@SpringBootApplication
class ChatApplication

fun main(args: Array<String>) {
	runApplication<ChatApplication>(*args)
}
