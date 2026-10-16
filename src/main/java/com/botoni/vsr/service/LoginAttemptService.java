package com.botoni.vsr.service;

import com.botoni.vsr.exception.custom.RateLimitException;
import com.botoni.vsr.exception.enums.problem.RateLimitProblem;
import com.botoni.vsr.network.Network;
import com.botoni.vsr.ratelimit.RateLimitPolicy;
import com.botoni.vsr.ratelimit.TokenBucket;
import com.botoni.vsr.ratelimit.TokenBucket.Allowance;
import com.botoni.vsr.vo.Email;
import com.botoni.vsr.vo.Password;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.net.InetAddress;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final String NETWORK = "login:%s:network:%s";
    private static final String ACCOUNT = "login:%s";

    private final RateLimitPolicy rateLimitPolicy;
    private final AuthenticationManager authenticationManager;

    public Authentication verify(Email email, Password password, InetAddress ip) {
        within(network(email, ip));
        within(account(email));
        return authenticate(email, password);
    }

    private Allowance network(Email email, InetAddress ip) {
        return rateLimitPolicy.account().allow(String.format(NETWORK, email.value(), Network.from(ip).key()));
    }

    private Allowance account(Email email) {
        return rateLimitPolicy.ceiling().allow(String.format(ACCOUNT, email.value()));
    }

    private Authentication authenticate(Email email, Password password) {
        return authenticationManager.authenticate(credentials(email, password));
    }

    private static UsernamePasswordAuthenticationToken credentials(Email email, Password password) {
        return UsernamePasswordAuthenticationToken.unauthenticated(email.value(), password.value());
    }

    private static void within(Allowance allowance) {
        if (!allowance.allowed()) {
            throw new RateLimitException(RateLimitProblem.EXCEEDED, TokenBucket.retry(allowance));
        }
    }
}
