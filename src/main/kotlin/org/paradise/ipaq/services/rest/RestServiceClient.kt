package org.paradise.ipaq.services.rest

import org.apache.commons.lang3.math.NumberUtils
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.ProxyAuthenticationStrategy
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
class RestServiceClient {

    private val restTemplate: RestTemplate = RestTemplate()

    init {
        restTemplate.requestFactory = httpComponentsClientHttpRequestFactory()
        restTemplate.errorHandler = createResponseErrorHandler()
    }

    fun <T> exchange(url: String, method: HttpMethod, requestEntity: HttpEntity<*>, responseType: Class<T>, vararg uriVariables: Any): ResponseEntity<T>
            = restTemplate.exchange(url, method, requestEntity, responseType, *uriVariables)

    private fun httpComponentsClientHttpRequestFactory(): HttpComponentsClientHttpRequestFactory {

        /**
         * Consider using HttpClientBuilder.useSystemProperties() call in Apache HttpClient which use system properties
         * when creating and configuring default implementations, including http.proxyHost and http.proxyPort.

         * In Apache HTTP Client 5, http.proxyUser and http.proxyPassword also defined in:

         * @see org.apache.hc.client5.http.impl.auth.SystemDefaultCredentialsProvider
         */
        val systemProperties = System.getProperties()

        val proxyHost = systemProperties.getProperty(PROXY_HOST_PROPERTY)
        val proxyPort = NumberUtils.toInt(systemProperties.getProperty(PROXY_PORT_PROPERTY), -1)

        val proxyUser = systemProperties.getProperty(PROXY_USER_PROPERTY)
        val proxyPassword = systemProperties.getProperty(PROXY_PASSWORD_PROPERTY)

        val httpClientBuilder = HttpClientBuilder.create()

        if (proxyHost != null && proxyPort != -1) {
            httpClientBuilder.setProxy(HttpHost(proxyHost, proxyPort))

            if (proxyUser != null && proxyPassword != null) {
                val passwordCredentials = UsernamePasswordCredentials(proxyUser, proxyPassword)

                val credentialsProvider = BasicCredentialsProvider()
                credentialsProvider.setCredentials(AuthScope(proxyHost, proxyPort), passwordCredentials)

                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                httpClientBuilder.setProxyAuthenticationStrategy(ProxyAuthenticationStrategy())
            }
        }

        return HttpComponentsClientHttpRequestFactory(httpClientBuilder.build())
    }

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

    companion object {

        private val PROXY_HOST_PROPERTY = "http.proxyHost"
        private val PROXY_PORT_PROPERTY = "http.proxyPort"
        private val PROXY_USER_PROPERTY = "http.proxyUser"
        private val PROXY_PASSWORD_PROPERTY = "http.proxyPassword"
    }

}
