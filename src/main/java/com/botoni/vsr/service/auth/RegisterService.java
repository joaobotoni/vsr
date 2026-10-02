package com.botoni.vsr.service.auth;

import com.botoni.vsr.dto.request.RegisterRequest;
import com.botoni.vsr.dto.response.RegisterResponse;
import com.botoni.vsr.entity.users.LocalCredential;
import com.botoni.vsr.entity.users.User;
import com.botoni.vsr.mapper.RegisterMapper;
import com.botoni.vsr.repository.DeviceRepository;
import com.botoni.vsr.repository.LocalCredentialRepository;
import com.botoni.vsr.repository.SessionRepository;
import com.botoni.vsr.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;
    private final LocalCredentialRepository localCredentialRepository;

    private final RegisterMapper registerMapper;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        User user = userRepository.save(registerMapper.toEntity(request));
        localCredentialRepository.save(LocalCredential.create(user, request.password(), passwordEncoder));
        return registerMapper.toResponse(user, tokenService.issue(user));
    }

}