package org.acgeek.phoebus.config

import org.acgeek.phoebus.exception.PhoebusResourceNotExistsException
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.server.WebExceptionHandler
import reactor.core.publisher.Mono


@Configuration
@EnableWebFluxSecurity
class GlobalConfig {
    @Bean
    fun reactiveRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, String> {
        return ReactiveRedisTemplate(factory, RedisSerializationContext.string())
    }

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http.authorizeExchange().anyExchange().permitAll().and()
                .csrf().disable().build()
    }

    @Bean
    fun exceptionHandler(): WebExceptionHandler {
        return WebExceptionHandler { exchange, ex ->
            if (ex is PhoebusResourceNotExistsException) {
                exchange.response.statusCode = HttpStatus.NOT_FOUND
                exchange.response.setComplete()
            } else {
                Mono.error(ex)
            }
        }
    }
}