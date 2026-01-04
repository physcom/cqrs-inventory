package kg.akyl.java.inventory.infra.services.impl;

import kg.akyl.java.inventory.domain.Supplier;
import kg.akyl.java.inventory.domain.dto.SupplierDTO;
import kg.akyl.java.inventory.domain.dto.SupplierStatsDTO;
import kg.akyl.java.inventory.domain.request.CreateSupplierRequest;
import kg.akyl.java.inventory.infra.repositories.SupplierRepository;
import kg.akyl.java.inventory.infra.services.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of SupplierService
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SupplierServiceImpl implements SupplierService {
    private final SupplierRepository supplierRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "suppliers", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<SupplierDTO> getAllSuppliers(Pageable pageable) {
        log.debug("Fetching all suppliers with pagination: {}", pageable);
        return supplierRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "supplier", key = "#id")
    public Optional<SupplierDTO> getSupplierById(Long id) {
        log.debug("Fetching supplier by ID: {}", id);
        return supplierRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "supplier", key = "#supplierCode")
    public Optional<SupplierDTO> getSupplierByCode(String supplierCode) {
        log.debug("Fetching supplier by code: {}", supplierCode);
        return supplierRepository.findBySupplierCode(supplierCode)
                .map(this::convertToDTO);
    }

    @Override
    @CacheEvict(value = {"suppliers", "supplier"}, allEntries = true)
    public SupplierDTO createSupplier(CreateSupplierRequest request) {
        log.info("Creating new supplier with code: {}", request.getSupplierCode());

        // Validate unique supplier code
        if (supplierRepository.existsBySupplierCode(request.getSupplierCode())) {
            throw new IllegalArgumentException("Supplier code already exists: " + request.getSupplierCode());
        }

        Supplier supplier = Supplier.builder()
                .supplierCode(request.getSupplierCode())
                .name(request.getName())
                .supplierType(Supplier.SupplierType.valueOf(request.getSupplierType()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .contactPerson(request.getContactPerson())
                .taxId(request.getTaxId())
                .creditLimit(request.getCreditLimit())
                .paymentTermsDays(request.getPaymentTermsDays() != null ? request.getPaymentTermsDays() : 30)
                .rating(request.getRating())
                .status(Supplier.SupplierStatus.ACTIVE)
                .notes(request.getNotes())
                .build();

        supplier = supplierRepository.save(supplier);
        log.info("Supplier created successfully with ID: {}", supplier.getId());

        return convertToDTO(supplier);
    }

    @Override
    @CacheEvict(value = {"suppliers", "supplier"}, allEntries = true)
    public SupplierDTO updateSupplier(Long id, CreateSupplierRequest request) {
        log.info("Updating supplier with ID: {}", id);

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Supplier not found with ID: " + id));

        // Check if supplier code is being changed and if new code already exists
        if (!supplier.getSupplierCode().equals(request.getSupplierCode()) &&
                supplierRepository.existsBySupplierCode(request.getSupplierCode())) {
            throw new IllegalArgumentException("Supplier code already exists: " + request.getSupplierCode());
        }

        // Update fields
        supplier.setSupplierCode(request.getSupplierCode());
        supplier.setName(request.getName());
        supplier.setSupplierType(Supplier.SupplierType.valueOf(request.getSupplierType()));
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        supplier.setCity(request.getCity());
        supplier.setCountry(request.getCountry());
        supplier.setPostalCode(request.getPostalCode());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setTaxId(request.getTaxId());
        supplier.setCreditLimit(request.getCreditLimit());
        supplier.setPaymentTermsDays(request.getPaymentTermsDays());
        supplier.setRating(request.getRating());
        supplier.setNotes(request.getNotes());

        supplier = supplierRepository.save(supplier);
        log.info("Supplier updated successfully with ID: {}", supplier.getId());

        return convertToDTO(supplier);
    }

    @Override
    @CacheEvict(value = {"suppliers", "supplier"}, allEntries = true)
    public void deleteSupplier(Long id) {
        log.info("Deleting supplier with ID: {}", id);

        if (!supplierRepository.existsById(id)) {
            throw new NoSuchElementException("Supplier not found with ID: " + id);
        }

        supplierRepository.deleteById(id);
        log.info("Supplier deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierDTO> getSuppliersByStatus(Supplier.SupplierStatus status, Pageable pageable) {
        log.debug("Fetching suppliers by status: {}", status);
        return supplierRepository.findByStatus(status, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierDTO> getSuppliersByType(Supplier.SupplierType type, Pageable pageable) {
        log.debug("Fetching suppliers by type: {}", type);
        return supplierRepository.findBySupplierType(type, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierDTO> searchSuppliers(String searchTerm, Pageable pageable) {
        log.debug("Searching suppliers with term: {}", searchTerm);
        return supplierRepository.searchSuppliers(searchTerm, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "activeSuppliers")
    public List<SupplierDTO> getActiveSuppliers() {
        log.debug("Fetching all active suppliers");
        return supplierRepository.findActiveSuppliers().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierDTO> getTopRatedSuppliers(int limit) {
        log.debug("Fetching top {} rated suppliers", limit);
        return supplierRepository.findTopRatedSuppliers(PageRequest.of(0, limit)).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierStatsDTO getSupplierStatistics() {
        log.debug("Calculating supplier statistics");

        List<Supplier> allSuppliers = supplierRepository.findAll();

        long totalSuppliers = allSuppliers.size();
        long activeSuppliers = countByStatus(Supplier.SupplierStatus.ACTIVE);
        long inactiveSuppliers = countByStatus(Supplier.SupplierStatus.INACTIVE);
        long suspendedSuppliers = countByStatus(Supplier.SupplierStatus.SUSPENDED);
        long pendingApprovalSuppliers = countByStatus(Supplier.SupplierStatus.PENDING_APPROVAL);

        // Calculate average rating
        BigDecimal averageRating = allSuppliers.stream()
                .map(Supplier::getRating)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(totalSuppliers), 2, java.math.RoundingMode.HALF_UP);

        // Group by type
        Map<String, Long> suppliersByType = allSuppliers.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getSupplierType().name(),
                        Collectors.counting()
                ));

        // Group by country
        Map<String, Long> suppliersByCountry = allSuppliers.stream()
                .filter(s -> s.getCountry() != null)
                .collect(Collectors.groupingBy(
                        Supplier::getCountry,
                        Collectors.counting()
                ));

        // Group by city
        Map<String, Long> suppliersByCity = allSuppliers.stream()
                .filter(s -> s.getCity() != null)
                .collect(Collectors.groupingBy(
                        Supplier::getCity,
                        Collectors.counting()
                ));

        return SupplierStatsDTO.builder()
                .totalSuppliers(totalSuppliers)
                .activeSuppliers(activeSuppliers)
                .inactiveSuppliers(inactiveSuppliers)
                .suspendedSuppliers(suspendedSuppliers)
                .pendingApprovalSuppliers(pendingApprovalSuppliers)
                .averageSupplierRating(averageRating)
                .suppliersByType(suppliersByType)
                .suppliersByCountry(suppliersByCountry)
                .suppliersByCity(suppliersByCity)
                .build();
    }

    @Override
    @CacheEvict(value = {"suppliers", "supplier"}, allEntries = true)
    public SupplierDTO updateSupplierStatus(Long id, Supplier.SupplierStatus status) {
        log.info("Updating supplier status for ID: {} to {}", id, status);

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Supplier not found with ID: " + id));

        supplier.setStatus(status);
        supplier = supplierRepository.save(supplier);

        log.info("Supplier status updated successfully");
        return convertToDTO(supplier);
    }

    @Override
    @CacheEvict(value = {"suppliers", "supplier"}, allEntries = true)
    public SupplierDTO updateSupplierRating(Long id, BigDecimal rating) {
        log.info("Updating supplier rating for ID: {} to {}", id, rating);

        if (rating.compareTo(BigDecimal.ZERO) < 0 || rating.compareTo(new BigDecimal("5.00")) > 0) {
            throw new IllegalArgumentException("Rating must be between 0 and 5");
        }

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Supplier not found with ID: " + id));

        supplier.setRating(rating);
        supplier = supplierRepository.save(supplier);

        log.info("Supplier rating updated successfully");
        return convertToDTO(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySupplierCode(String supplierCode) {
        return supplierRepository.existsBySupplierCode(supplierCode);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(Supplier.SupplierStatus status) {
        return supplierRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByType(Supplier.SupplierType type) {
        return supplierRepository.countBySupplierType(type);
    }

    /**
     * Convert Supplier entity to SupplierDTO
     */
    private SupplierDTO convertToDTO(Supplier supplier) {
        return SupplierDTO.builder()
                .id(supplier.getId())
                .supplierCode(supplier.getSupplierCode())
                .name(supplier.getName())
                .supplierType(supplier.getSupplierType().name())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .city(supplier.getCity())
                .country(supplier.getCountry())
                .postalCode(supplier.getPostalCode())
                .contactPerson(supplier.getContactPerson())
                .taxId(supplier.getTaxId())
                .creditLimit(supplier.getCreditLimit())
                .paymentTermsDays(supplier.getPaymentTermsDays())
                .rating(supplier.getRating())
                .status(supplier.getStatus().name())
                .notes(supplier.getNotes())
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .version(supplier.getVersion())
                .build();
    }
}
