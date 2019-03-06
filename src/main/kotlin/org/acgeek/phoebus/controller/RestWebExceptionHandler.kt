package org.acgeek.phoebus.controller

import org.acgeek.phoebus.exception.PhoebusResourceNotExistsException
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebExceptionHandler
import reactor.core.publisher.Mono

/**
 * 负责处理全部的已知异常，抛出所有的未知异常
 */
@Component
@Order(-2)
class  RestWebExceptionHandler: WebExceptionHandler {
    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        if (ex is PhoebusResourceNotExistsException) {
            exchange.response.statusCode = HttpStatus.NOT_FOUND
            return exchange.response.setComplete()
        }
        return Mono.error(ex)
    }

}