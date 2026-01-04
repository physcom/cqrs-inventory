package kg.akyl.java.inventory.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kg.akyl.java.inventory.domain.ProductStatus;
import kg.akyl.java.inventory.domain.dto.ProductDTO;
import kg.akyl.java.inventory.domain.dto.ProductStatsDTO;
import kg.akyl.java.inventory.domain.request.StockUpdateRequest;
import kg.akyl.java.inventory.domain.response.ApiResponse;
import kg.akyl.java.inventory.infra.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Product Management", description = "APIs for managing inventory products")
public class ProductAllController {
    private final ProductService productService;

    /**
     * Get all products with pagination and sorting
     */
    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieve paginated list of all products")
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> getAllProducts(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)")
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        log.debug("GET /api/v1/products - page: {}, size: {}, sortBy: {}, sortDirection: {}",
                page, size, sortBy, sortDirection);

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ProductDTO> products = productService.getAllProducts(pageable);

        return ResponseEntity.ok(ApiResponse.success(products, "Products retrieved successfully"));
    }

    /**
     * Get product by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve a single product by its ID")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(
            @Parameter(description = "Product ID")
            @PathVariable Long id) {

        log.debug("GET /api/v1/products/{}", id);

        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product, "Product retrieved successfully"));
    }

    /**
     * Get product by SKU
     */
    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get product by SKU", description = "Retrieve a single product by its SKU")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductBySku(
            @Parameter(description = "Product SKU")
            @PathVariable String sku) {

        log.debug("GET /api/v1/products/sku/{}", sku);

        ProductDTO product = productService.getProductBySku(sku);
        return ResponseEntity.ok(ApiResponse.success(product, "Product retrieved successfully"));
    }

    /**
     * Search products
     */
    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Search products by name, SKU, or category")
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> searchProducts(
            @Parameter(description = "Search term")
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("GET /api/v1/products/search?query={}", query);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<ProductDTO> products = productService.searchProducts(query, pageable);

        return ResponseEntity.ok(ApiResponse.success(products, "Search completed successfully"));
    }

    /**
     * Get products by category
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "Get products by category", description = "Retrieve products filtered by category")
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> getProductsByCategory(
            @Parameter(description = "Category name")
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("GET /api/v1/products/category/{}", category);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        Page<ProductDTO> products = productService.getProductsByCategory(category, pageable);

        return ResponseEntity.ok(ApiResponse.success(products, "Products retrieved successfully"));
    }

    /**
     * Get products by status
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get products by status", description = "Retrieve products filtered by status")
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> getProductsByStatus(
            @Parameter(description = "Product status (IN_STOCK, LOW_STOCK, OUT_OF_STOCK)")
            @PathVariable ProductStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("GET /api/v1/products/status/{}", status);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "stockQuantity"));
        Page<ProductDTO> products = productService.getProductsByStatus(status, pageable);

        return ResponseEntity.ok(ApiResponse.success(products, "Products retrieved successfully"));
    }

    /**
     * Get low stock products
     */
    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock products", description = "Retrieve products with low stock levels")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getLowStockProducts(
            @Parameter(description = "Maximum number of results")
            @RequestParam(defaultValue = "50") int limit) {

        log.debug("GET /api/v1/products/low-stock?limit={}", limit);

        List<ProductDTO> products = productService.getLowStockProducts(limit);
        return ResponseEntity.ok(ApiResponse.success(products, "Low stock products retrieved successfully"));
    }

    /**
     * Get products requiring restock
     */
    @GetMapping("/restock-required")
    @Operation(summary = "Get products requiring restock", description = "Retrieve products that need restocking")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getRestockRequired() {
        log.debug("GET /api/v1/products/restock-required");

        List<ProductDTO> products = productService.getProductsRequiringRestock();
        return ResponseEntity.ok(ApiResponse.success(products, "Restock list retrieved successfully"));
    }

    /**
     * Get all categories
     */
    @GetMapping("/categories")
    @Operation(summary = "Get all categories", description = "Retrieve list of all product categories")
    public ResponseEntity<ApiResponse<List<String>>> getAllCategories() {
        log.debug("GET /api/v1/products/categories");

        List<String> categories = productService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories, "Categories retrieved successfully"));
    }

    /**
     * Get product statistics
     */
    @GetMapping("/stats")
    @Operation(summary = "Get product statistics", description = "Retrieve dashboard statistics")
    public ResponseEntity<ApiResponse<ProductStatsDTO>> getProductStats() {
        log.debug("GET /api/v1/products/stats");

        ProductStatsDTO stats = productService.getProductStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Statistics retrieved successfully"));
    }

    /**
     * Create new product
     */
    @PostMapping
    @Operation(summary = "Create new product", description = "Add a new product to inventory")
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(
            @Valid @RequestBody ProductDTO productDTO) {

        log.info("POST /api/v1/products - Creating product: {}", productDTO.getSku());

        ProductDTO createdProduct = productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdProduct, "Product created successfully"));
    }

    /**
     * Update existing product
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Update an existing product")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @Parameter(description = "Product ID")
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) {

        log.info("PUT /api/v1/products/{} - Updating product", id);

        ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(ApiResponse.success(updatedProduct, "Product updated successfully"));
    }

    /**
     * Update stock quantity
     */
    @PatchMapping("/{id}/stock")
    @Operation(summary = "Update stock quantity", description = "Update product stock quantity (add or subtract)")
    public ResponseEntity<ApiResponse<Void>> updateStockQuantity(
            @Parameter(description = "Product ID")
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request) {

        log.info("PATCH /api/v1/products/{}/stock - Updating stock by {}", id, request.getQuantity());

        productService.updateStockQuantity(id, request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success(null, "Stock quantity updated successfully"));
    }

    /**
     * Delete product
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Remove a product from inventory")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @Parameter(description = "Product ID")
            @PathVariable Long id) {

        log.info("DELETE /api/v1/products/{}", id);

        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
    }

    /**
     * Bulk delete products
     */
    @DeleteMapping("/bulk")
    @Operation(summary = "Bulk delete products", description = "Delete multiple products at once")
    public ResponseEntity<ApiResponse<Integer>> bulkDeleteProducts(
            @RequestBody List<Long> ids) {

        log.info("DELETE /api/v1/products/bulk - Deleting {} products", ids.size());

        int deletedCount = productService.bulkDeleteProducts(ids);
        return ResponseEntity.ok(ApiResponse.success(deletedCount, deletedCount + " products deleted successfully"));
    }

    /**
     * Refresh cache
     */
    @PostMapping("/cache/refresh")
    @Operation(summary = "Refresh cache", description = "Manually refresh product cache")
    public ResponseEntity<ApiResponse<Void>> refreshCache() {
        log.info("POST /api/v1/products/cache/refresh");

        productService.refreshCache();
        return ResponseEntity.ok(ApiResponse.success(null, "Cache refresh initiated"));
    }
}
