package com.company.trading.controller;

import com.company.trading.dto.CreateSignalRequest;
import com.company.trading.dto.SignalResponse;
import com.company.trading.service.TradingSignalService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("signals")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class TradingSignalController {

  private final TradingSignalService service;

  @PostMapping
  public ResponseEntity<@NonNull SignalResponse> createSignal(
      @Valid @RequestBody CreateSignalRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
  }

  @GetMapping
  public List<SignalResponse> getAllSignals() {
    return service.fetchAllSignals();
  }

  @GetMapping("{id}")
  public SignalResponse getSignal(@PathVariable Long id) {
    return service.fetchOneSignal(id);
  }

  @GetMapping("{id}/status")
  public SignalResponse getSignalStatus(@PathVariable Long id) {
    return service.fetchOneSignal(id);
  }

  @DeleteMapping("{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteSignal(@PathVariable Long id) {
    service.removeSignal(id);
  }
}
