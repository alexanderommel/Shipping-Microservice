package com.tongue.shippingservice.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration

public class RedisSessionConfig {



    @Bean
    public ConfigureRedisAction configureRedisAction(){
        return ConfigureRedisAction.NO_OP;
    }


    @Bean
    @ConditionalOnMissingBean(name="redisTemplate")
    @Primary
    public <T> RedisTemplate<String, T>
    redisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<String,T> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        ObjectMapper om = new ObjectMapper();
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.registerModule(new JavaTimeModule());

        Jackson2JsonRedisSerializer json2 = new Jackson2JsonRedisSerializer(Object.class);
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer(om));
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(om));
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setStringSerializer(new StringRedisSerializer());

        return redisTemplate;
    }

}
