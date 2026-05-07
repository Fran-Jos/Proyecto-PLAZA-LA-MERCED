package com.localcontrol.pos.repository;

import com.localcontrol.pos.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByUserIdOrderByCreatedAtDesc(Long userId);
}
