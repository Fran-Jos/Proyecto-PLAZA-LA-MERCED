package com.localcontrol.pos.repository;

import com.localcontrol.pos.model.CashSession;
import com.localcontrol.pos.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CashSessionRepository extends JpaRepository<CashSession, Long> {
    Optional<CashSession> findByUserAndStatus(User user, CashSession.SessionStatus status);
    List<CashSession> findByUserOrderByOpenedAtDesc(User user);
}
