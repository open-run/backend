package io.openur.domain.NFT.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NftBalanceReaderImplTest {

    private static final String OWNER = "0x1234567890123456789012345678901234567890";

    @Mock
    private NftContractBalanceClient nftContractBalanceClient;

    @InjectMocks
    private NftBalanceReaderImpl nftBalanceReader;

    @Test
    @DisplayName("balanceOfBatch 결과를 tokenId 문자열 key의 balance map으로 반환한다")
    void getBalances_usesBatchBalance() {
        when(nftContractBalanceClient.getBatchBalances(
            eq(OWNER),
            eq(List.of(BigInteger.valueOf(100), BigInteger.valueOf(200)))
        ))
            .thenReturn(List.of(BigInteger.ONE, BigInteger.ZERO));

        Map<String, BigInteger> balances = nftBalanceReader.getBalances(OWNER, List.of("100", "200"));

        assertThat(balances)
            .containsEntry("100", BigInteger.ONE)
            .containsEntry("200", BigInteger.ZERO);
    }

    @Test
    @DisplayName("balanceOfBatch 실패 시 balanceOf 반복 조회로 fallback한다")
    void getBalances_fallsBackToSingleBalance() {
        when(nftContractBalanceClient.getBatchBalances(
            eq(OWNER),
            eq(List.of(BigInteger.valueOf(100), BigInteger.valueOf(200)))
        ))
            .thenThrow(new RuntimeException("batch failed"));
        when(nftContractBalanceClient.getBalance(OWNER, BigInteger.valueOf(100))).thenReturn(BigInteger.ONE);
        when(nftContractBalanceClient.getBalance(OWNER, BigInteger.valueOf(200))).thenReturn(BigInteger.TEN);

        Map<String, BigInteger> balances = nftBalanceReader.getBalances(OWNER, List.of("100", "200"));

        assertThat(balances)
            .containsEntry("100", BigInteger.ONE)
            .containsEntry("200", BigInteger.TEN);
        verify(nftContractBalanceClient).getBalance(OWNER, BigInteger.valueOf(100));
        verify(nftContractBalanceClient).getBalance(OWNER, BigInteger.valueOf(200));
    }
}
