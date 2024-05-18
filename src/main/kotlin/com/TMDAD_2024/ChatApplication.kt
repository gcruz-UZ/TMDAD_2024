package com.TMDAD_2024

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@SpringBootApplication
class ChatApplication

fun main(args: Array<String>) {
	runApplication<ChatApplication>(*args)
}





//
//
//
//@Configuration
//class RestTemplateConfig {
//	@Bean
//	fun restTemplate(builder: RestTemplateBuilder): RestTemplate {
//		return builder.build()
//	}
//}
//
//@JsonIgnoreProperties(ignoreUnknown = true)
//@JvmRecord
//data class Message(val body: String)
//
//@Component
//class MessageRetriever(private val restTemplate: RestTemplate) : CommandLineRunner {
//	override fun run(vararg args: String?) {
//		println("GOING")
//		val tenMinutesAgoTimestamp = System.currentTimeMillis() - (10 * 60 * 1000)
//		val messages: Array<Message>? = restTemplate.getForObject(
//			"http://localhost:8080/api/messages?timestampFilter=$tenMinutesAgoTimestamp", Array<Message>::class.java
////			"http://localhost:8080/api/messages?timestampFilter=1715945457000", Array<Message>::class.java
//		)
//
//		messages?.forEach { message ->
//			println(message.body)
//		}
//	}
//}