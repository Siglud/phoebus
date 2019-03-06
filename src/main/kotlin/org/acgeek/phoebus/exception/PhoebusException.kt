package org.acgeek.phoebus.exception


open class PhoebusBaseException(message: String, val args: Array<Any>? = null): RuntimeException(message)

class PhoebusParameterException(message: String, args: Array<Any>? = null): PhoebusBaseException(message, args)

class PhoebusResourceNotExistsException(message: String, args: Array<Any>? = null): PhoebusBaseException(message, args)