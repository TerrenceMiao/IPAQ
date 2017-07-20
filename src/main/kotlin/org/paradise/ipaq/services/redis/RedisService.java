package org.paradise.ipaq.services.redis;

import au.com.auspost.microservice.exception.ApiException;
import au.com.auspost.microservice.service.SessionStore;
import au.com.auspost.signing.aws.KmsEncryption;
import com.fasterxml.jackson.databind.ObjectMapper;
import javaslang.control.Try;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Created by terrence on 30/5/17.
 */
@Service
public class RedisService {

    public static final String CANT_FIND_SESSION_IN_CACHE = "Can't find session in cache";

    @Autowired
    private RedisClient redisClient;

    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public <T> String persist(String key, T json) {

        String jsonAsString = Try.of(() -> objectMapper.writeValueAsString(json))
                .getOrElseThrow(Exception::new);

        return redisClient.persist(key, jsonAsString);
    }

    @Override
    public <T> T get(String key, Class<T> type) {

        String persistedValue = redisClient.get(key);

        if (StringUtils.isNotEmpty(persistedValue)) {
            return Try.of(() -> objectMapper.readValue(persistedValue, type))
                    .getOrElseThrow(Exception::new);
        } else {
            throw new Exception(CANT_FIND_SESSION_IN_CACHE);
        }
    }

    @Override
    public void delete(String key) {

        redisClient.delete(key);
    }

}
