package io.openur.global.common;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BaseController {

	@GetMapping("/health")
	@Operation(summary = "서버 상태 확인용 API")
	public ResponseEntity<Response<String>> health() {
		return ResponseEntity.ok().body(Response.<String>builder()
			.message("success")
			.data("ok")
			.build());
	}
}
