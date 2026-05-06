package com.localcontrol.pos.repository;

import com.localcontrol.pos.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
