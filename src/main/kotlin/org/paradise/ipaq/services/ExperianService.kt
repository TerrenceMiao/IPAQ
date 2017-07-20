package org.paradise.ipaq.services

import org.paradise.ipaq.domain.ExperianAddress
import org.paradise.ipaq.domain.ExperianSearchResult
import org.paradise.ipaq.services.rest.RestServiceClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service

/**
 * Created by terrence on 17/7/17.
 */
@Service
class ExperianService(private val restServiceClient: RestServiceClient,
                      @param:Value("\${app.experian.api.url}") private val experianApiUrl: String,
                      @param:Value("\${app.experian.api.token}") private val experianApiToken: String) {

    fun search(query: String?, country: String?, take: Int): ResponseEntity<ExperianSearchResult> {

        LOG.debug("Search address with query [{}], country [{}] and take [{}]", query, country, take)

        val urlStr = String.format(SEARCH_URL_FORMAT, experianApiUrl, query, country, take)

        return restServiceClient.exchange(urlStr, HttpMethod.GET, HttpEntity<Any>(requestHttpHeaders), ExperianSearchResult::class.java)
    }

    fun format(country: String?, id: String?): ResponseEntity<ExperianAddress> {

        LOG.debug("Format address for country [{}] and id [{}]", country, id)

        val urlStr = String.format(DETAILS_URL_FORMAT, experianApiUrl, country, id)

        return restServiceClient.exchange(urlStr, HttpMethod.GET, HttpEntity<Any>(requestHttpHeaders), ExperianAddress::class.java)
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