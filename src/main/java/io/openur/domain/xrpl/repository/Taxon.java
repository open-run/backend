package io.openur.domain.xrpl.repository;

import com.google.common.primitives.UnsignedLong;
import lombok.Getter;

@Getter
public enum Taxon {
    FACE_COLOR(UnsignedLong.valueOf(1)),
    HAIR(UnsignedLong.valueOf(2)),
    FACE(UnsignedLong.valueOf(3)),
    TOP(UnsignedLong.valueOf(4)),
    BOTTOM(UnsignedLong.valueOf(5)),
    SHOES(UnsignedLong.valueOf(6)),
    HAIR_ACCESSORY(UnsignedLong.valueOf(7)),
    EAR_ACCESSORY(UnsignedLong.valueOf(8)),
    BODY_ACCESSORY(UnsignedLong.valueOf(9));

    private final UnsignedLong value;

    Taxon(UnsignedLong value) {
        this.value = value;
    }

    public UnsignedLong getValue() {
        return value;
    }

    public static String getCategoryNameByValue(UnsignedLong value) {
        for (Taxon category : Taxon.values()) {
            if (category.getValue().equals(value)) {
                return category.name(); // Enum의 이름을 반환
            }
        }
        return null; // 일치하는 값이 없을 경우 null 반환
    }
}
