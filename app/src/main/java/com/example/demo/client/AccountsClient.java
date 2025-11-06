package com.example.demo.client;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class AccountsClient {
  public record Account(String id, String name, double balance, String currency) {}

  private final WebClient webClient;

  public AccountsClient() {
    this.webClient = WebClient.builder()
        .baseUrl(System.getProperty("accounts.baseUrl", "http://localhost:4010"))
        .build();
  }

  public Mono<List<Account>> getAccounts() {
    return webClient.get()
        .uri("/accounts")
        .retrieve()
        .bodyToFlux(Account.class)
        .collectList();
  }

  public Mono<Account> getAccount(String id) {
    return webClient.get()
        .uri("/accounts/{id}", id)
        .retrieve()
        .bodyToMono(Account.class);
  }
}
