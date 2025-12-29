package io.openur.global.exception;

import io.openur.domain.NFT.exception.MintException;
import io.openur.domain.bung.exception.CompleteBungException;
import io.openur.domain.bung.exception.EditBungException;
import io.openur.domain.bung.exception.GetBungException;
import io.openur.domain.bung.exception.JoinBungException;
import io.openur.domain.bung.exception.SearchBungException;
import io.openur.domain.user.exception.UserNotFoundException;
import io.openur.domain.userbung.exception.RemoveUserFromBungException;
import io.openur.global.dto.ExceptionDto;
import io.openur.global.jwt.InvalidJwtException;
import java.awt.HeadlessException;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler({IllegalArgumentException.class,
        MissingServletRequestParameterException.class,
        SearchBungException.class })
    public ResponseEntity<ExceptionDto> handleBadRequestException(Exception e) {
        return createResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler({
        NullPointerException.class,
        NoSuchElementException.class,
        UserNotFoundException.class,
        GetBungException.class
    })
    public ResponseEntity<ExceptionDto> handleNotFoundException(Exception ex) {
        return createResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionDto> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException e
    ) {
        String message = Optional.ofNullable(e.getBindingResult().getFieldError())
            .map(fieldError -> fieldError.getDefaultMessage())
            .orElse("Method argument not valid");
        return createResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler({AccessDeniedException.class, RemoveUserFromBungException.class, EditBungException.class})
    public ResponseEntity<ExceptionDto> handleForbiddenException(Exception e) {
        return createResponse(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler({InvalidJwtException.class})
    public ResponseEntity<ExceptionDto> handleInvalidJwtException(Exception e) {
        return createResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler({Exception.class, HeadlessException.class, MintException.class})
    public ResponseEntity<ExceptionDto> handleInternalServerErrorException(Exception e) {
        return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler({JoinBungException.class, CompleteBungException.class})
    public ResponseEntity<ExceptionDto> handleJoinBungException(Exception e) {
        return createResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    private ResponseEntity<ExceptionDto> createResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status.value())
            .body(ExceptionDto.builder()
                .statusCode(status.value())
                .state(status)
                .message(message)
                .build());
    }
}
