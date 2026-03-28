package com.grocerystore.grocery_store.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {

    @Bean
    RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>()
        template.setConnectionFactory(connectionFactory)
        template.setKeySerializer(new StringRedisSerializer())
        template.setValueSerializer(new StringRedisSerializer())
        return template
    }
}
