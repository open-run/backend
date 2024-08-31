package io.openur.domain.xrpl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.openur.domain.user.model.User;
import io.openur.domain.user.repository.UserRepositoryImpl;
import io.openur.domain.xrpl.dto.NftDataDto;
import io.openur.domain.xrpl.repository.XrplRepository;
import io.openur.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class XrplService {
    private final UserRepositoryImpl userRepository;
    private final XrplRepository xrplRepository;

    public NftDataDto mintNft(UserDetailsImpl userDetails) throws InterruptedException, JsonRpcClientErrorException, JsonProcessingException {
        User user = userRepository.findByEmail(userDetails.getUser().getEmail());
        return xrplRepository.mintNft(user.getUserId());
    }
}
