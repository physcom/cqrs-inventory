package kg.gns.java.inventorysystem.infra.repositories;

import kg.gns.java.inventorysystem.domain.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByProductId(Long productId);

    List<Sale> findByCustomerId(String customerId);

    @Query("SELECT s FROM Sale s WHERE s.saleDate BETWEEN :fromDate AND :toDate ORDER BY s.saleDate DESC")
    List<Sale> findBySaleDateBetween(@Param("fromDate") LocalDateTime fromDate,
                                     @Param("toDate") LocalDateTime toDate);

    @Query("SELECT s FROM Sale s WHERE s.productId = :productId AND s.saleDate >= :fromDate")
    List<Sale> findRecentSalesByProduct(@Param("productId") Long productId,
                                        @Param("fromDate") LocalDateTime fromDate);

    @Query(value = """
        SELECT s.* FROM sales s 
        JOIN products p ON s.product_id = p.id 
        WHERE p.category = :category 
        AND s.sale_date BETWEEN :fromDate AND :toDate
        ORDER BY s.sale_date DESC
        """, nativeQuery = true)
    List<Sale> findSalesByCategory(@Param("category") String category,
                                   @Param("fromDate") LocalDateTime fromDate,
                                   @Param("toDate") LocalDateTime toDate);
}
