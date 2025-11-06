package tests;

import com.example.demo.client.AccountsClient;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import reactor.test.StepVerifier;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ContractsIT {
  static AccountsClient client;

    @BeforeAll
    static void setup() {
        System.setProperty("accounts.baseUrl", "http://127.0.0.1:4010"); // was localhost
        waitFor("http://127.0.0.1:4010/accounts");
        client = new AccountsClient();
    }

    private static void waitFor(String url) {
        HttpClient client = HttpClient.newHttpClient();
        Awaitility.await().atMost(Duration.ofSeconds(25)).pollDelay(Duration.ofMillis(200)).until(() -> {
            try {
                var req = HttpRequest.newBuilder(URI.create(url)).GET().build();
                var resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                return resp.statusCode() >= 200 && resp.statusCode() < 500;
            } catch (Exception e) {
                return false;
            }
        });
    }


    @Test
    void listAccounts_matchesContractExamples() {
        StepVerifier.create(client.getAccounts())
                .assertNext(list -> {
                    org.junit.jupiter.api.Assertions.assertFalse(list.isEmpty(), "should return example data");
                    var a = list.get(0); // ← was getFirst()
                    org.junit.jupiter.api.Assertions.assertNotNull(a.id());
                    org.junit.jupiter.api.Assertions.assertNotNull(a.name());
                    org.junit.jupiter.api.Assertions.assertNotNull(a.currency());
                })
                .verifyComplete();
    }
}
