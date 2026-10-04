package com.botoni.vsr.infra.configuration;

import com.botoni.vsr.credential.entity.LocalCredential;
import com.botoni.vsr.credential.repository.LocalCredentialRepository;
import com.botoni.vsr.infra.security.Principal;
import com.botoni.vsr.shared.vo.Email;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthenticationConfig {

    private final LocalCredentialRepository localCredentialRepository;

    public AuthenticationConfig(LocalCredentialRepository localCredentialRepository) {
        this.localCredentialRepository = localCredentialRepository;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> localCredentialRepository.findWithUserByEmail(Email.of(username))
                .map(Principal::from).orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }

    @Bean
    AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }
}