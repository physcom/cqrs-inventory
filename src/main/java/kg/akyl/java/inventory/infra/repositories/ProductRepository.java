package kg.akyl.java.inventory.infra.repositories;

import jakarta.persistence.LockModeType;
import kg.akyl.java.inventory.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Product findByIdWithLock(@Param("id") Long id);

    Optional<Product> findBySku(String sku);

    List<Product> findByCategory(String category);

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

}
