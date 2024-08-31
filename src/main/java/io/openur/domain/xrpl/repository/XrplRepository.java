package io.openur.domain.xrpl.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedLong;
import io.openur.domain.xrpl.environment.ReportingTestnetEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.signing.SignatureService;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.bc.BcSignatureService;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.transactions.*;

@Repository
@Slf4j(topic = "Xrpl")
@Transactional(readOnly = true)
public class XrplRepository {
    public static int TIME_SLEEP = 1000;
    public static String NFTOKEN_URI = "ipfs://bafybeigdyrzt5sfp7udm7hu76uh7y26nf4dfuylqabf3oclgtqy55fbzdi";
    public static String ID = "5d22bd65-f1ed-4e7b-bc7b-0a59580d3176";

    public XrplClient xrplClient;
    protected ReportingTestnetEnvironment reportingTestnetEnvironment;
    protected SignatureService<PrivateKey> signatureService;

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
        KeyPair walletKeyPair = s.deriveKeyPair();
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
                    log.info(e.getMessage());
                }
                Thread.sleep(TIME_SLEEP);
            }
        }
        return walletKeyPair;
    }

    public AccountInfoResult accountInfo(KeyPair keyPair) throws JsonRpcClientErrorException {
        AccountInfoRequestParams params = AccountInfoRequestParams.builder()
            .account(keyPair.publicKey().deriveAddress())
            .ledgerSpecifier(LedgerSpecifier.VALIDATED)
            .build();

        return xrplClient.accountInfo(params);
    }

    public void mintFromOtherMinterAccount(KeyPair keyPair, KeyPair minterKeyPair) throws JsonRpcClientErrorException, JsonProcessingException {
        printAccountInfo(accountInfo(keyPair), keyPair);
        printAccountInfo(accountInfo(minterKeyPair), minterKeyPair);

        AccountSet accountSet = setAccount(keyPair, minterKeyPair);

        SingleSignedTransaction<AccountSet> signedAccountSet = signatureService.sign(keyPair.privateKey(), accountSet);
        SubmitResult<AccountSet> accountSetSubmitResult = xrplClient.submit(signedAccountSet);

        NfTokenMint nfTokenMint = setNfTokenMint(keyPair, minterKeyPair);

        SingleSignedTransaction<NfTokenMint> signedMint = signatureService.sign(minterKeyPair.privateKey(), nfTokenMint);
        //TODO Planned to change to submitAndWaitForValidation
        SubmitResult<NfTokenMint> mintSubmitResult = xrplClient.submit(signedMint);

        AccountInfoResult sourceAccountInfoAfterMint = xrplClient.accountInfo(
            AccountInfoRequestParams.of(keyPair.publicKey().deriveAddress())
        );

        log.info("NFT was minted successfully.");
    }

    private NfTokenMint setNfTokenMint(KeyPair keyPair, KeyPair minterKeyPair) throws JsonRpcClientErrorException {
        return NfTokenMint.builder()
            .tokenTaxon(UnsignedLong.ONE)
            .account(minterKeyPair.publicKey().deriveAddress())
            .fee(XrpCurrencyAmount.ofDrops(50))
            .signingPublicKey(minterKeyPair.publicKey())
            .sequence(accountInfo(minterKeyPair).accountData().sequence())
            .issuer(keyPair.publicKey().deriveAddress())
            .uri(NfTokenUri.ofPlainText(NFTOKEN_URI))
            .build();
    }

    private AccountSet setAccount(KeyPair keyPair, KeyPair minterKeyPair) throws JsonRpcClientErrorException {
        return AccountSet.builder()
            .account(keyPair.publicKey().deriveAddress())
            .sequence(accountInfo(keyPair).accountData().sequence())
            .fee(FeeUtils.computeNetworkFees(xrplClient.fee()).recommendedFee())
            .mintAccount(minterKeyPair.publicKey().deriveAddress())
            .setFlag(AccountSet.AccountSetFlag.AUTHORIZED_MINTER)
            .signingPublicKey(keyPair.publicKey())
            .build();
    }

    private void printAccountInfo(AccountInfoResult result, KeyPair keyPair) {
        log.info("Account Balane: " + result.accountData().balance());
        log.info("Sequence: " + result.accountData().sequence());
        log.info("Owner Count: " + result.accountData().ownerCount());
        log.info("account: " + result.accountData());
        log.info("wallet address: " + keyPair.publicKey().deriveAddress());
    }
}
