package io.openur.domain.NFT.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openur.domain.NFT.contract.NFTContract;
import org.springframework.web.bind.annotation.*;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.net.URL;

@RestController
@RequestMapping("/v1/nft")
public class NFTController {

    private static final String RPC_URL = "https://base-sepolia.g.alchemy.com/v2/FDDXOaIjm44THciq6GlNXqeR5KYR6xi_";
    private static final String PRIVATE_KEY = "0x866565bd00765f23f211de47c7b7e1a3371bb6cd24932a07c01484d19e6e2946";
    private static final String CONTRACT_ADDRESS = "0xcdb492969565839a6447cca6b9fcc9ffaa7ec5f9";

    private final Web3j web3j = Web3j.build(new HttpService(RPC_URL));
    private final Credentials credentials = Credentials.create(PRIVATE_KEY);
    private final NFTContract contract =
            NFTContract.load(CONTRACT_ADDRESS, web3j, credentials, new DefaultGasProvider());

    String baseUri = "ipfs://bafybeigi2w2q6zeq66uk27wk7uwcwkli6fbwc73lsezxpets7x43r2s6o4/";
    @PostMapping("/mint")
    public String mintNft(
            @RequestParam String to,
            @RequestParam BigInteger taskId,
            @RequestParam BigInteger itemId
    ) {
        try {
            // 1. task 등록
            contract.setTaskItem(taskId, itemId).send();

            // 2. baseURI 설정
            contract.setBaseURI(baseUri).send();

            // 3. 민팅
            TransactionReceipt receipt = contract.mintItemForTask(to, taskId, BigInteger.ONE).send();

            return "Mint Success! TxHash: " + receipt.getTransactionHash();

            /*Nft 해쉬 값 또는 taskId 중 하나를 db에 리스트 형식으로 저장할 수 있게 해야할 것 같음*/

        } catch (Exception e) {
            e.printStackTrace();
            return "Minting Failed: " + e.getMessage();
        }
    }

    @GetMapping("/metadataInfo")
    public String getNftMetadata(
            @RequestParam String walletAddress,
            @RequestParam BigInteger tokenId
    ) {
        try {
            BigInteger balance = contract.balanceOf(walletAddress, tokenId).send();
            if (balance.compareTo(BigInteger.ZERO) <= 0) {
                return "NFT 보유하지 않음";
            }

            String uri = contract.uri(tokenId).send();
            //String metadataFilename = "shoes4-3.json";
            //uri = uri.replace(tokenId.toString(), metadataFilename);

            String metadataUrl = uri.replace("ipfs://", "https://gateway.pinata.cloud/ipfs/");
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(new URL(metadataUrl));

            return "URI: " + uri + "\n🧾 Metadata:\n" + json.toPrettyString();

        } catch (Exception e) {
            e.printStackTrace();
            return "정보 조회 실패: " + e.getMessage();
        }
    }
}
