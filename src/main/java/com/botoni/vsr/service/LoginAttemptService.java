package com.botoni.vsr.service;

import com.botoni.vsr.attempt.Attempt;
import com.botoni.vsr.attempt.IAttempt;
import com.botoni.vsr.network.Network;
import com.botoni.vsr.vo.Email;
import com.botoni.vsr.vo.Password;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final String ACCOUNT = "login:%s";
    private static final String DEVICE = "login:%s:device:%s";
    private static final String NETWORK = "login:%s:network:%s";

    private final IAttempt attempts;
    private final DeviceService deviceService;
    private final AuthenticationManager authenticationManager;

    public Authentication verify(Email email, Password password, UUID device, InetAddress ip) {
        return attempts.attempt(attempt(email, device, ip), () -> authenticate(email, password));
    }

    private Attempt attempt(Email email, UUID device, InetAddress ip) {
        return known(email, device) ? trusted(email, device) : stranger(email, ip);
    }

    private static Attempt trusted(Email email, UUID device) {
        return Attempt.of(device(email, device), BadCredentialsException.class);
    }

    private static Attempt stranger(Email email, InetAddress ip) {
        return Attempt.ceiled(network(email, ip), account(email), BadCredentialsException.class);
    }

    private Authentication authenticate(Email email, Password password) {
        return authenticationManager.authenticate(credentials(email, password));
    }

    private static UsernamePasswordAuthenticationToken credentials(Email email, Password password) {
        return UsernamePasswordAuthenticationToken.unauthenticated(email.value(), password.value());
    }

    private static String device(Email email, UUID device) {
        return String.format(DEVICE, email.value(), device);
    }

    private static String network(Email email, InetAddress ip) {
        return String.format(NETWORK, email.value(), Network.from(ip).key());
    }

    private static String account(Email email) {
        return String.format(ACCOUNT, email.value());
    }

    private boolean known(Email email, UUID device) {
        return deviceService.known(email, device);
    }
}
