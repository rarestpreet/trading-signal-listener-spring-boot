package com.company.trading.dto;

import com.company.trading.domain.enums.Direction;
import com.company.trading.domain.enums.SignalStatus;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;

@Builder
public record SignalResponse(
    Long id,
    String symbol,
    Direction direction,
    BigDecimal entryPrice,
    BigDecimal stopLoss,
    BigDecimal targetPrice,
    Instant entryTime,
    Instant expiryTime,
    Instant createdAt,
    SignalStatus status,
    BigDecimal realizedRoi,
    BigDecimal currentRoi) {}
