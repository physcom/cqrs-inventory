package kg.gns.java.inventorysystem.infra.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component("inventoryHealth")
public class InventoryHealthIndicator implements HealthIndicator {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public Health health() {
        Health.Builder healthBuilder = Health.up();

        // Check database connectivity
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                healthBuilder.withDetail("database", "UP");
            } else {
                healthBuilder.down().withDetail("database", "Connection invalid");
            }
        } catch (Exception e) {
            healthBuilder.down().withDetail("database", "DOWN: " + e.getMessage());
        }

        // Check Redis connectivity
        try {
            redisTemplate.opsForValue().get("health-check");
            healthBuilder.withDetail("redis", "UP");
        } catch (Exception e) {
            healthBuilder.down().withDetail("redis", "DOWN: " + e.getMessage());
        }

        // Check Kafka connectivity
        try {
            kafkaTemplate.getProducerFactory().createProducer().close();
            healthBuilder.withDetail("kafka", "UP");
        } catch (Exception e) {
            healthBuilder.down().withDetail("kafka", "DOWN: " + e.getMessage());
        }

        return healthBuilder.build();
    }
}
