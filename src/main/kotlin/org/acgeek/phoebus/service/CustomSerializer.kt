package org.acgeek.phoebus.service

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import java.time.LocalDateTime
import java.time.ZoneOffset

@Serializer(forClass = LocalDateTime::class)
class LocalDateTimeSerializer {
    override val descriptor: SerialDescriptor
        get() = StringDescriptor.withName("LocalDateTime")

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.ofEpochSecond(decoder.decodeLong(), 0, ZoneOffset.UTC)
    }

    override fun serialize(encoder: Encoder, obj: LocalDateTime) {
        encoder.encodeLong(obj.toEpochSecond(ZoneOffset.UTC))
    }
}