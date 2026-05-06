package com.localcontrol.pos.repository;

import com.localcontrol.pos.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
