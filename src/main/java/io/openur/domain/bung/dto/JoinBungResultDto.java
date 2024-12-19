package io.openur.domain.bung.dto;

public enum JoinBungResultDto {
	SUCCESSFULLY_JOINED("successfully joined"),
	BUNG_HAS_ALREADY_STARTED("bung has already started"),
	BUNG_IS_FULL("bung is full"),
	USER_HAS_ALREADY_JOINED("user has already joined"),
	;

	private final String value;

	JoinBungResultDto(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}
}
