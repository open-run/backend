package io.openur.domain.user.service.login;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.openur.domain.user.dto.GetUsersLoginDto;
import io.openur.domain.user.exception.InvalidSignatureException;
import io.openur.domain.user.model.EVMAddress;
import io.openur.domain.user.model.User;
import io.openur.domain.user.repository.UserRepositoryImpl;
import io.openur.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j(topic = "Smart Wallet Login")
@RequiredArgsConstructor
@Service
public class SmartWalletService {
    private final JwtUtil jwtUtil;
    private final UserRepositoryImpl userRepository;
    private final NonceCacheService nonceCacheService;

    public String getNonce(EVMAddress walletAddress) {
        String nonce = nonceCacheService.getNonce(walletAddress);
        if (nonce == null) {
            return nonceCacheService.generateAndStoreNonce(walletAddress);
        }
        return nonce;
    }

    public GetUsersLoginDto login(EVMAddress walletAddress, String signature, String nonce) throws JsonProcessingException {
        verifyNonceSignature(walletAddress, signature, nonce);
        log.debug("Nonce signature verified: {}", walletAddress.getValue());

        User user = registerUserIfNew(walletAddress);
        log.info("User found/created successfully. User ID: {}", user.getUserId());

        String token = jwtUtil.createToken(walletAddress.getValue());
        log.debug("JWT token generated successfully");

        return new GetUsersLoginDto(
            walletAddress.getValue(),
            user.getNickname(),
            token
        );
    }

    protected Boolean verifyNonceSignature(EVMAddress walletAddress, String signature, String nonce) throws InvalidSignatureException {
        String cachedNonce = nonceCacheService.getNonce(walletAddress);
        
        if (cachedNonce == null) {
            throw new InvalidSignatureException("No stored nonce found for wallet: " + walletAddress.getValue());
        }
        
        if (!cachedNonce.equals(nonce)) {
            log.warn("Nonce mismatch for wallet: {}. Expected: {}, Received: {}", 
                    walletAddress.getValue(), cachedNonce, nonce);
            throw new InvalidSignatureException("Nonce mismatch for wallet: " + walletAddress.getValue());
        }
        
        // TODO: verify signature
        
        // Remove the used nonce to prevent replay attacks
        nonceCacheService.evictNonce(walletAddress);
        return true;
    }

    protected User registerUserIfNew(EVMAddress walletAddress) {
        // DB 에 중복된 블록체인 주소의 유저가 있는지 확인
        User user = userRepository.findUser(new User(walletAddress.getValue()));
        if (user == null) {
            // 없으면 회원가입
            User newUser = new User(walletAddress.getValue());
            return userRepository.save(newUser);
        } else {
            return user;
        }
    }
} 