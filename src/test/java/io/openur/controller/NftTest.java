package io.openur.controller;

import io.openur.contract.OpenRunNFTTest;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.crypto.Credentials;
import org.web3j.tx.gas.DefaultGasProvider;
import java.math.BigInteger;


public class NftTest {


    public static void main(String[] args) throws Exception {
        String RPC_URL = "https://base-sepolia.g.alchemy.com/v2/FDDXOaIjm44THciq6GlNXqeR5KYR6xi_";
        Web3j web3j = Web3j.build(new HttpService(RPC_URL));

        String privateKey = "0x866565bd00765f23f211de47c7b7e1a3371bb6cd24932a07c01484d19e6e2946";
        String contractAddress = "0xcdb492969565839a6447cca6b9fcc9ffaa7ec5f9";
        Credentials credentials = Credentials.create(privateKey);

        OpenRunNFTTest contract = OpenRunNFTTest.load(contractAddress, web3j, credentials, new DefaultGasProvider());

        String to = "0x4f76b5121838b16a5f938d4c8369021f22ad659d";
        BigInteger taskId = BigInteger.valueOf(403);
        BigInteger amount = BigInteger.ONE;
        BigInteger itemId = BigInteger.valueOf(403);

        // task 등록
        contract.setTaskItem(taskId, itemId).send();
        System.out.println("taskId " + taskId + "에 itemId " + itemId + " 등록 완료");


        String baseUri = "ipfs://bafybeigi2w2q6zeq66uk27wk7uwcwkli6fbwc73lsezxpets7x43r2s6o4/";

        // baseURI 설정
        TransactionReceipt uriReceipt = contract.setBaseURI(baseUri).send();
        System.out.println("baseURI 설정 완료: " + uriReceipt.getTransactionHash());

        // 블록 포함 대기
        Thread.sleep(2000);

        // NFT 민팅
        TransactionReceipt receipt = contract.mintItemForTask(to, taskId, amount).send();
        System.out.println("Mint Success! TxHash: " + receipt.getTransactionHash());



        // 확인할 지갑 주소와 tokenId
        String walletAddress = "0x4f76b5121838b16a5f938d4c8369021f22ad659d";
        BigInteger tokenId = BigInteger.valueOf(403);

        //보유량 확인
        BigInteger balance = contract.balanceOf(walletAddress, tokenId).send();
        System.out.println("NFT 보유량 (tokenId = " + tokenId + "): " + balance);

        if (balance.compareTo(BigInteger.ZERO) > 0) {
            //메타데이터 URI 조회
            String uri = contract.uri(tokenId).send();
            System.out.println("메타데이터 URI: " + uri);

            //tokenId 403에 대해 메타데이터 파일명 치환
            if (tokenId.equals(BigInteger.valueOf(403))) {
                uri = uri.replace("403", "shoes4-3.json");
            }

            //게이트웨이 URL로 변환
            String metadataUrl = uri.replace("ipfs://", "https://gateway.pinata.cloud/ipfs/");
            System.out.println("메타데이터 확인: " + metadataUrl);

            System.out.println("\n 브라우저에서 확인:\n" + metadataUrl);
        } else {
            System.out.println(" 해당 지갑은 이 NFT를 보유하고 있지 않습니다.");
        }
    }
}
