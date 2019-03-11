package org.acgeek.phoebus.controller

import kotlinx.serialization.*
import org.acgeek.phoebus.constan.PackageConst
import org.acgeek.phoebus.dao.UserRepository
import org.acgeek.phoebus.dto.UserDto
import org.acgeek.phoebus.dto.UserUpdateDto
import org.acgeek.phoebus.exception.PhoebusResourceNotExistsException
import org.acgeek.phoebus.model.UserDo
import org.acgeek.phoebus.service.CacheService
import org.acgeek.phoebus.service.PackageUtils
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.WebSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*
import java.util.function.Supplier
import javax.validation.Valid
import javax.validation.constraints.Positive


@RestController
@RequestMapping("/api/user")
class UserController(private val userRepository: UserRepository,
                     private val cacheService: CacheService,
                     private val packageUtils: PackageUtils): BaseController {
    companion object {
        private val logger = LoggerFactory.getLogger(UserController::class.java)
    }

    @GetMapping
    fun listUser(@RequestParam("status") status: Int?,
                 @Positive @RequestParam("page") page: Int?,
                 @Positive @RequestParam("per_page") pageSize: Int?,
                 session: WebSession): Flux<UserDo> {
        logger.info(session.currentUid())
        val limit = pageSize ?: PackageConst.DEFAULT_PER_PAGE
        val start = page ?: 0
        return userRepository.findAllBy(PageRequest.of(start, limit)).cache()
    }

    @ImplicitReflectionSerializer
    @GetMapping("/{userId:[0-9a-z\\-]+}")
    fun getUserById(@PathVariable userId: String): Mono<UserDo> {
        val key = "u_$userId"
        return cacheService.getFromCache(key, UserDo::class, Supplier { userRepository.getUserDoByUid(userId) })
                .switchIfEmpty(Mono.error(PhoebusResourceNotExistsException("")))
    }

    @PostMapping
    fun addUser(@RequestBody @Valid user: UserDto): Mono<UserDo> {
        val userId = UUID.randomUUID().toString()
        val newUser = UserDo(
                uid = userId,
                mail = user.userEmail,
                nick = user.nickName,
                password = packageUtils.sha512Hash(user.userPassword + userId),
                avatar = user.avatar ?: "",
                credit = 0L,
                desc = user.description ?: "",
                status = 0,
                create = LocalDateTime.now(),
                active = LocalDateTime.now()
        )
        return userRepository.save(newUser)
    }

    @DeleteMapping("/{userId:[0-9a-z\\-]+}")
    fun deleteUser(@PathVariable userId: String): Mono<Void> {
        return userRepository.deleteById(userId)
    }

    @PutMapping("/{userId:[0-9a-z\\-]+}")
    fun updateUser(@PathVariable userId: String, @RequestBody @Valid user: UserUpdateDto): Mono<UserDo> {
        return userRepository.getUserDoByUid(userId).flatMap {
            it.mail = user.userEmail ?: it.mail
            userRepository.save(it).thenReturn(it)
        }.switchIfEmpty(Mono.error(PhoebusResourceNotExistsException("")))
    }
}