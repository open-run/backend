package io.openur.domain.user.service.login;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.openur.domain.user.dto.GetUsersLoginDto;
import io.openur.domain.user.exception.InvalidSignatureException;
import io.openur.domain.user.model.User;
import io.openur.domain.user.repository.UserRepositoryImpl;
import io.openur.global.common.validation.ValidEthereumAddress;
import io.openur.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j(topic = "Smart Wallet Login")
@Service
@RequiredArgsConstructor
public class SmartWalletService {
    private final JwtUtil jwtUtil;
    private final UserRepositoryImpl userRepository;

    public String generateNonce(@ValidEthereumAddress String walletAddress) {
        // TODO
        return "nonce";
    }

    public GetUsersLoginDto login(@ValidEthereumAddress String walletAddress, String signature, String nonce) throws JsonProcessingException {
        if (!verifyNonceSignature(walletAddress, signature, nonce)) {
            throw new InvalidSignatureException(walletAddress, nonce);
        }
        log.debug("Nonce signature verified: {}", walletAddress);

        User user = registerUserIfNew(walletAddress);
        log.info("User found/created successfully. User ID: {}", user.getUserId());

        String token = jwtUtil.createToken(walletAddress);
        log.debug("JWT token generated successfully");

        return new GetUsersLoginDto(
            walletAddress,
            user.getNickname(),
            token
        );
    }

    protected Boolean verifyNonceSignature(String walletAddress, String signature, String nonce) {
        // TODO: verify nonce signature
        return true;
    }

    protected User registerUserIfNew(String walletAddress) {
        // DB 에 중복된 블록체인 주소의 유저가 있는지 확인
        User user = userRepository.findUser(new User(walletAddress));
        if (user == null) {
            // 없으면 회원가입
            User newUser = new User(walletAddress);
            return userRepository.save(newUser);
        } else {
            return user;
        }
    }
} 