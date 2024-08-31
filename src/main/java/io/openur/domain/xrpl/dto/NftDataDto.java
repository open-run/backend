package io.openur.domain.xrpl.dto;

import com.google.common.primitives.UnsignedInteger;
import lombok.Getter;

@Getter
public class NftDataDto {

    // Getters
    private final String nfTokenId;
    private final UnsignedInteger taxon;
    private final String nftSerial;
    private final String decodedUri;
    private final String decodedMemoData;

    // Constructor
    public NftDataDto(String nfTokenId, UnsignedInteger taxon, String nftSerial, String decodedUri, String decodedMemoData) {
        this.nfTokenId = nfTokenId;
        this.taxon = taxon;
        this.nftSerial = nftSerial;
        this.decodedUri = decodedUri;
        this.decodedMemoData = decodedMemoData;
    }

    @Override
    public String toString() {
        return "NftDataDto{" +
            "nfTokenId='" + nfTokenId + '\'' +
            ", taxon=" + taxon +
            ", nftSerial='" + nftSerial + '\'' +
            ", decodedUri='" + decodedUri + '\'' +
            ", decodedMemoData='" + decodedMemoData + '\'' +
            '}';
    }
}
