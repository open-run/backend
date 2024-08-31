package io.openur.domain.xrpl.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import io.openur.domain.xrpl.environment.ReportingTestnetEnvironment;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Seed;
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
import org.xrpl.xrpl4j.model.immutables.FluentCompareTo;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.NfTokenUri;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.TransactionResultCodes;

@Repository
@Transactional(readOnly = true)
public class XrplRepository {
    protected XrplClient xrplClient;
    protected ReportingTestnetEnvironment reportingTestnetEnvironment;
    protected BcSignatureService signatureService;

    XrplRepository() {
        this.reportingTestnetEnvironment = new ReportingTestnetEnvironment();
        this.xrplClient = reportingTestnetEnvironment.getXrplClient();
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
                        FluentCompareTo.is(latestValidatedLedgerIndex.unsignedIntegerValue())
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
