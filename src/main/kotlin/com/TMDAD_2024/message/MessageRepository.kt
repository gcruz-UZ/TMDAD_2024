package com.TMDAD_2024.message

import org.springframework.data.repository.CrudRepository
import java.sql.Timestamp

interface MessageRepository : CrudRepository<Message, Int>
{
    //Para obtener los mensajes que pertenecen a una room dado el Id en BBDD de la room
    fun findByRoomId(roomId: Int) : List<Message>

    //Para obtener los mensajes que son AD
    fun findByIsAd(isAd: Boolean) : List<Message>

    //Para borrar mensajes de una room
    fun deleteByRoomId(roomId: Int)
}