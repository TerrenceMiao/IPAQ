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
class IpaqController(val experianService: ExperianService, var environmentLocal: EnvironmentLocal) {

    @RequestMapping(value = "/Search", method = arrayOf(RequestMethod.GET), produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun search(@RequestParam(value = "query") query: String,
               @RequestParam(value = "country") country: String,
               @RequestParam(value = "take", defaultValue = "10") take: Int): ResponseEntity<ExperianSearchResult> {

        // Save environment dependent variables into request scope bean
        environmentLocal.query = query
        environmentLocal.country = country

        return experianService.search(query, country, take)
    }

    @RequestMapping(value = "/format", method = arrayOf(RequestMethod.GET), produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun format(@RequestParam(value = "country") country: String,
               @RequestParam(value = "id") id: String): ResponseEntity<ExperianAddress>
            = experianService.format(country, id)

}

