package com.localcontrol.pos.controller;

import com.localcontrol.pos.dto.SaleRequest;
import com.localcontrol.pos.model.Sale;
import com.localcontrol.pos.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    public ResponseEntity<Sale> create(@RequestBody SaleRequest request, Authentication authentication) {
        return ResponseEntity.ok(saleService.createSale(request, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<Sale>> getAll() {
        return ResponseEntity.ok(saleService.getAllSales());
    }
}
