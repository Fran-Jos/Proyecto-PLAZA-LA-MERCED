package com.localcontrol.pos.controller;

import com.localcontrol.pos.dto.SaleRequest;
import com.localcontrol.pos.model.Sale;
import com.localcontrol.pos.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    public ResponseEntity<Sale> create(@RequestBody SaleRequest request) {
        return ResponseEntity.ok(saleService.createSale(request));
    }

    @GetMapping
    public ResponseEntity<List<Sale>> getAll() {
        return ResponseEntity.ok(saleService.getAllSales());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Sale>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(saleService.getSalesByUser(userId));
    }
}
