package org.paradise.ipaq.config

import org.apache.commons.lang3.math.NumberUtils
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.ProxyAuthenticationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory

import java.util.Properties

/**
 * Created by terrence on 13/5/17.
 */
@Configuration
class ProxyConfig {

    @Bean
    fun httpComponentsClientHttpRequestFactory(): HttpComponentsClientHttpRequestFactory {

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

        if (proxyHost != null && proxyPort !== -1) {
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

    companion object {

        private val PROXY_HOST_PROPERTY = "http.proxyHost"
        private val PROXY_PORT_PROPERTY = "http.proxyPort"
        private val PROXY_USER_PROPERTY = "http.proxyUser"
        private val PROXY_PASSWORD_PROPERTY = "http.proxyPassword"
    }

}
