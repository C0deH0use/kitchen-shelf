package pl.codehouse.restaurant.error;

import static org.springframework.http.ResponseEntity.badRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global Exception handler.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String ERRORS_PROPERTY_NAME = "errors";

    @Override
    protected Mono<ResponseEntity<Object>> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            ServerWebExchange exchange) {

        String requestUri = exchange.getRequest().getURI().toString();
        List<String> exceptions = ex.getParameterValidationResults()
                .stream()
                .map(ParameterValidationResult::getResolvableErrors)
                .map(Objects::toString)
                .toList();
        log.error("HandlerMethodValidationException when processing request to {}. Exceptions: {}", requestUri, exceptions);

        List<MessageSourceResolvable> validationErrors = ex.getParameterValidationResults()
                .stream()
                .map(ParameterValidationResult::getResolvableErrors)
                .flatMap(List::stream)
                .toList();

        ProblemDetail errorResponse = ex.getBody();
        errorResponse.setProperties(Map.of(ERRORS_PROPERTY_NAME, validationErrors));

        return Mono.just(badRequest().body(errorResponse));
    }

    @Override
    protected Mono<ResponseEntity<Object>> handleWebExchangeBindException(WebExchangeBindException ex,
                                                                          HttpHeaders headers,
                                                                          HttpStatusCode status,
                                                                          ServerWebExchange exchange) {
        String requestUri = exchange.getRequest().getURI().toString();
        List<String> exceptions = ex.getAllErrors()
                .stream()
                .map(Objects::toString)
                .toList();
        log.error("WebExchangeBindException when processing request to {}. Exceptions: {}", requestUri, exceptions);

        ProblemDetail errorResponse = ex.getBody();
        errorResponse.setProperties(Map.of(ERRORS_PROPERTY_NAME, ex.getAllErrors()));

        return Mono.just(badRequest().body(errorResponse));
    }

    /**
     * Handle constraint violations.
     *
     * @param ex      the exception
     * @param exchange the current exchange
     * @return a {@link ProblemDetail} instance
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleConstraintViolationException(ConstraintViolationException ex, ServerWebExchange exchange) {
        String requestUri = exchange.getRequest().getURI().toString();
        List<FieldError> exceptions = ex.getConstraintViolations()
                .stream()
                .map(GlobalExceptionHandler::convertToFieldError)
                .toList();
        log.error("Constraint violations when processing request to {}. Exceptions: {}", requestUri, exceptions);

        ProblemDetail errorResponse = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        errorResponse.setInstance(exchange.getRequest().getURI());
        errorResponse.setProperties(Map.of(ERRORS_PROPERTY_NAME, exceptions));

        return errorResponse;
    }

    private static FieldError convertToFieldError(ConstraintViolation<?> cv) {
        String propertyPath = cv.getPropertyPath().toString();
        Object invalidValue = cv.getInvalidValue();
        String[] errorCodes = {cv.getMessageTemplate()};
        return new FieldError(propertyPath, propertyPath, invalidValue, false, errorCodes, null, cv.getMessage());
    }
}
