package io.openur.domain.user.service.login;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.openur.domain.challenge.event.ChallengeEventsPublisher;
import io.openur.domain.user.dto.GetUsersLoginDto;
import io.openur.domain.user.dto.SmartWalletUserInfoDto;
import io.openur.domain.user.model.User;
import io.openur.domain.user.repository.UserRepositoryImpl;
import io.openur.global.common.validation.ValidEthereumAddress;
import io.openur.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j(topic = "Smart Wallet Login")
@Service
public class SmartWalletService extends LoginService {
    private final JwtUtil jwtUtil;
    private final UserRepositoryImpl userRepository;
    private final ChallengeEventsPublisher eventsPublisher;

    public SmartWalletService(
        RestTemplate restTemplate,
        JwtUtil jwtUtil,
        UserRepositoryImpl userRepository,
        ChallengeEventsPublisher eventsPublisher
    ) {
        super(restTemplate);
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.eventsPublisher = eventsPublisher;
    }

    @Override
    public GetUsersLoginDto login(@ValidEthereumAddress String walletAddress, String signature) throws JsonProcessingException {
        try {
            log.info("Starting Smart Wallet login process for address: {}", walletAddress);

            // Find or create user
            User user = registerUserIfNew(SmartWalletUserInfoDto.of(walletAddress));
            log.info("User found/created successfully. User ID: {}", user.getUserId());

            // Generate JWT token using wallet address
            String token = jwtUtil.createToken(walletAddress);
            log.debug("JWT token generated successfully");

            return new GetUsersLoginDto(
                walletAddress,  // Use wallet address as identifier
                user.getNickname(),
                token
            );
        } catch (Exception e) {
            String errorMsg = "Failed to process smart wallet login: " + e.getMessage();
            log.error(errorMsg, e);
            throw new IllegalArgumentException(errorMsg, e);
        }
    }

    protected User registerUserIfNew(SmartWalletUserInfoDto smartWalletUserInfoDto) {
        // DB 에 중복된 블록체인 주소의 유저가 있는지 확인
        String blockchainAddress = smartWalletUserInfoDto.getBlockchainAddress();
        User user = userRepository.findUser(new User(blockchainAddress));
        if (user == null) {
            // 없으면 회원가입
            User newUser = new User(blockchainAddress);

            eventsPublisher.publishUserRegistration(newUser);

            return userRepository.save(newUser);
        } else {
            return user;
        }
    }
} 
