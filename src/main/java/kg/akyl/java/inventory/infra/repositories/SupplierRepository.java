package kg.akyl.java.inventory.infra.repositories;

import kg.akyl.java.inventory.domain.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository  extends JpaRepository<Supplier, Long> {
    /**
     * Find supplier by code
     */
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Optional<Supplier> findBySupplierCode(String supplierCode);

    /**
     * Check if supplier code exists
     */
    boolean existsBySupplierCode(String supplierCode);

    /**
     * Find suppliers by status
     */
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<Supplier> findByStatus(Supplier.SupplierStatus status, Pageable pageable);

    /**
     * Find suppliers by type
     */
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<Supplier> findBySupplierType(Supplier.SupplierType supplierType, Pageable pageable);

    /**
     * Search suppliers by name
     */
    @Query("SELECT s FROM Supplier s WHERE " +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.supplierCode) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<Supplier> searchSuppliers(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Get active suppliers
     */
    @Query("SELECT s FROM Supplier s WHERE s.status = 'ACTIVE' ORDER BY s.name ASC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<Supplier> findActiveSuppliers();

    /**
     * Count suppliers by status
     */
    long countByStatus(Supplier.SupplierStatus status);

    /**
     * Count suppliers by type
     */
    long countBySupplierType(Supplier.SupplierType supplierType);

    /**
     * Get top suppliers by rating
     */
    @Query("SELECT s FROM Supplier s WHERE s.status = 'ACTIVE' AND s.rating IS NOT NULL ORDER BY s.rating DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<Supplier> findTopRatedSuppliers(Pageable pageable);
}
