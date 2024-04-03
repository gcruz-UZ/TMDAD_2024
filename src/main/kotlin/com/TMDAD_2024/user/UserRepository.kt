package com.TMDAD_2024.user

import org.springframework.data.repository.CrudRepository
import java.util.*

interface UserRepository : CrudRepository<User, Int>
{
    fun findByLogin(login: String) : Optional<User>
}