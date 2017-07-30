package org.paradise.ipaq.integration;

import au.com.auspost.signing.aws.KmsEncryption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by terrence on 26/5/17.
 */
@Configuration
public class IntegrationConfig {

    public static final String ENCRYPTED_PERSISTED_VALUE = "Encrypted persisted value";

    @Value("${app.encryptionKey.arn}")
    private String arn;

    @Bean
    public RedisConnectionFactory connectionFactory() {

        RedisConnectionFactory redisConnectionFactory = mock(RedisConnectionFactory.class);
        RedisConnection redisConnection = mock(RedisConnection.class);

        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);

        return redisConnectionFactory;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate() {

        StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        doNothing().when(valueOperations).set(any(String.class), any(String.class));

        when(valueOperations.get(eq(AbstractIntegrationTest.TOKEN))).thenReturn(ENCRYPTED_PERSISTED_VALUE);
        when(valueOperations.get(eq(AbstractIntegrationTest.BEARER))).thenReturn(ENCRYPTED_PERSISTED_VALUE);

        doNothing().when(stringRedisTemplate).delete(anyString());

        return stringRedisTemplate;
    }

    @Bean
    public KmsEncryption kmsEncryption() throws IOException {

        KmsEncryption kmsEncryption = mock(KmsEncryption.class);

        when(kmsEncryption.encrypt(any(String.class))).thenReturn(ENCRYPTED_PERSISTED_VALUE);

        return kmsEncryption;
    }

}
