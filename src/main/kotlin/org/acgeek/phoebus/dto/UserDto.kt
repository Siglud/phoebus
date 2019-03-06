package org.acgeek.phoebus.dto

import org.hibernate.validator.constraints.Length
import javax.validation.constraints.Email


data class UserDto(
        @field:Email(regexp = ".+@.+\\..+", message = "{user.mail.illegal}")
        @get:Length(min=1, max = 200, message = "{user.mail.length.illegal}") val userEmail: String,
        @get:Length(min=1, max = 200, message = "{user.length.illegal}") val nickName: String,
        @get:Length(min=8, max = 30, message = "{user.password.length.illegal}") val userPassword: String,
        val avatar: String?,
        val description: String?
)