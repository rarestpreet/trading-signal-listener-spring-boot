package com.company.trading.domain;

import com.company.trading.domain.enums.Direction;
import com.company.trading.domain.enums.SignalStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;

@Entity
@Table(
    name = "trading_signals",
    indexes = @Index(name = "idx_signal_status", columnList = "status"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradingSignal {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 20)
  private String symbol;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 4)
  private Direction direction;

  @Column(name = "entry_price", nullable = false, precision = 19, scale = 8)
  private BigDecimal entryPrice;

  @Column(name = "stop_loss", nullable = false, precision = 19, scale = 8)
  private BigDecimal stopLoss;

  @Column(name = "target_price", nullable = false, precision = 19, scale = 8)
  private BigDecimal targetPrice;

  @Column(name = "entry_time", nullable = false)
  private Instant entryTime;

  @Column(name = "expiry_time", nullable = false)
  private Instant expiryTime;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private SignalStatus status;

  @Column(name = "realized_roi", precision = 10, scale = 2)
  private BigDecimal realizedRoi;

  @Version private Long version;

  @PrePersist
  void init() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
    if (status == null) {
      status = SignalStatus.OPEN;
    }
  }
}
