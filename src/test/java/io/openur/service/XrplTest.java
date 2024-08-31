package io.openur.service;

import io.openur.domain.xrpl.repository.XrplRepository;
import java.util.List;
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

	@Test
	void testCreateAccounts() throws InterruptedException {
		List<KeyPair> accounts = xrplRepository.createAccounts();

		for (KeyPair account : accounts) {
			try {
				System.out.println("wallet address: " + account.publicKey().deriveAddress());

				// accountInfo 메서드를 사용하여 계정 정보를 조회합니다.
				AccountInfoResult result = xrplRepository.accountInfo(account);

				// 조회된 계정 정보 출력
				System.out.println("Account Balance: " + result.accountData().balance());
				System.out.println("Sequence: " + result.accountData().sequence());
				System.out.println("Owner Count: " + result.accountData().ownerCount());
				System.out.println("account: " + result.accountData().account());
				System.out.println();
			} catch (JsonRpcClientErrorException e) {
				// JSON-RPC 오류 처리
				System.err.println("Failed to retrieve account info: " + e.getMessage());
			}
		}
	}

}
