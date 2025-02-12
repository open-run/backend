package io.openur.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.openur.global.jwt.JwtUtil;
import io.openur.global.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@SpringBootTest(properties = {"spring.config.location=classpath:application-test.properties"})
public class TestSupport {

    protected final static String AUTH_HEADER = "Authorization";
    @Autowired
    protected JwtUtil jwtUtil;
    protected MockMvc mockMvc;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

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

    public String getTestUserToken(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        UsernamePasswordAuthenticationToken authenticatedToken
            = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authenticatedToken);
        return jwtUtil.createToken(email);
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
