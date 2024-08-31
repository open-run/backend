package io.openur.domain.xrpl.repository;

import static org.xrpl.xrpl4j.model.immutables.FluentCompareTo.is;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import io.openur.domain.xrpl.dto.NftDataDto;
import io.openur.domain.xrpl.environment.ReportingTestnetEnvironment;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.bc.BcSignatureService;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.client.nft.NftInfoRequestParams;
import org.xrpl.xrpl4j.model.client.nft.NftInfoResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.Memo;
import org.xrpl.xrpl4j.model.transactions.MemoWrapper;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;
import org.xrpl.xrpl4j.model.transactions.NfTokenMint;
import org.xrpl.xrpl4j.model.transactions.NfTokenUri;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

@Repository
@Transactional(readOnly = true)
public class XrplRepository {

    @Value("${minter}")
    private String minter;

    public XrplClient xrplClient;
    protected ReportingTestnetEnvironment reportingTestnetEnvironment;
    protected BcSignatureService signatureService;

    XrplRepository() {
        this.reportingTestnetEnvironment = new ReportingTestnetEnvironment();
        this.xrplClient = reportingTestnetEnvironment.getXrplClient();
        this.signatureService = new BcSignatureService();
    }

    public NftDataDto mintNft(String userId) throws InterruptedException, JsonRpcClientErrorException, JsonProcessingException {
        KeyPair keyPair = createAccount(userId);
        KeyPair minterKeyPair = createAccount(minter);
        SubmitResult<NfTokenMint> mintSubmitResult = mintFromOtherMinterAccount(minterKeyPair,
            keyPair, NftUri.OUTER_SET.getUri(), Taxon.TOP, "common");
        return accountNftData(keyPair, mintSubmitResult);
    }

    public List<NftDataDto> getNftDataList(List<String> nftIndexList)
        throws JsonRpcClientErrorException {
        List<NftDataDto> nftDataDtoList = new ArrayList<>();
        for (String nftIndex : nftIndexList) {
            NftInfoRequestParams params = NftInfoRequestParams.builder()
                .nfTokenId(NfTokenId.of(nftIndex))
                .ledgerSpecifier(LedgerSpecifier.VALIDATED)
                .build();
            NftInfoResult nftInfo = xrplClient.nftInfo(
                params
            );

            NftDataDto nftDataDto = new NftDataDto(
                nftInfo.nftId().toString(),
                UnsignedInteger.valueOf(nftInfo.nftTaxon().toString()),
                nftInfo.nftSerial().toString(),
                nftInfo.uri().toString(),
                "common"
            );
            nftDataDtoList.add(nftDataDto);
        }
        return nftDataDtoList;
    }

    public KeyPair createAccount(String userId) throws InterruptedException {
        KeyPair walletKeyPair = Seed.secp256k1SeedFromPassphrase(Passphrase.of(userId)).deriveKeyPair();
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

    public void printAccountInfo(KeyPair keyPair) throws JsonRpcClientErrorException {
        AccountInfoResult accountInfo = this.accountInfo(keyPair);

        System.out.println("Account Balance: " + accountInfo.accountData().balance());
        System.out.println("Sequence: " + accountInfo.accountData().sequence());
        System.out.println("Owner Count: " + accountInfo.accountData().ownerCount());
        System.out.println("account: " + accountInfo.accountData().account());
        System.out.println();
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private String getMemo(String memoData) {
        String decodedData = null;
        if (memoData == null) {
            System.out.println("No memos found in this transaction.");
        } else {
            // Optional[] 안에 있는 실제 값을 추출
            String hexData = memoData.replace("Optional[", "").replace("]", "");

            // Hex 문자열을 디코딩
            decodedData = new String(hexStringToByteArray(hexData));
        }
        return decodedData;
    }

    public NftDataDto accountNftData(KeyPair ownerKeyPair,
        SubmitResult<NfTokenMint> mintSubmitResult) throws JsonRpcClientErrorException {
        String nfTokenId = xrplClient.accountNfts(ownerKeyPair.publicKey().deriveAddress())
            .accountNfts()
            .get(0).nfTokenId().toString();

        UnsignedInteger taxon = xrplClient.accountNfts(ownerKeyPair.publicKey().deriveAddress())
            .accountNfts().get(0).taxon();

        String nftSerial = xrplClient.accountNfts(ownerKeyPair.publicKey().deriveAddress())
            .accountNfts()
            .get(0).nftSerial().toString();

        String tokenUri = xrplClient.accountNfts(ownerKeyPair.publicKey().deriveAddress())
            .accountNfts()
            .get(0).uri().toString();

        // Optional[NfTokenUri( ... )] 안에 있는 실제 Hex 데이터를 추출
        String hexuri = tokenUri.replace("Optional[NfTokenUri(", "").replace(")]", "");

        // Hex 문자열을 디코딩
        String decodeduri = new String(hexStringToByteArray(hexuri));

        String decodedData = null;
        if (mintSubmitResult.transactionResult().transaction().memos().isEmpty()) {
            System.out.println("No memos found in this transaction.");

        } else {
            String memoData = mintSubmitResult.transactionResult().transaction().memos().get(0).memo().memoData().toString();
            // Optional[] 안에 있는 실제 값을 추출
            String hexData = memoData.replace("Optional[", "").replace("]", "");

            // Hex 문자열을 디코딩
            decodedData = new String(hexStringToByteArray(hexData));
        }
        // 추출/디코딩한 정보들을 NftDataDto 객체로 생성하여 반환
        return new NftDataDto(nfTokenId, taxon, nftSerial, decodeduri, decodedData);
    }

    public SubmitResult<NfTokenMint> mintFromOtherMinterAccount(KeyPair issuerKeyPair,
        KeyPair ownerKeyPair, String nft_uri,
        Taxon category, String memoContent)
        throws JsonRpcClientErrorException, JsonProcessingException {
        this.printAccountInfo(issuerKeyPair);
        this.printAccountInfo(ownerKeyPair);

        AccountSet accountSet = AccountSet.builder()
            .account(issuerKeyPair.publicKey().deriveAddress())
            .sequence(this.accountInfo(issuerKeyPair).accountData().sequence())
            .fee(FeeUtils.computeNetworkFees(xrplClient.fee()).recommendedFee())
            .mintAccount(ownerKeyPair.publicKey().deriveAddress())
            .setFlag(AccountSet.AccountSetFlag.AUTHORIZED_MINTER)
            .signingPublicKey(issuerKeyPair.publicKey())
            .build();

        SingleSignedTransaction<AccountSet> signedAccountSet = signatureService.sign(
            issuerKeyPair.privateKey(), accountSet);
        //TODO Planned to change to submitAndWaitForValidation
        SubmitResult<AccountSet> accountSetSubmitResult = xrplClient.submit(signedAccountSet);

        NfTokenUri uri = NfTokenUri.ofPlainText(nft_uri);

        //Nft mint transaction
        NfTokenMint nfTokenMint = NfTokenMint.builder()
            .tokenTaxon(category.getValue())
            .account(ownerKeyPair.publicKey().deriveAddress())
            .fee(XrpCurrencyAmount.ofDrops(50))
            .addMemos(MemoWrapper.builder()
                .memo(Memo.withPlaintext(memoContent).build())
                .build())
            .signingPublicKey(ownerKeyPair.publicKey())
            .sequence(accountInfo(ownerKeyPair).accountData().sequence())
            .issuer(issuerKeyPair.publicKey().deriveAddress())
            .uri(uri)
            .build();

        SingleSignedTransaction<NfTokenMint> signedMint = signatureService.sign(
            ownerKeyPair.privateKey(), nfTokenMint);
        //TODO Planned to change to submitAndWaitForValidation
        SubmitResult<NfTokenMint> mintSubmitResult = xrplClient.submit(signedMint);

        AccountInfoResult sourceAccountInfoAfterMint = xrplClient.accountInfo(
            AccountInfoRequestParams.of(issuerKeyPair.publicKey().deriveAddress())
        );

        System.out.println("NFT was minted successfully.");

        return mintSubmitResult;
    }

    public AccountInfoResult accountInfo(KeyPair keyPair) throws JsonRpcClientErrorException {
        // AccountInfoRequestParams 객체를 생성하여 조회할 계정과 원장을 지정합니다.
        AccountInfoRequestParams params = AccountInfoRequestParams.builder()
            .account(keyPair.publicKey().deriveAddress())  // 조회할 계정의 주소
            .ledgerSpecifier(LedgerSpecifier.VALIDATED)  // 검증된 원장에서 데이터를 조회
            .build();

        return xrplClient.accountInfo(params);
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
