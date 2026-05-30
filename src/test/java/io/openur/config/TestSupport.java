package io.openur.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.cloud.storage.Storage;
import io.openur.domain.NFT.service.NFTService;
import io.openur.domain.NFT.service.NftContractBalanceClient;
import io.openur.domain.NFT.service.NftMintClient;
import io.openur.global.jwt.JwtUtil;
import io.openur.global.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@SpringBootTest(properties = {
    "spring.config.location=classpath:application.yml",
    "spring.config.additional-location=classpath:application-test.properties"
})
public class TestSupport {

    protected final static String AUTH_HEADER = "Authorization";
    @Autowired
    protected JwtUtil jwtUtil;
    protected MockMvc mockMvc;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @MockBean
    protected Storage gcsStorage;

    // web3j @PostConstruct(NFTService, Web3jNftContractBalanceClient, Web3jNftMintClient) 가
    // 시동 시 실 RPC/contract 에 ENS resolve 를 시도해 로컬 ApplicationContext 로드를 막으므로
    // 모든 통합 test 에서 mock 으로 우회. CI 는 실제 secret 으로 동작하므로 무해.
    @MockBean
    protected NFTService nftService;
    @MockBean
    protected NftContractBalanceClient nftContractBalanceClient;
    @MockBean
    protected NftMintClient nftMintClient;

    /**
     * If using RestDocs for extended description of API documents, Add RestDocumentation Extension
     */
    @BeforeEach
    void setMockMvc() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .addFilter(new CharacterEncodingFilter("UTF-8", true))
            .build();
    }

    public String getTestUserToken1() {
        return getTestUserToken("0x1234567890123456789012345678901234567890");
    }
    
    public String getTestUserToken2() {
        return getTestUserToken("0x1234567890123456789012345678901234567891");
    }
    
    public String getTestUserToken3() {
        return getTestUserToken("0x1234567890123456789012345678901234567892");
    }
    
    private String getTestUserToken(String address) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(address);
        UsernamePasswordAuthenticationToken authenticatedToken
            = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authenticatedToken);
        return jwtUtil.createToken(address);
    }

    // Implementation of DTO
    protected String jsonify(Object req) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(req);
    }

    protected <T> T parseResponse(String resString, TypeReference<T> typeReference)
        throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.readValue(resString, typeReference);
    }
}
