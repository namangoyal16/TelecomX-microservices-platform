package com.telecomx.provisioning.service;

import com.telecomx.provisioning.dto.SubscriberStatusCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class SubscriberCacheService {

    private static final String KEY_PREFIX = "subscriber:status:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final long ttlSeconds;

    public SubscriberCacheService(RedisTemplate<String, Object> redisTemplate,
                                   @Value("${cache.subscriber-status-ttl-seconds}") long ttlSeconds) {
        this.redisTemplate = redisTemplate;
        this.ttlSeconds = ttlSeconds;
    }

    public void put(Long customerId, SubscriberStatusCache status) {
        redisTemplate.opsForValue().set(KEY_PREFIX + customerId, status, Duration.ofSeconds(ttlSeconds));
    }

    public SubscriberStatusCache get(Long customerId) {
        return (SubscriberStatusCache) redisTemplate.opsForValue().get(KEY_PREFIX + customerId);
    }

    public void evict(Long customerId) {
        redisTemplate.delete(KEY_PREFIX + customerId);
    }
}
