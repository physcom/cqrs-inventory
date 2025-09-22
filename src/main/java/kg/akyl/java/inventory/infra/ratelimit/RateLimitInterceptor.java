package kg.akyl.java.inventory.infra.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final long DEFAULT_LIMIT = 1000; // requests per minute
    private static final Duration WINDOW_SIZE = Duration.ofMinutes(1);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientId = getClientId(request);
        String key = RATE_LIMIT_PREFIX + clientId;

        Long currentCount = redisTemplate.opsForValue().increment(key);

        if (currentCount == 1) {
            // First request in the window, set expiration
            redisTemplate.expire(key, WINDOW_SIZE);
        }

        if (currentCount > DEFAULT_LIMIT) {
            response.setStatus(429); // Too Many Requests
            response.getWriter().write("{\"error\":\"Rate limit exceeded\"}");
            return false;
        }

        // Add rate limit headers
        response.setHeader("X-RateLimit-Limit", String.valueOf(DEFAULT_LIMIT));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, DEFAULT_LIMIT - currentCount)));
        response.setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + WINDOW_SIZE.toMillis()));

        return true;
    }

    private String getClientId(HttpServletRequest request) {
        // In production, you might use API keys, user IDs, or other identifiers
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null) {
            return "api:" + apiKey;
        }

        // Fallback to IP address
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null) {
            clientIp = request.getRemoteAddr();
        }
        return "ip:" + clientIp;
    }
}
