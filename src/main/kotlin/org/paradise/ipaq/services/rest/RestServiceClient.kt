package org.paradise.ipaq.services.rest

import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate
import java.io.IOException

/**
 * RESTful service client, based on Spring RestTemplate and Apache HTTP Client.
 */
@Component
class RestServiceClient(httpComponentsClientHttpRequestFactory: HttpComponentsClientHttpRequestFactory) {

    private val restTemplate: RestTemplate = RestTemplate()

    init {
        restTemplate.requestFactory = httpComponentsClientHttpRequestFactory
        restTemplate.errorHandler = createResponseErrorHandler()
    }

    fun <T> exchange(url: String, method: HttpMethod, requestEntity: HttpEntity<*>, responseType: Class<T>, vararg uriVariables: Any): ResponseEntity<T>
            = restTemplate.exchange(url, method, requestEntity, responseType, *uriVariables)

    /**
     * By pass the default Exception Handler. When make RESTful call, disable any error handler when use this
     * restTemplate, and directly back error message to service(s) or controller(s).

     * @return ResponseErrorHandler created
     */
    private fun createResponseErrorHandler(): ResponseErrorHandler {

        return object : ResponseErrorHandler {

            @Throws(IOException::class)
            override fun hasError(response: ClientHttpResponse): Boolean {

                return false
            }

            @Throws(IOException::class)
            override fun handleError(response: ClientHttpResponse) {

            }
        }
    }

}
