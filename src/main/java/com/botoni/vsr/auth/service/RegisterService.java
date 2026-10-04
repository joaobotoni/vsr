package com.botoni.vsr.auth.service;

import com.botoni.vsr.auth.dto.request.RegisterRequest;
import com.botoni.vsr.bundle.SignUpBundle;
import com.botoni.vsr.bundle.SessionBundle;
import com.botoni.vsr.auth.dto.response.RegisterResponse;
import com.botoni.vsr.token.dto.response.TokenResponse;
import com.botoni.vsr.credential.entity.LocalCredential;
import com.botoni.vsr.user.entity.User;
import com.botoni.vsr.auth.mapper.RegisterMapper;
import com.botoni.vsr.auth.mapper.RegisterUserMapper;
import com.botoni.vsr.bundle.mapper.AccessBundleMapper;
import com.botoni.vsr.bundle.mapper.LocalCredentialBundleMapper;
import com.botoni.vsr.user.repository.UserRepository;
import com.botoni.vsr.infra.security.Principal;
import com.botoni.vsr.credential.service.LocalCredentialService;
import com.botoni.vsr.session.service.AccessService;
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
    private final RegisterUserMapper registerUserMapper;
    private final LocalCredentialBundleMapper localCredentialBundleMapper;
    private final AccessBundleMapper accessBundleMapper;

    @Transactional
    public RegisterResponse register(SignUpBundle bundle) {
        Principal principal = enroll(bundle.register());
        TokenResponse token = grant(principal, bundle.session());
        return respond(principal, token);
    }

    private Principal enroll(RegisterRequest request) {
        User user = save(request);
        LocalCredential credential = credential(user, request);
        return Principal.from(credential);
    }

    private User save(RegisterRequest request) {
        return userRepository.save(registerUserMapper.toEntity(request));
    }

    private LocalCredential credential(User user, RegisterRequest request) {
        return localCredentialService.create(localCredentialBundleMapper.toBundle(user, request.password()));
    }

    private TokenResponse grant(Principal principal, SessionBundle session) {
        return accessService.grant(accessBundleMapper.toBundle(principal, session));
    }

    private RegisterResponse respond(Principal principal, TokenResponse token) {
        return registerMapper.toResponse(principal.user(), token);
    }
}
