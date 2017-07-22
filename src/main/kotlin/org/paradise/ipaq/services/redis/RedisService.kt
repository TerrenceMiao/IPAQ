package org.paradise.ipaq.services.redis

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.IOException

/**
 * Created by terrence on 30/5/17.
 */
@Service
class RedisService(val redisClient: RedisClient, val objectMapper: ObjectMapper) {

    fun <T> persist(key: String, json: T): String {

        var jsonAsString: String? = try {
            objectMapper.writeValueAsString(json)
        } catch (e: JsonProcessingException) {
            LOG.error("Exception thrown while serialising JSON object: [{}]", e.message)
            null
        }

        LOG.debug("Persisting key [{}] / value [{}] into Redis server", key, jsonAsString)

        return redisClient.persist(key, jsonAsString!!)
    }

    fun <T> get(key: String, type: Class<T>): T? {

        LOG.debug("Retrieving from Redis server by key [{}]", key)

        val value = redisClient.get(key)

        if (StringUtils.isNotEmpty(value)) {
            try {
                return objectMapper.readValue(value, type)
            } catch (ex: Exception) {
                when (ex) {
                    is JsonParseException, is JsonMappingException -> {
                        LOG.error("JSON exception thrown while de-serialising object: [{}]" + ex.message)
                    }
                    is IOException -> {
                        LOG.error("IO exception thrown while de-serialising object: [{}]" + ex.message)
                    }
                }
            }
        } else {
            LOG.error(CANT_FIND_IN_REDIS_SERVER)
        }

        return null
    }

    fun delete(key: String) {

        LOG.debug("Deleting key [{}] from Redis server", key)

        redisClient.delete(key)
    }

    companion object {

        val CANT_FIND_IN_REDIS_SERVER = "Can't find in Redis server"

        private val LOG = LoggerFactory.getLogger(RedisService::class.java)
    }

}
