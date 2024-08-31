package io.openur.domain.xrpl.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import io.openur.domain.xrpl.environment.ReportingTestnetEnvironment;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.crypto.JavaKeystoreLoader;
import org.xrpl.xrpl4j.crypto.ServerSecret;
import org.xrpl.xrpl4j.crypto.keys.*;
import org.xrpl.xrpl4j.crypto.signing.SignatureService;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.bc.BcDerivedKeySignatureService;
import org.xrpl.xrpl4j.crypto.signing.bc.BcSignatureService;
import org.xrpl.xrpl4j.model.client.accounts.*;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryResult;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.immutables.FluentCompareTo;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.ledger.NfToken;
import org.xrpl.xrpl4j.model.ledger.NfTokenPageObject;
import org.xrpl.xrpl4j.model.ledger.NfTokenWrapper;
import org.xrpl.xrpl4j.model.transactions.*;
import java.security.Key;
import java.security.KeyStore;

import static org.xrpl.xrpl4j.model.immutables.FluentCompareTo.is;


@Repository
@Transactional(readOnly = true)
public class XrplRepository {
    public XrplClient xrplClient;
    protected ReportingTestnetEnvironment reportingTestnetEnvironment;
    //protected BcSignatureService signatureService;
    protected SignatureService<PrivateKey> signatureService;

    //public static final Duration POLL_INTERVAL = Durations.ONE_HUNDRED_MILLISECONDS;

    XrplRepository() {
        this.reportingTestnetEnvironment = new ReportingTestnetEnvironment();
        this.xrplClient = reportingTestnetEnvironment.getXrplClient();
        this.signatureService = this.constructSignatureService();
    }

    public KeyPair createAccount() throws InterruptedException {
        // Create KeyPair -----------------------
        KeyPair walletKeyPair = Seed.ed25519Seed().deriveKeyPair();

        // Fund the account using the testnet Faucet -------------------------------
        reportingTestnetEnvironment.fundAccount(walletKeyPair.publicKey().deriveAddress());

        // If you go too soon, the funding transaction might slip back a ledger and
        // then your starting Sequence number will be off. This is mostly relevant
        // when you want to use a Testnet account right after getting a reply from
        // the faucet.
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

    public void mintFromOtherMinterAccount(KeyPair keyPair, KeyPair minterKeyPair) throws JsonRpcClientErrorException, JsonProcessingException {

        // accountInfo 메서드를 사용하여 계정 정보를 조회합니다.
        AccountInfoResult result = accountInfo(keyPair);

        // 조회된 계정 정보 출력
        System.out.println("Account Balance: " + result.accountData().balance());
        System.out.println("Sequence: " + result.accountData().sequence());
        System.out.println("Owner Count: " + result.accountData().ownerCount());
        System.out.println("account: " + result.accountData());
        System.out.println();
        System.out.println("wallet address: " + keyPair.publicKey().deriveAddress());

        AccountInfoResult result1 = accountInfo(minterKeyPair);

        // 조회된 계정 정보 출력
        System.out.println("Account Balance: " + result1.accountData().balance());
        System.out.println("Sequence: " + result1.accountData().sequence());
        System.out.println("Owner Count: " + result1.accountData().ownerCount());
        System.out.println("account: " + result1.accountData().account());
        System.out.println();
        System.out.println("wallet address: " + minterKeyPair.publicKey().deriveAddress());

        AccountSet accountSet = AccountSet.builder()
            .account(keyPair.publicKey().deriveAddress())
            .sequence(accountInfo(keyPair).accountData().sequence())
            .fee(FeeUtils.computeNetworkFees(xrplClient.fee()).recommendedFee())
            .mintAccount(minterKeyPair.publicKey().deriveAddress())
            .setFlag(AccountSet.AccountSetFlag.AUTHORIZED_MINTER)
            .signingPublicKey(keyPair.publicKey())
            .build();


        SingleSignedTransaction<AccountSet> signedAccountSet = signatureService.sign(keyPair.privateKey(), accountSet);
        SubmitResult<AccountSet> accountSetSubmitResult = xrplClient.submit(signedAccountSet);

        NfTokenUri uri = NfTokenUri.ofPlainText("ipfs://bafybeigdyrzt5sfp7udm7hu76uh7y26nf4dfuylqabf3oclgtqy55fbzdi");

        //Nft mint transaction
        NfTokenMint nfTokenMint = NfTokenMint.builder()
            .tokenTaxon(UnsignedLong.ONE)
            .account(minterKeyPair.publicKey().deriveAddress())
            .fee(XrpCurrencyAmount.ofDrops(50))
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
    protected SignatureService<PrivateKey> constructSignatureService() {
        return new BcSignatureService();
    }

}
