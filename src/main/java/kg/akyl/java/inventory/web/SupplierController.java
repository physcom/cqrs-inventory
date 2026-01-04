package kg.akyl.java.inventory.web;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kg.akyl.java.inventory.domain.Supplier;
import kg.akyl.java.inventory.domain.dto.SupplierDTO;
import kg.akyl.java.inventory.domain.dto.SupplierStatsDTO;
import kg.akyl.java.inventory.domain.request.CreateSupplierRequest;
import kg.akyl.java.inventory.domain.response.ApiResponse;
import kg.akyl.java.inventory.infra.services.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST Controller for Supplier management
 */
@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Suppliers", description = "Supplier management APIs")
public class SupplierController {
    private final SupplierService supplierService;

    /**
     * Get all suppliers with pagination
     */
    @GetMapping
    @Operation(summary = "Get all suppliers", description = "Retrieve all suppliers with pagination and sorting")
    public ResponseEntity<ApiResponse<Page<SupplierDTO>>> getAllSuppliers(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "name") String sortBy,

            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("GET /api/v1/suppliers - page: {}, size: {}, sortBy: {}, sortDir: {}",
                page, size, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<SupplierDTO> suppliers = supplierService.getAllSuppliers(pageable);

        return ResponseEntity.ok(ApiResponse.success(suppliers, "Suppliers retrieved successfully"));
    }

    /**
     * Get supplier by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get supplier by ID", description = "Retrieve a specific supplier by ID")
    public ResponseEntity<ApiResponse<SupplierDTO>> getSupplierById(
            @Parameter(description = "Supplier ID")
            @PathVariable Long id) {

        log.info("GET /api/v1/suppliers/{}", id);

        return supplierService.getSupplierById(id)
                .map(supplier -> ResponseEntity.ok(ApiResponse.success(supplier, "Supplier found")))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Supplier not found with ID: " + id)));
    }

    /**
     * Get supplier by supplier code
     */
    @GetMapping("/code/{supplierCode}")
    @Operation(summary = "Get supplier by code", description = "Retrieve a specific supplier by supplier code")
    public ResponseEntity<ApiResponse<SupplierDTO>> getSupplierByCode(
            @Parameter(description = "Supplier code")
            @PathVariable String supplierCode) {

        log.info("GET /api/v1/suppliers/code/{}", supplierCode);

        return supplierService.getSupplierByCode(supplierCode)
                .map(supplier -> ResponseEntity.ok(ApiResponse.success(supplier, "Supplier found")))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Supplier not found with code: " + supplierCode)));
    }

    /**
     * Create new supplier
     */
    @PostMapping
    @Operation(summary = "Create new supplier", description = "Create a new supplier")
    public ResponseEntity<ApiResponse<SupplierDTO>> createSupplier(
            @Valid @RequestBody CreateSupplierRequest request) {

        log.info("POST /api/v1/suppliers - Creating supplier: {}", request.getSupplierCode());

        try {
            SupplierDTO supplier = supplierService.createSupplier(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(supplier, "Supplier created successfully"));
        } catch (IllegalArgumentException e) {
            log.error("Error creating supplier: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Update supplier
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update supplier", description = "Update an existing supplier")
    public ResponseEntity<ApiResponse<SupplierDTO>> updateSupplier(
            @Parameter(description = "Supplier ID")
            @PathVariable Long id,

            @Valid @RequestBody CreateSupplierRequest request) {

        log.info("PUT /api/v1/suppliers/{} - Updating supplier", id);

        try {
            SupplierDTO supplier = supplierService.updateSupplier(id, request);
            return ResponseEntity.ok(ApiResponse.success(supplier, "Supplier updated successfully"));
        } catch (IllegalArgumentException e) {
            log.error("Error updating supplier: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (java.util.NoSuchElementException e) {
            log.error("Supplier not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Delete supplier
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete supplier", description = "Delete a supplier by ID")
    public ResponseEntity<ApiResponse<Void>> deleteSupplier(
            @Parameter(description = "Supplier ID")
            @PathVariable Long id) {

        log.info("DELETE /api/v1/suppliers/{}", id);

        try {
            supplierService.deleteSupplier(id);
            return ResponseEntity.ok(ApiResponse.success(null, "Supplier deleted successfully"));
        } catch (java.util.NoSuchElementException e) {
            log.error("Supplier not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get suppliers by status
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get suppliers by status", description = "Retrieve suppliers filtered by status")
    public ResponseEntity<ApiResponse<Page<SupplierDTO>>> getSuppliersByStatus(
            @Parameter(description = "Supplier status")
            @PathVariable String status,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/v1/suppliers/status/{}", status);

        try {
            Supplier.SupplierStatus supplierStatus = Supplier.SupplierStatus.valueOf(status.toUpperCase());
            Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
            Page<SupplierDTO> suppliers = supplierService.getSuppliersByStatus(supplierStatus, pageable);

            return ResponseEntity.ok(ApiResponse.success(suppliers, "Suppliers retrieved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid status: " + status));
        }
    }

    /**
     * Get suppliers by type
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "Get suppliers by type", description = "Retrieve suppliers filtered by type")
    public ResponseEntity<ApiResponse<Page<SupplierDTO>>> getSuppliersByType(
            @Parameter(description = "Supplier type")
            @PathVariable String type,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/v1/suppliers/type/{}", type);

        try {
            Supplier.SupplierType supplierType = Supplier.SupplierType.valueOf(type.toUpperCase());
            Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
            Page<SupplierDTO> suppliers = supplierService.getSuppliersByType(supplierType, pageable);

            return ResponseEntity.ok(ApiResponse.success( suppliers, "Suppliers retrieved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid type: " + type));
        }
    }

    /**
     * Search suppliers
     */
    @GetMapping("/search")
    @Operation(summary = "Search suppliers", description = "Search suppliers by name or code")
    public ResponseEntity<ApiResponse<Page<SupplierDTO>>> searchSuppliers(
            @Parameter(description = "Search term")
            @RequestParam String q,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/v1/suppliers/search?q={}", q);

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<SupplierDTO> suppliers = supplierService.searchSuppliers(q, pageable);

        return ResponseEntity.ok(ApiResponse.success(suppliers, "Search completed successfully"));
    }

    /**
     * Get active suppliers
     */
    @GetMapping("/active")
    @Operation(summary = "Get active suppliers", description = "Retrieve all active suppliers without pagination")
    public ResponseEntity<ApiResponse<List<SupplierDTO>>> getActiveSuppliers() {
        log.info("GET /api/v1/suppliers/active");

        List<SupplierDTO> suppliers = supplierService.getActiveSuppliers();
        return ResponseEntity.ok(ApiResponse.success(suppliers, "Active suppliers retrieved successfully"));
    }

    /**
     * Get top rated suppliers
     */
    @GetMapping("/top-rated")
    @Operation(summary = "Get top rated suppliers", description = "Retrieve top rated suppliers")
    public ResponseEntity<ApiResponse<List<SupplierDTO>>> getTopRatedSuppliers(
            @Parameter(description = "Number of suppliers to retrieve")
            @RequestParam(defaultValue = "10") int limit) {

        log.info("GET /api/v1/suppliers/top-rated?limit={}", limit);

        List<SupplierDTO> suppliers = supplierService.getTopRatedSuppliers(limit);
        return ResponseEntity.ok(ApiResponse.success(suppliers, "Top rated suppliers retrieved successfully"));
    }

    /**
     * Get supplier statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get supplier statistics", description = "Retrieve comprehensive supplier statistics")
    public ResponseEntity<ApiResponse<SupplierStatsDTO>> getSupplierStatistics() {
        log.info("GET /api/v1/suppliers/statistics");

        SupplierStatsDTO stats = supplierService.getSupplierStatistics();
        return ResponseEntity.ok(ApiResponse.success( stats, "Statistics retrieved successfully"));
    }

    /**
     * Update supplier status
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update supplier status", description = "Update the status of a supplier")
    public ResponseEntity<ApiResponse<SupplierDTO>> updateSupplierStatus(
            @Parameter(description = "Supplier ID")
            @PathVariable Long id,

            @Parameter(description = "New status")
            @RequestParam String status) {

        log.info("PATCH /api/v1/suppliers/{}/status?status={}", id, status);

        try {
            Supplier.SupplierStatus supplierStatus = Supplier.SupplierStatus.valueOf(status.toUpperCase());
            SupplierDTO supplier = supplierService.updateSupplierStatus(id, supplierStatus);

            return ResponseEntity.ok(ApiResponse.success(supplier, "Supplier status updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid status: " + status));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Update supplier rating
     */
    @PatchMapping("/{id}/rating")
    @Operation(summary = "Update supplier rating", description = "Update the rating of a supplier")
    public ResponseEntity<ApiResponse<SupplierDTO>> updateSupplierRating(
            @Parameter(description = "Supplier ID")
            @PathVariable Long id,

            @Parameter(description = "New rating (0.00 - 5.00)")
            @RequestParam BigDecimal rating) {

        log.info("PATCH /api/v1/suppliers/{}/rating?rating={}", id, rating);

        try {
            SupplierDTO supplier = supplierService.updateSupplierRating(id, rating);
            return ResponseEntity.ok(ApiResponse.success( supplier, "Supplier rating updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Check if supplier code exists
     */
    @GetMapping("/exists/{supplierCode}")
    @Operation(summary = "Check supplier code", description = "Check if a supplier code already exists")
    public ResponseEntity<ApiResponse<Boolean>> checkSupplierCodeExists(
            @Parameter(description = "Supplier code to check")
            @PathVariable String supplierCode) {

        log.info("GET /api/v1/suppliers/exists/{}", supplierCode);

        boolean exists = supplierService.existsBySupplierCode(supplierCode);
        return ResponseEntity.ok(ApiResponse.success( exists, "Check completed"));
    }

    /**
     * Count suppliers by status
     */
    @GetMapping("/count/status/{status}")
    @Operation(summary = "Count suppliers by status", description = "Get count of suppliers by status")
    public ResponseEntity<ApiResponse<Long>> countByStatus(
            @Parameter(description = "Supplier status")
            @PathVariable String status) {

        log.info("GET /api/v1/suppliers/count/status/{}", status);

        try {
            Supplier.SupplierStatus supplierStatus = Supplier.SupplierStatus.valueOf(status.toUpperCase());
            long count = supplierService.countByStatus(supplierStatus);

            return ResponseEntity.ok(ApiResponse.success( count, "Count retrieved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid status: " + status));
        }
    }

    /**
     * Count suppliers by type
     */
    @GetMapping("/count/type/{type}")
    @Operation(summary = "Count suppliers by type", description = "Get count of suppliers by type")
    public ResponseEntity<ApiResponse<Long>> countByType(
            @Parameter(description = "Supplier type")
            @PathVariable String type) {

        log.info("GET /api/v1/suppliers/count/type/{}", type);

        try {
            Supplier.SupplierType supplierType = Supplier.SupplierType.valueOf(type.toUpperCase());
            long count = supplierService.countByType(supplierType);

            return ResponseEntity.ok(ApiResponse.success( count, "Count retrieved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid type: " + type));
        }
    }
}
