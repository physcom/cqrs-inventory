package kg.akyl.java.inventory.domain.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for Supply Chain Network Visualization
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplyChainNetworkDTO {
    private List<NetworkNodeDTO> nodes;
    private List<NetworkEdgeDTO> edges;
    private NetworkStatisticsDTO statistics;
}

/**
 * Network Node representing a supplier
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class NetworkNodeDTO {
    private Long id;
    private String name;
    private String code;
    private String type;
    private String status;
    private BigDecimal rating;
    private Integer outgoingTransactions;
    private Integer incomingTransactions;
    private BigDecimal totalVolume;
    private String city;
    private String country;
}

/**
 * Network Edge representing supplier connection
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class NetworkEdgeDTO {
    private Long fromId;
    private Long toId;
    private String fromName;
    private String toName;
    private Integer transactionCount;
    private BigDecimal totalValue;
    private BigDecimal averageValue;
    private Double averageLeadTime;
}

/**
 * Network Statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class NetworkStatisticsDTO {
    private Integer totalSuppliers;
    private Integer totalConnections;
    private Integer totalTransactions;
    private BigDecimal totalVolume;
    private BigDecimal averageTransactionValue;
    private Integer activeSuppliers;
    private Integer isolatedSuppliers;
    private Double networkDensity; // percentage
    private Double averageConnectionsPerSupplier;
    private Double criticalPathLength; // average hops
}