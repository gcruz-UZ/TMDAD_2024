package com.TMDAD_2024.message

import com.TMDAD_2024.Metrics
import com.TMDAD_2024.room.RoomRepository
import com.TMDAD_2024.user.UserRepository
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.amqp.rabbit.core.RabbitTemplate
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
import org.springframework.messaging.simp.SimpMessagingTemplate
import java.text.SimpleDateFormat

@RestController
@RequestMapping("/api/messages")
class MessageController(@Autowired private val messageRepository: MessageRepository,
                        @Autowired private val userRepository: UserRepository,
                        @Autowired private val messagingTemplate: SimpMessagingTemplate,
                        @Autowired private val roomRepository: RoomRepository,
                        @Autowired private val rabbitTemplate: RabbitTemplate
)
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

    @CrossOrigin(origins = ["http://localhost:3000", "https://tmdad2024front-6457f4860338.herokuapp.com"], allowCredentials = "true")
    @GetMapping("/ad/last")
    fun getLastAdMessage(): Message? =
        messageRepository.findLastAdMessage()


    //create message
    @PostMapping("")
    fun createMessage(@RequestBody msg: Message): ResponseEntity<*> {
        if(msg.body.length > 500)
            return ResponseEntity("Not allowed messages larger than 500 characters", HttpStatus.BAD_REQUEST)

        msg.timeSent = Timestamp(System.currentTimeMillis())
        val savedMsg = messageRepository.save(msg)
        return ResponseEntity(savedMsg, HttpStatus.CREATED)
    }

    private val targetLocation: Path = Paths.get("uploaded-files")

    init {
        Files.createDirectories(targetLocation)
    }

    @CrossOrigin(origins = ["http://localhost:3000", "https://tmdad2024front-6457f4860338.herokuapp.com"], allowCredentials = "true")
    @PostMapping("/file")
//    fun uploadFile(@RequestParam("file") file: MultipartFile, @RequestParam("body") body: String): String {
    fun uploadFile(@RequestParam("file") file: MultipartFile,
                   @RequestParam("body") body: String,
                   @RequestParam("filename") filename: String,
                   @RequestParam("isAd") isAd: Boolean,
                   @RequestParam("userId") userId: Int,
                   @RequestParam("userLogin") userLogin: String,
                   @RequestParam("roomId") roomId: Int?): ResponseEntity<*> {
//        println("** REQUEST **")
//        println(body)
//        println(filename)
//        println(isAd)
//        println(userId)
//        println(userLogin)
//        println(roomId)

        if(body.length > 500)
            return ResponseEntity("Not allowed messages larger than 500 characters", HttpStatus.BAD_REQUEST)

        if((file.size / (1024*1024)) > 20)
            return ResponseEntity("Not allowed files larger than 20MB", HttpStatus.BAD_REQUEST)

        val user = userRepository.findById(userId).orElse(null)
        if(user == null)
            return ResponseEntity("User with ID ${userId} not found", HttpStatus.BAD_REQUEST)

        if(isAd && !user.isSuperuser)
            return ResponseEntity("Ad is only for superusers", HttpStatus.BAD_REQUEST)

        //Añadimos el timestamp y lo almacenamos en BBDD
        val timeSent = Timestamp(System.currentTimeMillis())
        val msg = Message(body, timeSent, filename, isAd, userId, userLogin, roomId)
        messageRepository.save(msg)

        val targetPath = targetLocation.resolve(msg.id.toString() +
                SimpleDateFormat("yyyyMMddHHmmss").format(timeSent) +
                "_" + file.originalFilename!!)

        file.inputStream.use { inputStream ->
            Files.copy(inputStream, targetPath)
        }

        //Si es AD, enviamos y ya
        if(isAd)
        {
            println("Sending AD to topic")
            messagingTemplate.convertAndSend("/topic/ad", msg)
            return ResponseEntity(msg, HttpStatus.OK)
        }

        //Ahora, obtenemos la room a la que pertenece el mensaje, y lo reenviamos a los users de dicha room
        val room = roomRepository.findById(msg.roomId ?: -1).orElse(null)
        userRepository.findByRooms(listOf(room)).map {
            println("Sending to users: ${it.login}")
            messagingTemplate.convertAndSend("/topic/messages/${it.login}", msg)
        }

        //Añadimos el mensaje a la estructura de metricas
        Metrics.addMessage(Metrics.MetricsMessage(timeSent, msg.body.length + file.size))

        //Enviamos el body del mensaje al analizador de palabras
        rabbitTemplate.convertAndSend("MESSAGE_EXCHANGE", "MESSAGE_ROUTING_KEY", msg.body)

        println("File uploaded successfully: ${file.originalFilename}")
        return ResponseEntity(msg, HttpStatus.OK)
    }

    @CrossOrigin(origins = ["http://localhost:3000", "https://tmdad2024front-6457f4860338.herokuapp.com"], allowCredentials = "true")
    @GetMapping("/{id}/download")
    fun downloadFile(@PathVariable id: Int): ResponseEntity<*> {
//        println("MEssage ID is: " + id)
        val msg = messageRepository.findById(id).orElse(null)
        if(msg == null)
            return ResponseEntity("Message with ID ${id} not found", HttpStatus.NOT_FOUND)

        if(msg.filename == "")
            return ResponseEntity("Message with ID ${id} does not have an associated file", HttpStatus.BAD_REQUEST)

        val file: Path = targetLocation.resolve(msg.id.toString() +
                SimpleDateFormat("yyyyMMddHHmmss").format(msg.timeSent) +
                "_" + msg.filename).normalize()
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