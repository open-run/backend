package io.openur.domain.xrpl.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import io.openur.domain.xrpl.dto.NftDataDto;
import io.openur.domain.xrpl.environment.ReportingTestnetEnvironment;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.signing.SignatureService;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.bc.BcSignatureService;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.transactions.*;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;


import static org.xrpl.xrpl4j.model.immutables.FluentCompareTo.is;

@Repository
@Transactional(readOnly = true)
public class XrplRepository {
    public XrplClient xrplClient;
    protected ReportingTestnetEnvironment reportingTestnetEnvironment;
    //protected BcSignatureService signatureService;
    protected SignatureService<PrivateKey> signatureService;
    public static String ID = "5d22bd65-f1ed-4e7b-bc7b-0a59580d3176";

    //public static final Duration POLL_INTERVAL = Durations.ONE_HUNDRED_MILLISECONDS;

    XrplRepository() {
        this.reportingTestnetEnvironment = new ReportingTestnetEnvironment();
        this.xrplClient = reportingTestnetEnvironment.getXrplClient();
        this.signatureService = this.constructSignatureService();
    }

    protected SignatureService<PrivateKey> constructSignatureService() {
        return new BcSignatureService();
    }

    public KeyPair createAccount() throws InterruptedException {
        Seed s = Seed.secp256k1SeedFromPassphrase(Passphrase.of(ID));
        KeyPair walletKeyPair = Seed.ed25519Seed().deriveKeyPair();
        reportingTestnetEnvironment.fundAccount(walletKeyPair.publicKey().deriveAddress());

        boolean accountsFunded = false;
        while (!accountsFunded) {
            try {
                xrplClient.accountInfo(
                    AccountInfoRequestParams.builder()
                        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
                        .account(walletKeyPair.publicKey().deriveAddress())
                        .build()
                );

                accountsFunded = true;
            } catch (JsonRpcClientErrorException e) {
                if (!e.getMessage().equals("Account not found.")) {
                    System.out.println("Account not found: " + e.getMessage());
                }
                Thread.sleep(1000);
            }
        }
        System.out.println();
        return walletKeyPair;
    }

    public SubmitResult<NfTokenMint> mintFromOtherMinterAccount(KeyPair keyPair, KeyPair minterKeyPair,String nft_uri,taxon category,String memoContent) throws JsonRpcClientErrorException, JsonProcessingException {
        // accountInfo 메서드를 사용하여 계정 정보를 조회합니다.
        AccountInfoResult result = accountInfo(keyPair);

        // 조회된 계정 정보 출력
        System.out.println("Account Balance: " + result.accountData().balance());
        System.out.println("Sequence: " + result.accountData().sequence());
        System.out.println("Owner Count: " + result.accountData().ownerCount());
        System.out.println("account: " + result.accountData().account());
        System.out.println();


        // accountInfo 메서드를 사용하여 계정 정보를 조회합니다.
        AccountInfoResult minterKeyPair_result = accountInfo(minterKeyPair);

        // 조회된 계정 정보 출력
        System.out.println("Account Balance: " + minterKeyPair_result.accountData().balance());
        System.out.println("Sequence: " + minterKeyPair_result.accountData().sequence());
        System.out.println("Owner Count: " + minterKeyPair_result.accountData().ownerCount());
        System.out.println("account: " + minterKeyPair_result.accountData().account());
        System.out.println();



        AccountSet accountSet = AccountSet.builder()
            .account(keyPair.publicKey().deriveAddress())
            .sequence(accountInfo(keyPair).accountData().sequence())
            .fee(FeeUtils.computeNetworkFees(xrplClient.fee()).recommendedFee())
            .mintAccount(minterKeyPair.publicKey().deriveAddress())
            .setFlag(AccountSet.AccountSetFlag.AUTHORIZED_MINTER)
            .signingPublicKey(keyPair.publicKey())
            .build();


        SingleSignedTransaction<AccountSet> signedAccountSet = signatureService.sign(keyPair.privateKey(), accountSet);
        //TODO Planned to change to submitAndWaitForValidation
        SubmitResult<AccountSet> accountSetSubmitResult = xrplClient.submit(signedAccountSet);

        NfTokenUri uri = NfTokenUri.ofPlainText(nft_uri);

        //Nft mint transaction
        NfTokenMint nfTokenMint = NfTokenMint.builder()
            .tokenTaxon(category.getValue())
            .account(minterKeyPair.publicKey().deriveAddress())
            .fee(XrpCurrencyAmount.ofDrops(50))
            .addMemos(MemoWrapper.builder()
                .memo(Memo.withPlaintext(memoContent).build())
                .build())
            .signingPublicKey(minterKeyPair.publicKey())
            .sequence(accountInfo(minterKeyPair).accountData().sequence())
            .issuer(keyPair.publicKey().deriveAddress())
            .uri(uri)
            .build();

        SingleSignedTransaction<NfTokenMint> signedMint = signatureService.sign(minterKeyPair.privateKey(), nfTokenMint);
        //TODO Planned to change to submitAndWaitForValidation
        SubmitResult<NfTokenMint> mintSubmitResult = xrplClient.submit(signedMint);

        AccountInfoResult sourceAccountInfoAfterMint = xrplClient.accountInfo(
            AccountInfoRequestParams.of(keyPair.publicKey().deriveAddress())
        );

        System.out.println("NFT was minted successfully.");

        return mintSubmitResult;
    }

    public NftDataDto accountNftsData(KeyPair minterKeyPair, SubmitResult<NfTokenMint> mintSubmitResult) throws JsonRpcClientErrorException, JsonProcessingException {
        String nfTokenId = xrplClient.accountNfts(minterKeyPair.publicKey().deriveAddress()).accountNfts().get(0).nfTokenId().toString();
        System.out.println("Account nfTokenId: " + nfTokenId);

        UnsignedInteger taxon = xrplClient.accountNfts(minterKeyPair.publicKey().deriveAddress()).accountNfts().get(0).taxon();
        System.out.println("Account taxon: " + taxon);

        String nftSerial = xrplClient.accountNfts(minterKeyPair.publicKey().deriveAddress()).accountNfts().get(0).nftSerial().toString();
        System.out.println("Account nftSerial: " + nftSerial);

        String tokenUri = xrplClient.accountNfts(minterKeyPair.publicKey().deriveAddress()).accountNfts().get(0).uri().toString();
        System.out.println("Account tokenUri: " + tokenUri);

        // Optional[NfTokenUri( ... )] 안에 있는 실제 Hex 데이터를 추출
        String hexuri = tokenUri.replace("Optional[NfTokenUri(", "").replace(")]", "");

        // Hex 문자열을 디코딩
        String decodeduri = new String(hexStringToByteArray(hexuri));

        // 디코딩된 값 출력
        System.out.println("Decoded TokenUri: " + decodeduri);

        String decodedData = null;
        if (mintSubmitResult.transactionResult().transaction().memos().isEmpty()) {
            System.out.println("No memos found in this transaction.");

        } else {
            String memoData = mintSubmitResult.transactionResult().transaction().memos().get(0).memo().memoData().toString();
            // 메모 데이터 접근
            System.out.println("Memo Data: " + memoData);

            // Optional[] 안에 있는 실제 값을 추출
            String hexData = memoData.replace("Optional[", "").replace("]", "");

            // Hex 문자열을 디코딩
            decodedData = new String(hexStringToByteArray(hexData));

            // 디코딩된 값 출력
            System.out.println("Decoded Memo Data: " + decodedData);

        }
        return new NftDataDto(nfTokenId, taxon, nftSerial, decodeduri, decodedData);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }


    public AccountInfoResult accountInfo(KeyPair keyPair) throws JsonRpcClientErrorException {
        // AccountInfoRequestParams 객체를 생성하여 조회할 계정과 원장을 지정합니다.
        AccountInfoRequestParams params = AccountInfoRequestParams.builder()
            .account(keyPair.publicKey().deriveAddress())  // 조회할 계정의 주소
            .ledgerSpecifier(LedgerSpecifier.VALIDATED)  // 검증된 원장에서 데이터를 조회
            .build();

        return xrplClient.accountInfo(params);
    }

    private UnsignedInteger computeLastLedgerSequence()
        throws JsonRpcClientErrorException {
        // Get the latest validated ledger index
        LedgerIndex validatedLedger = xrplClient.ledger(
                LedgerRequestParams.builder()
                    .ledgerSpecifier(LedgerSpecifier.VALIDATED)
                    .build()
            )
            .ledgerIndex()
            .orElseThrow(() -> new RuntimeException("LedgerIndex not available."));

        // Workaround for https://github.com/XRPLF/xrpl4j/issues/84
        return UnsignedInteger.valueOf(
            validatedLedger.plus(UnsignedInteger.valueOf(4)).unsignedIntegerValue().intValue()
        );
    }

    private void submitAndWaitForValidation(SingleSignedTransaction<?> signedTransaction)
        throws InterruptedException, JsonRpcClientErrorException, JsonProcessingException {

        xrplClient.submit(signedTransaction);

        boolean transactionValidated = false;
        boolean transactionExpired = false;
        while (!transactionValidated && !transactionExpired) {
            Thread.sleep(1000);
            LedgerIndex latestValidatedLedgerIndex = xrplClient.ledger(
                    LedgerRequestParams.builder().ledgerSpecifier(LedgerSpecifier.VALIDATED).build()
                )
                .ledgerIndex()
                .orElseThrow(() ->
                    new RuntimeException("Ledger response did not contain a LedgerIndex.")
                );

            TransactionResult<Payment> transactionResult = xrplClient.transaction(
                TransactionRequestParams.of(signedTransaction.hash()),
                Payment.class
            );

            if (transactionResult.validated()) {
                System.out.println("Transaction was validated with result code " +
                    transactionResult.metadata().get().transactionResult());
                transactionValidated = true;
            } else {

                boolean lastLedgerSequenceHasPassed = signedTransaction.signedTransaction().lastLedgerSequence()
                    .map((signedTransactionLastLedgerSeq) ->
                        is(latestValidatedLedgerIndex.unsignedIntegerValue())
                            .greaterThan(signedTransactionLastLedgerSeq)
                    )
                    .orElse(false);

                if (lastLedgerSequenceHasPassed) {
                    System.out.println("LastLedgerSequence has passed. Last tx response: " +
                        transactionResult);
                    transactionExpired = true;
                } else {
                    System.out.println("Transaction not yet validated.");
                }
            }
        }
    }
}