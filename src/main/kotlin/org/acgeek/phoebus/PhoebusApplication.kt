package org.acgeek.phoebus

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PhoebusApplication

fun main(args: Array<String>) {
    runApplication<PhoebusApplication>(*args)
}
