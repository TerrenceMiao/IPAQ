package org.paradise.ipaq.services.redis;

import au.com.auspost.microservice.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

/**
 * Created by terrence on 25/5/17.
 */
@Component
public class RedisClient {

    private static final Logger LOG = LoggerFactory.getLogger(RedisClient.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public String persist(String key, String value) {

        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();

        valueOperations.set(key, value);

        String persistedValue = valueOperations.get(key);

        if (!value.equals(persistedValue)) {
            throw new RedisSystemException(Constants.REDIS_SERVER_PERSISTENCE_ERROR, null);
        }

        LOG.info("Key [{}] and Value [{}] has been successfully persisted into Redis server", key, persistedValue);

        return persistedValue;
    }

    public String get(String key) {

        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();

        String persistedValue = valueOperations.get(key);

        LOG.info("Value [{}] has been gotten from Redis server with key [{}]", persistedValue, key);

        return persistedValue;
    }

    public void delete(String key) {

        stringRedisTemplate.delete(key);

        LOG.info("Key [{}] has been deleted from Redis server", key);
    }

}
