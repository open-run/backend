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

    public static Taxon fromValue(UnsignedLong value) {
        for (Taxon category : Taxon.values()) {
            if (category.value.equals(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }
}
