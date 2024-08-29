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
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.TransactionRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.immutables.FluentCompareTo;
import org.xrpl.xrpl4j.model.transactions.Payment;

@Repository
@Transactional(readOnly = true)
public class XrplRepository {
    protected XrplClient xrplClient;
    protected KeyPair coldWalletKeyPair;
    protected KeyPair hotWalletKeyPair;

    XrplRepository() throws JsonRpcClientErrorException, InterruptedException {
        ReportingTestnetEnvironment reportingTestnetEnvironment = new ReportingTestnetEnvironment();
        this.xrplClient = reportingTestnetEnvironment.getXrplClient();
        // Get the current network fee
        FeeResult feeResult = xrplClient.fee();

        // Create cold and hot KeyPairs -----------------------
        this.coldWalletKeyPair = Seed.ed25519Seed().deriveKeyPair();
        this.hotWalletKeyPair = Seed.ed25519Seed().deriveKeyPair();

        // Fund the account using the testnet Faucet -------------------------------
        reportingTestnetEnvironment.fundAccount(coldWalletKeyPair.publicKey().deriveAddress());
        reportingTestnetEnvironment.fundAccount(hotWalletKeyPair.publicKey().deriveAddress());


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
                        .account(coldWalletKeyPair.publicKey().deriveAddress())
                        .build()
                );

                xrplClient.accountInfo(
                    AccountInfoRequestParams.builder()
                        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
                        .account(hotWalletKeyPair.publicKey().deriveAddress())
                        .build()
                );

                accountsFunded = true;
            } catch (JsonRpcClientErrorException e) {
                if (!e.getMessage().equals("Account not found.")) {
                    throw e;
                }
                Thread.sleep(1000);
            }
        }

        System.out.println("Cold wallet address: " + coldWalletKeyPair.publicKey().deriveAddress());
        System.out.println("Hot wallet address: " + hotWalletKeyPair.publicKey().deriveAddress());

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
