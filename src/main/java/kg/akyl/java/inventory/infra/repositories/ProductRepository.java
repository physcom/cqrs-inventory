package kg.akyl.java.inventory.infra.repositories;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import kg.akyl.java.inventory.domain.Product;
import kg.akyl.java.inventory.domain.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Product findByIdWithLock(@Param("id") Long id);

    Optional<Product> findBySku(String sku);

    /**
     * Check if SKU exists
     */
    boolean existsBySku(String sku);

    List<Product> findByCategory(String category);

    /**
     * Find products by category with pagination
     */
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<Product> findByCategory(String category, Pageable pageable);

    /**
     * Find products by status with pagination
     */
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.quantity - p.reservedQuantity <= :threshold AND p.status = 'ACTIVE'")
    List<Product> findLowStockProducts(@Param("threshold") int threshold);

    @Modifying
    @Query("UPDATE Product p SET p.quantity = p.quantity + :quantity WHERE p.id = :id")
    int updateQuantity(@Param("id") Long id, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE Product p SET p.reservedQuantity = p.reservedQuantity + :quantity WHERE p.id = :id")
    int updateReservedQuantity(@Param("id") Long id, @Param("quantity") int quantity);

    @Query(value = """
        SELECT p.* FROM products p 
        WHERE p.name ILIKE :searchTerm 
        OR p.description ILIKE :searchTerm 
        OR p.sku ILIKE :searchTerm
        ORDER BY 
            CASE 
                WHEN p.name ILIKE :exactTerm THEN 1
                WHEN p.sku ILIKE :exactTerm THEN 2
                ELSE 3
            END
        """, nativeQuery = true)
    List<Product> searchProducts(@Param("searchTerm") String searchTerm, @Param("exactTerm") String exactTerm);

    /**
     * Search products by name or SKU with pagination
     */
    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.category) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<Product> searchProducts(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Count products by status
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = :status")
    long countByStatus(@Param("status") ProductStatus status);

    /**
     * Get low stock products (optimized for dashboard)
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity < p.minStockLevel AND p.stockQuantity > 0 ORDER BY p.stockQuantity ASC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<Product> findLowStockProducts(Pageable pageable);

    /**
     * Calculate total inventory value
     */
    @Query("SELECT SUM(p.price * p.stockQuantity) FROM Product p")
    BigDecimal calculateTotalInventoryValue();

    /**
     * Get products by multiple categories (batch query optimization)
     */
    @Query("SELECT p FROM Product p WHERE p.category IN :categories")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<Product> findByCategories(@Param("categories") List<String> categories);

    /**
     * Bulk update stock quantity (for high-load scenarios)
     */
    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :quantity WHERE p.id = :productId")
    int updateStockQuantity(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * Get top selling products (based on stock changes)
     */
    @Query(value = "SELECT p.* FROM products p " +
            "ORDER BY p.stock_quantity DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Product> findTopProducts(@Param("limit") int limit);

    /**
     * Get products requiring restock
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= p.minStockLevel")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<Product> findProductsRequiringRestock();

    /**
     * Count distinct categories
     */
    @Query("SELECT COUNT(DISTINCT p.category) FROM Product p")
    long countDistinctCategories();

    /**
     * Get all distinct categories
     */
    @Query("SELECT DISTINCT p.category FROM Product p ORDER BY p.category")
    List<String> findAllCategories();

    /**
     * Batch delete by IDs (for bulk operations)
     */
    @Modifying
    @Query("DELETE FROM Product p WHERE p.id IN :ids")
    int deleteByIdIn(@Param("ids") List<Long> ids);

}
