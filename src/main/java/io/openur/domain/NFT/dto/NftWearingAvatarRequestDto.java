package io.openur.domain.NFT.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.openur.domain.NFT.enums.NftAvatarWearingSlot;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NftWearingAvatarRequestDto {

    private Long fullSet;
    private Long upperClothing;
    private Long lowerClothing;
    private Long footwear;
    private Long face;
    private Long skin;
    private Long hair;
    private Accessories accessories;

    public Map<NftAvatarWearingSlot, Long> toSlotItemIds() {
        Map<NftAvatarWearingSlot, Long> result = new LinkedHashMap<>();
        result.put(NftAvatarWearingSlot.upper_clothing, upperClothing);
        result.put(NftAvatarWearingSlot.lower_clothing, lowerClothing);
        result.put(NftAvatarWearingSlot.footwear, footwear);
        result.put(NftAvatarWearingSlot.face, face);
        result.put(NftAvatarWearingSlot.skin, skin);
        result.put(NftAvatarWearingSlot.hair, hair);

        if (accessories != null) {
            result.put(NftAvatarWearingSlot.head_accessories, accessories.headAccessories);
            result.put(NftAvatarWearingSlot.eye_accessories, accessories.eyeAccessories);
            result.put(NftAvatarWearingSlot.ear_accessories, accessories.earAccessories);
            result.put(NftAvatarWearingSlot.body_accessories, accessories.bodyAccessories);
        }

        return result;
    }

    @Getter
    @NoArgsConstructor
    public static class Accessories {

        @JsonProperty("head-accessories")
        private Long headAccessories;

        @JsonProperty("eye-accessories")
        private Long eyeAccessories;

        @JsonProperty("ear-accessories")
        private Long earAccessories;

        @JsonProperty("body-accessories")
        private Long bodyAccessories;
    }
}
