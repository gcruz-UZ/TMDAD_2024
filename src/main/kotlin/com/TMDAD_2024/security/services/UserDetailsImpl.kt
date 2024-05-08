package com.TMDAD_2024.security.services

import com.TMDAD_2024.room.Room
import com.TMDAD_2024.user.User
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.stream.Collectors

class UserDetailsImpl(
    val id: Int?,
    private val login: String,
    val name: String,
    @field:JsonIgnore private val password: String,
    val isSuperuser: Boolean,
    val rooms: List<Room>,
    private val authorities: kotlin.collections.Collection<GrantedAuthority>
) : UserDetails  {

    override fun getAuthorities(): kotlin.collections.Collection<GrantedAuthority> {
        return authorities
    }

    override fun getPassword(): String {
        return password
    }

    override fun getUsername(): String {
        return login
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val user = o as UserDetailsImpl
        return id == user.id
    }

    companion object {
        private const val serialVersionUID = 1L

        fun build(user: User): UserDetailsImpl {
            val roles: List<String> = if (user.isSuperuser) {
//                listOf("USER", "ADMIN")
                listOf("ROLE_USER", "ROLE_ADMIN")
            } else {
//                listOf("USER")
                listOf("ROLE_USER")
            }

            val authorities: kotlin.collections.List<GrantedAuthority> = roles.stream()
                .map { role -> SimpleGrantedAuthority(role) }
                .collect(Collectors.toList())

            return UserDetailsImpl(
                user.id,
                user.login,
                user.name,
                user.password,
                user.isSuperuser,
                user.rooms,
                authorities
            )
        }
    }
}