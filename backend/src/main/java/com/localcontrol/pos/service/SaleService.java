package com.localcontrol.pos.service;

import com.localcontrol.pos.dto.SaleRequest;
import com.localcontrol.pos.model.*;
import com.localcontrol.pos.repository.ProductRepository;
import com.localcontrol.pos.repository.SaleRepository;
import com.localcontrol.pos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CashService cashService;

    @Transactional
    public Sale createSale(SaleRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        CashSession activeCashSession = cashService.getActiveSession()
                .orElseThrow(() -> new RuntimeException("No hay caja abierta. Abra una caja antes de vender."));

        Sale sale = Sale.builder()
                .paymentMethod(Sale.PaymentMethod.valueOf(request.getPaymentMethod()))
                .referenceCode(request.getReferenceCode())
                .user(user)
                .cashSession(activeCashSession)
                .items(new ArrayList<>())
                .total(BigDecimal.ZERO)
                .build();

        BigDecimal totalSale = BigDecimal.ZERO;

        for (SaleRequest.ItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + itemReq.getProductId()));

            if (product.getStock() < itemReq.getQuantity()) {
                throw new RuntimeException("Stock insuficiente para: " + product.getName());
            }

            // Descontar stock
            product.setStock(product.getStock() - itemReq.getQuantity());
            productRepository.save(product);

            BigDecimal subtotal = product.getPrice().multiply(new BigDecimal(itemReq.getQuantity()));
            
            SaleItem saleItem = SaleItem.builder()
                    .sale(sale)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(product.getPrice())
                    .subtotal(subtotal)
                    .build();

            sale.getItems().add(saleItem);
            totalSale = totalSale.add(subtotal);
        }

        sale.setTotal(totalSale);

        // Validación estricta para transferencias: Debe existir un número de comprobante
        if (sale.getPaymentMethod() == Sale.PaymentMethod.DEUNA_TRANSFER) {
            if (sale.getReferenceCode() == null || sale.getReferenceCode().trim().isEmpty()) {
                throw new RuntimeException("El número de comprobante es obligatorio para transferencias DEUNA");
            }
        }

        return saleRepository.save(sale);
    }

    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    public List<Sale> getSalesByUser(Long userId) {
        return saleRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
