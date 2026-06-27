package com.company.trading.service;

import com.company.trading.domain.*;
import com.company.trading.domain.enums.Direction;
import com.company.trading.domain.enums.SignalStatus;
import com.company.trading.dto.CreateSignalRequest;
import com.company.trading.exception.BadRequestException;
import java.math.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

@Component
public class SignalCalculator {
  public void validate(CreateSignalRequest r, Instant now) {
    if (r.entryTime().isBefore(now.minus(24, ChronoUnit.HOURS)))
      throw new BadRequestException("Entry time may not be more than 24 hours in the past");

    if (r.entryTime().isAfter(now))
      throw new BadRequestException("Entry time may not be in the future");

    if (!r.expiryTime().isAfter(r.entryTime()))
      throw new BadRequestException("Expiry time must be after entry time");

    if (r.direction() == Direction.BUY
        && (r.stopLoss().compareTo(r.entryPrice()) >= 0
            || r.targetPrice().compareTo(r.entryPrice()) <= 0))
      throw new BadRequestException("BUY requires stop loss < entry price < target price");

    if (r.direction() == Direction.SELL
        && (r.stopLoss().compareTo(r.entryPrice()) <= 0
            || r.targetPrice().compareTo(r.entryPrice()) >= 0))
      throw new BadRequestException("SELL requires target price < entry price < stop loss");
  }

  public SignalStatus status(TradingSignal s, BigDecimal price, Instant now) {

    if (s.getStatus() != SignalStatus.OPEN) return s.getStatus();

    if (s.getDirection() == Direction.BUY) {

      if (price.compareTo(s.getTargetPrice()) >= 0) return SignalStatus.TARGET_HIT;

      if (price.compareTo(s.getStopLoss()) <= 0) return SignalStatus.STOPLOSS_HIT;
    } else {

      if (price.compareTo(s.getTargetPrice()) <= 0) return SignalStatus.TARGET_HIT;

      if (price.compareTo(s.getStopLoss()) >= 0) return SignalStatus.STOPLOSS_HIT;
    }

    return now.isAfter(s.getExpiryTime()) ? SignalStatus.EXPIRED : SignalStatus.OPEN;
  }

  public BigDecimal roi(Direction d, BigDecimal entry, BigDecimal current) {
    BigDecimal delta = d == Direction.BUY ? current.subtract(entry) : entry.subtract(current);

    return delta.multiply(BigDecimal.valueOf(100)).divide(entry, 2, RoundingMode.HALF_UP);
  }
}
