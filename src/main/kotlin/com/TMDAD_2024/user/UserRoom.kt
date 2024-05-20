package com.TMDAD_2024.user

import jakarta.persistence.*
import java.io.Serializable
import java.sql.Timestamp

@Embeddable
data class UserRoomKey (
    @Column(name = "user_id")
    val userId: Int?,
    @Column(name = "room_id")
    val roomId: Int?
) : Serializable

@Entity
@Table(name = "user_room")
//@IdClass(UserRoomKey::class)
data class UserRoom
    (
    @EmbeddedId
    val key: UserRoomKey,
    @Column(name = "last_access")
    var lastAccess: Timestamp
)