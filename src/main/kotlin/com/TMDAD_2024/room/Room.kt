package com.TMDAD_2024.room

import com.TMDAD_2024.message.Message
import com.TMDAD_2024.user.User
import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*
import java.sql.Timestamp

@Entity
@Table(name = "room")
data class Room
(
    //Clave primaria en BBDD
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int?,

    //Nombre de la room
    var name: String,

    //Momento en el que se creo
    var createdAt: Timestamp,

    //Id de usuario en BBDD que controla esta sala
    var moderatorId: Int?,

    //Lista de usuarios que pertenecen a la room
    @JsonBackReference
    @ManyToMany(mappedBy = "rooms")
    var users: List<User> = listOf(),

    //Lista con los logins de los usuarios que pertenecen a la room
    @Transient
    var logins: List<String>,

    //Si hay mensajes, fecha del ultimo mensaje. Si no, fecha de creacion de la sala
    @Transient
    var lastMessageTime: Timestamp,

    //Ultimo mensaje escrito en la room
    @Transient
    var lastMessage: Message?
)
{
    //Constructor que recibe el nombre de la room
    constructor(name: String, createdAt: Timestamp, moderatorId: Int?, logins: List<String>)
            : this (null, name, createdAt, moderatorId, logins = logins, lastMessage = null, lastMessageTime = createdAt)
}