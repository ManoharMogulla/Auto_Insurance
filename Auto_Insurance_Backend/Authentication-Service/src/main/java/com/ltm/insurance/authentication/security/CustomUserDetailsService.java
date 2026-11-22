package com.ltm.insurance.authentication.security;

import com.ltm.insurance.authentication.repository.AuthUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AuthUserRepository userRepository;

    public CustomUserDetailsService(AuthUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmailIgnoreCase(email.trim())
                .map(AuthUserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found"));
    }
}
