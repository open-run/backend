package io.openur.global.security;

import io.openur.domain.user.model.User;
import io.openur.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = userRepository.findUser(new User(identifier));

        if (user == null) {
            throw new UsernameNotFoundException("Not Found " + identifier);
        }

        return new UserDetailsImpl(user);
    }
}
