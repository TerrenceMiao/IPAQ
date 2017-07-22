package org.paradise.ipaq.services.redis

import org.springframework.data.redis.RedisSystemException
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

/**
 * Created by terrence on 25/5/17.
 */
@Component
class RedisClient(val stringRedisTemplate: StringRedisTemplate) {

    val valueOperations = stringRedisTemplate.opsForValue()!!

    fun persist(key: String, value: String): String {

        valueOperations.set(key, value)

        return valueOperations.get(key) ?: throw RedisSystemException("Redis Server persistence error", null)
    }

    fun get(key: String): String? = valueOperations.get(key)

    fun delete(key: String) = stringRedisTemplate.delete(key)

}
