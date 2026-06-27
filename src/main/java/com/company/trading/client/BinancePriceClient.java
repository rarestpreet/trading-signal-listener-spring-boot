package com.company.trading.client;

import java.math.BigDecimal;

public interface BinancePriceClient {
  BigDecimal getPrice(String symbol);
}
