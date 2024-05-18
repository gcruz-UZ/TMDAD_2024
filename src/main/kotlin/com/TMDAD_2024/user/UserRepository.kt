package com.TMDAD_2024.user

import com.TMDAD_2024.room.Room
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.sql.Timestamp
import java.util.*


interface UserRepository : CrudRepository<User, Int>
{
    //Para obtener un usuario dado su login (el login es unico en BBDD)
    fun findByLogin(login: String) : Optional<User>

    //Para obtener los usuarios que pertenecen a una room (Aunque el argumento es una lista,
    // se le pasa una lista con una unica room, y ya)
    fun findByRooms(rooms: List<Room>) : List<User>

    //Para consultar si existe un usuario por login
    fun existsByLogin(login: String): Boolean

    //Para updatear el ultimo signin dado el id de user en BBDD
    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.lastSignIn = :lastSignIn WHERE u.id = :id")
    fun updateLastSignInById(id: Int?, lastSignIn: Timestamp)
}