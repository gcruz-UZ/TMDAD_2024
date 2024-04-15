package com.TMDAD_2024.user

import com.TMDAD_2024.room.Room
import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*

@Entity
@Table(name = "chat_user")
data class User (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int,
    var login: String,
    var name: String,
    var isSuperuser: Boolean = false,
    @JsonManagedReference
    @ManyToMany
    @JoinTable(
        name = "user_room",
        joinColumns = arrayOf(JoinColumn(name = "user_id")),
        inverseJoinColumns = arrayOf(JoinColumn(name = "room_id"))
    )
    val rooms: List<Room> = listOf()
)