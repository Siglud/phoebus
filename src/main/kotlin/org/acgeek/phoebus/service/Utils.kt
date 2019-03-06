package org.acgeek.phoebus.service

import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.util.*

@Service
class PackageUtils {
    private fun hashBytesToHex(byteArray: ByteArray): String {
        return with(StringBuilder(128)) {
            byteArray.forEach {
                val hexStr = Integer.toHexString((it.toInt().and(0xFF)))
                if (hexStr.length == 1) {
                    append("0").append(hexStr)
                } else {
                    append(hexStr)
                }
            }
            this.toString()
        }
    }

    fun sha512Hash(input: String): String {
        return hashBytesToHex(MessageDigest.getInstance("SHA-512").digest(input.toByteArray()))
    }

    fun createPasswordSalt(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }
}