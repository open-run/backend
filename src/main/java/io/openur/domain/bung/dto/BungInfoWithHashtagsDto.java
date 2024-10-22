package io.openur.domain.bung.dto;

import io.openur.domain.bung.model.Bung;
import java.util.List;
import lombok.Getter;

@Getter
public class BungInfoWithHashtagsDto extends BungInfoDto {

	private final List<String> hashtags;

	public BungInfoWithHashtagsDto(Bung bung, List<String> hashtags) {
		super(bung);
		this.hashtags = hashtags;
	}
}
