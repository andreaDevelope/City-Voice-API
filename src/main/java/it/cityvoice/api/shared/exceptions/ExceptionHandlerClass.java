package it.cityvoice.api.shared.exceptions;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ExceptionHandlerClass {

    private static final Logger log = LoggerFactory.getLogger(ExceptionHandlerClass.class);

    // 400 - validazione su parametri e path variable
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleParamValidation(ConstraintViolationException ex) {
        log.warn("Validazione parametri fallita", ex);
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> {
                            String path = v.getPropertyPath().toString();
                            return path.contains(".")
                                    ? path.substring(path.lastIndexOf('.') + 1)
                                    : path;
                        },
                        v -> v.getMessage(),
                        (existing, replacement) -> existing
                ));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // 409
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> handleConflict(ConflictException ex) {
        log.warn("Conflitto risorsa", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", ex.getMessage()));
    }

    // 400
    @ExceptionHandler({
            BadRequestException.class,
            IllegalArgumentException.class,
            DataIntegrityViolationException.class
    })
    public ResponseEntity<Map<String, String>> handleBadRequest(RuntimeException ex) {
        log.warn("Richiesta non valida", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage()));
    }

    // 401 - username o password sbagliati
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Credenziali non valide", ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Username o password non validi"));
    }

    // 403
    @ExceptionHandler({AccessDeniedException.class, UnauthorizedException.class})
    public ResponseEntity<Map<String, String>> handleForbidden(Exception ex) {
        log.warn("Accesso negato", ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Non hai i permessi per questa operazione"));
    }

    // 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Risorsa non trovata", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", ex.getMessage()));
    }

    // 400 - validazione su @RequestBody con @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleBodyValidation(MethodArgumentNotValidException ex) {
        log.warn("Validazione body fallita", ex);
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        org.springframework.validation.FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Valore non valido",
                        (existing, replacement) -> existing
                ));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // 400 - body JSON malformato o tipo di campo non valido
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadableBody(HttpMessageNotReadableException ex) {
        log.warn("Body della richiesta non leggibile", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Formato della richiesta non valido"));
    }

    // 500 - messaggio fisso, il dettaglio resta nel log
    @ExceptionHandler({InternalServerErrorException.class, Exception.class})
    public ResponseEntity<Map<String, String>> handleServerError(Exception ex) {
        log.error("Errore interno del server", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Errore interno del server"));
    }
}