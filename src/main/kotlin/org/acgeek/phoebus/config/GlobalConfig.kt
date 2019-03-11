package org.acgeek.phoebus.config

import org.acgeek.phoebus.dao.AdminRepository
import org.acgeek.phoebus.dao.UserRepository
import org.acgeek.phoebus.dto.CustomUser
import org.acgeek.phoebus.model.UserDo
import org.acgeek.phoebus.service.PackageUtils
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.serializer.*
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.session.data.redis.config.annotation.SpringSessionRedisConnectionFactory
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono


@Configuration
@EnableWebFluxSecurity
@EnableRedisWebSession
class GlobalConfig(@Value("\${session-redis-db}") val sessionRedisDb: Int,
                   private val customReactiveAuthenticationManager :CustomReactiveAuthenticationManager) {
    @Bean
    @Primary
    fun reactiveRedisTemplate(@Qualifier("redisConnectionFactory") factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, String> {
        return ReactiveRedisTemplate(factory, RedisSerializationContext.string())
    }

    @SpringSessionRedisConnectionFactory
    @Bean
    fun reactiveSessionRedisTemplate(properties: RedisProperties): ReactiveRedisConnectionFactory {
        return LettuceConnectionFactory(RedisStandaloneConfiguration().apply {
            port = properties.port ?: 6379
            hostName = properties.host ?: "localhost"
            database = sessionRedisDb
        })
    }

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http.authorizeExchange()
                .pathMatchers(HttpMethod.DELETE, "/api/user/*").hasAuthority("ADMIN")
                .pathMatchers(HttpMethod.POST, "/api/*").hasAuthority("USER")
                .anyExchange().permitAll()
                .and()
                .authenticationManager(customReactiveAuthenticationManager)
                //.exceptionHandling()
                //.authenticationEntryPoint(HttpStatusServerEntryPoint(HttpStatus.FORBIDDEN))
                //.and()
                .formLogin()
                .and()
                .httpBasic().disable()
                .csrf().disable().build()
    }
}

@Component
class CustomReactiveAuthenticationManager(private val userRepository: UserRepository,
                                          private val adminRepository: AdminRepository,
                                          private val packageUtils: PackageUtils): ReactiveAuthenticationManager {
    override fun authenticate(authentication: Authentication?): Mono<Authentication> {
        val userMail = authentication?.name ?: ""
        val password = authentication?.credentials.toString()
        return userRepository.getUserDoByMail(userMail)
                .filter{
                    it.password == packageUtils.sha512Hash(password + it.uid) && it.status == 0
                }
                .switchIfEmpty(Mono.defer {
                    Mono.error<UserDo>(BadCredentialsException("Invalid Credentials")) })
                .flatMap { u ->
                    val user = User.builder().username(u.mail)
                        .password(u.password).accountExpired(false)
                        .accountLocked(false).disabled(false)
                adminRepository.getAdminDoByUid(u.uid).map {
                    UsernamePasswordAuthenticationToken(CustomUser(user.roles("ADMIN").build(), u.uid), "ADMIN", setOf(SimpleGrantedAuthority("ADMIN"), SimpleGrantedAuthority("USER")))
                }.defaultIfEmpty(UsernamePasswordAuthenticationToken(CustomUser(user.roles("ADMIN").build(), u.uid), "USER", setOf(SimpleGrantedAuthority("USER"))))
            }
    }
}