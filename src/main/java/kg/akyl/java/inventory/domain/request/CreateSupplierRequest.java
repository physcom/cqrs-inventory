package kg.akyl.java.inventory.domain.request;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new supplier
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSupplierRequest {
    @NotBlank(message = "Supplier code is required")
    @Size(max = 50)
    private String supplierCode;

    @NotBlank(message = "Supplier name is required")
    @Size(max = 255)
    private String name;

    @NotNull(message = "Supplier type is required")
    private String supplierType; // MANUFACTURER, DISTRIBUTOR, etc.

    @Email(message = "Invalid email format")
    @Size(max = 100)
    private String email;

    @Size(max = 20)
    private String phone;

    @Size(max = 500)
    private String address;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String country;

    @Size(max = 20)
    private String postalCode;

    @Size(max = 100)
    private String contactPerson;

    @Size(max = 50)
    private String taxId;

    @Min(value = 0, message = "Credit limit cannot be negative")
    private BigDecimal creditLimit;

    @Min(value = 0, message = "Payment terms cannot be negative")
    private Integer paymentTermsDays;

    @Min(value = 0, message = "Rating cannot be negative")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private BigDecimal rating;

    @Size(max = 1000)
    private String notes;
}

/**
 * Request DTO for updating a supplier
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class UpdateSupplierRequest {

    @Size(max = 255)
    private String name;

    private String supplierType;

    @Email(message = "Invalid email format")
    @Size(max = 100)
    private String email;

    @Size(max = 20)
    private String phone;

    @Size(max = 500)
    private String address;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String country;

    @Size(max = 20)
    private String postalCode;

    @Size(max = 100)
    private String contactPerson;

    @Size(max = 50)
    private String taxId;

    @Min(value = 0)
    private BigDecimal creditLimit;

    @Min(value = 0)
    private Integer paymentTermsDays;

    @Min(value = 0)
    @Max(value = 5)
    private BigDecimal rating;

    private String status; // ACTIVE, INACTIVE, SUSPENDED

    @Size(max = 1000)
    private String notes;
}

