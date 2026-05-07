package com.localcontrol.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CashSessionRequest {
    private Long userId;
    private BigDecimal balance; // Opening or Reported balance
    private String boxName;
}
