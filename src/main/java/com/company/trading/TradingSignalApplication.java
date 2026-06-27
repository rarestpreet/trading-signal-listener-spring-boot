package com.company.trading;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching
@EnableScheduling
@SpringBootApplication
public class TradingSignalApplication {
  public static void main(String[] args) {
    SpringApplication.run(TradingSignalApplication.class, args);
  }
}
