package com.TMDAD_2024.user

import jakarta.persistence.*

@Entity
@Table(name = "chat_user")
data class User (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int,
    var login: String,
    var name: String,
    var isSuperuser: Boolean = false
)