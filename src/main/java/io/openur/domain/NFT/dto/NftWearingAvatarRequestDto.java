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

    private String fullSet;
    private String upperClothing;
    private String lowerClothing;
    private String footwear;
    private String face;
    private String skin;
    private String hair;
    private Accessories accessories;

    public Map<NftAvatarWearingSlot, String> toSlotTokenIds() {
        Map<NftAvatarWearingSlot, String> result = new LinkedHashMap<>();
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
        private String headAccessories;

        @JsonProperty("eye-accessories")
        private String eyeAccessories;

        @JsonProperty("ear-accessories")
        private String earAccessories;

        @JsonProperty("body-accessories")
        private String bodyAccessories;
    }
}
