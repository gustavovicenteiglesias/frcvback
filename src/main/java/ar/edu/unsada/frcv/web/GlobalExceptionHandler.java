// GlobalExceptionHandler.java
package ar.edu.unsada.frcv.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 404
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiError> handleNotFound(NoSuchElementException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req);
    }

    // 400 - payload malformado o tipos inválidos
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", "Cuerpo de la solicitud inválido o malformado.", req);
    }

    // 400 - @Valid en @RequestBody (Bean Validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        var fields = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ApiError.FieldError(fe.getField(), fe.getDefaultMessage()))
                .collect(Collectors.toList());
        return buildWithFields(HttpStatus.BAD_REQUEST, "Validation Failed", "Hay errores de validación.", req, fields);
    }

    // 400 - @Validated en parámetros de handler (Spring 6+)


    // 400 - @Validated en servicios/repos o @PathVariable/@RequestParam (jakarta.validation)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        var fields = ex.getConstraintViolations().stream()
                .map(cv -> new ApiError.FieldError(cv.getPropertyPath().toString(), cv.getMessage()))
                .collect(Collectors.toList());
        return buildWithFields(HttpStatus.BAD_REQUEST, "Validation Failed", "Hay errores de validación.", req, fields);
    }

    // 400 - faltan parámetros requeridos
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", "Falta el parámetro: " + ex.getParameterName(), req);
    }

    // 405 - método HTTP no soportado
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", ex.getMessage(), req);
    }

    // 400 - errores de negocio simples
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req);
    }

    // 409 - conflictos de estado/reglas (ej: ya existe, 1–1 ocupado)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), req);
    }

    // 409 - violaciones de integridad (unique, FK, etc.)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        String msg = "Violación de integridad de datos (unique/FK).";
        if (ex.getMostSpecificCause() != null && ex.getMostSpecificCause().getMessage() != null) {
            msg = msg + " Detalle: " + ex.getMostSpecificCause().getMessage();
        }
        return build(HttpStatus.CONFLICT, "Conflict", msg, req);
    }

    // 403 - seguridad
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "Forbidden", "Acceso denegado.", req);
    }

    // 500 - fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
        // TODO: loggear ex con stacktrace (logger)
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Error interno. " + ex.getMessage(), req);
    }

    // Helpers
    private ResponseEntity<ApiError> build(HttpStatus status, String error, String message, HttpServletRequest req) {
        var body = new ApiError()
                .status(status.value())
                .error(error)
                .message(message)
                .path(req.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }

    private ResponseEntity<ApiError> buildWithFields(HttpStatus status, String error, String message,
                                                     HttpServletRequest req, java.util.List<ApiError.FieldError> fields) {
        var body = new ApiError()
                .status(status.value())
                .error(error)
                .message(message)
                .path(req.getRequestURI())
                .fieldErrors(fields);
        return ResponseEntity.status(status).body(body);
    }
}
