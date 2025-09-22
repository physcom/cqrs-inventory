package kg.akyl.java.inventory.infra.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class InventoryMetrics {
    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    @Qualifier("readJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    private Counter productCreatedCounter;
    private Counter saleProcessedCounter;
    private Counter inventoryUpdateCounter;
    private Timer productQueryTimer;
    private Timer saleProcessingTimer;
    private final AtomicLong lowStockItems = new AtomicLong();
    private final AtomicLong totalProducts = new AtomicLong();

    @PostConstruct
    public void initMetrics() {
        // Counters
        productCreatedCounter = Counter.builder("inventory.products.created")
                .description("Number of products created")
                .register(meterRegistry);

        saleProcessedCounter = Counter.builder("inventory.sales.processed")
                .description("Number of sales processed")
                .register(meterRegistry);

        inventoryUpdateCounter = Counter.builder("inventory.updates")
                .description("Number of inventory updates")
                .register(meterRegistry);

        // Timers
        productQueryTimer = Timer.builder("inventory.product.query.duration")
                .description("Time taken to query products")
                .register(meterRegistry);

        saleProcessingTimer = Timer.builder("inventory.sale.processing.duration")
                .description("Time taken to process sales")
                .register(meterRegistry);

        // Gauges
        meterRegistry.gauge("inventory.products.total", this, InventoryMetrics::getTotalProducts);

        meterRegistry.gauge("inventory.products.low_stock", this, InventoryMetrics::getLowStockItems);

        // Schedule periodic updates for gauges
        updateGaugeMetrics();
    }

    public void incrementProductsCreated() {
        productCreatedCounter.increment();
    }

    public void incrementSalesProcessed() {
        saleProcessedCounter.increment();
    }

    public void incrementInventoryUpdates() {
        inventoryUpdateCounter.increment();
    }

    public Timer.Sample startProductQueryTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordProductQueryTime(Timer.Sample sample) {
        sample.stop(productQueryTimer);
    }

    public Timer.Sample startSaleProcessingTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordSaleProcessingTime(Timer.Sample sample) {
        sample.stop(saleProcessingTimer);
    }

    public double getTotalProducts() {
        return totalProducts.get();
    }

    public double getLowStockItems() {
        return lowStockItems.get();
    }

    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 60000) // Every minute
    public void updateGaugeMetrics() {
        try {
            // Update total products count
            Long total = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM products WHERE status = 'ACTIVE'", Long.class);
            totalProducts.set(total != null ? total : 0);

            // Update low stock items count
            Long lowStock = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM products WHERE (quantity - reserved_quantity) <= 10 AND status = 'ACTIVE'",
                    Long.class);
            lowStockItems.set(lowStock != null ? lowStock : 0);
        } catch (Exception e) {
            // Log error but don't fail the application
            System.err.println("Error updating metrics: " + e.getMessage());
        }
    }
}
