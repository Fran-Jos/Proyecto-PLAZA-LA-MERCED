package com.localcontrol.pos.controller;

import com.localcontrol.pos.dto.CashSessionRequest;
import com.localcontrol.pos.model.CashSession;
import com.localcontrol.pos.service.CashService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cash")
@RequiredArgsConstructor
public class CashController {

    private final CashService cashService;

    @GetMapping("/active")
    public ResponseEntity<CashSession> getActive(Authentication authentication) {
        return cashService.getActiveSession(authentication.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/open")
    public ResponseEntity<CashSession> open(@RequestBody CashSessionRequest request, Authentication authentication) {
        return ResponseEntity.ok(cashService.openSession(authentication.getName(), request.getBalance()));
    }

    @PostMapping("/close")
    public ResponseEntity<CashSession> close(@RequestBody CashSessionRequest request, Authentication authentication) {
        return ResponseEntity.ok(cashService.closeSession(authentication.getName(), request.getBalance()));
    }
}
