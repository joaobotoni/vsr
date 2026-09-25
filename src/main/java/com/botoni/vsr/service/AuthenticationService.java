package com.botoni.vsr.service;

import com.botoni.vsr.dto.request.LoginRequest;
import com.botoni.vsr.dto.request.RegisterRequest;
import com.botoni.vsr.dto.response.LoginResponse;
import com.botoni.vsr.dto.response.UserResponse;
import com.botoni.vsr.entity.LocalCredential;
import com.botoni.vsr.entity.User;
import com.botoni.vsr.exception.custom.IncorrectCurrentPasswordException;
import com.botoni.vsr.mapper.AuthenticationMapper;
import com.botoni.vsr.mapper.UserMapper;
import com.botoni.vsr.repository.LocalCredentialRepository;
import com.botoni.vsr.repository.UserRepository;
import com.botoni.vsr.vo.Email;
import com.botoni.vsr.vo.Password;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                                 JwtService jwtService,
                                 PasswordEncoder passwordEncoder,
                                 UserRepository userRepository,
                                 LocalCredentialRepository localCredentialRepository,
                                 UserMapper userMapper,
                                 AuthenticationMapper authenticationMapper) {
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
        localCredentialRepository.save(new LocalCredential(user, request.password(), passwordEncoder));
        return userMapper.toResponse(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = authenticate(request);
        return authenticationMapper.toLoginResponse(jwtService.generateToken(user), jwtService.getExpirationTime());
    }

    @Transactional
    public void changePassword(Integer userId, String currentPassword, Password newPassword) {
        LocalCredential credential = findCredential(userId);
        if (isIncorrectPassword(credential, currentPassword)) {
            throw new IncorrectCurrentPasswordException();
        }
        credential.changePassword(newPassword, passwordEncoder);
    }

    private User authenticate(LoginRequest request) {
        if (isMalformedEmail(request.email())) {
            throw new BadCredentialsException("");
        }
        return (User) authenticationManager.authenticate(toAuthenticationToken(request)).getPrincipal();
    }

    private LocalCredential findCredential(Integer userId) {
        return localCredentialRepository.findById(userId)
                .orElseThrow(IncorrectCurrentPasswordException::new);
    }

    private boolean isIncorrectPassword(LocalCredential credential, String password) {
        return !credential.isPasswordValid(password, passwordEncoder);
    }

    private static boolean isMalformedEmail(String email) {
        return !Email.isValid(email);
    }

    private static UsernamePasswordAuthenticationToken toAuthenticationToken(LoginRequest request) {
        return new UsernamePasswordAuthenticationToken(new Email(request.email()).value(), request.password());
    }
}