package kg.akyl.java.inventory.domain.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierDTO {
    private Long id;

    @NotBlank(message = "Supplier code is required")
    @Size(max = 50)
    private String supplierCode;

    @NotBlank(message = "Supplier name is required")
    @Size(max = 255)
    private String name;

    private String supplierType;

    @Email(message = "Invalid email format")
    private String email;

    private String phone;
    private String address;
    private String city;
    private String country;
    private String postalCode;
    private String contactPerson;
    private String taxId;
    private BigDecimal creditLimit;
    private Integer paymentTermsDays;
    private BigDecimal rating;
    private String status;
    private String notes;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private Long version;
}
