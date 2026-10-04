package com.botoni.vsr.auth.controller;

import com.botoni.vsr.auth.dto.request.LoginRequest;
import com.botoni.vsr.auth.dto.request.RegisterRequest;
import com.botoni.vsr.session.dto.request.DeviceRequest;
import com.botoni.vsr.bundle.SessionBundle;
import com.botoni.vsr.auth.dto.response.LoginResponse;
import com.botoni.vsr.auth.dto.response.RegisterResponse;
import com.botoni.vsr.bundle.mapper.SessionBundleMapper;
import com.botoni.vsr.bundle.mapper.SignInBundleMapper;
import com.botoni.vsr.bundle.mapper.SignUpBundleMapper;
import com.botoni.vsr.auth.service.LoginService;
import com.botoni.vsr.auth.service.RegisterService;
import com.botoni.vsr.infra.web.RemoteAddress;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(path = "/auth", version = "1")
@RequiredArgsConstructor
public class AuthenticationController {

    private final RegisterService registerService;
    private final LoginService loginService;
    private final SignUpBundleMapper signUpBundleMapper;
    private final SignInBundleMapper signInBundleMapper;
    private final SessionBundleMapper sessionBundleMapper;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody @Valid RegisterRequest request, HttpServletRequest http) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registerService.register(signUpBundleMapper.toBundle(request, session(request.device(), http))));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request, HttpServletRequest http) {
        return ResponseEntity.ok(loginService.login(signInBundleMapper.toBundle(request, session(request.device(), http))));
    }

    private SessionBundle session(DeviceRequest device, HttpServletRequest http) {
        return sessionBundleMapper.toBundle(device, RemoteAddress.of(http));
    }
}
