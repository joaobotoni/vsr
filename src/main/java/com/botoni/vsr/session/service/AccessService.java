package com.botoni.vsr.session.service;

import com.botoni.vsr.bundle.AccessBundle;
import com.botoni.vsr.token.dto.response.TokenResponse;
import com.botoni.vsr.session.entity.Session;
import com.botoni.vsr.bundle.mapper.SessionOwnerBundleMapper;
import com.botoni.vsr.token.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccessService {

    private final SessionService sessionService;
    private final TokenService tokenService;
    private final SessionOwnerBundleMapper sessionOwnerBundleMapper;

    @Transactional
    public TokenResponse grant(AccessBundle bundle) {
        Session session = open(bundle);
        return issue(bundle, session);
    }

    private Session open(AccessBundle bundle) {
        return sessionService.open(sessionOwnerBundleMapper.toBundle(bundle));
    }

    private TokenResponse issue(AccessBundle bundle, Session session) {
        return tokenService.issue(bundle.principal(), session.getId());
    }
}
