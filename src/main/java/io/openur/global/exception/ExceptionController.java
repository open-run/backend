package io.openur.global.exception;

import io.openur.global.dto.ExceptionDto;
import io.openur.global.jwt.InvalidJwtException;
import java.awt.HeadlessException;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionController {

	@ExceptionHandler({IllegalArgumentException.class})
	public ResponseEntity<ExceptionDto> handBadRequestException(Exception e) {
		return createResponse(HttpStatus.BAD_REQUEST, e.getMessage());
	}

	@ExceptionHandler({NullPointerException.class, NoSuchElementException.class})
	public ResponseEntity<ExceptionDto> handleNotFoundException(Exception ex) {
		return createResponse(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ExceptionDto> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException e
	) {
		return createResponse(HttpStatus.BAD_REQUEST,
			e.getBindingResult().getFieldError().getDefaultMessage());
	}

	@ExceptionHandler({AccessDeniedException.class})
	public ResponseEntity<ExceptionDto> handleForbiddenException(Exception e) {
		return createResponse(HttpStatus.FORBIDDEN, e.getMessage());
	}

	@ExceptionHandler({InvalidJwtException.class})
	public ResponseEntity<ExceptionDto> handleInvalidJwtException(Exception e) {
		return createResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
	}

	@ExceptionHandler({Exception.class, HeadlessException.class})
	public ResponseEntity<ExceptionDto> handleInternalServerErrorException(Exception e) {
		return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
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
