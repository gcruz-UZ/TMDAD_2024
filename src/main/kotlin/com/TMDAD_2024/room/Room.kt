package com.TMDAD_2024.room

import com.TMDAD_2024.user.User
import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*

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

    //Id de usuario en BBDD que controla esta sala
    var moderatorId: Int?,

    //Lista de usuarios que pertenecen a la room
    @JsonBackReference
    @ManyToMany(mappedBy = "rooms")
    var users: List<User> = listOf(),

    //Lista con los logins de los usuarios que pertenecen a la room
    @Transient
    var logins: List<String>
)
{
    //Constructor que recibe el nombre de la room
    constructor(name: String, moderatorId: Int?, logins: List<String>) : this (null, name, moderatorId, logins = logins)
}