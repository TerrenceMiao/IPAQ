package org.paradise.ipaq

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class ColtApplication

object Constants {

    val CUSTOMER_NAME_VARIABLE: String = "customer_name"

    val MAXIMUM_TAKE: Int = 100
}

fun main(args: Array<String>) {

    SpringApplication.run(ColtApplication::class.java, *args)
}
