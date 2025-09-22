package kg.gns.java.inventorysystem.command.commands;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateProductQuantityCommand {
    private Long productId;
    private Integer newQuantity;
}
