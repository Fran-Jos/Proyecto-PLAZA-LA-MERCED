package com.localcontrol.pos.repository;

import com.localcontrol.pos.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT COUNT(p) FROM Product p WHERE p.stock <= p.minStock")
    Integer countLowStock();

    @Query("SELECT SUM(p.price * p.stock) FROM Product p")
    BigDecimal sumInventoryValue();
}
