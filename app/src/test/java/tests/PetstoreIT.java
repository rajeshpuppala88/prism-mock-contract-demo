package tests;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import org.awaitility.Awaitility;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;


public class PetstoreIT {
  static OpenApiValidatorUtil validator;


  @BeforeAll
  static void setup() throws Exception {
    RestAssured.baseURI = "http://localhost:4020";
    waitFor("http://localhost:4020/pets");
    validator = new OpenApiValidatorUtil("api/petstore.yaml");
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

  @ParameterizedTest
  @ValueSource(ints = {1, 5, 10})
  void listPets_ok_validatesContract(int limit) {
    var resp = given()
            .accept("application/json")
            .queryParam("limit", limit)
            .when().get("/pets")
            .then()
            .statusCode(200)
            .body("$", notNullValue())
            .extract().response();

    validator.assertResponseValid("GET", "/pets", 200, resp.asString(), "application/json");
  }

  @Test
  void listPets_badRequest_forced400() {
    var forced = given()
        .accept("application/json")
        .header(new Header("Prefer", "code=400,example=badLimit"))
        .queryParam("limit", 999)
        .when().get("/pets")
        .then()
        .statusCode(400)
        .body("code", equalTo("BAD_REQUEST"))
        .extract().response();

    validator.assertResponseValid("GET", "/pets", 400, forced.asString(), "application/json");
  }

  @Test
  void createPet_created_and_valid() {
    var reqBody = """
      { "name": "Buddy", "tag": "dog" }
    """;

    var resp = given()
        .contentType("application/json").accept("application/json")
        .body(reqBody)
        .when().post("/pets")
        .then()
        .statusCode(201)
        .body("id", notNullValue())
        .body("name", equalTo("Buddy"))
        .extract().response();

    validator.assertResponseValid("POST", "/pets", 201, resp.asString(), "application/json");
  }

  @Test
  void createPet_missingName_returns400() {
    // valid text block (note the newlines after opening/closing """)
    var invalid = """
    { "tag": "cat" }
    """;

    var resp = given()
            .contentType("application/json").accept("application/json")
            .header(new Header("Prefer", "code=400,example=missingName"))
            .body(invalid)
            .when().post("/pets")
            .then()
            .statusCode(400)
            .body("code", equalTo("VALIDATION_ERROR"))
            .extract().response();

    validator.assertResponseValid("POST", "/pets", 400, resp.asString(), "application/json");
  }


  @Test
  void getPet_found_and_notFound_valid() {
    var ok = given().accept("application/json")
            .when().get("/pets/1001")
            .then().statusCode(anyOf(is(200), is(404)))
            .extract().response();

    // use the template path for validation
    validator.assertResponseValid("GET", "/pets/{petId}", ok.statusCode(), ok.asString(), "application/json");

    var notFound = given().accept("application/json")
            .header(new Header("Prefer", "code=404,example=notFound"))
            .when().get("/pets/999999")
            .then().statusCode(404)
            .body("code", equalTo("NOT_FOUND"))
            .extract().response();

    validator.assertResponseValid("GET", "/pets/{petId}", 404, notFound.asString(), "application/json");
  }

}
