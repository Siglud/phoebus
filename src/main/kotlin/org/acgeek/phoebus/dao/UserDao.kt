package org.acgeek.phoebus.dao

import org.acgeek.phoebus.model.UserDo
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


interface UserRepository: ReactiveMongoRepository<UserDo, String> {

    fun getUserDoByUid(id: String): Mono<UserDo?>

    fun findAllBy(page: Pageable): Flux<UserDo>
}