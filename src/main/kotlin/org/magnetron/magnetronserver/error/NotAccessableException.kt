package org.magnetron.magnetronserver.error

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.Exception

@ResponseStatus(value = HttpStatus.GONE)
class NotAccessableException(message: String) : Exception(message)