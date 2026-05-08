package com.localcontrol.pos.controller;

import com.localcontrol.pos.model.Category;
import com.localcontrol.pos.model.Product;
import com.localcontrol.pos.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> getAll() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        return ResponseEntity.ok(productService.saveProduct(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        return ResponseEntity.ok(productService.saveProduct(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(productService.getAllCategories());
    }

    @PostMapping("/categories")
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        return ResponseEntity.ok(productService.saveCategory(category));
    }

    @PatchMapping("/{id}/favorite")
    public ResponseEntity<Product> toggleFavorite(@PathVariable Long id) {
        return ResponseEntity.ok(productService.toggleFavorite(id));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<Product> updateStock(
            @PathVariable Long id,
            @RequestParam(required = false) Integer quantity,
            @RequestBody(required = false) Map<String, Integer> payload
    ) {
        Integer quantityValue = quantity;
        if (quantityValue == null && payload != null) {
            quantityValue = payload.get("quantity");
        }

        if (quantityValue == null) {
            throw new IllegalArgumentException("La cantidad es obligatoria");
        }

        return ResponseEntity.ok(productService.updateStock(id, quantityValue));
    }
}
