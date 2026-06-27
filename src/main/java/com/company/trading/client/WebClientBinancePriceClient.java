package com.company.trading.client;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class WebClientBinancePriceClient implements BinancePriceClient {

  private final WebClient client;

  public WebClientBinancePriceClient(@Value("${binance.base-url}") String baseUrl) {
    this.client = WebClient.builder().baseUrl(baseUrl).build();
  }

  @Override
  @Cacheable(value = "prices", key = "#symbol")
  public BigDecimal getPrice(String symbol) {
    PriceResponse r =
        client
            .get()
            .uri(u -> u.path("/api/v3/ticker/price").queryParam("symbol", symbol).build())
            .retrieve()
            .bodyToMono(PriceResponse.class)
            .block();

    if (r == null || r.price() == null) {
      throw new IllegalStateException("Binance returned no price for " + symbol);
    }

    return r.price();
  }

  private record PriceResponse(String symbol, BigDecimal price) {}
}
