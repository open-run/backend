package io.openur.domain.xrpl.repository;

import lombok.Getter;

@Getter
public enum NftUri {
    GREEN_SHOE_BIG("https://xrpl-s3-bucket.s3.ap-northeast-2.amazonaws.com/green_shoe_big.png"),
    GREEN_SHOE("https://xrpl-s3-bucket.s3.ap-northeast-2.amazonaws.com/green_shoe.png"),
    OUTER_SET_SMALL("https://xrpl-s3-bucket.s3.ap-northeast-2.amazonaws.com/ourter_set_small.png"),
    OUTER_SET("https://xrpl-s3-bucket.s3.ap-northeast-2.amazonaws.com/outer_set.png"),
    OUTER_SET2_SMALL("https://xrpl-s3-bucket.s3.ap-northeast-2.amazonaws.com/outer_set2_small.png"),
    OUTER_SET2("https://xrpl-s3-bucket.s3.ap-northeast-2.amazonaws.com/outer_set2.png"),
    OUTER_SET3_SMALL("https://xrpl-s3-bucket.s3.ap-northeast-2.amazonaws.com/outer_set3_small.png"),
    OUTER_SET3("https://xrpl-s3-bucket.s3.ap-northeast-2.amazonaws.com/outer_set3.png"),
    PINK_SHOE_BIG("https://xrpl-s3-bucket.s3.ap-northeast-2.amazonaws.com/pink_shoe_big.png"),
    PINK_SHOE("https://xrpl-s3-bucket.s3.ap-northeast-2.amazonaws.com/pink_shoe.png"),
    RED_SHOE_BIG("https://xrpl-s3-bucket.s3.ap-northeast-2.amazonaws.com/red_shoe_big.png"),
    RED_SHOE("https://xrpl-s3-bucket.s3.ap-northeast-2.amazonaws.com/red_shoe.png"),
    SKELETON("https://xrpl-s3-bucket.s3.ap-northeast-2.amazonaws.com/skeleton.png");

    private final String uri;

    NftUri(String uri) {this.uri = uri;}
}
