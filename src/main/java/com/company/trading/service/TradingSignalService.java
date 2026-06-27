package com.company.trading.service;

import com.company.trading.client.BinancePriceClient;
import com.company.trading.domain.TradingSignal;
import com.company.trading.domain.enums.SignalStatus;
import com.company.trading.dto.CreateSignalRequest;
import com.company.trading.dto.SignalResponse;
import com.company.trading.exception.NotFoundException;
import com.company.trading.repository.TradingSignalRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TradingSignalService {

  private final TradingSignalRepository tradingSignalRepository;
  private final BinancePriceClient prices;
  private final SignalCalculator signalCalculator;
  private final Clock clock = Clock.systemUTC();

  @Transactional
  public SignalResponse create(CreateSignalRequest request) {
    signalCalculator.validate(request, clock.instant());

    TradingSignal signal =
        TradingSignal.builder()
            .symbol(request.symbol().toUpperCase())
            .direction(request.direction())
            .entryPrice(request.entryPrice())
            .stopLoss(request.stopLoss())
            .targetPrice(request.targetPrice())
            .entryTime(request.entryTime())
            .expiryTime(request.expiryTime())
            .status(SignalStatus.OPEN)
            .build();

    return evaluateAndMap(tradingSignalRepository.save(signal));
  }

  @Transactional
  public List<SignalResponse> fetchAllSignals() {

    return tradingSignalRepository.findAll().stream().map(this::evaluateAndMap).toList();
  }

  @Transactional
  public SignalResponse fetchOneSignal(Long id) {
    return evaluateAndMap(findSignal(id));
  }

  @Transactional
  public void removeSignal(Long id) {
    tradingSignalRepository.delete(findSignal(id));
  }

  @Transactional
  public void evaluateOpenSignals() {
    tradingSignalRepository.findByStatus(SignalStatus.OPEN).forEach(this::evaluateAndMap);
  }

  private TradingSignal findSignal(Long id) {
    return tradingSignalRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Signal " + id + " not found"));
  }

  private SignalResponse evaluateAndMap(TradingSignal signal) {
    BigDecimal price = prices.getPrice(signal.getSymbol());

    SignalStatus next = signalCalculator.status(signal, price, clock.instant());

    BigDecimal roi = signalCalculator.roi(signal.getDirection(), signal.getEntryPrice(), price);

    if (next != SignalStatus.OPEN && signal.getStatus() == SignalStatus.OPEN) {
      signal.setStatus(next);
      signal.setRealizedRoi(roi);
      tradingSignalRepository.save(signal);
    }

    return SignalResponse.builder()
        .id(signal.getId())
        .symbol(signal.getSymbol())
        .direction(signal.getDirection())
        .entryPrice(signal.getEntryPrice())
        .stopLoss(signal.getStopLoss())
        .targetPrice(signal.getTargetPrice())
        .entryTime(signal.getEntryTime())
        .expiryTime(signal.getExpiryTime())
        .createdAt(signal.getCreatedAt())
        .status(signal.getStatus())
        .realizedRoi(signal.getRealizedRoi())
        .currentRoi(roi)
        .build();
  }
}
