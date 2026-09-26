package com.botoni.vsr.service;

import com.botoni.vsr.dto.request.LoginRequest;
import com.botoni.vsr.dto.request.RegisterRequest;
import com.botoni.vsr.dto.response.LoginResponse;
import com.botoni.vsr.dto.response.UserResponse;
import com.botoni.vsr.entity.LocalCredential;
import com.botoni.vsr.entity.User;
import com.botoni.vsr.mapper.AuthenticationMapper;
import com.botoni.vsr.mapper.UserMapper;
import com.botoni.vsr.repository.LocalCredentialRepository;
import com.botoni.vsr.repository.UserRepository;
import com.botoni.vsr.vo.Password;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;
    private final LocalCredentialRepository localCredentialRepository;

    private final UserMapper userMapper;
    private final AuthenticationMapper authenticationMapper;

    public AuthenticationService(AuthenticationManager authenticationManager,
                                 JwtService jwtService, PasswordEncoder passwordEncoder,
                                 UserRepository userRepository, LocalCredentialRepository localCredentialRepository,
                                 UserMapper userMapper, AuthenticationMapper authenticationMapper) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.localCredentialRepository = localCredentialRepository;
        this.userMapper = userMapper;
        this.authenticationMapper = authenticationMapper;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        User user = userRepository.save(userMapper.toEntity(request));
        localCredentialRepository.save(authenticationMapper.toEntity(user, request, passwordEncoder));
        return userMapper.toResponse(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = authenticate(request);
        return authenticationMapper.toResponse(jwtService.issue(user), jwtService.expirationInMillis());
    }

    @Transactional
    public void changePassword(Integer userId, Password currentPassword, Password newPassword) {
        updatePassword(findCredential(userId), currentPassword, newPassword);
    }

    private User authenticate(LoginRequest request) {
        return (User) authenticationManager.authenticate(authentication(request)).getPrincipal();
    }

    private void updatePassword(LocalCredential credential, Password currentPassword, Password newPassword) {
        if (isIncorrect(credential, currentPassword)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Senha atual incorreta");
        }
        credential.changePassword(newPassword, passwordEncoder);
    }

    private boolean isIncorrect(LocalCredential credential, Password password) {
        return !credential.matches(password, passwordEncoder);
    }

    private LocalCredential findCredential(Integer userId) {
        return localCredentialRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Senha atual incorreta"));
    }

    private static UsernamePasswordAuthenticationToken authentication(LoginRequest request) {
        return UsernamePasswordAuthenticationToken.unauthenticated(request.email().value(), request.password().value());
    }
}