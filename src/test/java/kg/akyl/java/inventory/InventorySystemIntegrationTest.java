package kg.gns.java.inventorysystem;

import kg.gns.java.inventorysystem.InventoryApplication;
import kg.gns.java.inventorysystem.web.ProductController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = InventoryApplication.class)
@AutoConfigureWebMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
public class InventorySystemIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateAndRetrieveProduct() throws Exception {
        // Create product request
        ProductController.CreateProductRequest createRequest = new ProductController.CreateProductRequest();
        createRequest.setSku("TEST-001");
        createRequest.setName("Test Product");
        createRequest.setDescription("Test Description");
        createRequest.setPrice(new java.math.BigDecimal("29.99"));
        createRequest.setQuantity(100);
        createRequest.setCategory("electronics");

        String requestJson = objectMapper.writeValueAsString(createRequest);

        // Create product
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        // Retrieve product by SKU
        mockMvc.perform(get("/api/products/sku/TEST-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("TEST-001"))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    public void testInventoryUpdate() throws Exception {
        // First create a product (setup)
        // Then test quantity update
        ProductController.UpdateQuantityRequest updateRequest = new ProductController.UpdateQuantityRequest();
        updateRequest.setNewQuantity(50);

        String requestJson = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/api/products/1/quantity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());
    }

    @Test
    public void testSaleProcessing() throws Exception {
        // Test sale processing workflow
        // This would include creating a product, processing a sale, and verifying inventory reduction
    }

    @Test
    public void testAnalyticsEndpoints() throws Exception {
        // Test analytics endpoints
        mockMvc.perform(get("/api/analytics/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
