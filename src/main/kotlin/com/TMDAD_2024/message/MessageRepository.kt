package com.TMDAD_2024.message

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface MessageRepository : CrudRepository<Message, Int>
{
    //Para obtener los mensajes que pertenecen a una room dado el Id en BBDD de la room
    fun findByRoomId(roomId: Int) : List<Message>

    //Para obtener los mensajes que son AD
    fun findByIsAd(isAd: Boolean) : List<Message>

    //Busca el ultimo mensaje de una room
    @Query("SELECT m FROM Message m WHERE m.roomId = :roomId ORDER BY m.timeSent DESC LIMIT 1")
    fun findLastMessageByRoomId(roomId: Int): Message?

    @Query("SELECT m FROM Message m WHERE m.isAd = true ORDER BY m.timeSent DESC LIMIT 1")
    fun findLastAdMessage(): Message?

    //Para borrar mensajes de una room
    fun deleteByRoomId(roomId: Int)
}