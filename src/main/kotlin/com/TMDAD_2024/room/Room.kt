package com.TMDAD_2024.room

import com.TMDAD_2024.user.User
import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*

@Entity
@Table(name = "room")
data class Room(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int?,
    var name: String,
    @JsonBackReference
    @ManyToMany(mappedBy = "rooms")
    val users: List<User> = listOf()
) {
    constructor(name: String) : this (null, name)
}