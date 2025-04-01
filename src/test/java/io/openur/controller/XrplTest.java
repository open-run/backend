package io.openur.controller;

import com.google.common.primitives.UnsignedInteger;
import okhttp3.HttpUrl;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.signing.SignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.SingleKeySignatureService;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.flags.NfTokenMintFlags;
import org.xrpl.xrpl4j.model.transactions.NfTokenMint;
import org.xrpl.xrpl4j.model.transactions.TransactionResultCodes;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
//import org.xrpl.xrpl4j.model.transactions.Url;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class XrplTest {
    private static final String TESTNET_URL = "https://s.altnet.rippletest.net:51234/"; // XRPL Testnet RPC
    private static final String METADATA_URL = "https://example.com/nft-metadata.json"; // NFT 메타데이터 URL

    public static void main(String[] args) {
        try {
            XrplClient xrplClient = new XrplClient(HttpUrl.get(TESTNET_URL));

            Seed seed = Seed.ed25519Seed();
            KeyPair keyPair = seed.deriveKeyPair();
            String walletAddress = keyPair.publicKey().deriveAddress().toString();

            System.out.println("새로운 XRPL Testnet 지갑 생성 완료!");
            System.out.println("Seed (비밀 키): " + seed.decodedSeed());
            System.out.println("Public Key: " + keyPair.publicKey().base16Value());
            System.out.println("Wallet Address: " + walletAddress);
/*
            AccountInfoRequestParams requestParams = AccountInfoRequestParams.of(keyPair.publicKey().deriveAddress());
            AccountInfoResult accountInfo = xrplClient.accountInfo(requestParams);
            BigInteger sequence = accountInfo.accountData().sequence().bigIntegerValue();

//https://github.com/XRPLF/xrpl-dev-portal/blob/master/_code-samples/get-started/java/GetAccountInfo.java 여기 뒤져가면서 gpt랑 작업 중
            String metadataHex = bytesToHex(METADATA_URL.getBytes(StandardCharsets.UTF_8)); // Base16(HEX) 인코딩

            NfTokenMint mintTransaction = NfTokenMint.builder()
                .account(keyPair.publicKey().deriveAddress())
                .fee(XrpCurrencyAmount.ofDrops(10))
                .sequence(UnsignedInteger.valueOf(sequence))
                .uri(metadataHex)
                .flags(NfTokenMintFlags.builder()
                    .tfTransferable(true)
                    .build())
                .build();

            SingleKeySignatureService signer = new SingleKeySignatureService(keyPair.privateKey());
            SignedTransaction<NfTokenMint> signedTx = signer.sign(keyPair.privateKey(), mintTransaction);

            SubmitResult result = xrplClient.submit(signedTx);

            System.out.println("=== NFT Minting 트랜잭션 결과 ===");
            System.out.println("TX 해시: " + result.hash());
            System.out.println("결과 코드: " + result.result());

            if (result.engineResult().equals(TransactionResultCodes.TES_SUCCESS)) {
                System.out.println("NFT 민팅 성공!");
            } else {
                System.err.println("NFT 민팅 실패: " + result.result());
            }
*/
        } catch (Exception e) {
            System.err.println("오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
