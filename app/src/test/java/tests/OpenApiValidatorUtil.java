package tests;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.model.Response;
import com.atlassian.oai.validator.model.SimpleResponse;
import com.atlassian.oai.validator.report.ValidationReport;

public class OpenApiValidatorUtil {
  private final OpenApiInteractionValidator validator;

  public OpenApiValidatorUtil(String openApiPath) {
    // openApiPath is relative to the project root (-Dproject.root set in the POM)
    String abs = System.getProperty("project.root") + "/" + openApiPath;
    this.validator = OpenApiInteractionValidator.createFor(abs).build();
  }

  /** Validate a response against an operation identified by (method + path template). */
  public void assertResponseValid(String method, String pathTemplate, int status, String body, String contentType) {
    Response resp = SimpleResponse.Builder
            .status(status)
            .withContentType(contentType)
            .withBody(body == null ? "" : body)
            .build();

    // NOTE: correct overload is (String pathTemplate, Request.Method method, Response resp)
    ValidationReport report = validator.validateResponse(pathTemplate, Request.Method.valueOf(method), resp);
    if (report.hasErrors()) {
      throw new AssertionError("OpenAPI validation errors: " + report);
    }
  }
}
