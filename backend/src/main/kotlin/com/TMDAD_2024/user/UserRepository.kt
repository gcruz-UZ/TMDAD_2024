package com.TMDAD_2024.user

import org.springframework.data.repository.CrudRepository

interface UserRepository : CrudRepository<User, Int>