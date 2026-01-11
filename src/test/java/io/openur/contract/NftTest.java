package io.openur.contract;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import io.openur.domain.NFT.service.NFTService;
import jakarta.transaction.Transactional;

@SpringBootTest(properties = {
    "spring.config.location=classpath:application.yml",
    "spring.config.additional-location=classpath:application-test.properties"
})
@Transactional
public class NftTest {
    @Autowired
    private NFTService nftService;

    @Value("${nft.rpc-url}")
    private String rpcUrl;
    
    @Value("${nft.private-key}")
    private String privateKey;
    
    @Value("${nft.contract-address}")
    private String contractAddress;

    String baseUri = "bafybeigastgvseo5r2cnrzjhaknsuxr4oj53zutxswyqztd7j2gyvwbhrm";
    
    @Test
    @Disabled
    @DisplayName("Set baseURI")
    void set_base_uri() throws Exception {
        nftService.setBaseURI(baseUri);
    }

   @Test
   @Disabled
   @DisplayName("test mintNFT")
   void mint_nft() throws Exception {
       String to = "0xA83F8f7f67F54F4D946E08CD6565700113B9a5Fb";
       nftService.mintNFT(to, 2L);
   }
}
