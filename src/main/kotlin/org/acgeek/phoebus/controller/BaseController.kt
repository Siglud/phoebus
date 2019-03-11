package org.acgeek.phoebus.controller

import org.acgeek.phoebus.dto.CustomUser
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.web.server.WebSession

interface BaseController {
    fun WebSession.currentUid(): String? {
        val principal = this.getAttribute<SecurityContextImpl?>(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY)
                ?.authentication?.principal
        if (principal is CustomUser) {
            return principal.uid
        }
        return null
    }
}