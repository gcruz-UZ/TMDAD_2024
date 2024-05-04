package com.TMDAD_2024.message

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.sql.Timestamp

@RestController
@RequestMapping("/api/messages")
class MessageController(@Autowired private val messageRepository: MessageRepository)
{
    //get all messages
    @GetMapping("")
    fun getAllMessages(): List<Message> =
        messageRepository.findAll().toList()

    //create message
    @PostMapping("")
    fun createMessage(@RequestBody msg: Message): ResponseEntity<Message> {
        msg.timeSent = Timestamp(System.currentTimeMillis())
        val savedMsg = messageRepository.save(msg)
        return ResponseEntity(savedMsg, HttpStatus.CREATED)
    }
}