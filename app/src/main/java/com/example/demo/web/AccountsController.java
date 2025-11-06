package com.example.demo.web;

import com.example.demo.client.AccountsClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import java.util.List;

@RestController
@RequestMapping("/demo")
public class AccountsController {
  private final AccountsClient client;

  public AccountsController(AccountsClient client) {
    this.client = client;
  }

  @GetMapping("/accounts")
  public Mono<List<AccountsClient.Account>> list() {
    return client.getAccounts();
  }
}
