package org.acgeek.phoebus.controller

import kotlinx.serialization.*
import kotlinx.serialization.protobuf.ProtoBuf
import org.acgeek.phoebus.constan.PackageConst
import org.acgeek.phoebus.dao.UserRepository
import org.acgeek.phoebus.dto.UserDto
import org.acgeek.phoebus.exception.PhoebusResourceNotExistsException
import org.acgeek.phoebus.model.UserDo
import org.acgeek.phoebus.service.CacheService
import org.acgeek.phoebus.service.PackageUtils
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.*
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
                     private val packageUtils: PackageUtils) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserController::class.java)
    }

    @GetMapping
    fun listUser(@RequestParam("status") status: Int?,
                 @Positive @RequestParam("page") page: Int?,
                 @Positive @RequestParam("per_page") pageSize: Int?): Flux<UserDo> {
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
}