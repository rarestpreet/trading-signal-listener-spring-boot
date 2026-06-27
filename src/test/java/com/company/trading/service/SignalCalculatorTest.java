package com.company.trading.service;

import static org.junit.jupiter.api.Assertions.*;

import com.company.trading.domain.*;
import com.company.trading.domain.enums.Direction;
import com.company.trading.domain.enums.SignalStatus;
import com.company.trading.dto.CreateSignalRequest;
import com.company.trading.exception.BadRequestException;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.*;

class SignalCalculatorTest {
  private final SignalCalculator calc = new SignalCalculator();
  private final Instant now = Instant.parse("2026-01-02T12:00:00Z");

  @Test
  void acceptsValidBuy() {
    assertDoesNotThrow(
        () ->
            calc.validate(
                req(Direction.BUY, "100", "90", "120", now.minusSeconds(60), now.plusSeconds(60)),
                now));
  }

  @Test
  void rejectsInvalidBuy() {
    assertThrows(
        BadRequestException.class,
        () ->
            calc.validate(req(Direction.BUY, "100", "101", "120", now, now.plusSeconds(60)), now));
  }

  @Test
  void acceptsValidSell() {
    assertDoesNotThrow(
        () ->
            calc.validate(req(Direction.SELL, "100", "110", "80", now, now.plusSeconds(60)), now));
  }

  @Test
  void rejectsInvalidSell() {
    assertThrows(
        BadRequestException.class,
        () -> calc.validate(req(Direction.SELL, "100", "90", "80", now, now.plusSeconds(60)), now));
  }

  @Test
  void rejectsEntryOlderThan24Hours() {
    assertThrows(
        BadRequestException.class,
        () ->
            calc.validate(
                req(
                    Direction.BUY,
                    "100",
                    "90",
                    "120",
                    now.minusSeconds(86401),
                    now.plusSeconds(60)),
                now));
  }

  @Test
  void rejectsExpiryBeforeEntry() {
    assertThrows(
        BadRequestException.class,
        () -> calc.validate(req(Direction.BUY, "100", "90", "120", now, now.minusSeconds(1)), now));
  }

  @Test
  void buyTargetAndStop() {
    assertEquals(SignalStatus.TARGET_HIT, calc.status(signal(Direction.BUY), bd("120"), now));
    assertEquals(SignalStatus.STOPLOSS_HIT, calc.status(signal(Direction.BUY), bd("90"), now));
  }

  @Test
  void sellTargetAndStop() {
    assertEquals(SignalStatus.TARGET_HIT, calc.status(signal(Direction.SELL), bd("80"), now));
    assertEquals(SignalStatus.STOPLOSS_HIT, calc.status(signal(Direction.SELL), bd("110"), now));
  }

  @Test
  void expiresOpenSignal() {
    TradingSignal s = signal(Direction.BUY);
    s.setExpiryTime(now.minusSeconds(1));
    assertEquals(SignalStatus.EXPIRED, calc.status(s, bd("100"), now));
  }

  @Test
  void terminalStateNeverChanges() {
    TradingSignal s = signal(Direction.BUY);
    s.setStatus(SignalStatus.TARGET_HIT);
    assertEquals(SignalStatus.TARGET_HIT, calc.status(s, bd("1"), now.plusSeconds(999)));
  }

  @Test
  void calculatesRoi() {
    assertEquals(bd("20.00"), calc.roi(Direction.BUY, bd("100"), bd("120")));
    assertEquals(bd("20.00"), calc.roi(Direction.SELL, bd("100"), bd("80")));
  }

  private CreateSignalRequest req(
      Direction d, String e, String sl, String t, Instant entry, Instant expiry) {
    return new CreateSignalRequest("BTCUSDT", d, bd(e), bd(sl), bd(t), entry, expiry);
  }

  private TradingSignal signal(Direction d) {
    return TradingSignal.builder()
        .direction(d)
        .entryPrice(bd("100"))
        .stopLoss(bd(d == Direction.BUY ? "90" : "110"))
        .targetPrice(bd(d == Direction.BUY ? "120" : "80"))
        .expiryTime(now.plusSeconds(60))
        .status(SignalStatus.OPEN)
        .build();
  }

  private BigDecimal bd(String n) {
    return new BigDecimal(n);
  }
}
