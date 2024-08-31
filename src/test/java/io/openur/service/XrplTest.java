package io.openur.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.openur.domain.xrpl.dto.NftDataDto;
import io.openur.domain.xrpl.repository.XrplRepository;
import io.openur.domain.xrpl.repository.taxon;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.transactions.NfTokenMint;

@SpringBootTest(properties = {"spring.config.location=classpath:application-test.properties"})
public class XrplTest {

	@Autowired
	XrplRepository xrplRepository;


	@Test
	void testCreateAccounts() throws InterruptedException, JsonRpcClientErrorException {
		KeyPair account = xrplRepository.createAccount();

		System.out.println("wallet address: " + account.publicKey().deriveAddress());

		// accountInfo 메서드를 사용하여 계정 정보를 조회합니다.
		AccountInfoResult result = xrplRepository.accountInfo(account);

		// 조회된 계정 정보 출력
		System.out.println("Account Balance: " + result.accountData().balance());
		System.out.println("Sequence: " + result.accountData().sequence());
		System.out.println("Owner Count: " + result.accountData().ownerCount());
		System.out.println("account: " + result.accountData().account());
		System.out.println();
	}

	@Test
	void testMint() throws InterruptedException, JsonRpcClientErrorException, JsonProcessingException {
		KeyPair keyPair = xrplRepository.createAccount();
		KeyPair minterKeyPair = xrplRepository.createAccount();
        String memoContent = "Unique";
        taxon category = taxon.HAIR_ACCESSORY;
        String nft_uri = "ipfs://bafybeigdyrzt5sfp7udm7hu76uh7y26nf4dfuylqabf3oclgtqy55fbzdi";

        SubmitResult<NfTokenMint> mintSubmitResult = xrplRepository.mintFromOtherMinterAccount(keyPair, minterKeyPair,nft_uri,category,memoContent);

        //xrplRepository.accountNftsData(minterKeyPair,mintSubmitResult);

        NftDataDto nftDataDto = xrplRepository.accountNftsData(minterKeyPair, mintSubmitResult);

        // DTO의 내용을 출력
        System.out.println("NFT Data:");
        System.out.println("Token ID: " + nftDataDto.getNfTokenId());
        System.out.println("Taxon: " + nftDataDto.getTaxon());
        System.out.println("NFT Serial: " + nftDataDto.getNftSerial());
        System.out.println("Decoded URI: " + nftDataDto.getDecodedUri());
        System.out.println("Decoded Memo Data: " + nftDataDto.getDecodedMemoData());

	}

}
