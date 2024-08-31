package io.openur.domain.xrpl.mint;

import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.PrivateKeyable;
import org.xrpl.xrpl4j.crypto.signing.SignatureService;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.bc.BcSignatureService;
import org.xrpl.xrpl4j.model.flags.NfTokenMintFlags;
import org.xrpl.xrpl4j.model.transactions.NfTokenMint;
import org.xrpl.xrpl4j.model.transactions.NfTokenUri;

public class MintNft {

    public void mintNFT(KeyPair keyPair, XrplClient xrplClient) throws JsonRpcClientErrorException, InterruptedException {
        // 1. NFT 민팅할 메타데이터 설정
        String uri = "https://example.com/metadata.json"; // 메타데이터 URI
        NfTokenMint mint = NfTokenMint.builder()
            .account(keyPair.publicKey().deriveAddress()) // NFT를 민팅할 계정 주소
            .uri(NfTokenUri.of(uri)) // NFT 메타데이터 URI
            .flags(NfTokenMintFlags.of(0)) // 플래그 설정 (optional)
            .build();

        // 2. 서명된 트랜잭션 생성
        SignatureService signatureService = new BcSignatureService();
        PrivateKeyable privateKeyable = keyPair.privateKey();
        SingleSignedTransaction<NfTokenMint> signedTransaction = signatureService.sign(privateKeyable, mint);

        // 3. 트랜잭션 제출 및 검증 대기
        // 일단 주석
        //submitAndWaitForValidation(signedTransaction, xrplClient);
    }

//    private void submitAndWaitForValidation(SingleSignedTransaction<?> signedTransaction, XrplClient xrplClient)
//        throws InterruptedException, JsonRpcClientErrorException {
//        // 트랜잭션 제출
//        xrplClient.submit(signedTransaction);
//
//        boolean transactionValidated = false;
//        while (!transactionValidated) {
//            Thread.sleep(1000);
//            TransactionResult<NfTokenMint> transactionResult = xrplClient.transaction(
//                TransactionRequestParams.of(signedTransaction.hash()),
//                NfTokenMint.class);
//
//            if (transactionResult.validated()) {
//                System.out.println("NFT Minted successfully with result code: " + transactionResult.metadata().get().transactionResult());
//                transactionValidated = true;
//            } else {
//                System.out.println("Transaction not yet validated.");
//            }
//        }
//    }

}
