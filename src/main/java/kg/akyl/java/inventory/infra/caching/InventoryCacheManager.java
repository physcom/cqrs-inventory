package kg.gns.java.inventorysystem.infra.caching;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

@Component
public class InventoryCacheManager {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String PRODUCT_CACHE_PREFIX = "product:";
    private static final String ANALYTICS_CACHE_PREFIX = "analytics:";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);

    public void cacheProductData(String productId, Object productData) {
        String key = PRODUCT_CACHE_PREFIX + productId;
        redisTemplate.opsForValue().set(key, productData, DEFAULT_TTL);
    }

    public Object getProductData(String productId) {
        String key = PRODUCT_CACHE_PREFIX + productId;
        return redisTemplate.opsForValue().get(key);
    }

    public void evictProductCache(String productId) {
        String key = PRODUCT_CACHE_PREFIX + productId;
        redisTemplate.delete(key);
    }

    public void evictProductCacheByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(PRODUCT_CACHE_PREFIX + pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public void cacheAnalyticsData(String cacheKey, Object analyticsData, Duration ttl) {
        String key = ANALYTICS_CACHE_PREFIX + cacheKey;
        redisTemplate.opsForValue().set(key, analyticsData, ttl);
    }

    public Object getAnalyticsData(String cacheKey) {
        String key = ANALYTICS_CACHE_PREFIX + cacheKey;
        return redisTemplate.opsForValue().get(key);
    }

    public void evictAnalyticsCache() {
        Set<String> keys = redisTemplate.keys(ANALYTICS_CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    // Warm up frequently accessed data
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 300000) // Every 5 minutes
    public void warmUpCache() {
        try {
            // This could pre-load frequently accessed products or analytics
            // Implementation depends on specific business requirements
        } catch (Exception e) {
            System.err.println("Error warming up cache: " + e.getMessage());
        }
    }
}
