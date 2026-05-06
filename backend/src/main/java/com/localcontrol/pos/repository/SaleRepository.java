package com.localcontrol.pos.repository;

import com.localcontrol.pos.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, Long> {
}
