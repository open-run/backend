package io.openur.global.security;

import io.openur.domain.user.model.User;
import io.openur.domain.user.repository.UserRepository;
import io.openur.global.common.validation.EthereumAddressValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final EthereumAddressValidator ethereumAddressValidator;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user;
        
        // Check if the identifier is a valid Ethereum address
        if (ethereumAddressValidator.isValid(identifier, null)) {
            user = userRepository.findUser(new User(identifier));
        } else {
            // If not a valid address, treat as email
            user = userRepository.findUser(new User(identifier, null));
        }

        if (user == null) {
            throw new UsernameNotFoundException("Not Found " + identifier);
        }

        return new UserDetailsImpl(user);
    }
}
