package org.paradise.ipaq.controllers

import com.fasterxml.jackson.databind.JsonNode
import org.paradise.ipaq.Constants
import org.paradise.ipaq.domain.ExperianAddress
import org.paradise.ipaq.domain.ExperianSearchResult
import org.paradise.ipaq.services.ExperianService
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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

    @RequestMapping(value = "/healthCheck", method = arrayOf(RequestMethod.GET), produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun healthCheck(@RequestHeader(value = Constants.COUNTRY, required = false) country: String?): ResponseEntity<JsonNode>  {

        LOG.debug("Country [{}]", country)

        return experianService.healthCheck()
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(IpaqController::class.java)
    }

}

