package org.paradise.ipaq.controllers

import org.paradise.ipaq.domain.ExperianAddress
import org.paradise.ipaq.domain.ExperianSearchResult
import org.paradise.ipaq.services.ExperianService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Created by terrence on 17/7/17.
 */
@RestController
class ipaqController(private val experianService: ExperianService) {

    @RequestMapping(value = "/Search", method = arrayOf(RequestMethod.GET), produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun search(@RequestParam(value = "query") query: String? = null,
               @RequestParam(value = "country") country: String? = null,
               @RequestParam(value = "take", defaultValue = "10") take: Int): ResponseEntity<ExperianSearchResult> {

        return experianService.search(query, country, take)
    }

    @RequestMapping(value = "/format", method = arrayOf(RequestMethod.GET), produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun format(@RequestParam(value = "country") country: String? = null,
               @RequestParam(value = "id") id: String? = null): ResponseEntity<ExperianAddress> {

        return experianService.format(country, id)
    }

}