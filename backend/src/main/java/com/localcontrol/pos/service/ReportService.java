package com.localcontrol.pos.service;

import com.localcontrol.pos.dto.DashboardResponse;
import com.localcontrol.pos.model.CashSession;
import com.localcontrol.pos.model.Sale;
import com.localcontrol.pos.repository.ProductRepository;
import com.localcontrol.pos.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final CashService cashService;

    public DashboardResponse getDashboardData() {
        Optional<CashSession> activeSessionOpt = cashService.getActiveSession();
        
        DashboardResponse.SessionSummary sessionSummary = null;
        BigDecimal cashSalesToday = BigDecimal.ZERO;
        BigDecimal deunaSalesToday = BigDecimal.ZERO;

        if (activeSessionOpt.isPresent()) {
            CashSession session = activeSessionOpt.get();
            List<Map<String, Object>> totalsByMethod = saleRepository.sumTotalByPaymentMethodForSession(session.getId());
            
            BigDecimal cashTotal = BigDecimal.ZERO;
            BigDecimal deunaTotal = BigDecimal.ZERO;

            for (Map<String, Object> map : totalsByMethod) {
                Sale.PaymentMethod method = (Sale.PaymentMethod) map.get("method");
                BigDecimal total = (BigDecimal) map.get("total");
                if (method == Sale.PaymentMethod.CASH) cashTotal = total;
                else if (method == Sale.PaymentMethod.DEUNA_TRANSFER) deunaTotal = total;
            }

            sessionSummary = DashboardResponse.SessionSummary.builder()
                    .id(session.getId())
                    .name(session.getName())
                    .openingBalance(session.getOpeningBalance())
                    .cashSales(cashTotal)
                    .deunaSales(deunaTotal)
                    .totalSales(cashTotal.add(deunaTotal))
                    .openedAt(session.getOpenedAt().toString())
                    .build();
            
            cashSalesToday = cashTotal;
            deunaSalesToday = deunaTotal;
        }

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

        return DashboardResponse.builder()
                .totalSalesToday(cashSalesToday.add(deunaSalesToday))
                .cashInBox(sessionSummary != null ? sessionSummary.getOpeningBalance().add(cashSalesToday) : BigDecimal.ZERO)
                .deunaSalesToday(deunaSalesToday)
                .dailySales(saleRepository.getDailySales())
                .topProducts(saleRepository.getTopSellingProducts())
                .leastProducts(saleRepository.getLeastSellingProducts())
                .salesByCategory(saleRepository.getSalesByCategory())
                .totalInventoryValue(productRepository.sumInventoryValue())
                .lowStockCount(productRepository.countLowStock())
                .totalSalesCount(saleRepository.countSalesToday(startOfToday))
                .activeSession(sessionSummary)
                .build();
    }
}
