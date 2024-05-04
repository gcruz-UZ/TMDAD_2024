package com.TMDAD_2024.user

import com.TMDAD_2024.room.Room
import org.springframework.data.repository.CrudRepository
import java.util.*

interface UserRepository : CrudRepository<User, Int>
{
    //Para obtener un usuario dado su login (el login es unico en BBDD)
    fun findByLogin(login: String) : Optional<User>

    //Para obtener los usuarios que pertenecen a una room (Aunque el argumento es una lista,
    // se le pasa una lista con una unica room, y ya)
    fun findByRooms(rooms: List<Room>) : List<User>
}