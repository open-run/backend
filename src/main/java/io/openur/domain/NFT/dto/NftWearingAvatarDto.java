package io.openur.domain.NFT.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NftWearingAvatarDto {

    private NftAvatarItemDto fullSet;
    private NftAvatarItemDto upperClothing;
    private NftAvatarItemDto lowerClothing;
    private NftAvatarItemDto footwear;
    private NftAvatarItemDto face;
    private NftAvatarItemDto skin;
    private NftAvatarItemDto hair;
    private Accessories accessories;

    public static NftWearingAvatarDto empty() {
        return NftWearingAvatarDto.builder()
            .fullSet(null)
            .upperClothing(null)
            .lowerClothing(null)
            .footwear(null)
            .face(null)
            .skin(null)
            .hair(null)
            .accessories(Accessories.empty())
            .build();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Accessories {

        @JsonProperty("head-accessories")
        private NftAvatarItemDto headAccessories;

        @JsonProperty("eye-accessories")
        private NftAvatarItemDto eyeAccessories;

        @JsonProperty("ear-accessories")
        private NftAvatarItemDto earAccessories;

        @JsonProperty("body-accessories")
        private NftAvatarItemDto bodyAccessories;

        public static Accessories empty() {
            return Accessories.builder()
                .headAccessories(null)
                .eyeAccessories(null)
                .earAccessories(null)
                .bodyAccessories(null)
                .build();
        }
    }
}
