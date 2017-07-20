package org.paradise.ipaq.services.redis

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.IOException

/**
 * Created by terrence on 30/5/17.
 */
@Service
class RedisService {

    @Autowired
    private val redisClient: RedisClient? = null

    @Autowired
    private val objectMapper: ObjectMapper? = null


    fun <T> persist(key: String, json: T): String {

        var jsonAsString: String? = null

        try {
            jsonAsString = objectMapper!!.writeValueAsString(json)
        } catch (e: JsonProcessingException) {
            LOG.error("Exception thrown while serialising JSON object: " + e.message)
        }

        return redisClient!!.persist(key, jsonAsString!!)
    }

    operator fun <T> get(key: String, type: Class<T>): T? {

        val persistedValue = redisClient!!.get(key)

        if (StringUtils.isNotEmpty(persistedValue)) {
            try {
                return objectMapper!!.readValue(persistedValue, type)
            } catch (e: JsonParseException) {
                LOG.error("JSON exception thrown while de-serialising object: " + e.message)
            } catch (e: JsonMappingException) {
                LOG.error("JSON exception thrown while de-serialising object: " + e.message)
            } catch (e: IOException) {
                LOG.error("IO exception thrown while de-serialising object: " + e.message)
            }
        } else {
            LOG.error(CANT_FIND_SESSION_IN_CACHE)
        }

        return null
    }

    fun delete(key: String) {

        redisClient!!.delete(key)
    }

    companion object {

        val CANT_FIND_SESSION_IN_CACHE = "Can't find session in cache"

        private val LOG = LoggerFactory.getLogger(RedisService::class.java)
    }

}
