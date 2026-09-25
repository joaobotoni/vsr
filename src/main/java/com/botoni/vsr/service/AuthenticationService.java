package com.botoni.vsr.service;

import com.botoni.vsr.dto.request.LoginRequest;
import com.botoni.vsr.dto.request.SignupRequest;
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
import lombok.NonNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService implements UserDetailsService {

    private static final String BAD_CREDENTIALS_MESSAGE = "Bad credentials";
    private static final String USER_NOT_FOUND_MESSAGE = "Usuário não encontrado";

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final LocalCredentialRepository localCredentialRepository;
    private final UserMapper userMapper;
    private final AuthenticationMapper authenticationMapper;

    public AuthenticationService(@Lazy AuthenticationManager authenticationManager,
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
    public UserResponse signup(SignupRequest request) {
        User user = userRepository.save(userMapper.toEntity(request));
        localCredentialRepository.save(new LocalCredential(user, request.password(), passwordEncoder));
        return userMapper.toResponse(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = authenticate(request);
        return authenticationMapper.toLoginResponse(jwtService.generateToken(user), jwtService.getExpirationMs());
    }

    @Transactional
    public void changePassword(Integer userId, String currentPassword, Password newPassword) {
        LocalCredential credential = findCredential(userId);
        if (isIncorrectPassword(credential, currentPassword)) {
            throw new IncorrectCurrentPasswordException();
        }
        credential.changePassword(newPassword, passwordEncoder);
    }

    @Override
    @NonNull
    @Transactional(readOnly = true)
    public User loadUserByUsername(@NonNull String email) {
        return localCredentialRepository.findWithUserByEmail(email)
                .map(LocalCredential::authenticated)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_MESSAGE));
    }

    private User authenticate(LoginRequest request) {
        if (isMalformedEmail(request.email())) {
            throw new BadCredentialsException(BAD_CREDENTIALS_MESSAGE);
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