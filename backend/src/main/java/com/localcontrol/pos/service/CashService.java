package com.localcontrol.pos.service;

import com.localcontrol.pos.model.CashSession;
import com.localcontrol.pos.model.Sale;
import com.localcontrol.pos.model.User;
import com.localcontrol.pos.repository.CashSessionRepository;
import com.localcontrol.pos.repository.SaleRepository;
import com.localcontrol.pos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CashService {

    private final CashSessionRepository cashSessionRepository;
    private final SaleRepository saleRepository;
    private final UserRepository userRepository;

    public Optional<CashSession> getActiveSession(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return cashSessionRepository.findByUserAndStatus(user, CashSession.SessionStatus.OPEN);
    }

    @Transactional
    public CashSession openSession(Long userId, BigDecimal openingBalance) {
        User user = userRepository.findById(userId).orElseThrow();
        
        if (getActiveSession(userId).isPresent()) {
            throw new RuntimeException("Ya existe una sesión abierta para este usuario.");
        }

        CashSession session = CashSession.builder()
                .user(user)
                .openingBalance(openingBalance)
                .status(CashSession.SessionStatus.OPEN)
                .openedAt(LocalDateTime.now())
                .build();

        return cashSessionRepository.save(session);
    }

    @Transactional
    public CashSession closeSession(Long userId, BigDecimal reportedBalance) {
        CashSession session = getActiveSession(userId)
                .orElseThrow(() -> new RuntimeException("No hay sesión abierta para cerrar."));

        // Calcular balance esperado (Ventas en EFECTIVO desde la apertura)
        // Nota: En un sistema real filtraríamos ventas por fecha y usuario
        List<Sale> sales = saleRepository.findAll(); // Simplificación para este MVP
        BigDecimal cashSalesTotal = sales.stream()
                .filter(s -> s.getCreatedAt().isAfter(session.getOpenedAt()))
                .filter(s -> s.getPaymentMethod() == Sale.PaymentMethod.CASH)
                .map(Sale::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expectedBalance = session.getOpeningBalance().add(cashSalesTotal);

        session.setClosingBalance(expectedBalance);
        session.setReportedBalance(reportedBalance);
        session.setStatus(CashSession.SessionStatus.CLOSED);
        session.setClosedAt(LocalDateTime.now());

        return cashSessionRepository.save(session);
    }
}
