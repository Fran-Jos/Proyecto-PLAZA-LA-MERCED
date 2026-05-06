package com.localcontrol.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaleRequest {
    private List<ItemRequest> items;
    private String paymentMethod; // CASH, DEUNA_TRANSFER
    private String referenceCode; // For DEUNA

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemRequest {
        private Long productId;
        private Integer quantity;
    }
}
