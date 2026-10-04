package com.botoni.vsr.service.auth;

import com.botoni.vsr.dto.request.auth.RegisterRequest;
import com.botoni.vsr.command.auth.SignUpCommand;
import com.botoni.vsr.command.session.SessionCommand;
import com.botoni.vsr.dto.response.RegisterResponse;
import com.botoni.vsr.dto.response.TokenResponse;
import com.botoni.vsr.entity.users.LocalCredential;
import com.botoni.vsr.entity.users.User;
import com.botoni.vsr.mapper.auth.RegisterMapper;
import com.botoni.vsr.mapper.credential.LocalCredentialMapper;
import com.botoni.vsr.mapper.session.AccessMapper;
import com.botoni.vsr.repository.UserRepository;
import com.botoni.vsr.security.Principal;
import com.botoni.vsr.service.credential.LocalCredentialService;
import com.botoni.vsr.service.session.AccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final AccessService accessService;
    private final LocalCredentialService localCredentialService;
    private final UserRepository userRepository;
    private final RegisterMapper registerMapper;
    private final LocalCredentialMapper localCredentialMapper;
    private final AccessMapper accessMapper;

    @Transactional
    public RegisterResponse register(SignUpCommand command) {
        Principal principal = enroll(command.register());
        TokenResponse token = grant(principal, command.session());
        return respond(principal, token);
    }

    private Principal enroll(RegisterRequest request) {
        User user = save(request);
        LocalCredential credential = credential(user, request);
        return Principal.from(credential);
    }

    private User save(RegisterRequest request) {
        return userRepository.save(registerMapper.toEntity(request));
    }

    private LocalCredential credential(User user, RegisterRequest request) {
        return localCredentialService.create(localCredentialMapper.toCommand(user, request.password()));
    }

    private TokenResponse grant(Principal principal, SessionCommand session) {
        return accessService.grant(accessMapper.toCommand(principal, session));
    }

    private RegisterResponse respond(Principal principal, TokenResponse token) {
        return registerMapper.toResponse(principal.user(), token);
    }
}
