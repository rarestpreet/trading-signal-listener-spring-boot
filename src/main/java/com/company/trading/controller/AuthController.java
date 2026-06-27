package com.company.trading.controller;

import com.company.trading.exception.BadRequestException;
import com.company.trading.security.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {

  private final BCryptPasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  @PostMapping("login")
  public Token login(@Valid @RequestBody Login request) {
    final String username = "admin";
    final String passwordHash = passwordEncoder.encode("admin123");

    if (!username.equals(request.username())
        || !passwordEncoder.matches(request.password(), passwordHash))
      throw new BadRequestException("Invalid credentials");

    return new Token(jwtService.create(username), "Bearer");
  }

  public record Login(@NotBlank String username, @NotBlank String password) {}

  public record Token(String accessToken, String tokenType) {}
}
