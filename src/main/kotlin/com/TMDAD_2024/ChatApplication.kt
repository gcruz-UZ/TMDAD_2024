package com.TMDAD_2024

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import java.sql.Timestamp
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class Metrics {
	data class MetricsMessage (
		var timeSent: Timestamp,
		var size: Long
	)

	companion object {
		private var messages = mutableListOf<MetricsMessage>()

		@Synchronized
		fun addMessage(m: MetricsMessage) {
			messages.add(m)
		}

		@Synchronized
		fun getMessagesInTenLastMinutes(): Pair<Int, Long> {
			val time = Timestamp(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(10))
			messages = messages.filter { it.timeSent > time }.toMutableList()
			return Pair(messages.size, messages.sumOf { it.size })
		}
	}
}

@Component
class StatsSender : CommandLineRunner {

	@Autowired
	private lateinit var connections: AtomicInteger

	@Autowired
	private lateinit var messagingTemplate: SimpMessagingTemplate

	override fun run(vararg args: String?) {
		val scheduler = Executors.newSingleThreadScheduledExecutor()
		val intervalInSeconds = 5L

		scheduler.scheduleAtFixedRate({
			val stats = StringBuilder()
			stats.append("Online users: ${connections.get()},")
			val metrics = Metrics.getMessagesInTenLastMinutes()
			stats.append("* ESTADISTICAS ULTIMOS 10 MINUTOS *,")
			stats.append("Mensajes totales: ${metrics.first},")
			stats.append("Mensajes intercambiados por minuto: ${metrics.first / 10.0},")
			val bytes = StringBuilder()
			if(metrics.second > (1024*1024))
			{
				bytes.append("${(metrics.second / (1024*1024)) / 10.0} MB")
			}
			else if(metrics.second > 1024)
			{
				bytes.append("${(metrics.second / 1024) / 10.0} KB")
			}
			else
			{
				bytes.append("${metrics.second / 10.0} B")
			}

			stats.append("Bytes intercambiados por minuto: ${bytes.toString()}")
			messagingTemplate.convertAndSend("/topic/stats", stats.toString())
		}, 0, intervalInSeconds, TimeUnit.SECONDS)
	}
}

@SpringBootApplication
class ChatApplication

fun main(args: Array<String>) {
	runApplication<ChatApplication>(*args)
}