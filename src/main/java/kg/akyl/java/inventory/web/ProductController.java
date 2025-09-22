package kg.gns.java.inventorysystem.web;

import kg.gns.java.inventorysystem.command.commands.CreateProductCommand;
import kg.gns.java.inventorysystem.command.commands.ReserveProductCommand;
import kg.gns.java.inventorysystem.command.commands.UpdateProductQuantityCommand;
import kg.gns.java.inventorysystem.command.handlers.ProductCommandHandler;
import kg.gns.java.inventorysystem.query.handlers.ProductQueryHandler;
import kg.gns.java.inventorysystem.query.projections.InventoryStatusProjection;
import kg.gns.java.inventorysystem.query.projections.ProductProjection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ProductController {

    private final ProductCommandHandler productCommandHandler;
    private final ProductQueryHandler productQueryHandler;

    @Autowired
    @Qualifier("commandExecutor")
    private Executor commandExecutor;

    @Autowired
    @Qualifier("queryExecutor")
    private Executor queryExecutor;

    // COMMAND ENDPOINTS
    @PostMapping
    @Async("commandExecutor")
    public CompletableFuture<ResponseEntity<String>> createProduct(@RequestBody CreateProductRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                CreateProductCommand command = new CreateProductCommand(
                        request.getSku(),
                        request.getName(),
                        request.getDescription(),
                        request.getPrice(),
                        request.getQuantity(),
                        request.getCategory()
                );

                productCommandHandler.handle(command);
                return ResponseEntity.ok("Product created successfully");
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Error creating product: " + e.getMessage());
            }
        }, commandExecutor);
    }

    @PutMapping("/{productId}/quantity")
    @Async("commandExecutor")
    public CompletableFuture<ResponseEntity<String>> updateQuantity(
            @PathVariable Long productId,
            @RequestBody UpdateQuantityRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                UpdateProductQuantityCommand command = new UpdateProductQuantityCommand(
                        productId, request.getNewQuantity()
                );

                productCommandHandler.handle(command);
                return ResponseEntity.ok("Quantity updated successfully");
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Error updating quantity: " + e.getMessage());
            }
        }, commandExecutor);
    }

    @PostMapping("/{productId}/reserve")
    @Async("commandExecutor")
    public CompletableFuture<ResponseEntity<String>> reserveProduct(
            @PathVariable Long productId,
            @RequestBody ReserveProductRequest request) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                ReserveProductCommand command = new ReserveProductCommand(
                        productId, request.getQuantity(), request.getCustomerId()
                );

                productCommandHandler.handle(command);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Error reserving product: " + e.getMessage());
            }
            return null;
        }, commandExecutor);
    }

    // QUERY ENDPOINTS
    @GetMapping("/{id}")
    @Async("queryExecutor")
    public CompletableFuture<ResponseEntity<ProductProjection>> getProduct(@PathVariable Long id) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<ProductProjection> product = productQueryHandler.findById(id);
            return product.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }, queryExecutor);
    }

    @GetMapping("/sku/{sku}")
    @Async("queryExecutor")
    public CompletableFuture<ResponseEntity<ProductProjection>> getProductBySku(@PathVariable String sku) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<ProductProjection> product = productQueryHandler.findBySku(sku);
            return product.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }, queryExecutor);
    }

    @GetMapping("/category/{category}")
    @Async("queryExecutor")
    public CompletableFuture<ResponseEntity<List<ProductProjection>>> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return CompletableFuture.supplyAsync(() -> {
            List<ProductProjection> products = productQueryHandler.findByCategory(category, limit, offset);
            return ResponseEntity.ok(products);
        }, queryExecutor);
    }

    @GetMapping("/search")
    @Async("queryExecutor")
    public CompletableFuture<ResponseEntity<List<ProductProjection>>> searchProducts(
            @RequestParam String q,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return CompletableFuture.supplyAsync(() -> {
            List<ProductProjection> products = productQueryHandler.searchProducts(q, limit, offset);
            return ResponseEntity.ok(products);
        }, queryExecutor);
    }

    @GetMapping("/low-stock")
    @Async("queryExecutor")
    public CompletableFuture<ResponseEntity<List<InventoryStatusProjection>>> getLowStockProducts(
            @RequestParam(defaultValue = "10") int threshold) {
        return CompletableFuture.supplyAsync(() -> {
            List<InventoryStatusProjection> products = productQueryHandler.getLowStockProducts(threshold);
            return ResponseEntity.ok(products);
        }, queryExecutor);
    }

    @Setter
    @Getter
    public static class CreateProductRequest {
        // Getters and setters
        private String sku;
        private String name;
        private String description;
        private BigDecimal price;
        private Integer quantity;
        private String category;

    }

    @Setter
    @Getter
    public static class UpdateQuantityRequest {
        private Integer newQuantity;

    }

    @Setter
    @Getter
    public static class ReserveProductRequest {
        private Integer quantity;
        private String customerId;

    }

}
