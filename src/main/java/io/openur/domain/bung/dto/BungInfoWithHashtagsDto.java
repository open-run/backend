package io.openur.domain.bung.dto;

import io.openur.domain.bung.model.Bung;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BungInfoWithHashtagsDto extends BungInfoDto {

	private List<String> hashtags = List.of();

	public BungInfoWithHashtagsDto(Bung bung, List<String> hashtags) {
		super(bung);
		this.hashtags = hashtags;
	}
}
