package org.paradise.ipaq.services

import org.paradise.ipaq.domain.ExperianAddress
import org.paradise.ipaq.domain.ExperianSearchResult
import org.paradise.ipaq.services.redis.RedisService
import org.paradise.ipaq.services.rest.RestServiceClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import java.util.*

/**
 * Created by terrence on 17/7/17.
 */
@Service
class ExperianService(val restServiceClient: RestServiceClient,
                      val redisService: RedisService,
                      @param:Value("\${app.experian.api.url}") val experianApiUrl: String,
                      @param:Value("\${app.experian.api.token}") val experianApiToken: String) {

    fun search(query: String, country: String, take: Int): ResponseEntity<ExperianSearchResult> {

        LOG.debug("Search address with query [{}], country [{}] and take [{}]", query, country, take)

        val key = query + ", " + country

        val experianSearchResult = redisService.get(key, ExperianSearchResult::class.java)

        if (Objects.nonNull(experianSearchResult)) {
            return ResponseEntity(experianSearchResult!!, HttpStatus.OK)
        } else {
            val experianSearchResultResponseEntity = restServiceClient.exchange(String.format(SEARCH_URL_FORMAT, experianApiUrl, query, country, take),
                    HttpMethod.GET, HttpEntity<Any>(requestHttpHeaders), ExperianSearchResult::class.java)

            if (Objects.nonNull(experianSearchResultResponseEntity.body)) {
                redisService.persist(key, experianSearchResultResponseEntity.body)
            }

            return experianSearchResultResponseEntity
        }
    }

    fun format(country: String, id: String): ResponseEntity<ExperianAddress> {

        LOG.debug("Format address for country [{}] and id [{}]", country, id)

        return restServiceClient.exchange(String.format(DETAILS_URL_FORMAT, experianApiUrl, country, id),
                HttpMethod.GET, HttpEntity<Any>(requestHttpHeaders), ExperianAddress::class.java)
    }

    private val requestHttpHeaders: HttpHeaders
        get() {
            val requestHeaders = HttpHeaders()

            requestHeaders.accept = listOf<MediaType>(MediaType.APPLICATION_JSON)
            requestHeaders.set(HTTP_HEADER_AUTH_TOKEN, experianApiToken)

            return requestHeaders
        }

    companion object {

        private val LOG = LoggerFactory.getLogger(ExperianService::class.java)

        private val HTTP_HEADER_AUTH_TOKEN = "Auth-Token"

        private val SEARCH_URL_FORMAT = "%s/Search?query=%s&country=%s&take=%s"
        private val DETAILS_URL_FORMAT = "%s/format?country=%s&id=%s"
    }

}