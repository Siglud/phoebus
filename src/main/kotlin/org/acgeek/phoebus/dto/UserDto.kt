package org.acgeek.phoebus.dto

import org.acgeek.phoebus.model.UserDo
import org.hibernate.validator.constraints.Length
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import javax.validation.constraints.Email

class CustomUser(u: UserDetails, val uid: String?): UserDetails by u

class CustomAuthentication(auth: UsernamePasswordAuthenticationToken, val user: UserDo?): Authentication by auth

data class UserDto(
        @field:Email(regexp = ".+@.+\\..+", message = "{user.mail.illegal}")
        @get:Length(min=1, max = 200, message = "{user.mail.length.illegal}") val userEmail: String,
        @get:Length(min=1, max = 200, message = "{user.length.illegal}") val nickName: String,
        @get:Length(min=8, max = 30, message = "{user.password.length.illegal}") val userPassword: String,
        @get:Length(min=8, max = 100, message = "{user.avatar.length.illegal}")val avatar: String?,
        @get:Length(min=8, max = 200, message = "{user.description.length.illegal}")val description: String?
)

data class UserUpdateDto(
        @field:Email(regexp = ".+@.+\\..+", message = "{user.mail.illegal}")
        @get:Length(min=1, max = 200, message = "{user.mail.length.illegal}") val userEmail: String?,
        @get:Length(min=1, max = 200, message = "{user.length.illegal}") val nickName: String?,
        @get:Length(min=8, max = 30, message = "{user.password.length.illegal}") val userPassword: String?,
        @get:Length(min=8, max = 30, message = "{user.password.length.illegal}") val oldPassword: String?,
        @get:Length(min=8, max = 100, message = "{user.avatar.length.illegal}")val avatar: String?,
        @get:Length(min=8, max = 200, message = "{user.description.length.illegal}")val description: String?
)