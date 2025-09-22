package kg.akyl.java.inventory.infra.locking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class DistributedLockManager {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String LOCK_PREFIX = "inventory:lock:";
    private static final long DEFAULT_LOCK_TIMEOUT = 30; // seconds

    public boolean acquireLock(String lockKey, long timeoutSeconds) {
        String key = LOCK_PREFIX + lockKey;
        String value = Thread.currentThread().getName() + ":" + System.currentTimeMillis();

        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(key, value, Duration.ofSeconds(timeoutSeconds));

        return Boolean.TRUE.equals(acquired);
    }

    public boolean acquireLock(String lockKey) {
        return acquireLock(lockKey, DEFAULT_LOCK_TIMEOUT);
    }

    public void releaseLock(String lockKey) {
        String key = LOCK_PREFIX + lockKey;
        redisTemplate.delete(key);
    }

    public boolean isLocked(String lockKey) {
        String key = LOCK_PREFIX + lockKey;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // Execute code with distributed lock
    public <T> T executeWithLock(String lockKey, java.util.function.Supplier<T> supplier) {
        if (!acquireLock(lockKey)) {
            throw new RuntimeException("Could not acquire lock for key: " + lockKey);
        }

        try {
            return supplier.get();
        } finally {
            releaseLock(lockKey);
        }
    }

    public void executeWithLock(String lockKey, Runnable runnable) {
        executeWithLock(lockKey, () -> {
            runnable.run();
            return null;
        });
    }
}
