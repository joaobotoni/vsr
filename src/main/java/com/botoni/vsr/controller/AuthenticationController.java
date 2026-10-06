package com.botoni.vsr.controller;

import com.botoni.vsr.dto.request.LoginRequest;
import com.botoni.vsr.dto.request.RefreshRequest;
import com.botoni.vsr.dto.request.RegisterRequest;
import com.botoni.vsr.dto.response.AuthenticationResponse;
import com.botoni.vsr.dto.response.TokenResponse;
import com.botoni.vsr.security.Principal;
import com.botoni.vsr.service.LoginService;
import com.botoni.vsr.service.RefreshService;
import com.botoni.vsr.service.RegisterService;
import com.botoni.vsr.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;

@RestController
@RequestMapping(path = "/auth", version = "1")
@RequiredArgsConstructor
public class AuthenticationController {

    private final RegisterService registerService;
    private final LoginService loginService;
    private final SessionService sessionService;
    private final RefreshService refreshService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody @Valid RegisterRequest request, HttpServletRequest http) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registerService.register(request, ip(http)));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody @Valid LoginRequest request, HttpServletRequest http) {
        return ResponseEntity.ok(loginService.login(request, ip(http)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody @Valid RefreshRequest request) {
        return ResponseEntity.ok(refreshService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Principal principal) {
        sessionService.revoke(principal.user(), principal.session());
        return ResponseEntity.noContent().build();
    }

    private static InetAddress ip(HttpServletRequest http) {
        return new ServletServerHttpRequest(http).getRemoteAddress().getAddress();
    }
}
