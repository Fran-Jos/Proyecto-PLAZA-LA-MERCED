package com.localcontrol.pos.repository;

import com.localcontrol.pos.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Sale> findByCashSessionId(Long cashSessionId);

    boolean existsByReferenceCode(String referenceCode);

    @Query("SELECT s.paymentMethod as method, SUM(s.total) as total FROM Sale s WHERE s.cashSession.id = :sessionId GROUP BY s.paymentMethod")
    List<Map<String, Object>> sumTotalByPaymentMethodForSession(@Param("sessionId") Long sessionId);

    @Query("SELECT CAST(s.createdAt AS date) as date, SUM(s.total) as total FROM Sale s GROUP BY CAST(s.createdAt AS date) ORDER BY CAST(s.createdAt AS date) DESC")
    List<Map<String, Object>> getDailySales();

    @Query("SELECT si.product.name as name, SUM(si.quantity) as totalSold FROM SaleItem si GROUP BY si.product.id, si.product.name ORDER BY SUM(si.quantity) DESC")
    List<Map<String, Object>> getTopSellingProducts();

    @Query("SELECT si.product.name as name, SUM(si.quantity) as totalSold FROM SaleItem si GROUP BY si.product.id, si.product.name ORDER BY SUM(si.quantity) ASC")
    List<Map<String, Object>> getLeastSellingProducts();

    @Query("SELECT si.product.category.name as name, SUM(si.subtotal) as total FROM SaleItem si GROUP BY si.product.category.id, si.product.category.name")
    List<Map<String, Object>> getSalesByCategory();

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.createdAt >= :startOfDay")
    Long countSalesToday(@Param("startOfDay") LocalDateTime startOfDay);
}
