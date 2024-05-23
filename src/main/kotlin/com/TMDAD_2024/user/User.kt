package com.TMDAD_2024.user

import com.TMDAD_2024.room.Room
import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*
import java.sql.Timestamp

@Entity
@Table(name = "chat_user")
data class User
(
    //Clave primaria en BBDD
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int?,

    //Login del usuario (debe ser unico en BBDD)
    var login: String,

    //Contraseña
    var password: String,

    //Nombre formal del usuario
    var name: String,

    //Booleano que indica si es superusuario
    var isSuperuser: Boolean = false,

    //Hora de ultimo signin del user
    var lastSignIn: Timestamp?,

    //Rooms a las que pertenece el usuario
    @JsonManagedReference
    @ManyToMany
    @JoinTable(
        name = "user_room",
        joinColumns = arrayOf(JoinColumn(name = "user_id")),
        inverseJoinColumns = arrayOf(JoinColumn(name = "room_id"))
    )
    val rooms: List<Room> = listOf()
)
{
    constructor(login: String, password: String, name: String, isSuperuser: Boolean)
            : this (null, login, password, name, isSuperuser, Timestamp(System.currentTimeMillis()))
}