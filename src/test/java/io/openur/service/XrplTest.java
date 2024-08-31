package io.openur.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.openur.domain.user.repository.UserRepositoryImpl;
import io.openur.domain.xrpl.dto.NftDataDto;
import io.openur.domain.xrpl.repository.XrplRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;

@SpringBootTest(properties = {"spring.config.location=classpath:application-test.properties"})
public class XrplTest {

	@Autowired
	XrplRepository xrplRepository;

	@Autowired
	UserRepositoryImpl userRepository;


	@Test
	void testCreateAccounts() throws InterruptedException, JsonRpcClientErrorException {
		KeyPair account = xrplRepository.createAccount("5d22bd65-f1ed-4e7b-bc7b-0a59580d3176");

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
		NftDataDto nftDataDto = xrplRepository.mintNft("334c38eb-4a82-4986-b572-d411a5d8c928");

        // DTO의 내용을 출력
        System.out.println("NFT Data:");
        System.out.println("Token ID: " + nftDataDto.getNfTokenId());
        System.out.println("Taxon: " + nftDataDto.getTaxon());
        System.out.println("NFT Serial: " + nftDataDto.getNftSerial());
        System.out.println("Decoded URI: " + nftDataDto.getDecodedUri());
        System.out.println("Decoded Memo Data: " + nftDataDto.getDecodedMemoData());

	}

}
