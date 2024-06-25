package io.openur.global.common;

import java.net.URI;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public class UtilController {

	public static URI createUri(String todoId) {
		return ServletUriComponentsBuilder.fromCurrentRequest()
			.path("/{id}")
			.buildAndExpand(todoId)
			.toUri();
	}
}
