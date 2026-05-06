package com.localcontrol.pos.controller;

import com.localcontrol.pos.dto.CashSessionRequest;
import com.localcontrol.pos.model.CashSession;
import com.localcontrol.pos.service.CashService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cash")
@RequiredArgsConstructor
public class CashController {

    private final CashService cashService;

    @GetMapping("/active/{userId}")
    public ResponseEntity<CashSession> getActive(@PathVariable Long userId) {
        return cashService.getActiveSession(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/open")
    public ResponseEntity<CashSession> open(@RequestBody CashSessionRequest request) {
        return ResponseEntity.ok(cashService.openSession(request.getUserId(), request.getBalance()));
    }

    @PostMapping("/close")
    public ResponseEntity<CashSession> close(@RequestBody CashSessionRequest request) {
        return ResponseEntity.ok(cashService.closeSession(request.getUserId(), request.getBalance()));
    }
}
