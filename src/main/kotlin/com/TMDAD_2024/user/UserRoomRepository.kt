package com.TMDAD_2024.user

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface UserRoomRepository : CrudRepository<UserRoom, UserRoomKey>
{
    //Para consultar si existe alguna room para un user especifico
    @Query("SELECT CASE WHEN COUNT(ur) > 0 THEN true ELSE false END FROM UserRoom ur WHERE ur.key.userId = :id")
    fun existsByUserId(id: Int): Boolean
}