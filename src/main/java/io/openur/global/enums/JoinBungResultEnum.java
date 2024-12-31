package io.openur.global.enums;

public enum JoinBungResultEnum {
	SUCCESSFULLY_JOINED("successfully joined"),
	BUNG_HAS_ALREADY_STARTED("bung has already started"),
	BUNG_IS_FULL("bung is full"),
	USER_HAS_ALREADY_JOINED("user has already joined"),
	;

	private final String value;

	JoinBungResultEnum(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}
}
