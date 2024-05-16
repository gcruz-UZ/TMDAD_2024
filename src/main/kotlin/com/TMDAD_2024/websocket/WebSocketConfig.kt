package com.TMDAD_2024.websocket

import com.TMDAD_2024.security.jwt.JwtUtils
import com.TMDAD_2024.security.services.UserDetailsServiceImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import java.util.concurrent.atomic.AtomicInteger

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    @Bean
    fun connections(): AtomicInteger {
        return AtomicInteger(0)
    }

    @Autowired
    private val jwtUtils: JwtUtils? = null

    @Autowired
    private val userDetailsService: UserDetailsServiceImpl? = null

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        //Broker de WS
        config.enableSimpleBroker("/topic")

        //Prefijo
        config.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        //Añadimos el endpoint de WS y permitimos el host web para evitar problemas de CORS
        registry.addEndpoint("/ws").setAllowedOrigins("http://localhost:3000")
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        val connections = connections()
        registration.interceptors(object : ChannelInterceptor {
            override fun preSend(message: Message<*>, channel: MessageChannel) : Message<*> {
                val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
                logger.info("INBOUND CHANNEL Headers: {}", accessor)

                assert(accessor != null)
                if (StompCommand.CONNECT == accessor?.command) {
                    val authorizationHeader = accessor.getFirstNativeHeader("Authorization")!!
                    val token = authorizationHeader.substring(7)
                    val username: String =
                        jwtUtils?.getUserNameFromJwtToken(jwtUtils.getJwtFromString(token).toString()).toString()
                    val userDetails = userDetailsService!!.loadUserByUsername(username)
                    val usernamePasswordAuthenticationToken =
                        UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                    SecurityContextHolder.getContext().authentication = usernamePasswordAuthenticationToken
                    accessor.user = usernamePasswordAuthenticationToken

                    connections.incrementAndGet()
                }

                if(StompCommand.DISCONNECT == accessor?.command)
                {
                    connections().decrementAndGet()
                }
                
                return message
            }
        })
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(JwtUtils::class.java)
    }
}