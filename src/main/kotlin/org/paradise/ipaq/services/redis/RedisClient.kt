package org.paradise.ipaq.services.redis

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.RedisSystemException
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

/**
 * Created by terrence on 25/5/17.
 */
@Component
class RedisClient {

    @Autowired
    private val stringRedisTemplate: StringRedisTemplate? = null

    fun persist(key: String, value: String): String {

        val valueOperations = stringRedisTemplate!!.opsForValue()

        valueOperations.set(key, value)

        val persistedValue = valueOperations.get(key)

        if (value != persistedValue) {
            throw RedisSystemException("Redis Server persistence error", null)
        }

        LOG.info("Key [{}] and Value [{}] has been successfully persisted into Redis server", key, persistedValue)

        return persistedValue
    }

    fun get(key: String): String {

        val valueOperations = stringRedisTemplate!!.opsForValue()

        val persistedValue = valueOperations.get(key)

        LOG.info("Value [{}] has been gotten from Redis server with key [{}]", persistedValue, key)

        return persistedValue
    }

    fun delete(key: String) {

        stringRedisTemplate!!.delete(key)

        LOG.info("Key [{}] has been deleted from Redis server", key)
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(RedisClient::class.java)
    }

}
