package kg.akyl.java.inventory.infra.services;

import kg.akyl.java.inventory.domain.Product;
import kg.akyl.java.inventory.domain.ProductStatus;
import kg.akyl.java.inventory.domain.dto.ProductDTO;
import kg.akyl.java.inventory.domain.dto.ProductStatsDTO;
import kg.akyl.java.inventory.infra.exceptions.ProductNotFoundException;
import kg.akyl.java.inventory.infra.exceptions.SkuAlreadyExistsException;
import kg.akyl.java.inventory.infra.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;

    /**
     * Get all products with pagination (cached for performance)
     */
    @Cacheable(value = "products", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        log.debug("Fetching products page: {} with size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return productRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get product by ID (cached)
     */
    @Cacheable(value = "product", key = "#id")
    public ProductDTO getProductById(Long id) {
        log.debug("Fetching product by ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
        return convertToDTO(product);
    }

    /**
     * Get product by SKU (cached)
     */
    @Cacheable(value = "product", key = "#sku")
    public ProductDTO getProductBySku(String sku) {
        log.debug("Fetching product by SKU: {}", sku);
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + sku));
        return convertToDTO(product);
    }

    /**
     * Search products with pagination
     */
    public Page<ProductDTO> searchProducts(String searchTerm, Pageable pageable) {
        log.debug("Searching products with term: {}", searchTerm);
        return productRepository.searchProducts(searchTerm, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get products by category with pagination
     */
    @Cacheable(value = "productsByCategory", key = "#category + '-' + #pageable.pageNumber")
    public Page<ProductDTO> getProductsByCategory(String category, Pageable pageable) {
        log.debug("Fetching products by category: {}", category);
        return productRepository.findByCategory(category, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get products by status with pagination
     */
    public Page<ProductDTO> getProductsByStatus(ProductStatus status, Pageable pageable) {
        log.debug("Fetching products by status: {}", status);
        return productRepository.findByStatus(status, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Create new product
     */
    @Transactional
    @CacheEvict(value = {"products", "productStats", "productsByCategory"}, allEntries = true)
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Creating new product with SKU: {}", productDTO.getSku());

        // Check if SKU already exists
        if (productRepository.existsBySku(productDTO.getSku())) {
            throw new SkuAlreadyExistsException("Product with SKU " + productDTO.getSku() + " already exists");
        }

        Product product = convertToEntity(productDTO);
        Product savedProduct = productRepository.save(product);

        log.info("Product created successfully with ID: {}", savedProduct.getId());
        return convertToDTO(savedProduct);
    }

    /**
     * Update existing product
     */
    @Transactional
    @CacheEvict(value = {"products", "product", "productStats", "productsByCategory"}, allEntries = true)
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        log.info("Updating product with ID: {}", id);

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

        // Check if SKU is being changed and if it already exists
        if (!existingProduct.getSku().equals(productDTO.getSku()) &&
                productRepository.existsBySku(productDTO.getSku())) {
            throw new SkuAlreadyExistsException("Product with SKU " + productDTO.getSku() + " already exists");
        }

        // Update fields
        existingProduct.setSku(productDTO.getSku());
        existingProduct.setName(productDTO.getName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setCategory(productDTO.getCategory());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setStockQuantity(productDTO.getStockQuantity());
        existingProduct.setMinStockLevel(productDTO.getMinStockLevel());

        Product updatedProduct = productRepository.save(existingProduct);

        log.info("Product updated successfully with ID: {}", updatedProduct.getId());
        return convertToDTO(updatedProduct);
    }

    /**
     * Update stock quantity (optimized for high-frequency updates)
     */
    @Transactional
    @CacheEvict(value = {"products", "product", "productStats"}, allEntries = true)
    public void updateStockQuantity(Long productId, Integer quantity) {
        log.debug("Updating stock quantity for product ID: {} by {}", productId, quantity);

        int updated = productRepository.updateStockQuantity(productId, quantity);
        if (updated == 0) {
            throw new ProductNotFoundException("Product not found with ID: " + productId);
        }
    }

    /**
     * Delete product
     */
    @Transactional
    @CacheEvict(value = {"products", "product", "productStats", "productsByCategory"}, allEntries = true)
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);

        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found with ID: " + id);
        }

        productRepository.deleteById(id);
        log.info("Product deleted successfully with ID: {}", id);
    }

    /**
     * Bulk delete products (for admin operations)
     */
    @Transactional
    @CacheEvict(value = {"products", "product", "productStats", "productsByCategory"}, allEntries = true)
    public int bulkDeleteProducts(List<Long> ids) {
        log.info("Bulk deleting {} products", ids.size());
        return productRepository.deleteByIdIn(ids);
    }

    /**
     * Get low stock products
     */
    public List<ProductDTO> getLowStockProducts(int limit) {
        log.debug("Fetching low stock products with limit: {}", limit);
        return productRepository.findLowStockProducts(Pageable.ofSize(limit)).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get products requiring restock
     */
    public List<ProductDTO> getProductsRequiringRestock() {
        log.debug("Fetching products requiring restock");
        return productRepository.findProductsRequiringRestock().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all categories
     */
    @Cacheable(value = "categories")
    public List<String> getAllCategories() {
        log.debug("Fetching all categories");
        return productRepository.findAllCategories();
    }

    /**
     * Get product statistics (cached for dashboard)
     */
    @Cacheable(value = "productStats")
    public ProductStatsDTO getProductStats() {
        log.debug("Calculating product statistics");

        long totalProducts = productRepository.count();
        long lowStockCount = productRepository.countByStatus(ProductStatus.LOW_STOCK);
        long outOfStockCount = productRepository.countByStatus(ProductStatus.OUT_OF_STOCK);
        long distinctCategories = productRepository.countDistinctCategories();
        BigDecimal totalValue = productRepository.calculateTotalInventoryValue();

        return ProductStatsDTO.builder()
                .totalProducts(totalProducts)
                .lowStockCount(lowStockCount)
                .outOfStockCount(outOfStockCount)
                .categoriesCount(distinctCategories)
                .totalInventoryValue(totalValue != null ? totalValue : BigDecimal.ZERO)
                .build();
    }

    /**
     * Async method to refresh product cache (can be scheduled)
     */
    @Async
    @CacheEvict(value = {"products", "productStats", "productsByCategory", "categories"}, allEntries = true)
    public CompletableFuture<Void> refreshCache() {
        log.info("Refreshing product cache");
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Convert Product entity to DTO
     */
    private ProductDTO convertToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .minStockLevel(product.getMinStockLevel())
                .status(product.getStatus().name())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .version(product.getVersion())
                .build();
    }

    /**
     * Convert DTO to Product entity
     */
    private Product convertToEntity(ProductDTO dto) {
        return Product.builder()
                .sku(dto.getSku())
                .name(dto.getName())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity())
                .minStockLevel(dto.getMinStockLevel() != null ? dto.getMinStockLevel() : 10)
                .build();
    }
}
