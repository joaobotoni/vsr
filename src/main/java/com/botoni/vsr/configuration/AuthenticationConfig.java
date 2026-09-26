package com.botoni.vsr.configuration;

import com.botoni.vsr.entity.LocalCredential;
import com.botoni.vsr.repository.LocalCredentialRepository;
import com.botoni.vsr.vo.Email;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class AuthenticationConfig {

    private final LocalCredentialRepository localCredentialRepository;

    public AuthenticationConfig(LocalCredentialRepository localCredentialRepository) {
        this.localCredentialRepository = localCredentialRepository;
    }

    @Bean
    UserDetailsService userDetailsService() {
        return username -> localCredentialRepository.findWithUserByEmail(Email.of(username))
                .map(LocalCredential::authenticated).orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }

    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }

    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}