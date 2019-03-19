package org.acgeek.phoebus.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.acgeek.phoebus.dao.AdminRepository
import org.acgeek.phoebus.dao.UserRepository
import org.acgeek.phoebus.dto.CustomAuthentication
import org.acgeek.phoebus.dto.CustomUser
import org.acgeek.phoebus.model.UserDo
import org.acgeek.phoebus.service.PackageUtils
import org.slf4j.LoggerFactory
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
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.session.data.redis.config.annotation.SpringSessionRedisConnectionFactory
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession
import org.springframework.stereotype.Component
import org.springframework.web.server.WebFilter
import reactor.core.publisher.Mono


@Configuration
@EnableWebFluxSecurity
@EnableRedisWebSession(maxInactiveIntervalInSeconds = 3_000_000)
class GlobalConfig(@Value("\${session-redis-db}") val sessionRedisDb: Int,
                   private val mapper: ObjectMapper,
                   private val customReactiveAuthenticationManager :CustomReactiveAuthenticationManager) {
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    @Bean
    @Primary
    fun reactiveRedisTemplate(@Qualifier("redisConnectionFactory") factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, String> {
        return ReactiveRedisTemplate(factory, RedisSerializationContext.string())
    }

    @SpringSessionRedisConnectionFactory
    @Bean
    fun reactiveSessionRedisTemplate(properties: RedisProperties): ReactiveRedisConnectionFactory {
        return LettuceConnectionFactory(RedisStandaloneConfiguration().apply {
            port = properties.port
            hostName = properties.host ?: "localhost"
            database = sessionRedisDb
        })
    }

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http
                .authorizeExchange()
                .pathMatchers(HttpMethod.DELETE, "/api/user/*").hasAuthority("ADMIN")
                .pathMatchers(HttpMethod.POST, "/api/*").hasAuthority("USER")
                .pathMatchers(HttpMethod.GET, "/api/**").hasAuthority("USER")
                .anyExchange().permitAll()
                .and()
                .exceptionHandling()
                // throw 401 if not login
                .authenticationEntryPoint(HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
                .and()
                .addFilterAt(alreadyLoginFilter, SecurityWebFiltersOrder.FORM_LOGIN)
                .addFilterAt(customAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .formLogin().disable()
                .httpBasic().disable()
                .logout().and()
                .csrf().disable()
        http.HttpsRedirectSpec()
        return http.build()
    }

    /**
     * if already login, throw 403
     */
    private val alreadyLoginFilter=  WebFilter { exchange, chain ->
        exchange.session.cache().flatMap {
            val session = it.getAttribute<SecurityContextImpl?>(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY)
                    ?.authentication?.principal
            if (session != null && session is CustomUser && session.uid?.isNotBlank() == true) {
                // disable login
                exchange.response.statusCode = HttpStatus.FORBIDDEN
                Mono.empty()
            } else {
                Mono.defer { chain.filter(exchange) }
            }
        }
    }

    private val customAuthenticationWebFilter: AuthenticationWebFilter by lazy {
        val auth = AuthenticationWebFilter(customReactiveAuthenticationManager)
        auth.setServerAuthenticationConverter { exchange ->
            val userName = exchange.request.headers["username"]?.first()
            val userPassword = exchange.request.headers["password"]?.first()
            Mono.just(UsernamePasswordAuthenticationToken(userName, userPassword))
        }
        auth.setSecurityContextRepository(WebSessionServerSecurityContextRepository())
        auth.setAuthenticationSuccessHandler { exchange, res ->
            val bodyString = (res as CustomAuthentication).user
            exchange.exchange.response.statusCode = HttpStatus.OK
                exchange.exchange.response.writeWith(Mono.just(exchange.exchange.response.bufferFactory().wrap(mapper.writeValueAsBytes(bodyString))))
        }
        auth.setAuthenticationFailureHandler { webFilterExchange, _ ->
            Mono.fromRunnable { webFilterExchange.exchange.response.statusCode = HttpStatus.UNAUTHORIZED }
        }
        auth.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, "/login"))
        auth
    }
}

@Component
class CustomReactiveAuthenticationManager(private val userRepository: UserRepository,
                                          private val adminRepository: AdminRepository,
                                          private val packageUtils: PackageUtils): ReactiveAuthenticationManager {
    override fun authenticate(authentication: Authentication?): Mono<Authentication> {
        val userMail = authentication?.name ?: ""
        val password = authentication?.credentials?.toString()
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
                    CustomAuthentication(UsernamePasswordAuthenticationToken(
                            CustomUser(user.roles("ADMIN").build(), u.uid),
                            "ADMIN",
                            setOf(SimpleGrantedAuthority("ADMIN"), SimpleGrantedAuthority("USER"))),
                            u)
                }.defaultIfEmpty(CustomAuthentication(
                        UsernamePasswordAuthenticationToken(
                                CustomUser(user.roles("ADMIN").build(), u.uid),
                                "USER",
                                setOf(SimpleGrantedAuthority("USER"))),
                        u)
                )
            }
    }
}