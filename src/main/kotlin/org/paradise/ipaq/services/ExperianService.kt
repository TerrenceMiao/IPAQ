package org.paradise.ipaq.services

import org.paradise.ipaq.Constants
import org.paradise.ipaq.domain.ExperianAddress
import org.paradise.ipaq.domain.ExperianSearchResult
import org.paradise.ipaq.services.redis.RedisService
import org.paradise.ipaq.services.rest.RestServiceClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import java.util.*
import kotlin.streams.toList

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

        var experianSearchResult = redisService.get(key, ExperianSearchResult::class.java)

        if (Objects.isNull(experianSearchResult)) {
            val experianSearchResultResponseEntity = restServiceClient.exchange(
                    String.format(SEARCH_URL_FORMAT, experianApiUrl, query, country, Constants.MAXIMUM_TAKE),
                    HttpMethod.GET, HttpEntity<Any>(requestHttpHeaders), ExperianSearchResult::class.java)

            if (experianSearchResultResponseEntity.hasBody() && experianSearchResultResponseEntity.body.count > 0) {
                redisService.persist(key, experianSearchResultResponseEntity.body)
            }

            experianSearchResult = experianSearchResultResponseEntity.body
        }

        return ResponseEntity(takeExperianSearchResult(experianSearchResult!!, take), HttpStatus.OK)
    }

    fun format(country: String, id: String): ResponseEntity<ExperianAddress> {

        LOG.debug("Format address for country [{}] and id [{}]", country, id)

        val key = id + ", " + country

        var experianAddressResult = redisService.get(key, ExperianAddress::class.java)

        if (Objects.isNull(experianAddressResult)) {
            val experianAddressResultResponseEntity = restServiceClient.exchange(String.format(DETAILS_URL_FORMAT, experianApiUrl, country, id),
                    HttpMethod.GET, HttpEntity<Any>(requestHttpHeaders), ExperianAddress::class.java)

            if (experianAddressResultResponseEntity.hasBody()
                    && Objects.nonNull(experianAddressResultResponseEntity.body.address)
                    && Objects.nonNull(experianAddressResultResponseEntity.body.components)) {
                redisService.persist(key, experianAddressResultResponseEntity.body)
            }

            experianAddressResult = experianAddressResultResponseEntity.body
        }

        return ResponseEntity(experianAddressResult!!, HttpStatus.OK)
    }

    private fun takeExperianSearchResult(experianSearchResult: ExperianSearchResult, take: Int): ExperianSearchResult {

        return if (experianSearchResult.count > take) ExperianSearchResult(take, experianSearchResult.results.stream().limit(take.toLong()).toList()) else experianSearchResult
    }

    private val requestHttpHeaders: HttpHeaders
        get() {
            val requestHeaders = HttpHeaders()

            requestHeaders.accept = listOf<MediaType>(MediaType.APPLICATION_JSON)
            requestHeaders.set(Constants.HTTP_HEADER_AUTH_TOKEN, experianApiToken)

            return requestHeaders
        }

    companion object {

        private val LOG = LoggerFactory.getLogger(ExperianService::class.java)

        private val SEARCH_URL_FORMAT = "%s/Search?query=%s&country=%s&take=%s"
        private val DETAILS_URL_FORMAT = "%s/format?country=%s&id=%s"
    }

}