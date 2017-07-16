package org.paradise.ipaq

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class ColtApplication

fun main(args: Array<String>) {

    SpringApplication.run(ColtApplication::class.java, *args)
}
