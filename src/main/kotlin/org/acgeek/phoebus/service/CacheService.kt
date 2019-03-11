package org.acgeek.phoebus.service

import kotlinx.serialization.*
import kotlinx.serialization.context.getOrDefault
import kotlinx.serialization.internal.HexConverter
import kotlinx.serialization.protobuf.ProtoBuf
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.function.Supplier
import kotlin.reflect.KClass

@Service
class CacheService(private val redisTemplate: ReactiveRedisTemplate<String, String>) {

    @ImplicitReflectionSerializer
    fun <T: Any> getFromCache(cacheKey: String, clazz: KClass<T>, supplier: Supplier<Mono<T>>): Mono<T> {
        return redisTemplate.opsForValue().get(cacheKey).map {
            ProtoBuf.load(ProtoBuf().context.getOrDefault(clazz), HexConverter.parseHexBinary(it))
        }.switchIfEmpty(supplier.get().flatMap {
            redisTemplate.opsForValue().set(cacheKey, ProtoBuf.dumps(ProtoBuf().context.getOrDefault(clazz), it))
                        .thenReturn(it)
        })
    }
}