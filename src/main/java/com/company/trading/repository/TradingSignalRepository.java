package com.company.trading.repository;

import com.company.trading.domain.*;
import com.company.trading.domain.enums.SignalStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradingSignalRepository extends JpaRepository<TradingSignal, Long> {
  List<TradingSignal> findByStatus(SignalStatus status);
}
