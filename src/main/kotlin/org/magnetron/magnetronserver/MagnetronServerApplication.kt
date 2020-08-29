package org.magnetron.magnetronserver

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class MagnetronServerApplication

fun main(args: Array<String>) {
	SpringApplication.run(MagnetronServerApplication::class.java, *args)
}
