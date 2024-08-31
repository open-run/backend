package io.openur.domain.xrpl.dto;

import com.google.common.primitives.UnsignedInteger;

public class NftDataDto {
    private String nfTokenId;
    private String taxon;
    private String nftSerial;
    private String decodedUri;
    private String decodedMemoData;

    // Constructor
    public NftDataDto(String nfTokenId, String taxon, String nftSerial, String decodedUri, String decodedMemoData) {
        this.nfTokenId = nfTokenId;
        this.taxon = taxon;
        this.nftSerial = nftSerial;
        this.decodedUri = decodedUri;
        this.decodedMemoData = decodedMemoData;
    }

    // Getters
    public String getNfTokenId() {
        return nfTokenId;
    }

    public String getTaxon() {
        return taxon;
    }

    public String getNftSerial() {
        return nftSerial;
    }

    public String getDecodedUri() {
        return decodedUri;
    }

    public String getDecodedMemoData() {
        return decodedMemoData;
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
