package io.openur.domain.hashtag.controller;

import io.openur.domain.hashtag.service.HashtagService;
import io.openur.global.common.Response;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/bungs")
@RequiredArgsConstructor
public class HashtagController {

	private final HashtagService hashtagService;

	@GetMapping("/hashtags")
	@Operation(summary = "입력받은 문자열이 포함된 기존 해시태그 리스트 불러오기")
	public ResponseEntity<Response<List<String>>> getHashtags(
		@RequestParam String tag
	) {
		List<String> hashtags = hashtagService.getHashtagList(tag);
		return ResponseEntity.ok().body(Response.<List<String>>builder()
			.message("success")
			.data(hashtags)
			.build());
	}
}
