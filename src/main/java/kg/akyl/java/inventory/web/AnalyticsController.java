package kg.akyl.java.inventory.web;

import kg.akyl.java.inventory.query.handlers.AnalyticsQueryHandler;
import kg.akyl.java.inventory.query.projections.InventoryAnalyticsProjection;
import kg.akyl.java.inventory.query.projections.SalesAnalyticsProjection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    @Autowired
    private AnalyticsQueryHandler analyticsQueryHandler;

    @Autowired
    @Qualifier("queryExecutor")
    private Executor queryExecutor;

    @GetMapping("/sales")
    @Async("queryExecutor")
    public CompletableFuture<ResponseEntity<List<SalesAnalyticsProjection>>> getSalesAnalytics(
            @RequestParam String fromDate,
            @RequestParam String toDate) {
        return CompletableFuture.supplyAsync(() -> {
            LocalDateTime from = LocalDateTime.parse(fromDate);
            LocalDateTime to = LocalDateTime.parse(toDate);
            List<SalesAnalyticsProjection> analytics = analyticsQueryHandler.getSalesAnalytics(from, to);
            return ResponseEntity.ok(analytics);
        }, queryExecutor);
    }

    @GetMapping("/inventory")
    @Async("queryExecutor")
    public CompletableFuture<ResponseEntity<List<InventoryAnalyticsProjection>>> getInventoryAnalytics() {
        return CompletableFuture.supplyAsync(() -> {
            List<InventoryAnalyticsProjection> analytics = analyticsQueryHandler.getInventoryAnalytics();
            return ResponseEntity.ok(analytics);
        }, queryExecutor);
    }

    @GetMapping("/top-selling")
    @Async("queryExecutor")
    public CompletableFuture<ResponseEntity<List<SalesAnalyticsProjection>>> getTopSellingProducts(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam String fromDate,
            @RequestParam String toDate) {
        return CompletableFuture.supplyAsync(() -> {
            LocalDateTime from = LocalDateTime.parse(fromDate);
            LocalDateTime to = LocalDateTime.parse(toDate);
            List<SalesAnalyticsProjection> topSelling = analyticsQueryHandler.getTopSellingProducts(limit, from, to);
            return ResponseEntity.ok(topSelling);
        }, queryExecutor);
    }
}
