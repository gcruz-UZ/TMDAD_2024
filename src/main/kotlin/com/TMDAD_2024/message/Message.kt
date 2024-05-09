package com.TMDAD_2024.message

import jakarta.persistence.*
import java.sql.Timestamp

@Entity
@Table(name = "message")
data class Message
(
    //Clave primaria en BBDD
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int,

    //Cuerpo del mensaje
    var body: String,

    //Momento en el que se envío
    var timeSent: Timestamp?,

    //String que indica si el mensaje es un archivo
    var filename: String,

    //Booleano que indica si el mensaje es publicidad
    var isAd: Boolean = false,

    //Id de usuario en BBDD que ha mandado el mensaje
    var userId: Int,

    //Id de room en BBDD a la que pertenece el mensaje
    var roomId: Int
)