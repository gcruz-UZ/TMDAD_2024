package com.TMDAD_2024.message

import jakarta.persistence.*
import java.sql.Timestamp

@Entity
@Table(name = "message")
data class Message (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int,
    var body: String,
    var timeSent: Timestamp?,
    var isFile: Boolean = false,
    var isAd: Boolean = false,
    var userId: Int,
    var roomId: Int
)