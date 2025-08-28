// ApiError.java
package ar.edu.unsada.frcv.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    public Instant timestamp = Instant.now();
    public int status;
    public String error;
    public String message;
    public String path;
    public List<FieldError> fieldErrors;

    public ApiError status(int s) { this.status = s; return this; }
    public ApiError error(String e) { this.error = e; return this; }
    public ApiError message(String m) { this.message = m; return this; }
    public ApiError path(String p) { this.path = p; return this; }
    public ApiError fieldErrors(List<FieldError> fe) { this.fieldErrors = fe; return this; }

    public record FieldError(String field, String message) {}
}
