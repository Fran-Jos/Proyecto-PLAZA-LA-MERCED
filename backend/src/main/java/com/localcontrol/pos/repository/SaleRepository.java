package com.localcontrol.pos.repository;

import com.localcontrol.pos.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    boolean existsByReferenceCodeIgnoreCase(String referenceCode);
    List<Sale> findAllByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime from, LocalDateTime to);
    List<Sale> findAllByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime from);
}
