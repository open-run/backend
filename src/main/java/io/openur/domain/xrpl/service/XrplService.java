package io.openur.domain.xrpl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.openur.domain.user.model.User;
import io.openur.domain.user.repository.UserRepositoryImpl;
import io.openur.domain.xrpl.dto.NftDataDto;
import io.openur.domain.xrpl.model.NftIndex;
import io.openur.domain.xrpl.repository.NftIndexRepositoryImpl;
import io.openur.domain.xrpl.repository.XrplRepository;
import io.openur.global.security.UserDetailsImpl;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class XrplService {
    private final UserRepositoryImpl userRepository;
    private final NftIndexRepositoryImpl nftIndexRepository;
    private final XrplRepository xrplRepository;

    @Transactional
    public NftDataDto mintNft(UserDetailsImpl userDetails) throws InterruptedException, JsonRpcClientErrorException, JsonProcessingException {
        User user = userRepository.findByEmail(userDetails.getUser().getEmail());
        NftDataDto nftDataDto = xrplRepository.mintNft(user.getUserId());
        nftIndexRepository.save(new NftIndex(nftDataDto.getNfTokenId(), user));
        return nftDataDto;
    }

    public List<NftDataDto> getNftList(UserDetailsImpl userDetails)
        throws JsonRpcClientErrorException {
        User user = userRepository.findByEmail(userDetails.getUser().getEmail());
        List<String> nftIndexList = nftIndexRepository.findByUserId(user.getUserId());

        return xrplRepository.getNftDataList(nftIndexList);
    }
}
