package com.grocerystore.grocery_store.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class ProductCacheService {

    private final RedisTemplate<String, Object> redisTemplate

    ProductCacheService(@Qualifier('redisTemplate') RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate
    }

    void saveProductName(Long id, String name) {
        String key = "product:${id}".toString()   // <-- convert GString -> String
        redisTemplate.opsForValue().set(key, name)
        // Set expiration time to 5 minutes (300 seconds)
        redisTemplate.expire(key, 5, java.util.concurrent.TimeUnit.MINUTES)
    }

    String getProductName(Long id) {
        String key = "product:${id}".toString()   // same here
        return redisTemplate.opsForValue().get(key)
    }
}
