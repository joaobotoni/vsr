package com.botoni.vsr.service.session;

import com.botoni.vsr.command.session.AccessCommand;
import com.botoni.vsr.dto.response.TokenResponse;
import com.botoni.vsr.entity.users.Session;
import com.botoni.vsr.mapper.session.SessionMapper;
import com.botoni.vsr.service.token.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccessService {

    private final SessionService sessionService;
    private final TokenService tokenService;
    private final SessionMapper sessionMapper;

    @Transactional
    public TokenResponse grant(AccessCommand command) {
        Session session = open(command);
        return issue(command, session);
    }

    private Session open(AccessCommand command) {
        return sessionService.open(sessionMapper.toCommand(command));
    }

    private TokenResponse issue(AccessCommand command, Session session) {
        return tokenService.issue(command.principal(), session.getId());
    }
}
