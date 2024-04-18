package com.TMDAD_2024.message

import com.TMDAD_2024.user.User
import org.springframework.data.repository.CrudRepository
import java.util.*

interface MessageRepository : CrudRepository<Message, Int>
{
    fun findByRoomId(roomId: Int) : List<Message>
}