package kg.akyl.java.inventory.infra.services;
import kg.akyl.java.inventory.domain.Supplier;
import kg.akyl.java.inventory.domain.dto.SupplierDTO;
import kg.akyl.java.inventory.domain.dto.SupplierStatsDTO;
import kg.akyl.java.inventory.domain.request.CreateSupplierRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
public interface SupplierService {
    /**
     * Get all suppliers with pagination
     */
    Page<SupplierDTO> getAllSuppliers(Pageable pageable);

    /**
     * Get supplier by ID
     */
    Optional<SupplierDTO> getSupplierById(Long id);

    /**
     * Get supplier by supplier code
     */
    Optional<SupplierDTO> getSupplierByCode(String supplierCode);

    /**
     * Create new supplier
     */
    SupplierDTO createSupplier(CreateSupplierRequest request);

    /**
     * Update existing supplier
     */
    SupplierDTO updateSupplier(Long id, CreateSupplierRequest request);

    /**
     * Delete supplier by ID
     */
    void deleteSupplier(Long id);

    /**
     * Get suppliers by status
     */
    Page<SupplierDTO> getSuppliersByStatus(Supplier.SupplierStatus status, Pageable pageable);

    /**
     * Get suppliers by type
     */
    Page<SupplierDTO> getSuppliersByType(Supplier.SupplierType type, Pageable pageable);

    /**
     * Search suppliers by name or code
     */
    Page<SupplierDTO> searchSuppliers(String searchTerm, Pageable pageable);

    /**
     * Get active suppliers (no pagination)
     */
    List<SupplierDTO> getActiveSuppliers();

    /**
     * Get top rated suppliers
     */
    List<SupplierDTO> getTopRatedSuppliers(int limit);

    /**
     * Get supplier statistics
     */
    SupplierStatsDTO getSupplierStatistics();

    /**
     * Update supplier status
     */
    SupplierDTO updateSupplierStatus(Long id, Supplier.SupplierStatus status);

    /**
     * Update supplier rating
     */
    SupplierDTO updateSupplierRating(Long id, java.math.BigDecimal rating);

    /**
     * Check if supplier code exists
     */
    boolean existsBySupplierCode(String supplierCode);

    /**
     * Count suppliers by status
     */
    long countByStatus(Supplier.SupplierStatus status);

    /**
     * Count suppliers by type
     */
    long countByType(Supplier.SupplierType type);
}
