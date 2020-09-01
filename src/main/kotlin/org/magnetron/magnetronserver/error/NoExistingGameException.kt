package org.magnetron.magnetronserver.error

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class NoExistingGameException(message: String = "") : RuntimeException(message)