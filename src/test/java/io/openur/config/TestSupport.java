package io.openur.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openur.global.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@SpringBootTest(properties = {"spring.config.location=classpath:application-test.properties"})
@AutoConfigureMockMvc
public class TestSupport {
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected JwtUtil jwtUtil;
    @Autowired
    private UserDetailsService userDetailsService;

    protected final static String AUTH_HEADER = "Authorization";

    /** If using RestDocs for extended description of API documents,
     *  Add RestDocumentation Extension
     */
    @BeforeEach
    void setMockMvc(final WebApplicationContext context) {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .addFilter(new CharacterEncodingFilter("UTF-8", true))
            .build();
    }

    // Testing token for now, Can be added or fixed
    public String getTestUserToken() {
        String email = "test1@test.com";
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
}
