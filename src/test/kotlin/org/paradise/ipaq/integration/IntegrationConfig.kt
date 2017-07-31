package org.paradise.ipaq.integration

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.mockito.Matchers.any
import org.mockito.Matchers.anyString
import org.mockito.Mockito.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.connection.RedisConnection
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.nio.charset.Charset

/**
 * Created by terrence on 26/5/17.
 */
@Configuration
class IntegrationConfig {

    @Bean
    fun connectionFactory(): RedisConnectionFactory {

        val redisConnectionFactory = mock(RedisConnectionFactory::class.java)
        val redisConnection = mock(RedisConnection::class.java)

        `when`(redisConnectionFactory.connection).thenReturn(redisConnection)

        return redisConnectionFactory
    }

    @Bean
    fun stringRedisTemplate(): StringRedisTemplate {

        val stringRedisTemplate = mock(StringRedisTemplate::class.java)
        val valueOperations = mock(ValueOperations::class.java)

        @Suppress("UNCHECKED_CAST")
        `when`(stringRedisTemplate.opsForValue()).thenReturn(valueOperations as ValueOperations<String, String>?)

        `when`(valueOperations!!.get(eq("1 Infinite Loop, USA")))
                .thenReturn(StringUtils.EMPTY)
                .thenReturn(IOUtils.toString(ClassPathResource("search-resp.json").inputStream, Charset.defaultCharset()))

        doNothing().`when`<ValueOperations<String, String>>(valueOperations).set(any(String::class.java), any(String::class.java))

        doNothing().`when`(stringRedisTemplate).delete(anyString())

        return stringRedisTemplate
    }

}
