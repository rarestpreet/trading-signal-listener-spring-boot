package com.company.trading.dto;

import com.company.trading.domain.enums.Direction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.Instant;

public record CreateSignalRequest(
    @NotBlank @Pattern(regexp = "^[A-Z0-9]{5,20}$") String symbol,
    @NotNull Direction direction,
    @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal entryPrice,
    @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal stopLoss,
    @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal targetPrice,
    @NotNull Instant entryTime,
    @NotNull Instant expiryTime) {}
