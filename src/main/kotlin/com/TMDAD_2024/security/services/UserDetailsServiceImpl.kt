package com.TMDAD_2024.security.services

import com.TMDAD_2024.user.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.function.Supplier

@Service
class UserDetailsServiceImpl(@Autowired private val userRepository: UserRepository) : UserDetailsService {

    @Transactional
    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(login: String): UserDetails {
        val user = userRepository.findByLogin(login).orElseThrow(Supplier {
                UsernameNotFoundException(
                    "User Not Found with login: $login"
                )
            })

        val a = UserDetailsImpl.build(user)

        println("PRINTEANDO AUTHORITIES")
        println(a.authorities)

        return a
    }
}