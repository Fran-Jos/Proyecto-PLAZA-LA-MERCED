package com.localcontrol.pos.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardResponse {
    private BigDecimal totalSalesToday;
    private BigDecimal cashInBox;
    private BigDecimal deunaSalesToday;
    private List<Map<String, Object>> dailySales;
    private List<Map<String, Object>> topProducts;
    private List<Map<String, Object>> leastProducts;
    private List<Map<String, Object>> salesByCategory;
    private BigDecimal totalInventoryValue;
    private Integer lowStockCount;
    private Long totalSalesCount;
    private SessionSummary activeSession;

    @Data
    @Builder
    public static class SessionSummary {
        private Long id;
        private String name;
        private BigDecimal openingBalance;
        private BigDecimal cashSales;
        private BigDecimal deunaSales;
        private BigDecimal totalSales;
        private String openedAt;
    }
}
