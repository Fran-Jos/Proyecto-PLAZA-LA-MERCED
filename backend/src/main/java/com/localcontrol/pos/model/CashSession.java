package com.localcontrol.pos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cash_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "opening_balance", nullable = false)
    private BigDecimal openingBalance;

    @Column(name = "closing_balance")
    private BigDecimal closingBalance; // System expected balance

    @Column(name = "reported_balance")
    private BigDecimal reportedBalance; // What the cashier says

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    public enum SessionStatus {
        OPEN, CLOSED
    }
}
