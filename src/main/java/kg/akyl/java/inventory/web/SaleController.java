package kg.akyl.java.inventory.web;

import kg.akyl.java.inventory.command.commands.ProcessSaleCommand;
import kg.akyl.java.inventory.command.handlers.SaleCommandHandler;
import kg.akyl.java.inventory.domain.Sale;
import kg.akyl.java.inventory.infra.repositories.SaleRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RestController
@RequestMapping("/api/sales")
public class SaleController {
    @Autowired
    private SaleCommandHandler saleCommandHandler;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    @Qualifier("commandExecutor")
    private Executor commandExecutor;

    @Autowired
    @Qualifier("queryExecutor")
    private Executor queryExecutor;

    @PostMapping
    @Async("commandExecutor")
    public CompletableFuture<ResponseEntity<String>> processSale(@RequestBody ProcessSaleRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ProcessSaleCommand command = new ProcessSaleCommand(
                        request.getProductId(),
                        request.getQuantity(),
                        request.getUnitPrice(),
                        request.getCustomerId()
                );

                saleCommandHandler.handle(command);
                return ResponseEntity.ok("Sale processed successfully");
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Error processing sale: " + e.getMessage());
            }
        }, commandExecutor);
    }

    @GetMapping("/customer/{customerId}")
    @Async("queryExecutor")
    public CompletableFuture<ResponseEntity<List<Sale>>> getSalesByCustomer(@PathVariable String customerId) {
        return CompletableFuture.supplyAsync(() -> {
            List<Sale> sales = saleRepository.findByCustomerId(customerId);
            return ResponseEntity.ok(sales);
        }, queryExecutor);
    }

    @GetMapping("/product/{productId}")
    @Async("queryExecutor")
    public CompletableFuture<ResponseEntity<List<Sale>>> getSalesByProduct(@PathVariable Long productId) {
        return CompletableFuture.supplyAsync(() -> {
            List<Sale> sales = saleRepository.findByProductId(productId);
            return ResponseEntity.ok(sales);
        }, queryExecutor);
    }

    @GetMapping("/date-range")
    @Async("queryExecutor")
    public CompletableFuture<ResponseEntity<List<Sale>>> getSalesByDateRange(
            @RequestParam String fromDate,
            @RequestParam String toDate) {
        return CompletableFuture.supplyAsync(() -> {
            LocalDateTime from = LocalDateTime.parse(fromDate);
            LocalDateTime to = LocalDateTime.parse(toDate);
            List<Sale> sales = saleRepository.findBySaleDateBetween(from, to);
            return ResponseEntity.ok(sales);
        }, queryExecutor);
    }

    @Setter
    @Getter
    public static class ProcessSaleRequest {
        // Getters and setters
        private Long productId;
        private Integer quantity;
        private BigDecimal unitPrice;
        private String customerId;

    }
}
