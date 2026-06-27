package com.company.trading.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SignalScheduler {
  private final TradingSignalService service;

  public SignalScheduler(TradingSignalService s) {
    service = s;
  }

  @Scheduled(fixedDelayString = "${signals.evaluation-delay-ms:60000}")
  public void evaluate() {
    try {
      service.evaluateOpenSignals();
    } catch (Exception e) {
      log.error("Scheduled signal evaluation failed", e);
    }
  }
}
