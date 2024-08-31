package com.neu.vansven.config;

import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import springfox.documentation.spring.web.json.Json;

@Configuration
public class RedisTemplateConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        // redis 序列化：将 key 和 value 对象转换为字节或者其他形式的序列化值
        // reids 反序列胡：将 key 和 value 的序列化值组转换为对象，并将它和给定的key关联

        //采用StringRedisSerializer策略序列化反序列化 String类型的 key
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);

         // 采用Jackson2JsonRedisSerializer序列化反序列化 Object类型的 value
        Jackson2JsonRedisSerializer<Object> jsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        redisTemplate.setValueSerializer(jsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jsonRedisSerializer);
        return redisTemplate;
    }

//    @Bean
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory){
//        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
//        redisTemplate.setConnectionFactory(connectionFactory);
//
//        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
//
//        // 采用StringRedisSerializer策略 序列化和反序列化 key
//        redisTemplate.setKeySerializer(stringRedisSerializer);
//        //采用StringRedisSerializer策略 序列化和反序列化 value
//        redisTemplate.setValueSerializer(stringRedisSerializer);
//        return redisTemplate;
//    }


}
