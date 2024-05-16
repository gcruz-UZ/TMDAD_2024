package com.TMDAD_2024.message

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.Timestamp
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpHeaders

@RestController
@RequestMapping("/api/messages")
class MessageController(@Autowired private val messageRepository: MessageRepository)
{
    //get all messages
    @GetMapping("")
    fun getAllMessages() : List<Message> =
        messageRepository.findAll().toList()

    //Obtener mensajes que son AD
    @CrossOrigin(origins = ["http://localhost:3000", "https://tmdad2024front-6457f4860338.herokuapp.com"], allowCredentials = "true")
    @GetMapping("/ad")
    fun getAdMessages(): List<Message> =
        messageRepository.findByIsAd(true).toList()


    //create message
    @PostMapping("")
    fun createMessage(@RequestBody msg: Message): ResponseEntity<Message> {
        msg.timeSent = Timestamp(System.currentTimeMillis())
        val savedMsg = messageRepository.save(msg)
        return ResponseEntity(savedMsg, HttpStatus.CREATED)
    }

    private val targetLocation: Path = Paths.get("uploaded-files")

    init {
        Files.createDirectories(targetLocation)
    }

    @CrossOrigin(origins = ["http://localhost:3000", "https://tmdad2024front-6457f4860338.herokuapp.com"], allowCredentials = "true")
    @PostMapping("/fileUpload")
    fun uploadFile(@RequestParam("file") file: MultipartFile): String {
        val targetPath = targetLocation.resolve(file.originalFilename!!)
        file.inputStream.use { inputStream ->
            Files.copy(inputStream, targetPath)
        }
        return "File uploaded successfully: ${file.originalFilename}"
    }

    @CrossOrigin(origins = ["http://localhost:3000", "https://tmdad2024front-6457f4860338.herokuapp.com", allowCredentials = "true")
    @GetMapping("/download/{filename:.+}")
    fun downloadFile(@PathVariable filename: String): ResponseEntity<*> {
        val file: Path = targetLocation.resolve(filename).normalize()
        val resource: Resource = UrlResource(file.toUri())

        if (resource.exists() && resource.isReadable) {
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${resource.filename}\"")
                .body(resource)
        } else {
            return ResponseEntity("Something went wrong", HttpStatus.NOT_FOUND)
        }
    }
}